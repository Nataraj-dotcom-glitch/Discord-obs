package com.darkalise.obs

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

    // MediaProjection screen capture consent launcher
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
}
