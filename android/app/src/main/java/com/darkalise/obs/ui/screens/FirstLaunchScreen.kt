package com.darkalise.obs.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkalise.obs.ui.theme.ObsBlackBg
import com.darkalise.obs.ui.theme.ObsBorder
import com.darkalise.obs.ui.theme.ObsCardBg
import com.darkalise.obs.ui.theme.ObsGreenActive
import com.darkalise.obs.ui.theme.ObsPurplePrimary
import com.darkalise.obs.ui.theme.ObsRedRec
import com.darkalise.obs.ui.theme.ObsTextMuted
import com.darkalise.obs.ui.theme.ObsTextPrimary
import com.darkalise.obs.ui.theme.ObsTextSecondary

@Composable
fun FirstLaunchScreen(
    onPermissionsComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hasAudioPermission by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember {
        mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
    }

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsBlackBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hexagon / OBS Core Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1736))
                    .border(2.dp, ObsPurplePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ObsRedRec)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Name
            Text(
                text = "DARK ALISE OBS",
                color = ObsTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.SansSerif
            )

            // Subtitle
            Text(
                text = "Professional screen recording for Android",
                color = ObsPurplePrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Permissions Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsCardBg, RoundedCornerShape(12.dp))
                    .border(1.dp, ObsBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "REQUIRED PERMISSIONS",
                    color = ObsTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // 1. Microphone
                PermissionRow(
                    title = "Microphone",
                    desc = "Capture voice commentary and ambient sound",
                    isGranted = hasAudioPermission,
                    onRequest = { audioLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    icon = Icons.Default.Mic
                )

                // 2. Camera
                PermissionRow(
                    title = "Camera (Front)",
                    desc = "Front facecam PiP overlay while streaming/recording",
                    isGranted = hasCameraPermission,
                    onRequest = { cameraLauncher.launch(Manifest.permission.CAMERA) },
                    icon = Icons.Default.CameraAlt
                )

                // 3. Notification (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionRow(
                        title = "Notifications",
                        desc = "Keep foreground recording service alive with controls",
                        isGranted = hasNotificationPermission,
                        onRequest = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        icon = Icons.Default.Notifications
                    )
                }

                // 4. MediaProjection Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F0E1A), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenShare,
                        contentDescription = null,
                        tint = ObsPurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Screen capture consent will be requested directly by Android when starting each recording.",
                        color = ObsTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue Button
            Button(
                onClick = onPermissionsComplete,
                colors = ButtonDefaults.buttonColors(containerColor = ObsPurplePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Enter Dark Alise Studio",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    desc: String,
    isGranted: Boolean,
    onRequest: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF131021))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) ObsGreenActive else ObsTextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = ObsTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = desc,
                    color = ObsTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        if (isGranted) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(ObsGreenActive.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Granted",
                    tint = ObsGreenActive,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            OutlinedButton(
                onClick = onRequest,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Allow", fontSize = 11.sp, color = ObsPurplePrimary)
            }
        }
    }
}
