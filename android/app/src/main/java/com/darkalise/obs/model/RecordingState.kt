package com.darkalise.obs.model

import android.net.Uri

enum class RecordingStatus {
    IDLE,
    PREPARING,
    RECORDING,
    PAUSED,
    STOPPING,
    ERROR
}

data class RecordingInfo(
    val status: RecordingStatus = RecordingStatus.IDLE,
    val durationSeconds: Long = 0L,
    val currentFps: Int = 0,
    val cpuUsagePercent: Int = 0,
    val outputUri: Uri? = null,
    val outputFileName: String? = null,
    val errorMessage: String? = null,
    val deviceAudioWarning: String? = null
) {
    val formattedTimer: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}

data class RecordingItem(
    val id: Long,
    val uri: Uri,
    val fileName: String,
    val dateModifiedMillis: Long,
    val durationMs: Long,
    val sizeBytes: Long
) {
    val formattedSize: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return String.format("%.1f MB", mb)
        }

    val formattedDuration: String
        get() {
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / (1000 * 60)) % 60
            val hours = durationMs / (1000 * 60 * 60)
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}
