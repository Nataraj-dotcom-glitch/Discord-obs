package com.darkalise.obs.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.darkalise.obs.data.SceneRepository
import com.darkalise.obs.data.SettingsManager
import com.darkalise.obs.model.RecordingInfo
import com.darkalise.obs.model.RecordingItem
import com.darkalise.obs.model.RecordingSettings
import com.darkalise.obs.model.RecordingStatus
import com.darkalise.obs.model.Scene
import com.darkalise.obs.model.Source
import com.darkalise.obs.model.SourceType
import com.darkalise.obs.recorder.AudioCaptureManager
import com.darkalise.obs.recorder.MediaStoreManager
import com.darkalise.obs.service.ScreenRecordingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    STUDIO,
    RECORDINGS,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sceneRepo = SceneRepository(context)
    private val settingsManager = SettingsManager(context)
    private val mediaStoreManager = MediaStoreManager(context)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.STUDIO)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // First Launch flow
    val isFirstLaunch: StateFlow<Boolean> = settingsManager.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Settings
    val settings: StateFlow<RecordingSettings> = settingsManager.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, RecordingSettings())

    // Scenes & Active Scene
    private val _scenes = MutableStateFlow<List<Scene>>(SceneRepository.DEFAULT_SCENES)
    val scenes: StateFlow<List<Scene>> = _scenes.asStateFlow()

    private val _activeSceneId = MutableStateFlow("scene_gameplay")
    val activeSceneId: StateFlow<String> = _activeSceneId.asStateFlow()

    val activeScene: StateFlow<Scene> = combine(_scenes, _activeSceneId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull() ?: Scene(name = "Default Scene")
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SceneRepository.DEFAULT_SCENES.first())

    // Recording Service State
    private var recordingService: ScreenRecordingService? = null
    private var isServiceBound = false

    private val _recordingInfo = MutableStateFlow(RecordingInfo())
    val recordingInfo: StateFlow<RecordingInfo> = _recordingInfo.asStateFlow()

    // Live Audio Levels for Mixer
    private val _micLevel = MutableStateFlow(0f)
    val micLevel: StateFlow<Float> = _micLevel.asStateFlow()

    private val _deviceAudioLevel = MutableStateFlow(0f)
    val deviceAudioLevel: StateFlow<Float> = _deviceAudioLevel.asStateFlow()

    // Recordings List
    private val _recordings = MutableStateFlow<List<RecordingItem>>(emptyList())
    val recordings: StateFlow<List<RecordingItem>> = _recordings.asStateFlow()

    // Notification / Alert message for UI
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ScreenRecordingService.LocalBinder
            recordingService = binder.getService()
            isServiceBound = true

            // Collect service recording status
            viewModelScope.launch {
                recordingService?.recordingInfo?.collect { info ->
                    _recordingInfo.value = info
                }
            }

            // Collect live levels
            viewModelScope.launch {
                recordingService?.getAudioCaptureManager()?.micLevel?.collect {
                    _micLevel.value = it
                }
            }
            viewModelScope.launch {
                recordingService?.getAudioCaptureManager()?.deviceLevel?.collect {
                    _deviceAudioLevel.value = it
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            recordingService = null
            isServiceBound = false
        }
    }

    init {
        // Load scenes from datastore
        viewModelScope.launch {
            sceneRepo.scenesFlow.collect { savedScenes ->
                _scenes.value = savedScenes
            }
        }
        viewModelScope.launch {
            sceneRepo.activeSceneIdFlow.collect { id ->
                _activeSceneId.value = id
            }
        }
        refreshRecordings()
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
        if (tab == AppTab.RECORDINGS) {
            refreshRecordings()
        }
    }

    fun completeFirstLaunch() {
        viewModelScope.launch {
            settingsManager.setFirstLaunchCompleted()
        }
    }

    fun updateSettings(newSettings: RecordingSettings) {
        viewModelScope.launch {
            settingsManager.updateSettings(newSettings)
            _toastMessage.value = "Settings saved."
        }
    }

    // ==========================================
    // SCENE MANAGEMENT
    // ==========================================

    fun selectScene(sceneId: String) {
        _activeSceneId.value = sceneId
        viewModelScope.launch {
            sceneRepo.setActiveSceneId(sceneId)
        }
    }

    fun createScene(name: String) {
        val newScene = Scene(
            name = name,
            sources = listOf(
                Source(name = "Display Capture", type = SourceType.SCREEN_CAPTURE, isEnabled = true),
                Source(name = "Microphone", type = SourceType.MICROPHONE, isEnabled = true)
            )
        )
        val updated = _scenes.value + newScene
        _scenes.value = updated
        selectScene(newScene.id)
        persistScenes(updated)
    }

    fun renameScene(sceneId: String, newName: String) {
        val updated = _scenes.value.map {
            if (it.id == sceneId) it.copy(name = newName) else it
        }
        _scenes.value = updated
        persistScenes(updated)
    }

    fun duplicateScene(sceneId: String) {
        val original = _scenes.value.find { it.id == sceneId } ?: return
        val duplicated = original.duplicate()
        val updated = _scenes.value + duplicated
        _scenes.value = updated
        selectScene(duplicated.id)
        persistScenes(updated)
    }

    fun deleteScene(sceneId: String) {
        if (_scenes.value.size <= 1) {
            _toastMessage.value = "Cannot delete the last remaining scene."
            return
        }
        val updated = _scenes.value.filter { it.id != sceneId }
        _scenes.value = updated
        if (_activeSceneId.value == sceneId) {
            selectScene(updated.first().id)
        }
        persistScenes(updated)
    }

    private fun persistScenes(list: List<Scene>) {
        viewModelScope.launch {
            sceneRepo.saveScenes(list)
        }
    }

    // ==========================================
    // SOURCE MANAGEMENT
    // ==========================================

    fun addSource(type: SourceType, name: String = type.displayName) {
        val current = activeScene.value
        val newSource = Source(
            name = name,
            type = type,
            posX = if (type == SourceType.FRONT_CAMERA) 0.65f else 0f,
            posY = if (type == SourceType.FRONT_CAMERA) 0.05f else 0f,
            width = if (type == SourceType.FRONT_CAMERA) 0.30f else 1f,
            height = if (type == SourceType.FRONT_CAMERA) 0.25f else 1f,
            zIndex = (current.sources.maxOfOrNull { it.zIndex } ?: 0) + 1
        )
        val updatedSources = current.sources + newSource
        updateActiveSceneSources(updatedSources)
    }

    fun toggleSourceEnabled(sourceId: String) {
        val current = activeScene.value
        val updated = current.sources.map {
            if (it.id == sourceId) it.copy(isEnabled = !it.isEnabled) else it
        }
        updateActiveSceneSources(updated)
    }

    fun toggleSourceMuted(sourceId: String) {
        val current = activeScene.value
        val updated = current.sources.map {
            if (it.id == sourceId) it.copy(isMuted = !it.isMuted) else it
        }
        updateActiveSceneSources(updated)
    }

    fun setSourceVolume(sourceId: String, volume: Float) {
        val current = activeScene.value
        val updated = current.sources.map {
            if (it.id == sourceId) it.copy(volume = volume.coerceIn(0f, 1f)) else it
        }
        updateActiveSceneSources(updated)
    }

    fun toggleSourceLocked(sourceId: String) {
        val current = activeScene.value
        val updated = current.sources.map {
            if (it.id == sourceId) it.copy(isLocked = !it.isLocked) else it
        }
        updateActiveSceneSources(updated)
    }

    fun updateSourceTransform(sourceId: String, posX: Float, posY: Float, width: Float, height: Float) {
        val current = activeScene.value
        val updated = current.sources.map {
            if (it.id == sourceId && !it.isLocked) {
                it.copy(
                    posX = posX.coerceIn(0f, 0.9f),
                    posY = posY.coerceIn(0f, 0.9f),
                    width = width.coerceIn(0.1f, 1f),
                    height = height.coerceIn(0.1f, 1f)
                )
            } else it
        }
        updateActiveSceneSources(updated)
    }

    fun deleteSource(sourceId: String) {
        val current = activeScene.value
        val updated = current.sources.filter { it.id != sourceId }
        updateActiveSceneSources(updated)
    }

    fun reorderSource(sourceId: String, moveUp: Boolean) {
        val current = activeScene.value
        val index = current.sources.indexOfFirst { it.id == sourceId }
        if (index == -1) return
        val targetIndex = if (moveUp) index + 1 else index - 1
        if (targetIndex in current.sources.indices) {
            val mutable = current.sources.toMutableList()
            val item = mutable.removeAt(index)
            mutable.add(targetIndex, item)
            updateActiveSceneSources(mutable)
        }
    }

    private fun updateActiveSceneSources(sources: List<Source>) {
        val current = activeScene.value
        val updatedScene = current.copy(sources = sources)
        val allScenes = _scenes.value.map {
            if (it.id == updatedScene.id) updatedScene else it
        }
        _scenes.value = allScenes
        persistScenes(allScenes)
    }

    // ==========================================
    // RECORDING CONTROLS
    // ==========================================

    fun startRecording(resultCode: Int, data: Intent) {
        val intent = ScreenRecordingService.startIntent(context, resultCode, data)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun pauseRecording() {
        recordingService?.pauseRecording()
    }

    fun resumeRecording() {
        recordingService?.resumeRecording()
    }

    fun stopRecording() {
        recordingService?.stopRecordingInternal()
        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) { e.printStackTrace() }
            isServiceBound = false
        }
        refreshRecordings()
    }

    // ==========================================
    // RECORDINGS MANAGEMENT
    // ==========================================

    fun refreshRecordings() {
        viewModelScope.launch {
            _recordings.value = mediaStoreManager.queryRecordings()
        }
    }

    fun deleteRecording(item: RecordingItem) {
        viewModelScope.launch {
            val success = mediaStoreManager.deleteRecording(item.uri)
            if (success) {
                _toastMessage.value = "Recording deleted."
                refreshRecordings()
            } else {
                _toastMessage.value = "Failed to delete recording."
            }
        }
    }

    fun playRecording(item: RecordingItem) {
        try {
            val intent = mediaStoreManager.createPlayIntent(item.uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _toastMessage.value = "No video player application found."
        }
    }

    fun shareRecording(item: RecordingItem) {
        try {
            val intent = mediaStoreManager.createShareIntent(item.uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Recording").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            _toastMessage.value = "Unable to share recording."
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    override fun onCleared() {
        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) { e.printStackTrace() }
        }
        super.onCleared()
    }
}
