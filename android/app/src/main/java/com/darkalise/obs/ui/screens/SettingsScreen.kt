package com.darkalise.obs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkalise.obs.model.RecordingSettings
import com.darkalise.obs.model.VideoBitrate
import com.darkalise.obs.model.VideoEncoderPreference
import com.darkalise.obs.model.VideoFps
import com.darkalise.obs.model.VideoResolution
import com.darkalise.obs.ui.theme.ObsBlackBg
import com.darkalise.obs.ui.theme.ObsBorder
import com.darkalise.obs.ui.theme.ObsCardBg
import com.darkalise.obs.ui.theme.ObsPanelBg
import com.darkalise.obs.ui.theme.ObsPurplePrimary
import com.darkalise.obs.ui.theme.ObsTextMuted
import com.darkalise.obs.ui.theme.ObsTextPrimary
import com.darkalise.obs.ui.theme.ObsTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentSettings: RecordingSettings,
    onSaveSettings: (RecordingSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var settings by remember { mutableStateOf(currentSettings) }

    fun update(newVal: RecordingSettings) {
        settings = newVal
        onSaveSettings(newVal)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "STUDIO SETTINGS",
                        color = ObsTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ObsTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsPanelBg)
            )
        },
        containerColor = ObsBlackBg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Video Section
            SettingsSectionCard(title = "VIDEO ENCODING", icon = Icons.Default.Videocam) {
                // Resolution
                Text("Resolution", color = ObsTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoResolution.entries.forEach { res ->
                        FilterChip(
                            selected = settings.resolution == res,
                            onClick = { update(settings.copy(resolution = res)) },
                            label = { Text(res.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ObsPurplePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Frame Rate
                Text("Frame Rate (FPS)", color = ObsTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoFps.entries.forEach { fps ->
                        FilterChip(
                            selected = settings.fps == fps,
                            onClick = { update(settings.copy(fps = fps)) },
                            label = { Text(fps.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ObsPurplePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bitrate
                Text("Target Bitrate", color = ObsTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VideoBitrate.entries.forEach { bit ->
                        FilterChip(
                            selected = settings.bitrate == bit,
                            onClick = { update(settings.copy(bitrate = bit)) },
                            label = { Text(bit.label.substringBefore(" ("), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ObsPurplePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hardware vs Software Encoder
                Text("Encoder", color = ObsTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VideoEncoderPreference.entries.forEach { enc ->
                        FilterChip(
                            selected = settings.encoder == enc,
                            onClick = { update(settings.copy(encoder = enc)) },
                            label = { Text(enc.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ObsPurplePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Audio Section
            SettingsSectionCard(title = "AUDIO ENGINE", icon = Icons.Default.Audiotrack) {
                // Record Mic
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Record Microphone", color = ObsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Capture external microphone input", color = ObsTextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = settings.recordMic,
                        onCheckedChange = { update(settings.copy(recordMic = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsPurplePrimary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Record Device Audio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Android Playback / Device Audio", color = ObsTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Capture in-game audio via AudioPlaybackCapture (where permitted)", color = ObsTextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = settings.recordDeviceAudio,
                        onCheckedChange = { update(settings.copy(recordDeviceAudio = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsPurplePrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0E1A), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sample Rate: 48,000 Hz", color = ObsTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Bitrate: 192 kbps AAC", color = ObsTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Storage / Output Section
            SettingsSectionCard(title = "OUTPUT & STORAGE", icon = Icons.Default.Folder) {
                Text("Directory", color = ObsTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Movies/DarkAliseOBS/",
                    color = ObsPurplePrimary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0E1A), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Storage Framework: Android MediaStore (Scoped Storage API 29+)", color = ObsTextMuted, fontSize = 11.sp)
            }

            // Optimization Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF161326))
                    .border(1.dp, ObsBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ObsPurplePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Optimized for Samsung Galaxy A14 5G (MediaTek Dimensity 700 / Exynos 1330) hardware encoder with automatic software fallback.",
                    color = ObsTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ObsCardBg)
            .border(1.dp, ObsBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ObsPurplePrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = ObsTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        content()
    }
}
