package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.DeckPreferencesManager
import com.example.model.ServerProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Estado general de la aplicación orientado exclusivamente al Cliente Web Kiosk de Macro Deck 3.
 */
data class MacroDeckUiState(
    val serverHost: String = "192.168.1.100",
    val serverPort: Int = 8191,
    val webCustomUrl: String = "",
    val keepScreenOn: Boolean = true,
    val autoHideTopBar: Boolean = true,
    val isTopBarVisible: Boolean = true,
    val isSettingsOpen: Boolean = false,
    val reloadTrigger: Long = 0L,
    val savedProfiles: List<ServerProfile> = emptyList()
) {
    /**
     * Calcula la URL efectiva del cliente web (http://<host>:<port> o URL personalizada).
     */
    val effectiveWebUrl: String
        get() {
            if (webCustomUrl.isNotBlank()) {
                val trimmed = webCustomUrl.trim()
                return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    trimmed
                } else "http://$trimmed"
            }
            val cleanHost = serverHost.trim()
                .removePrefix("http://")
                .removePrefix("https://")
                .removePrefix("ws://")
            return "http://$cleanHost:$serverPort"
        }
}

/**
 * ViewModel que gestiona la configuración del Cliente Web de Macro Deck 3,
 * el auto-ocultamiento de la barra superior y los perfiles de servidor.
 */
class MacroDeckViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = DeckPreferencesManager(application)

    private val _uiState = MutableStateFlow(
        MacroDeckUiState(
            serverHost = prefs.serverHost,
            serverPort = prefs.serverPort,
            webCustomUrl = prefs.webCustomUrl,
            keepScreenOn = prefs.keepScreenOn,
            autoHideTopBar = prefs.autoHideTopBar,
            isTopBarVisible = true,
            savedProfiles = prefs.getSavedProfiles()
        )
    )
    val uiState: StateFlow<MacroDeckUiState> = _uiState.asStateFlow()

    fun setServerHost(host: String) {
        _uiState.update { it.copy(serverHost = host) }
        prefs.serverHost = host
    }

    fun setServerPort(port: Int) {
        _uiState.update { it.copy(serverPort = port) }
        prefs.serverPort = port
    }

    fun setWebCustomUrl(url: String) {
        _uiState.update { it.copy(webCustomUrl = url) }
        prefs.webCustomUrl = url
    }

    fun setKeepScreenOn(enabled: Boolean) {
        _uiState.update { it.copy(keepScreenOn = enabled) }
        prefs.keepScreenOn = enabled
    }

    fun setAutoHideTopBar(enabled: Boolean) {
        _uiState.update { it.copy(autoHideTopBar = enabled) }
        prefs.autoHideTopBar = enabled
    }

    fun setTopBarVisible(visible: Boolean) {
        _uiState.update { it.copy(isTopBarVisible = visible) }
    }

    fun toggleSettingsDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen) }
        if (isOpen) {
            // Asegurar que la barra sea visible mientras se edita la configuración
            _uiState.update { it.copy(isTopBarVisible = true) }
        }
    }

    fun triggerReload() {
        _uiState.update { it.copy(reloadTrigger = System.currentTimeMillis()) }
    }

    fun selectProfile(profile: ServerProfile) {
        _uiState.update {
            it.copy(
                serverHost = profile.host,
                serverPort = profile.port,
                webCustomUrl = "",
                reloadTrigger = System.currentTimeMillis()
            )
        }
        prefs.serverHost = profile.host
        prefs.serverPort = profile.port
        prefs.webCustomUrl = ""
        prefs.saveProfile(profile.copy(lastUsedTimestamp = System.currentTimeMillis()))
        _uiState.update { it.copy(savedProfiles = prefs.getSavedProfiles()) }
    }

    fun saveCurrentAsProfile(name: String) {
        val p = ServerProfile(
            id = System.currentTimeMillis().toString(),
            name = name.ifBlank { "Servidor ${_uiState.value.serverHost}" },
            host = _uiState.value.serverHost,
            port = _uiState.value.serverPort
        )
        prefs.saveProfile(p)
        _uiState.update { it.copy(savedProfiles = prefs.getSavedProfiles()) }
    }

    fun deleteProfile(id: String) {
        prefs.deleteProfile(id)
        _uiState.update { it.copy(savedProfiles = prefs.getSavedProfiles()) }
    }
}
