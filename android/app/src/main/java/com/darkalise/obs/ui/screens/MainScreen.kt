package com.darkalise.obs.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkalise.obs.model.RecordingStatus
import com.darkalise.obs.ui.components.AudioMixerPanel
import com.darkalise.obs.ui.components.ControlsPanel
import com.darkalise.obs.ui.components.LivePreview
import com.darkalise.obs.ui.components.ScenesPanel
import com.darkalise.obs.ui.components.SourcesPanel
import com.darkalise.obs.ui.theme.ObsBlackBg
import com.darkalise.obs.ui.theme.ObsBorder
import com.darkalise.obs.ui.theme.ObsGreenActive
import com.darkalise.obs.ui.theme.ObsPanelBg
import com.darkalise.obs.ui.theme.ObsPurplePrimary
import com.darkalise.obs.ui.theme.ObsRedRec
import com.darkalise.obs.ui.theme.ObsTextMuted
import com.darkalise.obs.ui.theme.ObsTextPrimary
import com.darkalise.obs.ui.theme.ObsTextSecondary
import com.darkalise.obs.viewmodel.AppTab
import com.darkalise.obs.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestMediaProjection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeScene by viewModel.activeScene.collectAsState()
    val scenes by viewModel.scenes.collectAsState()
    val recordingInfo by viewModel.recordingInfo.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val micLevel by viewModel.micLevel.collectAsState()
    val devLevel by viewModel.deviceAudioLevel.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToastMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // OBS Logo Core
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF261D42))
                                .border(1.5.dp, ObsPurplePrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(ObsRedRec)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DARK ALISE OBS",
                            color = ObsTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Live Status Pill
                        val isRec = recordingInfo.status == RecordingStatus.RECORDING
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isRec) ObsRedRec.copy(alpha = 0.2f) else Color(0xFF1E1A2E))
                                .border(1.dp, if (isRec) ObsRedRec else ObsBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isRec) "LIVE ${recordingInfo.formattedTimer}" else "STANDBY",
                                color = if (isRec) ObsRedRec else ObsTextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.selectTab(AppTab.RECORDINGS) }) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Recordings",
                            tint = ObsTextSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.selectTab(AppTab.SETTINGS) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = ObsTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsPanelBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ObsBlackBg,
        modifier = modifier
    ) { innerPadding ->
        if (isLandscape) {
            // Landscape Studio Layout (Optimal for Samsung Galaxy A14 5G Landscape)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left 60%: Live Preview + Audio Mixer
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LivePreview(
                        activeScene = activeScene,
                        onSourceTransformChange = { id, x, y, w, h ->
                            viewModel.updateSourceTransform(id, x, y, w, h)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    AudioMixerPanel(
                        micLevel = micLevel,
                        micVolume = settings.micVolume,
                        isMicMuted = false,
                        onMicVolumeChange = { vol ->
                            viewModel.updateSettings(settings.copy(micVolume = vol))
                        },
                        onToggleMicMute = {
                            val activeMic = activeScene.sources.find { it.type == com.darkalise.obs.model.SourceType.MICROPHONE }
                            activeMic?.let { viewModel.toggleSourceMuted(it.id) }
                        },
                        deviceLevel = devLevel,
                        deviceVolume = settings.deviceAudioVolume,
                        isDeviceMuted = false,
                        onDeviceVolumeChange = { vol ->
                            viewModel.updateSettings(settings.copy(deviceAudioVolume = vol))
                        },
                        onToggleDeviceMute = {
                            val activeDev = activeScene.sources.find { it.type == com.darkalise.obs.model.SourceType.DEVICE_AUDIO }
                            activeDev?.let { viewModel.toggleSourceMuted(it.id) }
                        },
                        deviceAudioWarning = recordingInfo.deviceAudioWarning
                    )
                }

                // Right 40%: Scenes + Sources + Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ControlsPanel(
                        recordingInfo = recordingInfo,
                        onRequestStartRecording = onRequestMediaProjection,
                        onPauseRecording = { viewModel.pauseRecording() },
                        onResumeRecording = { viewModel.resumeRecording() },
                        onStopRecording = { viewModel.stopRecording() },
                        onOpenRecordings = { viewModel.selectTab(AppTab.RECORDINGS) },
                        onOpenSettings = { viewModel.selectTab(AppTab.SETTINGS) }
                    )

                    ScenesPanel(
                        scenes = scenes,
                        activeSceneId = activeScene.id,
                        onSelectScene = { viewModel.selectScene(it) },
                        onCreateScene = { viewModel.createScene(it) },
                        onRenameScene = { id, name -> viewModel.renameScene(id, name) },
                        onDuplicateScene = { viewModel.duplicateScene(it) },
                        onDeleteScene = { viewModel.deleteScene(it) }
                    )

                    SourcesPanel(
                        sources = activeScene.sources,
                        onAddSource = { viewModel.addSource(it) },
                        onToggleEnable = { viewModel.toggleSourceEnabled(it) },
                        onToggleLock = { viewModel.toggleSourceLocked(it) },
                        onDeleteSource = { viewModel.deleteSource(it) },
                        onReorderSource = { id, up -> viewModel.reorderSource(id, up) }
                    )
                }
            }
        } else {
            // Portrait Studio Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top: Live Preview
                LivePreview(
                    activeScene = activeScene,
                    onSourceTransformChange = { id, x, y, w, h ->
                        viewModel.updateSourceTransform(id, x, y, w, h)
                    }
                )

                // Recording Controls
                ControlsPanel(
                    recordingInfo = recordingInfo,
                    onRequestStartRecording = onRequestMediaProjection,
                    onPauseRecording = { viewModel.pauseRecording() },
                    onResumeRecording = { viewModel.resumeRecording() },
                    onStopRecording = { viewModel.stopRecording() },
                    onOpenRecordings = { viewModel.selectTab(AppTab.RECORDINGS) },
                    onOpenSettings = { viewModel.selectTab(AppTab.SETTINGS) }
                )

                // Audio Mixer
                AudioMixerPanel(
                    micLevel = micLevel,
                    micVolume = settings.micVolume,
                    isMicMuted = false,
                    onMicVolumeChange = { vol ->
                        viewModel.updateSettings(settings.copy(micVolume = vol))
                    },
                    onToggleMicMute = {
                        val activeMic = activeScene.sources.find { it.type == com.darkalise.obs.model.SourceType.MICROPHONE }
                        activeMic?.let { viewModel.toggleSourceMuted(it.id) }
                    },
                    deviceLevel = devLevel,
                    deviceVolume = settings.deviceAudioVolume,
                    isDeviceMuted = false,
                    onDeviceVolumeChange = { vol ->
                        viewModel.updateSettings(settings.copy(deviceAudioVolume = vol))
                    },
                    onToggleDeviceMute = {
                        val activeDev = activeScene.sources.find { it.type == com.darkalise.obs.model.SourceType.DEVICE_AUDIO }
                        activeDev?.let { viewModel.toggleSourceMuted(it.id) }
                    },
                    deviceAudioWarning = recordingInfo.deviceAudioWarning
                )

                // Scenes Panel
                ScenesPanel(
                    scenes = scenes,
                    activeSceneId = activeScene.id,
                    onSelectScene = { viewModel.selectScene(it) },
                    onCreateScene = { viewModel.createScene(it) },
                    onRenameScene = { id, name -> viewModel.renameScene(id, name) },
                    onDuplicateScene = { viewModel.duplicateScene(it) },
                    onDeleteScene = { viewModel.deleteScene(it) }
                )

                // Sources Panel
                SourcesPanel(
                    sources = activeScene.sources,
                    onAddSource = { viewModel.addSource(it) },
                    onToggleEnable = { viewModel.toggleSourceEnabled(it) },
                    onToggleLock = { viewModel.toggleSourceLocked(it) },
                    onDeleteSource = { viewModel.deleteSource(it) },
                    onReorderSource = { id, up -> viewModel.reorderSource(id, up) }
                )
            }
        }
    }
}
