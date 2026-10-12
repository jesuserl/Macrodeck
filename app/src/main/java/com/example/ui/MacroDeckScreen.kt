package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.viewmodel.MacroDeckViewModel
import kotlinx.coroutines.delay

/**
 * Pantalla principal para el Cliente Web Kiosk de Macro Deck 3.
 * Renderiza el cliente web a pantalla completa al 100%, con una barra superior flotante
 * dotada de auto-ocultamiento temporal para una visualización inmersiva de hardware.
 */
@Composable
fun MacroDeckScreen(
    viewModel: MacroDeckViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Temporizador de auto-ocultamiento de la barra superior (3.5 segundos de inactividad)
    LaunchedEffect(uiState.isTopBarVisible, uiState.autoHideTopBar, lastInteractionTime) {
        if (uiState.isTopBarVisible && uiState.autoHideTopBar && !uiState.isSettingsOpen) {
            delay(3500L)
            viewModel.setTopBarVisible(false)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0D14))
            .testTag("macro_deck_screen")
    ) {
        // 1. Capa Principal: Cliente Web oficial en WebView a pantalla completa total
        DeckWebView(
            webUrl = uiState.effectiveWebUrl,
            reloadTrigger = uiState.reloadTrigger,
            onUserInteraction = {
                lastInteractionTime = System.currentTimeMillis()
            },
            onOpenSettings = {
                viewModel.toggleSettingsDialog(true)
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Capa Flotante: Barra Superior con animación de entrada/salida vertical
        AnimatedVisibility(
            visible = uiState.isTopBarVisible,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            DeckTopBar(
                webUrl = uiState.effectiveWebUrl,
                autoHideEnabled = uiState.autoHideTopBar,
                onToggleAutoHide = { enabled ->
                    viewModel.setAutoHideTopBar(enabled)
                    lastInteractionTime = System.currentTimeMillis()
                },
                onReloadClick = {
                    viewModel.triggerReload()
                    lastInteractionTime = System.currentTimeMillis()
                },
                onOpenSettingsClick = {
                    viewModel.toggleSettingsDialog(true)
                },
                onHideBarClick = {
                    viewModel.setTopBarVisible(false)
                }
            )
        }

        // 3. Tirador minimalista y ultra-translúcido para restaurar la barra sin obstruir la visualización
        if (!uiState.isTopBarVisible && !uiState.isSettingsOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
                    .clickable {
                        viewModel.setTopBarVisible(true)
                        lastInteractionTime = System.currentTimeMillis()
                    }
                    .padding(horizontal = 18.dp, vertical = 3.dp)
                    .testTag("restore_top_bar_handle"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Mostrar barra superior",
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 4. Modal Overlay de Configuración
        if (uiState.isSettingsOpen) {
            SettingsModal(
                serverHost = uiState.serverHost,
                serverPort = uiState.serverPort,
                webCustomUrl = uiState.webCustomUrl,
                keepScreenOn = uiState.keepScreenOn,
                autoHideTopBar = uiState.autoHideTopBar,
                savedProfiles = uiState.savedProfiles,
                onSaveHost = { viewModel.setServerHost(it) },
                onSavePort = { viewModel.setServerPort(it) },
                onSaveWebCustomUrl = { viewModel.setWebCustomUrl(it) },
                onToggleKeepScreenOn = { viewModel.setKeepScreenOn(it) },
                onToggleAutoHideTopBar = { viewModel.setAutoHideTopBar(it) },
                onReload = { viewModel.triggerReload() },
                onSelectProfile = { viewModel.selectProfile(it) },
                onSaveCurrentProfile = { viewModel.saveCurrentAsProfile(it) },
                onDeleteProfile = { viewModel.deleteProfile(it) },
                onDismiss = {
                    viewModel.toggleSettingsDialog(false)
                    lastInteractionTime = System.currentTimeMillis()
                }
            )
        }
    }
}
