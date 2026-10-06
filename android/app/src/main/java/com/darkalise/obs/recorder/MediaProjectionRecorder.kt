package com.darkalise.obs.recorder

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Surface
import com.darkalise.obs.model.RecordingSettings
import com.darkalise.obs.model.VideoEncoderPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class MediaProjectionRecorder(private val context: Context) {

    companion object {
        private const val TAG = "MediaProjectionRecorder"
        private const val VIDEO_MIME = "video/avc"
        private const val AUDIO_MIME = "audio/mp4a-latm"
        private const val TIMEOUT_USEC = 10_000L
    }

    private var videoEncoder: MediaCodec? = null
    private var audioEncoder: MediaCodec? = null
    private var inputSurface: Surface? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaMuxer: MediaMuxer? = null

    private var videoTrackIndex = -1
    private var audioTrackIndex = -1
    private var isMuxerStarted = false
    private val muxerLock = Object()

    private val isRecording = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)

    private var videoDrainJob: Job? = null
    private var audioDrainJob: Job? = null

    // Presentation timestamp management for Pause/Resume
    private var pauseStartTimeNs = 0L
    private var totalPauseOffsetUs = 0L
    private var lastVideoPtsUs = 0L

    var onError: ((String) -> Unit)? = null

    /**
     * Initializes and starts recording the screen and audio via MediaCodec and MediaMuxer.
     */
    fun start(
        mediaProjection: MediaProjection,
        fileDescriptor: FileDescriptor,
        settings: RecordingSettings,
        scope: CoroutineScope,
        audioCaptureManager: AudioCaptureManager
    ): Boolean {
        try {
            isRecording.set(true)
            isPaused.set(false)
            totalPauseOffsetUs = 0L
            pauseStartTimeNs = 0L

            val width = settings.resolution.width
            val height = settings.resolution.height
            val fps = settings.fps.fps
            val bitrate = settings.bitrate.bps

            // 1. Initialize MediaMuxer
            mediaMuxer = MediaMuxer(fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            // 2. Setup Video MediaFormat & Encoder
            val videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1) // 1 second keyframe
                setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
            }

            videoEncoder = createVideoCodec(settings.encoder, videoFormat)
            inputSurface = videoEncoder?.createInputSurface()
            videoEncoder?.start()

            // 3. Setup VirtualDisplay for MediaProjection
            val densityDpi = context.resources.displayMetrics.densityDpi
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "DarkAliseOBS_VD",
                width,
                height,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                inputSurface,
                null,
                null
            )

            // 4. Setup Audio MediaFormat & Encoder
            val audioFormat = MediaFormat.createAudioFormat(AUDIO_MIME, settings.audioSampleRate, 2).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, settings.audioBitrate)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            }
            audioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME)
            audioEncoder?.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            audioEncoder?.start()

            // Connect AudioCaptureManager mixed PCM callback to AudioCodec
            audioCaptureManager.onMixedPcmData = { pcmData, ptsUs ->
                if (isRecording.get() && !isPaused.get()) {
                    queueAudioData(pcmData, ptsUs - totalPauseOffsetUs)
                }
            }

            // 5. Start Drain Loops
            videoDrainJob = scope.launch(Dispatchers.IO) { drainVideoEncoder() }
            audioDrainJob = scope.launch(Dispatchers.IO) { drainAudioEncoder() }

            Log.i(TAG, "Recording started successfully: ${width}x${height} @ ${fps}fps, ${bitrate / 1000}kbps")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MediaProjection recording: ${e.message}", e)
            onError?.invoke(e.message ?: "Failed to start screen encoder")
            stop()
            return false
        }
    }

    /**
     * Resolves Hardware Encoder or falls back to Software AVC
     */
    private fun createVideoCodec(pref: VideoEncoderPreference, format: MediaFormat): MediaCodec {
        return try {
            if (pref == VideoEncoderPreference.SOFTWARE_FALLBACK) {
                MediaCodec.createByCodecName("c2.android.avc.encoder").apply {
                    configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                }
            } else {
                MediaCodec.createEncoderByType(VIDEO_MIME).apply {
                    configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Hardware encoder failed, falling back to software AVC: ${e.message}")
            // Fallback for Samsung Galaxy A14 5G or restricted chipsets
            MediaCodec.createByCodecName("c2.android.avc.encoder").apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            }
        }
    }

    fun pause() {
        if (isRecording.get() && !isPaused.get()) {
            isPaused.set(true)
            pauseStartTimeNs = System.nanoTime()
        }
    }

    fun resume() {
        if (isRecording.get() && isPaused.get()) {
            val pauseDurationNs = System.nanoTime() - pauseStartTimeNs
            totalPauseOffsetUs += (pauseDurationNs / 1000L)
            isPaused.set(false)
        }
    }

    /**
     * Drains encoded video H.264 frames and writes to MediaMuxer
     */
    private fun drainVideoEncoder() {
        val encoder = videoEncoder ?: return
        val bufferInfo = MediaCodec.BufferInfo()

        while (isRecording.get()) {
            val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                synchronized(muxerLock) {
                    if (videoTrackIndex < 0) {
                        val newFormat = encoder.outputFormat
                        videoTrackIndex = mediaMuxer?.addTrack(newFormat) ?: -1
                        checkStartMuxer()
                    }
                }
            } else if (outputBufferIndex >= 0) {
                val encodedData = encoder.getOutputBuffer(outputBufferIndex)
                if (encodedData != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                    if (bufferInfo.size != 0 && isMuxerStarted && !isPaused.get()) {
                        // Adjust timestamp for pause offsets
                        bufferInfo.presentationTimeUs -= totalPauseOffsetUs
                        if (bufferInfo.presentationTimeUs > lastVideoPtsUs) {
                            lastVideoPtsUs = bufferInfo.presentationTimeUs
                            synchronized(muxerLock) {
                                mediaMuxer?.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                            }
                        }
                    }
                }
                encoder.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
    }

    /**
     * Feeds PCM Audio into AAC Encoder input buffers
     */
    private fun queueAudioData(pcmData: ByteArray, ptsUs: Long) {
        val encoder = audioEncoder ?: return
        try {
            val inputBufferIndex = encoder.dequeueInputBuffer(TIMEOUT_USEC)
            if (inputBufferIndex >= 0) {
                val inputBuffer = encoder.getInputBuffer(inputBufferIndex) ?: return
                inputBuffer.clear()
                inputBuffer.put(pcmData)
                encoder.queueInputBuffer(inputBufferIndex, 0, pcmData.size, ptsUs, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Drains encoded AAC audio packets and writes to MediaMuxer
     */
    private fun drainAudioEncoder() {
        val encoder = audioEncoder ?: return
        val bufferInfo = MediaCodec.BufferInfo()

        while (isRecording.get()) {
            val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                synchronized(muxerLock) {
                    if (audioTrackIndex < 0) {
                        val newFormat = encoder.outputFormat
                        audioTrackIndex = mediaMuxer?.addTrack(newFormat) ?: -1
                        checkStartMuxer()
                    }
                }
            } else if (outputBufferIndex >= 0) {
                val encodedData = encoder.getOutputBuffer(outputBufferIndex)
                if (encodedData != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                    if (bufferInfo.size != 0 && isMuxerStarted && !isPaused.get()) {
                        synchronized(muxerLock) {
                            mediaMuxer?.writeSampleData(audioTrackIndex, encodedData, bufferInfo)
                        }
                    }
                }
                encoder.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
    }

    private fun checkStartMuxer() {
        if (!isMuxerStarted && videoTrackIndex >= 0 && (audioTrackIndex >= 0 || audioEncoder == null)) {
            mediaMuxer?.start()
            isMuxerStarted = true
            Log.i(TAG, "MediaMuxer started with Video track: $videoTrackIndex, Audio track: $audioTrackIndex")
        }
    }

    fun stop() {
        isRecording.set(false)
        videoDrainJob?.cancel()
        audioDrainJob?.cancel()

        try {
            virtualDisplay?.release()
        } catch (e: Exception) { e.printStackTrace() }
        virtualDisplay = null

        try {
            inputSurface?.release()
        } catch (e: Exception) { e.printStackTrace() }
        inputSurface = null

        try {
            videoEncoder?.stop()
            videoEncoder?.release()
        } catch (e: Exception) { e.printStackTrace() }
        videoEncoder = null

        try {
            audioEncoder?.stop()
            audioEncoder?.release()
        } catch (e: Exception) { e.printStackTrace() }
        audioEncoder = null

        synchronized(muxerLock) {
            try {
                if (isMuxerStarted) {
                    mediaMuxer?.stop()
                    mediaMuxer?.release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaMuxer = null
            isMuxerStarted = false
            videoTrackIndex = -1
            audioTrackIndex = -1
        }
    }
}
