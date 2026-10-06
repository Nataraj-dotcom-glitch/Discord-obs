package com.darkalise.obs.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkalise.obs.model.RecordingInfo
import com.darkalise.obs.model.RecordingStatus
import com.darkalise.obs.ui.theme.ObsBorder
import com.darkalise.obs.ui.theme.ObsCardBg
import com.darkalise.obs.ui.theme.ObsGreenActive
import com.darkalise.obs.ui.theme.ObsPurplePrimary
import com.darkalise.obs.ui.theme.ObsRedRec
import com.darkalise.obs.ui.theme.ObsRedRecDark
import com.darkalise.obs.ui.theme.ObsTextMuted
import com.darkalise.obs.ui.theme.ObsTextPrimary
import com.darkalise.obs.ui.theme.ObsTextSecondary

@Composable
fun ControlsPanel(
    recordingInfo: RecordingInfo,
    onRequestStartRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onOpenRecordings: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecording = recordingInfo.status == RecordingStatus.RECORDING
    val isPaused = recordingInfo.status == RecordingStatus.PAUSED
    val isIdle = recordingInfo.status == RecordingStatus.IDLE || recordingInfo.status == RecordingStatus.ERROR

    Column(
        modifier = modifier
            .background(ObsCardBg, RoundedCornerShape(8.dp))
            .border(1.dp, ObsBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        // Status & Telemetry Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isRecording -> ObsRedRec
                                isPaused -> Color(0xFFF59E0B)
                                else -> ObsTextMuted
                            }
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        isRecording -> "REC"
                        isPaused -> "PAUSED"
                        else -> "STANDBY"
                    },
                    color = when {
                        isRecording -> ObsRedRec
                        isPaused -> Color(0xFFF59E0B)
                        else -> ObsTextMuted
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = recordingInfo.formattedTimer,
                color = if (isRecording || isPaused) ObsTextPrimary else ObsTextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${recordingInfo.currentFps} FPS",
                    color = ObsGreenActive,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${recordingInfo.cpuUsagePercent}% CPU",
                    color = ObsTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Recording Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isIdle) {
                Button(
                    onClick = onRequestStartRecording,
                    colors = ButtonDefaults.buttonColors(containerColor = ObsRedRec),
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Start Recording",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            } else {
                // Pause / Resume Button
                Button(
                    onClick = if (isPaused) onResumeRecording else onPauseRecording,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2942)),
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        tint = ObsTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPaused) "Resume" else "Pause",
                        color = ObsTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                // Stop Button
                Button(
                    onClick = onStopRecording,
                    colors = ButtonDefaults.buttonColors(containerColor = ObsRedRecDark),
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Stop",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onOpenRecordings,
                modifier = Modifier.weight(1f).height(36.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp), tint = ObsPurplePrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Recordings", fontSize = 11.sp, color = ObsTextPrimary)
            }

            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f).height(36.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp), tint = ObsPurplePrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Settings", fontSize = 11.sp, color = ObsTextPrimary)
            }
        }
    }
}
