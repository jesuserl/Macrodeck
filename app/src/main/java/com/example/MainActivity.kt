package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ui.MacroDeckScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.ImmersiveModeHelper
import com.example.viewmodel.MacroDeckViewModel
import kotlinx.coroutines.launch

/**
 * Actividad principal nativa para Macro Deck 3 Web Client.
 * Configura el modo inmersivo horizontal completo (Kiosk Mode),
 * el bloqueo de suspensión de pantalla (Wake Lock) y el renderizado a pantalla completa.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MacroDeckViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Activar Modo Inmersivo de pantalla completa sticky
        ImmersiveModeHelper.enableImmersiveMode(window)

        // 2. Controlar si se mantiene la pantalla encendida reactivamente
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    ImmersiveModeHelper.setKeepScreenOn(window, state.keepScreenOn)
                }
            }
        }

        setContent {
            MyApplicationTheme {
                MacroDeckScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.post {
            ImmersiveModeHelper.enableImmersiveMode(window)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.post {
                ImmersiveModeHelper.enableImmersiveMode(window)
            }
        }
    }
}
