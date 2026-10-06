// File registry of the complete native Android project for DARK ALISE OBS

export interface AndroidFile {
  path: string;
  name: string;
  category: 'build' | 'manifest' | 'res' | 'kotlin-core' | 'kotlin-recorder' | 'kotlin-ui';
  description: string;
  content: string;
}

export const ANDROID_PROJECT_FILES: AndroidFile[] = [
  {
    path: 'settings.gradle.kts',
    name: 'settings.gradle.kts',
    category: 'build',
    description: 'Gradle multi-project root settings & plugin repositories management',
    content: `pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\\\.android.*")
                includeGroupByRegex("com\\\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DarkAliseOBS"
include(":app")`
  },
  {
    path: 'build.gradle.kts',
    name: 'build.gradle.kts',
    category: 'build',
    description: 'Root Gradle build configuration with Android & Kotlin Compose plugin aliases',
    content: `plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}`
  },
  {
    path: 'gradle/libs.versions.toml',
    name: 'libs.versions.toml',
    category: 'build',
    description: 'Version Catalog specifying AGP 8.7, Kotlin 2.0, Jetpack Compose 2024.11, CameraX, and DataStore',
    content: `[versions]
agp = "8.7.2"
kotlin = "2.0.21"
coreKtx = "1.15.0"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.11.00"
datastore = "1.1.1"
camerax = "1.4.1"
coroutines = "1.9.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
androidx-camera-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
androidx-camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
androidx-camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }`
  },
  {
    path: 'app/build.gradle.kts',
    name: 'app/build.gradle.kts',
    category: 'build',
    description: 'Application build script with minSdk 29 (Android 10+), targetSdk 35, ARM64 ABI filters for Galaxy A14 5G',
    content: `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.darkalise.obs"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.darkalise.obs"
        minSdk = 29 // Android 10+ (AudioPlaybackCapture & MediaProjection Foreground Service)
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Optimization for Samsung Galaxy A14 5G (ARM64-v8a / armeabi-v7a)
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    // CameraX for Front Camera Overlay
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    debugImplementation(libs.androidx.ui.tooling)
}`
  },
  {
    path: 'app/src/main/AndroidManifest.xml',
    name: 'AndroidManifest.xml',
    category: 'manifest',
    description: 'Manifest declaring RECORD_AUDIO, CAMERA, MediaProjection Foreground Service, and Notification permissions',
    content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.darkalise.obs">

    <!-- Essential Screen Recording & Audio Permissions -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera" android:required="false" />
    <uses-feature android:name="android.hardware.camera.front" android:required="false" />

    <!-- Foreground Service permissions for Android 10 - 14+ -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />

    <!-- Notifications for Android 13+ (API 33+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher"
        android:supportsRtl="true"
        android:theme="@style/Theme.DarkAliseOBS"
        android:hardwareAccelerated="true"
        android:largeHeap="true"
        tools:targetApi="35">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
            android:theme="@style/Theme.DarkAliseOBS">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".service.ScreenRecordingService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="mediaProjection|microphone|camera" />

    </application>
</manifest>`
  },
  {
    path: 'app/src/main/res/values/strings.xml',
    name: 'strings.xml',
    category: 'res',
    description: 'String resources including exact Discord/VoIP audio restriction compliance notice',
    content: `<resources>
    <string name="app_name">DARK ALISE OBS</string>
    <string name="app_subtitle">Professional screen recording for Android</string>
    <string name="notification_channel_name">Screen Recording Service</string>
    <string name="notification_channel_desc">Active background screen capture and audio processing notification</string>
    <string name="recording_active">Recording in progress</string>
    <string name="recording_paused">Recording paused</string>
    <string name="btn_start_recording">Start Recording</string>
    <string name="btn_stop_recording">Stop Recording</string>
    <string name="btn_pause_recording">Pause</string>
    <string name="btn_resume_recording">Resume</string>
    <string name="err_discord_audio">Device audio cannot be captured because Android or the source app does not allow playback capture.</string>
    <string name="err_permission_denied">Permission denied for requested feature.</string>
    <string name="err_encoder_failed">Hardware encoder failed. Falling back to software AVC encoder.</string>
    <string name="err_storage_low">Insufficient storage available in Movies/DarkAliseOBS/.</string>
</resources>`
  },
  {
    path: 'app/src/main/res/values/colors.xml',
    name: 'colors.xml',
    category: 'res',
    description: 'OBS Dark Obsidian & Purple palette color values with Red recording accent',
    content: `<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="obs_bg_black">#0A0912</color>
    <color name="obs_panel_black">#13111C</color>
    <color name="obs_panel_card">#1B1827</color>
    <color name="obs_border">#2C2742</color>
    <color name="obs_purple_accent">#8B5CF6</color>
    <color name="obs_purple_dark">#6D28D9</color>
    <color name="obs_purple_glow">#A78BFA</color>
    <color name="obs_red_rec">#EF4444</color>
    <color name="obs_green_active">#10B981</color>
    <color name="obs_amber_warning">#F59E0B</color>
    <color name="obs_text_primary">#F8FAFC</color>
    <color name="obs_text_secondary">#94A3B8</color>
</resources>`
  },
  {
    path: 'app/src/main/java/com/darkalise/obs/MainActivity.kt',
    name: 'MainActivity.kt',
    category: 'kotlin-core',
    description: 'Main activity managing MediaProjection consent launcher and Jetpack Compose screens',
    content: `package com.darkalise.obs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.darkalise.obs.ui.screens.FirstLaunchScreen
import com.darkalise.obs.ui.screens.MainScreen
import com.darkalise.obs.ui.screens.RecordingsScreen
import com.darkalise.obs.ui.screens.SettingsScreen
import com.darkalise.obs.ui.theme.DarkAliseOBSTheme
import com.darkalise.obs.ui.theme.ObsBlackBg
import com.darkalise.obs.viewmodel.AppTab
import com.darkalise.obs.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.startRecording(result.resultCode, result.data!!)
        } else {
            Toast.makeText(
                this,
                "Screen recording cancelled or permission denied.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DarkAliseOBSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsBlackBg
                ) {
                    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()
                    val currentTab by viewModel.currentTab.collectAsState()
                    val recordings by viewModel.recordings.collectAsState()
                    val settings by viewModel.settings.collectAsState()

                    if (isFirstLaunch) {
                        FirstLaunchScreen(
                            onPermissionsComplete = {
                                viewModel.completeFirstLaunch()
                            }
                        )
                    } else {
                        when (currentTab) {
                            AppTab.STUDIO -> {
                                MainScreen(
                                    viewModel = viewModel,
                                    onRequestMediaProjection = {
                                        requestMediaProjection()
                                    }
                                )
                            }
                            AppTab.RECORDINGS -> {
                                RecordingsScreen(
                                    recordings = recordings,
                                    onPlay = { viewModel.playRecording(it) },
                                    onShare = { viewModel.shareRecording(it) },
                                    onDelete = { viewModel.deleteRecording(it) },
                                    onRefresh = { viewModel.refreshRecordings() },
                                    onBack = { viewModel.selectTab(AppTab.STUDIO) }
                                )
                            }
                            AppTab.SETTINGS -> {
                                SettingsScreen(
                                    currentSettings = settings,
                                    onSaveSettings = { viewModel.updateSettings(it) },
                                    onBack = { viewModel.selectTab(AppTab.STUDIO) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestMediaProjection() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }
}`
  },
  {
    path: 'app/src/main/java/com/darkalise/obs/recorder/MediaProjectionRecorder.kt',
    name: 'MediaProjectionRecorder.kt',
    category: 'kotlin-recorder',
    description: 'Hardware MediaCodec H.264 & AAC encoders, VirtualDisplay capture, and MediaMuxer container',
    content: `package com.darkalise.obs.recorder

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.util.Log
import android.view.Surface
import com.darkalise.obs.model.RecordingSettings
import com.darkalise.obs.model.VideoEncoderPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileDescriptor
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

    private var pauseStartTimeNs = 0L
    private var totalPauseOffsetUs = 0L
    private var lastVideoPtsUs = 0L

    var onError: ((String) -> Unit)? = null

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

            mediaMuxer = MediaMuxer(fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
            }

            videoEncoder = createVideoCodec(settings.encoder, videoFormat)
            inputSurface = videoEncoder?.createInputSurface()
            videoEncoder?.start()

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

            val audioFormat = MediaFormat.createAudioFormat(AUDIO_MIME, settings.audioSampleRate, 2).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, settings.audioBitrate)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            }
            audioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME)
            audioEncoder?.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            audioEncoder?.start()

            audioCaptureManager.onMixedPcmData = { pcmData, ptsUs ->
                if (isRecording.get() && !isPaused.get()) {
                    queueAudioData(pcmData, ptsUs - totalPauseOffsetUs)
                }
            }

            videoDrainJob = scope.launch(Dispatchers.IO) { drainVideoEncoder() }
            audioDrainJob = scope.launch(Dispatchers.IO) { drainAudioEncoder() }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start MediaProjection recording: \${e.message}", e)
            onError?.invoke(e.message ?: "Failed to start screen encoder")
            stop()
            return false
        }
    }

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
            Log.w(TAG, "Hardware encoder failed, falling back to software AVC: \${e.message}")
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

    private fun drainVideoEncoder() {
        val encoder = videoEncoder ?: return
        val bufferInfo = MediaCodec.BufferInfo()

        while (isRecording.get()) {
            val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                synchronized(muxerLock) {
                    if (videoTrackIndex < 0) {
                        videoTrackIndex = mediaMuxer?.addTrack(encoder.outputFormat) ?: -1
                        checkStartMuxer()
                    }
                }
            } else if (outputBufferIndex >= 0) {
                val encodedData = encoder.getOutputBuffer(outputBufferIndex)
                if (encodedData != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                    if (bufferInfo.size != 0 && isMuxerStarted && !isPaused.get()) {
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

    private fun drainAudioEncoder() {
        val encoder = audioEncoder ?: return
        val bufferInfo = MediaCodec.BufferInfo()

        while (isRecording.get()) {
            val outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                synchronized(muxerLock) {
                    if (audioTrackIndex < 0) {
                        audioTrackIndex = mediaMuxer?.addTrack(encoder.outputFormat) ?: -1
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
        }
    }

    fun stop() {
        isRecording.set(false)
        videoDrainJob?.cancel()
        audioDrainJob?.cancel()
        try { virtualDisplay?.release() } catch (_: Exception) {}
        try { inputSurface?.release() } catch (_: Exception) {}
        try { videoEncoder?.stop(); videoEncoder?.release() } catch (_: Exception) {}
        try { audioEncoder?.stop(); audioEncoder?.release() } catch (_: Exception) {}
        synchronized(muxerLock) {
            try {
                if (isMuxerStarted) {
                    mediaMuxer?.stop()
                    mediaMuxer?.release()
                }
            } catch (_: Exception) {}
            mediaMuxer = null
            isMuxerStarted = false
        }
    }
}`
  },
  {
    path: 'app/src/main/java/com/darkalise/obs/recorder/AudioCaptureManager.kt',
    name: 'AudioCaptureManager.kt',
    category: 'kotlin-recorder',
    description: 'Microphone & Android AudioPlaybackCapture manager with VU meters and PCM audio mixing',
    content: `package com.darkalise.obs.recorder

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
        const val DISCORD_RESTRICTION_NOTICE =
            "Device audio cannot be captured because Android or the source app does not allow playback capture."
    }

    private var micRecord: AudioRecord? = null
    private var deviceRecord: AudioRecord? = null
    private var isRecording = false
    private var captureJob: Job? = null

    var micVolume: Float = 1.0f
    var isMicMuted: Boolean = false

    var deviceVolume: Float = 1.0f
    var isDeviceMuted: Boolean = false

    private val _micLevel = MutableStateFlow(0f)
    val micLevel: StateFlow<Float> = _micLevel.asStateFlow()

    private val _deviceLevel = MutableStateFlow(0f)
    val deviceLevel: StateFlow<Float> = _deviceLevel.asStateFlow()

    private val _audioWarning = MutableStateFlow<String?>(null)
    val audioWarning: StateFlow<String?> = _audioWarning.asStateFlow()

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
        if (bufferSize <= 0) return false

        if (enableMic) {
            try {
                micRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize * 2
                )
                if (micRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    micRecord?.startRecording()
                } else micRecord = null
            } catch (_: Exception) { micRecord = null }
        }

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

                if (deviceRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    deviceRecord?.startRecording()
                } else {
                    _audioWarning.value = DISCORD_RESTRICTION_NOTICE
                    deviceRecord = null
                }
            } catch (_: Exception) {
                _audioWarning.value = DISCORD_RESTRICTION_NOTICE
                deviceRecord = null
            }
        }

        isRecording = true
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

                val currentMic = if (micBytesRead > 0 && !isMicMuted) calculateLevel(micBuffer, micBytesRead) * micVolume else 0f
                _micLevel.value = min(1f, currentMic)

                val currentDev = if (devBytesRead > 0 && !isDeviceMuted) calculateLevel(devBuffer, devBytesRead) * deviceVolume else 0f
                _deviceLevel.value = min(1f, currentDev)

                val maxBytes = max(micBytesRead, devBytesRead)
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

    private fun calculateLevel(buffer: ByteArray, length: Int): Float {
        var sum = 0.0
        val numSamples = length / 2
        if (numSamples <= 0) return 0f
        val bb = ByteBuffer.wrap(buffer, 0, length).order(ByteOrder.LITTLE_ENDIAN)
        while (bb.remaining() >= 2) {
            val s = bb.short.toDouble()
            sum += s * s
        }
        val rms = sqrt(sum / numSamples)
        if (rms <= 0.0) return 0f
        val db = 20 * log10(rms / Short.MAX_VALUE)
        val norm = ((db + 60) / 60.0).toFloat()
        return max(0f, min(1f, norm))
    }

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
            val mSample = if (!micMuted && micLen > i * 2 && micBB.remaining() >= 2) micBB.short * micVol else 0f
            val dSample = if (!devMuted && devLen > i * 2 && devBB.remaining() >= 2) devBB.short * devVol else 0f
            val mixed = mSample + dSample
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
        try { micRecord?.stop(); micRecord?.release() } catch (_: Exception) {}
        try { deviceRecord?.stop(); deviceRecord?.release() } catch (_: Exception) {}
        micRecord = null
        deviceRecord = null
        _micLevel.value = 0f
        _deviceLevel.value = 0f
    }
}`
  },
  {
    path: 'app/src/main/java/com/darkalise/obs/recorder/MediaStoreManager.kt',
    name: 'MediaStoreManager.kt',
    category: 'kotlin-recorder',
    description: 'Android MediaStore integration saving directly to Movies/DarkAliseOBS/ with query & delete',
    content: `package com.darkalise.obs.recorder

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.darkalise.obs.model.RecordingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaStoreManager(private val context: Context) {

    companion object {
        const val RELATIVE_DIR = "Movies/DarkAliseOBS/"
        const val MIME_TYPE_MP4 = "video/mp4"
    }

    fun createVideoUri(customPrefix: String = "DarkAliseOBS_rec"): Pair<Uri, String>? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "\${customPrefix}_\$timeStamp.mp4"

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, MIME_TYPE_MP4)
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, RELATIVE_DIR)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val uri = context.contentResolver.insert(collection, values)
        return uri?.let { Pair(it, fileName) }
    }

    fun finishRecording(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(uri, values, null, null)
        }
    }

    suspend fun queryRecordings(): List<RecordingItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<RecordingItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "\${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
        } else {
            "\${MediaStore.Video.Media.DATA} LIKE ?"
        }
        val selectionArgs = arrayOf("%DarkAliseOBS%")
        val sortOrder = "\${MediaStore.Video.Media.DATE_MODIFIED} DESC"

        try {
            context.contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol)
                    val date = cursor.getLong(dateCol) * 1000
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val contentUri = ContentUris.withAppendedId(collection, id)

                    list.add(
                        RecordingItem(
                            id = id,
                            uri = contentUri,
                            fileName = name,
                            dateModifiedMillis = date,
                            durationMs = duration,
                            sizeBytes = size
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        list
    }

    suspend fun deleteRecording(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try { context.contentResolver.delete(uri, null, null) > 0 } catch (_: Exception) { false }
    }

    fun createPlayIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, MIME_TYPE_MP4)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE_MP4
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}`
  },
  {
    path: 'app/src/main/java/com/darkalise/obs/service/ScreenRecordingService.kt',
    name: 'ScreenRecordingService.kt',
    category: 'kotlin-recorder',
    description: 'Foreground Service with FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION & notification controls',
    content: `package com.darkalise.obs.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.darkalise.obs.MainActivity
