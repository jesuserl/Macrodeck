package com.example.model

import android.graphics.Bitmap

/**
 * Representa un botón en la cuadrícula de Macro Deck 3.
 * Contiene coordenadas (X, Y), etiquetas visuales, color de fondo e icono en Base64.
 */
data class DeckButton(
    val id: String = "",
    val positionX: Int,
    val positionY: Int,
    val label: String = "",
    val iconBase64: String? = null,
    val backgroundColorHex: String? = null,
    val labelColorHex: String? = null,
    val fontSize: Float = 14f,
    val state: Boolean = false,
    val actionType: String = "PRESS",
    val isPressed: Boolean = false
)

/**
 * Configuración de la cuadrícula enviada por Macro Deck 3 Server.
 */
data class DeckGridConfig(
    val rows: Int = 3,
    val columns: Int = 5,
    val folderName: String = "Principal",
    val buttons: List<DeckButton> = emptyList()
)

/**
 * Estados de conexión del cliente WebSocket con Macro Deck 3.
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    data class Connecting(val host: String, val port: Int) : ConnectionState()
    data class Connected(val host: String, val port: Int, val latencyMs: Long = 0L) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * Perfil de conexión a un servidor Macro Deck guardado por el usuario.
 */
data class ServerProfile(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Evento de registro para la consola de depuración en vivo.
 */
data class LogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: Long = System.currentTimeMillis(),
    val direction: Direction,
    val message: String
) {
    enum class Direction { INCOMING, OUTGOING, SYSTEM }
}
