package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Gestor de respuesta háptica y vibración física de ultra-baja latencia
 * para botones de Macro Deck. Cumple con las especificaciones de hardware
 * moderno usando [VibratorManager], [VibrationEffect] y [HapticFeedbackConstants].
 */
class HapticFeedbackHelper(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Vibración háptica táctil al pulsar un botón (Click rápido, ~15-20ms).
     */
    fun performClickFeedback(enabled: Boolean = true, view: View? = null) {
        if (!enabled) return

        // 1. Intento primario con API View Haptic Feedback si hay vista disponible
        view?.let {
            if (it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)) {
                return
            }
        }

        // 2. Ejecución directa a través del motor háptico del dispositivo
        vibrator?.let { vib ->
            if (!vib.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                vib.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(20)
            }
        }
    }

    /**
     * Vibración fuerte para pulsación larga o confirmación de acción crítica.
     */
    fun performHeavyFeedback(enabled: Boolean = true) {
        if (!enabled) return
        vibrator?.let { vib ->
            if (!vib.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                vib.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(45)
            }
        }
    }

    /**
     * Vibración doble para notificación de error o desconexión.
     */
    fun performErrorFeedback(enabled: Boolean = true) {
        if (!enabled) return
        vibrator?.let { vib ->
            if (!vib.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 30, 60, 40)
                val amplitudes = intArrayOf(0, 200, 0, 255)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(longArrayOf(0, 30, 60, 40), -1)
            }
        }
    }
}