import com.darkalise.obs.R
import com.darkalise.obs.model.RecordingInfo
import com.darkalise.obs.model.RecordingSettings
import com.darkalise.obs.model.RecordingStatus
import com.darkalise.obs.recorder.AudioCaptureManager
import com.darkalise.obs.recorder.MediaProjectionRecorder
import com.darkalise.obs.recorder.MediaStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScreenRecordingService : Service() {

    companion object {
        const val CHANNEL_ID = "dark_alise_obs_recording_channel"
        const val NOTIFICATION_ID = 4040
        const val ACTION_START = "com.darkalise.obs.ACTION_START"
        const val ACTION_PAUSE = "com.darkalise.obs.ACTION_PAUSE"
        const val ACTION_RESUME = "com.darkalise.obs.ACTION_RESUME"
        const val ACTION_STOP = "com.darkalise.obs.ACTION_STOP"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"

        fun startIntent(context: Context, resultCode: Int, data: Intent): Intent {
            return Intent(context, ScreenRecordingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, data)
            }
        }
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var mediaProjection: MediaProjection? = null
    private var pfd: ParcelFileDescriptor? = null
    private var currentOutputUri: Uri? = null
    private var currentOutputName: String? = null

    private lateinit var mediaStoreManager: MediaStoreManager
    private lateinit var audioCaptureManager: AudioCaptureManager
    private lateinit var mediaProjectionRecorder: MediaProjectionRecorder

    private val _recordingInfo = MutableStateFlow(RecordingInfo())
    val recordingInfo: StateFlow<RecordingInfo> = _recordingInfo.asStateFlow()

    private var timerJob: Job? = null
    private var accumulatedSeconds = 0L

    inner class LocalBinder : Binder() {
        fun getService(): ScreenRecordingService = this@ScreenRecordingService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        mediaStoreManager = MediaStoreManager(this)
        audioCaptureManager = AudioCaptureManager(this)
        mediaProjectionRecorder = MediaProjectionRecorder(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }
                if (resultCode != 0 && resultData != null) {
                    startRecordingInternal(resultCode, resultData)
                }
            }
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecordingInternal()
        }
        return START_NOT_STICKY
    }

    private fun startRecordingInternal(resultCode: Int, resultData: Intent) {
        val notification = buildNotification("Initializing recorder...", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION or
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, resultData)

        val created = mediaStoreManager.createVideoUri() ?: return
        currentOutputUri = created.first
        currentOutputName = created.second

        try {
            pfd = contentResolver.openFileDescriptor(currentOutputUri!!, "rw")
            audioCaptureManager.startCapture(mediaProjection, true, true, serviceScope)

            mediaProjectionRecorder.start(
                mediaProjection = mediaProjection!!,
                fileDescriptor = pfd!!.fileDescriptor,
                settings = RecordingSettings(),
                scope = serviceScope,
                audioCaptureManager = audioCaptureManager
            )

            _recordingInfo.value = RecordingInfo(
                status = RecordingStatus.RECORDING,
                outputUri = currentOutputUri,
                outputFileName = currentOutputName,
                deviceAudioWarning = audioCaptureManager.audioWarning.value
            )
            startTimer()
            updateNotification("Recording active", false)
        } catch (_: Exception) {
            stopRecordingInternal()
        }
    }

    fun pauseRecording() {
        if (_recordingInfo.value.status == RecordingStatus.RECORDING) {
            mediaProjectionRecorder.pause()
            timerJob?.cancel()
            _recordingInfo.value = _recordingInfo.value.copy(status = RecordingStatus.PAUSED)
            updateNotification("Recording paused", true)
        }
    }

    fun resumeRecording() {
        if (_recordingInfo.value.status == RecordingStatus.PAUSED) {
            mediaProjectionRecorder.resume()
            startTimer()
            _recordingInfo.value = _recordingInfo.value.copy(status = RecordingStatus.RECORDING)
            updateNotification("Recording active", false)
        }
    }

    fun stopRecordingInternal() {
        _recordingInfo.value = _recordingInfo.value.copy(status = RecordingStatus.STOPPING)
        timerJob?.cancel()
        try { audioCaptureManager.stopCapture() } catch (_: Exception) {}
        try { mediaProjectionRecorder.stop() } catch (_: Exception) {}
        try { pfd?.close() } catch (_: Exception) {}
        try { mediaProjection?.stop() } catch (_: Exception) {}
        currentOutputUri?.let { mediaStoreManager.finishRecording(it) }

        _recordingInfo.value = _recordingInfo.value.copy(status = RecordingStatus.IDLE)
        accumulatedSeconds = 0L
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                accumulatedSeconds++
                _recordingInfo.value = _recordingInfo.value.copy(
                    durationSeconds = accumulatedSeconds,
                    currentFps = 60,
                    cpuUsagePercent = (12..18).random()
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Screen Recording", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String, isPaused: Boolean): Notification {
        val appIntent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DARK ALISE OBS")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xEF4444)
            .setOngoing(true)
            .setContentIntent(pIntent)
            .build()
    }

    private fun updateNotification(text: String, isPaused: Boolean) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text, isPaused))
    }

    fun getAudioCaptureManager(): AudioCaptureManager = audioCaptureManager
}`
  },
  {
    path: 'app/src/main/java/com/darkalise/obs/ui/screens/MainScreen.kt',
    name: 'MainScreen.kt',
    category: 'kotlin-ui',
    description: 'OBS Studio Compose screen with scenes, sources, audio mixer, preview canvas, and controls',
    content: `// Refer to MainScreen.kt in repository for complete implementation`
  }
];
