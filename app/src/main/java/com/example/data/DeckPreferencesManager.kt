package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ServerProfile
import org.json.JSONArray
import org.json.JSONObject

/**
 * Gestor de persistencia local para configuraciones del cliente Macro Deck 3.
 * Guarda IP de servidor, puerto, nombre de dispositivo cliente, perfiles y preferencias de UX.
 */
class DeckPreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("macro_deck_preferences", Context.MODE_PRIVATE)

    var serverHost: String
        get() = prefs.getString(KEY_HOST, "192.168.1.100") ?: "192.168.1.100"
        set(value) = prefs.edit().putString(KEY_HOST, value.trim()).apply()

    var serverPort: Int
        get() = prefs.getInt(KEY_PORT, 8191)
        set(value) = prefs.edit().putInt(KEY_PORT, value).apply()

    var clientDeviceName: String
        get() = prefs.getString(KEY_DEVICE_NAME, "Android Macro Deck") ?: "Android Macro Deck"
        set(value) = prefs.edit().putString(KEY_DEVICE_NAME, value.trim()).apply()

    var autoReconnect: Boolean
        get() = prefs.getBoolean(KEY_AUTO_RECONNECT, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_RECONNECT, value).apply()

    var hapticFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()

    var autoHideTopBar: Boolean
        get() = prefs.getBoolean(KEY_AUTO_HIDE_TOP_BAR, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_HIDE_TOP_BAR, value).apply()

    var webCustomUrl: String
        get() = prefs.getString(KEY_WEB_CUSTOM_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEB_CUSTOM_URL, value).apply()

    fun getSavedProfiles(): List<ServerProfile> {
        val jsonString = prefs.getString(KEY_PROFILES, null) ?: return defaultProfiles()
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<ServerProfile>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ServerProfile(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        name = obj.optString("name", "Servidor"),
                        host = obj.optString("host", "192.168.1.100"),
                        port = obj.optInt("port", 8191),
                        lastUsedTimestamp = obj.optLong("lastUsedTimestamp", 0L)
                    )
                )
            }
            if (list.isEmpty()) defaultProfiles() else list
        } catch (e: Exception) {
            defaultProfiles()
        }
    }

    fun saveProfile(profile: ServerProfile) {
        val current = getSavedProfiles().toMutableList()
        val index = current.indexOfFirst { it.id == profile.id || (it.host == profile.host && it.port == profile.port) }
        if (index >= 0) {
            current[index] = profile
        } else {
            current.add(0, profile)
        }
        persistProfiles(current)
    }

    fun deleteProfile(profileId: String) {
        val current = getSavedProfiles().filterNot { it.id == profileId }
        persistProfiles(current)
    }

    private fun persistProfiles(profiles: List<ServerProfile>) {
        val jsonArray = JSONArray()
        for (p in profiles) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("host", p.host)
                put("port", p.port)
                put("lastUsedTimestamp", p.lastUsedTimestamp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_PROFILES, jsonArray.toString()).apply()
    }

    private fun defaultProfiles(): List<ServerProfile> = listOf(
        ServerProfile("def_pc", "PC Principal (Gaming/OBS)", "192.168.1.100", 8191),
        ServerProfile("def_laptop", "Laptop / Secundario", "192.168.1.150", 8191)
    )

    companion object {
        private const val KEY_HOST = "key_server_host"
        private const val KEY_PORT = "key_server_port"
        private const val KEY_DEVICE_NAME = "key_device_name"
        private const val KEY_AUTO_RECONNECT = "key_auto_reconnect"
        private const val KEY_HAPTIC_ENABLED = "key_haptic_enabled"
        private const val KEY_KEEP_SCREEN_ON = "key_keep_screen_on"
        private const val KEY_AUTO_HIDE_TOP_BAR = "key_auto_hide_top_bar"
        private const val KEY_WEB_CUSTOM_URL = "key_web_custom_url"
        private const val KEY_PROFILES = "key_saved_profiles"
    }
}
