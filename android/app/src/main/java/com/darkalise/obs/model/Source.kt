package com.darkalise.obs.model

import java.util.UUID

enum class SourceType(val displayName: String) {
    SCREEN_CAPTURE("Screen Capture"),
    MICROPHONE("Microphone"),
    DEVICE_AUDIO("Device Audio"),
    FRONT_CAMERA("Front Camera Overlay"),
    IMAGE("Image Layer"),
    VIDEO("Video Media"),
    TEXT("Text Label"),
    COLOR("Color Background")
}

data class Source(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: SourceType,
    val isEnabled: Boolean = true,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f, // 0.0 to 1.0
    val isLocked: Boolean = false,
    val posX: Float = 0.0f,   // Normalized 0..1 or pixel relative
    val posY: Float = 0.0f,
    val width: Float = 1.0f,
    val height: Float = 1.0f,
    val zIndex: Int = 0,
    val textContent: String = "OBS Live",
    val colorHex: String = "#7C3AED",
    val mediaUri: String? = null
)
