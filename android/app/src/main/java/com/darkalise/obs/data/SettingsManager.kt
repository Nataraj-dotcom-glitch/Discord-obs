package com.darkalise.obs.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.darkalise.obs.model.RecordingSettings
import com.darkalise.obs.model.VideoBitrate
import com.darkalise.obs.model.VideoEncoderPreference
import com.darkalise.obs.model.VideoFps
import com.darkalise.obs.model.VideoResolution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "dark_alise_obs_settings")

class SettingsManager(private val context: Context) {

    companion object {
        val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch_completed")
        val KEY_RESOLUTION = stringPreferencesKey("video_resolution")
        val KEY_FPS = intPreferencesKey("video_fps")
        val KEY_BITRATE = intPreferencesKey("video_bitrate")
        val KEY_ENCODER = stringPreferencesKey("video_encoder")
        val KEY_RECORD_MIC = booleanPreferencesKey("record_mic")
        val KEY_RECORD_DEVICE_AUDIO = booleanPreferencesKey("record_device_audio")
        val KEY_MIC_VOLUME = floatPreferencesKey("mic_volume")
        val KEY_DEVICE_AUDIO_VOLUME = floatPreferencesKey("device_audio_volume")
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !(preferences[KEY_FIRST_LAUNCH] ?: false)
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[KEY_FIRST_LAUNCH] = true
        }
    }

    val settingsFlow: Flow<RecordingSettings> = context.dataStore.data.map { preferences ->
        val resName = preferences[KEY_RESOLUTION] ?: VideoResolution.RES_1080P.name
        val res = try { VideoResolution.valueOf(resName) } catch (_: Exception) { VideoResolution.RES_1080P }

        val fpsVal = preferences[KEY_FPS] ?: 60
        val fps = if (fpsVal == 30) VideoFps.FPS_30 else VideoFps.FPS_60

        val bitrateVal = preferences[KEY_BITRATE] ?: 8_000_000
        val bitrate = VideoBitrate.entries.find { it.bps == bitrateVal } ?: VideoBitrate.BITRATE_8M

        val encoderName = preferences[KEY_ENCODER] ?: VideoEncoderPreference.HARDWARE_AUTO.name
        val encoder = try { VideoEncoderPreference.valueOf(encoderName) } catch (_: Exception) { VideoEncoderPreference.HARDWARE_AUTO }

        RecordingSettings(
            resolution = res,
            fps = fps,
            bitrate = bitrate,
            encoder = encoder,
            recordMic = preferences[KEY_RECORD_MIC] ?: true,
            recordDeviceAudio = preferences[KEY_RECORD_DEVICE_AUDIO] ?: true,
            micVolume = preferences[KEY_MIC_VOLUME] ?: 1.0f,
            deviceAudioVolume = preferences[KEY_DEVICE_AUDIO_VOLUME] ?: 1.0f
        )
    }

    suspend fun updateSettings(settings: RecordingSettings) {
        context.dataStore.edit { preferences ->
            preferences[KEY_RESOLUTION] = settings.resolution.name
            preferences[KEY_FPS] = settings.fps.fps
            preferences[KEY_BITRATE] = settings.bitrate.bps
            preferences[KEY_ENCODER] = settings.encoder.name
            preferences[KEY_RECORD_MIC] = settings.recordMic
            preferences[KEY_RECORD_DEVICE_AUDIO] = settings.recordDeviceAudio
            preferences[KEY_MIC_VOLUME] = settings.micVolume
            preferences[KEY_DEVICE_AUDIO_VOLUME] = settings.deviceAudioVolume
        }
    }
}
