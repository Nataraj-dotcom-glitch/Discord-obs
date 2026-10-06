package com.darkalise.obs.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.darkalise.obs.model.Scene
import com.darkalise.obs.model.Source
import com.darkalise.obs.model.SourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.scenesDataStore by preferencesDataStore(name = "dark_alise_obs_scenes")

class SceneRepository(private val context: Context) {

    companion object {
        private val KEY_SCENES_JSON = stringPreferencesKey("saved_scenes_json")
        private val KEY_ACTIVE_SCENE_ID = stringPreferencesKey("active_scene_id")

        val DEFAULT_SCENES: List<Scene> = listOf(
            Scene(
                id = "scene_gameplay",
                name = "Gaming Live",
                isDefault = true,
                sources = listOf(
                    Source(
                        id = "src_screen",
                        name = "Display Capture",
                        type = SourceType.SCREEN_CAPTURE,
                        isEnabled = true,
                        isLocked = true,
                        posX = 0f,
                        posY = 0f,
                        width = 1f,
                        height = 1f,
                        zIndex = 0
                    ),
                    Source(
                        id = "src_cam",
                        name = "Cam Overlay",
                        type = SourceType.FRONT_CAMERA,
                        isEnabled = true,
                        isLocked = false,
                        posX = 0.65f,
                        posY = 0.05f,
                        width = 0.30f,
                        height = 0.25f,
                        zIndex = 10
                    ),
                    Source(
                        id = "src_mic",
                        name = "Main Microphone",
                        type = SourceType.MICROPHONE,
                        isEnabled = true,
                        volume = 0.95f,
                        zIndex = 1
                    ),
                    Source(
                        id = "src_device_audio",
                        name = "Device Game Audio",
                        type = SourceType.DEVICE_AUDIO,
                        isEnabled = true,
                        volume = 0.85f,
                        zIndex = 2
                    )
                )
            ),
            Scene(
                id = "scene_chatting",
                name = "Facecam & Chat",
                isDefault = false,
                sources = listOf(
                    Source(
                        id = "src_cam_full",
                        name = "Camera Fullscreen",
                        type = SourceType.FRONT_CAMERA,
                        isEnabled = true,
                        isLocked = true,
                        posX = 0f,
                        posY = 0f,
                        width = 1f,
                        height = 1f,
                        zIndex = 0
                    ),
                    Source(
                        id = "src_text_title",
                        name = "Overlay Title",
                        type = SourceType.TEXT,
                        isEnabled = true,
                        textContent = "DARK ALISE OBS • LIVE",
                        posX = 0.05f,
                        posY = 0.05f,
                        zIndex = 5
                    ),
                    Source(
                        id = "src_mic_chat",
                        name = "Studio Mic",
                        type = SourceType.MICROPHONE,
                        isEnabled = true,
                        volume = 1f,
                        zIndex = 1
                    )
                )
            ),
            Scene(
                id = "scene_clean_screen",
                name = "Pure Screen",
                isDefault = false,
                sources = listOf(
                    Source(
                        id = "src_pure_screen",
                        name = "Display Capture Only",
                        type = SourceType.SCREEN_CAPTURE,
                        isEnabled = true,
                        isLocked = true,
                        posX = 0f,
                        posY = 0f,
                        width = 1f,
                        height = 1f,
                        zIndex = 0
                    )
                )
            )
        )
    }

    val scenesFlow: Flow<List<Scene>> = context.scenesDataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_SCENES_JSON]
        if (jsonStr.isNullOrEmpty()) {
            DEFAULT_SCENES
        } else {
            deserializeScenes(jsonStr)
        }
    }

    val activeSceneIdFlow: Flow<String> = context.scenesDataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_SCENE_ID] ?: "scene_gameplay"
    }

    suspend fun saveScenes(scenes: List<Scene>) {
        val serialized = serializeScenes(scenes)
        context.scenesDataStore.edit { prefs ->
            prefs[KEY_SCENES_JSON] = serialized
        }
    }

    suspend fun setActiveSceneId(sceneId: String) {
        context.scenesDataStore.edit { prefs ->
            prefs[KEY_ACTIVE_SCENE_ID] = sceneId
        }
    }

    private fun serializeScenes(scenes: List<Scene>): String {
        val array = JSONArray()
        for (scene in scenes) {
            val obj = JSONObject()
            obj.put("id", scene.id)
            obj.put("name", scene.name)
            obj.put("isDefault", scene.isDefault)

            val sourcesArray = JSONArray()
            for (src in scene.sources) {
                val sObj = JSONObject()
                sObj.put("id", src.id)
                sObj.put("name", src.name)
                sObj.put("type", src.type.name)
                sObj.put("isEnabled", src.isEnabled)
                sObj.put("isMuted", src.isMuted)
                sObj.put("volume", src.volume.toDouble())
                sObj.put("isLocked", src.isLocked)
                sObj.put("posX", src.posX.toDouble())
                sObj.put("posY", src.posY.toDouble())
                sObj.put("width", src.width.toDouble())
                sObj.put("height", src.height.toDouble())
                sObj.put("zIndex", src.zIndex)
                sObj.put("textContent", src.textContent)
                sObj.put("colorHex", src.colorHex)
                sourcesArray.put(sObj)
            }
            obj.put("sources", sourcesArray)
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeScenes(json: String): List<Scene> {
        return try {
            val list = mutableListOf<Scene>()
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id")
                val name = obj.optString("name")
                val isDefault = obj.optBoolean("isDefault", false)
                val sArray = obj.optJSONArray("sources")
                val sources = mutableListOf<Source>()
                if (sArray != null) {
                    for (j in 0 until sArray.length()) {
                        val sObj = sArray.getJSONObject(j)
                        val typeStr = sObj.optString("type")
                        val type = try { SourceType.valueOf(typeStr) } catch (_: Exception) { SourceType.SCREEN_CAPTURE }
                        sources.add(
                            Source(
                                id = sObj.optString("id"),
                                name = sObj.optString("name"),
                                type = type,
                                isEnabled = sObj.optBoolean("isEnabled", true),
                                isMuted = sObj.optBoolean("isMuted", false),
                                volume = sObj.optDouble("volume", 1.0).toFloat(),
                                isLocked = sObj.optBoolean("isLocked", false),
                                posX = sObj.optDouble("posX", 0.0).toFloat(),
                                posY = sObj.optDouble("posY", 0.0).toFloat(),
                                width = sObj.optDouble("width", 1.0).toFloat(),
                                height = sObj.optDouble("height", 1.0).toFloat(),
                                zIndex = sObj.optInt("zIndex", 0),
                                textContent = sObj.optString("textContent", "OBS Live"),
                                colorHex = sObj.optString("colorHex", "#7C3AED")
                            )
                        )
                    }
                }
                list.add(Scene(id = id, name = name, sources = sources, isDefault = isDefault))
            }
            if (list.isEmpty()) DEFAULT_SCENES else list
        } catch (_: Exception) {
            DEFAULT_SCENES
        }
    }
}
