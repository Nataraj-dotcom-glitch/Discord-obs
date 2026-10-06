package com.darkalise.obs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkalise.obs.ui.theme.ObsAmberWarn
import com.darkalise.obs.ui.theme.ObsBorder
import com.darkalise.obs.ui.theme.ObsCardBg
import com.darkalise.obs.ui.theme.ObsGreenActive
import com.darkalise.obs.ui.theme.ObsPurplePrimary
import com.darkalise.obs.ui.theme.ObsRedRec
import com.darkalise.obs.ui.theme.ObsTextMuted
import com.darkalise.obs.ui.theme.ObsTextPrimary
import com.darkalise.obs.ui.theme.ObsTextSecondary
import com.darkalise.obs.ui.theme.VuGreen
import com.darkalise.obs.ui.theme.VuRed
import com.darkalise.obs.ui.theme.VuYellow

@Composable
fun AudioMixerPanel(
    micLevel: Float,
    micVolume: Float,
    isMicMuted: Boolean,
    onMicVolumeChange: (Float) -> Unit,
    onToggleMicMute: () -> Unit,
    deviceLevel: Float,
    deviceVolume: Float,
    isDeviceMuted: Boolean,
    onDeviceVolumeChange: (Float) -> Unit,
    onToggleDeviceMute: () -> Unit,
    deviceAudioWarning: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(ObsCardBg, RoundedCornerShape(8.dp))
            .border(1.dp, ObsBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AUDIO MIXER",
                color = ObsTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "PCM 48kHz STEREO",
                color = ObsPurplePrimary,
                fontSize = 10.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Microphone Channel
        AudioChannelStrip(
            channelName = "Microphone",
            level = micLevel,
            volume = micVolume,
            isMuted = isMicMuted,
            onVolumeChange = onMicVolumeChange,
            onToggleMute = onToggleMicMute,
            icon = {
                Icon(
                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mic Mute",
                    tint = if (isMicMuted) ObsRedRec else ObsGreenActive,
                    modifier = Modifier.size(16.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Device Audio Channel
        AudioChannelStrip(
            channelName = "Device Audio",
            level = deviceLevel,
            volume = deviceVolume,
            isMuted = isDeviceMuted,
            onVolumeChange = onDeviceVolumeChange,
            onToggleMute = onToggleDeviceMute,
            icon = {
                Icon(
                    imageVector = if (isDeviceMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Device Audio Mute",
                    tint = if (isDeviceMuted) ObsRedRec else ObsGreenActive,
                    modifier = Modifier.size(16.dp)
                )
            }
        )

        // Strict Android Restriction Notice when applicable
        if (deviceAudioWarning != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2E1A1A), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF5E2A2A), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ObsAmberWarn,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = deviceAudioWarning,
                    color = Color(0xFFFCA5A5),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AudioChannelStrip(
    channelName: String,
    level: Float,
    volume: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    icon: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0E17), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) ObsRedRec else ObsGreenActive)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = channelName,
                    color = ObsTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isMuted) "MUTED" else "${(volume * 100).toInt()}%",
                    color = if (isMuted) ObsRedRec else ObsTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier.size(24.dp)
                ) {
                    icon()
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Level VU Meter Bar
        VuMeter(level = if (isMuted) 0f else level)

        // Volume Slider
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = ObsPurplePrimary,
                activeTrackColor = ObsPurplePrimary,
                inactiveTrackColor = ObsBorder
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
fun VuMeter(level: Float, modifier: Modifier = Modifier) {
    val clamped = level.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF1E1C2B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = clamped)
        ) {
            // Green Range (0..70%)
            Box(
                modifier = Modifier
                    .weight(0.7f, fill = false)
                    .fillMaxHeight()
                    .background(VuGreen)
            )
            // Yellow Range (70..90%)
            if (clamped > 0.7f) {
                Box(
                    modifier = Modifier
                        .weight(0.2f, fill = false)
                        .fillMaxHeight()
                        .background(VuYellow)
                )
            }
            // Red Clip Range (90..100%)
            if (clamped > 0.9f) {
                Box(
                    modifier = Modifier
                        .weight(0.1f, fill = false)
                        .fillMaxHeight()
                        .background(VuRed)
                )
            }
        }
    }
}
