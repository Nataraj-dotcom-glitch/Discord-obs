package com.darkalise.obs.recorder

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class AudioCaptureManager(
    private val context: Context,
    private val sampleRate: Int = 48000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_STEREO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {

    companion object {
        private const val TAG = "AudioCaptureManager"
        const val DISCORD_RESTRICTION_NOTICE =
            "Device audio cannot be captured because Android or the source app does not allow playback capture."
    }

    private var micRecord: AudioRecord? = null
    private var deviceRecord: AudioRecord? = null
    private var isRecording = false
    private var captureJob: Job? = null

    // Volume & Mute states (0.0 to 1.0)
    var micVolume: Float = 1.0f
    var isMicMuted: Boolean = false

    var deviceVolume: Float = 1.0f
    var isDeviceMuted: Boolean = false

    // Live Meter Levels (0.0f to 1.0f)
    private val _micLevel = MutableStateFlow(0f)
    val micLevel: StateFlow<Float> = _micLevel.asStateFlow()

    private val _deviceLevel = MutableStateFlow(0f)
    val deviceLevel: StateFlow<Float> = _deviceLevel.asStateFlow()

    private val _audioWarning = MutableStateFlow<String?>(null)
    val audioWarning: StateFlow<String?> = _audioWarning.asStateFlow()

    // Listener for mixed PCM output to pass to MediaCodec AAC Encoder
    var onMixedPcmData: ((ByteArray, Long) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun startCapture(
        mediaProjection: MediaProjection?,
        enableMic: Boolean,
        enableDeviceAudio: Boolean,
        scope: CoroutineScope
    ): Boolean {
        _audioWarning.value = null
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (bufferSize <= 0) {
            Log.e(TAG, "Invalid buffer size: $bufferSize")
            return false
        }

        // 1. Initialize Microphone AudioRecord
        if (enableMic) {
            try {
                micRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize * 2
                )
                if (micRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.w(TAG, "Mic AudioRecord failed to initialize")
                    micRecord = null
                } else {
                    micRecord?.startRecording()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start Mic recording: ${e.message}")
                micRecord = null
            }
        }

        // 2. Initialize Device Playback AudioRecord (Android 10+ Q only)
        if (enableDeviceAudio && mediaProjection != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val playbackConfig = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                    .addMatchingUsage(AudioAttributes.USAGE_GAME)
                    .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                    .build()

                deviceRecord = AudioRecord.Builder()
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelConfig)
                            .build()
                    )
                    .setAudioPlaybackCaptureConfig(playbackConfig)
                    .setBufferSizeInBytes(bufferSize * 2)
                    .build()

                if (deviceRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.w(TAG, "Playback capture not permitted by OS or security policies")
                    _audioWarning.value = DISCORD_RESTRICTION_NOTICE
                    deviceRecord = null
                } else {
                    deviceRecord?.startRecording()
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Device audio capture restricted by system security: ${e.message}")
                _audioWarning.value = DISCORD_RESTRICTION_NOTICE
                deviceRecord = null
            } catch (e: Exception) {
                Log.e(TAG, "AudioPlaybackCapture exception: ${e.message}")
                _audioWarning.value = DISCORD_RESTRICTION_NOTICE
                deviceRecord = null
            }
        } else if (enableDeviceAudio && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            _audioWarning.value = "Device audio capture requires Android 10+."
        }

        isRecording = true

        // 3. Audio Mixing Loop
        captureJob = scope.launch(Dispatchers.IO) {
            val chunkBytes = 4096
            val micBuffer = ByteArray(chunkBytes)
            val devBuffer = ByteArray(chunkBytes)
            val mixedBuffer = ByteArray(chunkBytes)

            while (isActive && isRecording) {
                var micBytesRead = 0
                var devBytesRead = 0

                if (micRecord != null && !isMicMuted) {
                    micBytesRead = micRecord?.read(micBuffer, 0, chunkBytes) ?: 0
                }

                if (deviceRecord != null && !isDeviceMuted) {
                    devBytesRead = deviceRecord?.read(devBuffer, 0, chunkBytes) ?: 0
                }

                // Calculate Levels
                val currentMicLevel = if (micBytesRead > 0 && !isMicMuted) {
                    calculateLevel(micBuffer, micBytesRead) * micVolume
                } else 0f
                _micLevel.value = min(1.0f, currentMicLevel)

                val currentDevLevel = if (devBytesRead > 0 && !isDeviceMuted) {
                    calculateLevel(devBuffer, devBytesRead) * deviceVolume
                } else 0f
                _deviceLevel.value = min(1.0f, currentDevLevel)

                // Mix PCM 16-bit stereo buffers
                val maxBytes = max(if (micBytesRead > 0) micBytesRead else 0, if (devBytesRead > 0) devBytesRead else 0)
                if (maxBytes > 0) {
                    mixPcmBuffers(
                        micBuffer, micBytesRead, micVolume, isMicMuted,
                        devBuffer, devBytesRead, deviceVolume, isDeviceMuted,
                        mixedBuffer, maxBytes
                    )

                    val ptsUs = System.nanoTime() / 1000L
                    onMixedPcmData?.invoke(mixedBuffer.copyOf(maxBytes), ptsUs)
                }
            }
        }

        return true
    }

    /**
     * Accurately calculates RMS audio amplitude level (0.0f to 1.0f)
     */
    private fun calculateLevel(buffer: ByteArray, length: Int): Float {
        var sum = 0.0
        val numSamples = length / 2
        if (numSamples <= 0) return 0f

        val bb = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN)
        while (bb.remaining() >= 2) {
            val sample = bb.short.toDouble()
            sum += sample * sample
        }
        val rms = sqrt(sum / numSamples)
        if (rms <= 0.0) return 0f

        // Convert to dB scale relative to Short.MAX_VALUE
        val db = 20 * log10(rms / Short.MAX_VALUE)
        // Normalize -60dB -> 0.0, 0dB -> 1.0
        val normalized = ((db + 60) / 60.0).toFloat()
        return max(0f, min(1f, normalized))
    }

    /**
     * 16-bit PCM Linear Mixing with soft clipping prevention
     */
    private fun mixPcmBuffers(
        mic: ByteArray, micLen: Int, micVol: Float, micMuted: Boolean,
        dev: ByteArray, devLen: Int, devVol: Float, devMuted: Boolean,
        out: ByteArray, totalBytes: Int
    ) {
        val micBB = ByteBuffer.wrap(mic).order(ByteOrder.LITTLE_ENDIAN)
        val devBB = ByteBuffer.wrap(dev).order(ByteOrder.LITTLE_ENDIAN)
        val outBB = ByteBuffer.wrap(out).order(ByteOrder.LITTLE_ENDIAN)

        val samples = totalBytes / 2
        for (i in 0 until samples) {
            val micSample: Float = if (!micMuted && micLen > i * 2 && micBB.remaining() >= 2) {
                micBB.short * micVol
            } else 0f

            val devSample: Float = if (!devMuted && devLen > i * 2 && devBB.remaining() >= 2) {
                devBB.short * devVol
            } else 0f

            val mixed = micSample + devSample
            // Soft clamp to Short range
            val clamped = when {
                mixed > Short.MAX_VALUE -> Short.MAX_VALUE
                mixed < Short.MIN_VALUE -> Short.MIN_VALUE
                else -> mixed.toInt().toShort()
            }
            outBB.putShort(clamped)
        }
    }

    fun stopCapture() {
        isRecording = false
        captureJob?.cancel()
        captureJob = null

        try {
            micRecord?.stop()
            micRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        micRecord = null

        try {
            deviceRecord?.stop()
            deviceRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        deviceRecord = null

        _micLevel.value = 0f
        _deviceLevel.value = 0f
    }
}
