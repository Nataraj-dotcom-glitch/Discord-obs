package com.darkalise.obs.model

enum class VideoResolution(val width: Int, val height: Int, val label: String) {
    RES_720P(1280, 720, "720p HD (1280x720)"),
    RES_1080P(1920, 1080, "1080p Full HD (1920x1080)")
}

enum class VideoFps(val fps: Int, val label: String) {
    FPS_30(30, "30 FPS (Standard)"),
    FPS_60(60, "60 FPS (High Smoothness)")
}

enum class VideoBitrate(val bps: Int, val label: String) {
    BITRATE_4M(4_000_000, "4 Mbps (Balanced)"),
    BITRATE_8M(8_000_000, "8 Mbps (Recommended Galaxy A14)"),
    BITRATE_12M(12_000_000, "12 Mbps (High Quality)"),
    BITRATE_16M(16_000_000, "16 Mbps (Master Quality)")
}

enum class VideoEncoderPreference(val label: String, val codecName: String) {
    HARDWARE_AUTO("Hardware H.264 (Auto / MediaTek OMX)", "video/avc"),
    SOFTWARE_FALLBACK("Software AVC / H.264 (Fallback)", "c2.android.avc.encoder")
}

data class RecordingSettings(
    val resolution: VideoResolution = VideoResolution.RES_1080P,
    val fps: VideoFps = VideoFps.FPS_60,
    val bitrate: VideoBitrate = VideoBitrate.BITRATE_8M,
    val encoder: VideoEncoderPreference = VideoEncoderPreference.HARDWARE_AUTO,
    val recordMic: Boolean = true,
    val recordDeviceAudio: Boolean = true,
    val audioSampleRate: Int = 48000,
    val audioBitrate: Int = 192000,
    val micVolume: Float = 1.0f,
    val deviceAudioVolume: Float = 1.0f,
    val saveDirectory: String = "Movies/DarkAliseOBS/",
    val filenamePrefix: String = "DarkAliseOBS_rec"
)
