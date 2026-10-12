package com.example.util

import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Utilidad para configurar el Modo Inmersivo (Kiosk/Deck) en pantalla completa.
 * Oculta la barra de estado y los botones de navegación del sistema,
 * permitiendo una experiencia de hardware dedicado sin distracciones.
 */
object ImmersiveModeHelper {

    /**
     * Activa el modo inmersivo completo (Full-Screen Sticky Immersive Mode).
     * Los gestos desde los bordes muestran barras translúcidas temporales
     * que vuelven a ocultarse automáticamente tras unos segundos.
     */
    fun enableImmersiveMode(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.let { insetsController ->
            // Ocultar ambas barras (estado y navegación)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            // Comportamiento inmersivo al deslizar: no altera el layout permanente
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /**
     * Mantiene la pantalla encendida permanentemente mientras el Deck esté activo.
     */
    fun setKeepScreenOn(window: Window, keepOn: Boolean) {
        if (keepOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
