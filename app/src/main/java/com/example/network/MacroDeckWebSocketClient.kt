package com.example.network

import com.example.model.ConnectionState
import com.example.model.DeckButton
import com.example.model.DeckGridConfig
import com.example.model.LogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Cliente WebSocket de alto rendimiento para Macro Deck 3 Server.
 * Gestiona conexiones persistentes de baja latencia con OkHttp,
 * reconexión exponencial automática, emisión inmediata de eventos táctiles
 * y análisis de mensajes JSON offloaded a [Dispatchers.Default].
 */
class MacroDeckWebSocketClient(
    private val clientScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // WebSocket no debe expirar en lectura
        .build()

    private var activeWebSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var pingJob: Job? = null

    private var currentHost: String = ""
    private var currentPort: Int = 8191
    private var currentClientName: String = "Android Macro Deck"
    private var autoReconnect: Boolean = true
    private var retryCount = 0
    private val clientId = UUID.randomUUID().toString()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _gridConfig = MutableStateFlow(DeckGridConfig())
    val gridConfig: StateFlow<DeckGridConfig> = _gridConfig.asStateFlow()

    private val _buttonUpdate = MutableSharedFlow<DeckButton>(extraBufferCapacity = 64)
    val buttonUpdate: SharedFlow<DeckButton> = _buttonUpdate.asSharedFlow()

    private val _logs = MutableSharedFlow<LogEntry>(extraBufferCapacity = 100)
    val logs: SharedFlow<LogEntry> = _logs.asSharedFlow()

    /**
     * Inicia la conexión con el servidor Macro Deck 3 en ws://<host>:<port>.
     */
    fun connect(host: String, port: Int, clientName: String, enableAutoReconnect: Boolean = true) {
        currentHost = host
        currentPort = port
        currentClientName = clientName
        autoReconnect = enableAutoReconnect
        retryCount = 0

        reconnectJob?.cancel()
        disconnectInternal(notify = false)

        val cleanHost = host.trim().removePrefix("ws://").removePrefix("http://")
        val wsUrl = "ws://$cleanHost:$port"

        _connectionState.value = ConnectionState.Connecting(cleanHost, port)
        emitLog(LogEntry.Direction.SYSTEM, "Conectando a Macro Deck 3 Server: $wsUrl")

        val request = try {
            Request.Builder().url(wsUrl).build()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("URL inválida: ${e.message}")
            emitLog(LogEntry.Direction.SYSTEM, "Error en formato de URL: ${e.message}")
            return
        }

        activeWebSocket = okHttpClient.newWebSocket(request, createWebSocketListener(cleanHost, port))
    }

    /**
     * Cierra la conexión activa de forma limpia.
     */
    fun disconnect() {
        autoReconnect = false
        reconnectJob?.cancel()
        pingJob?.cancel()
        disconnectInternal(notify = true)
    }

    private fun disconnectInternal(notify: Boolean) {
        try {
            activeWebSocket?.close(1000, "Cierre voluntario por el cliente")
        } catch (_: Exception) {
        }
        activeWebSocket = null
        if (notify) {
            _connectionState.value = ConnectionState.Disconnected
            emitLog(LogEntry.Direction.SYSTEM, "Desconectado del servidor")
        }
    }

    /**
     * Emite un evento de pulsación [BUTTON_PRESS] con ultra-baja latencia hacia el PC.
     */
    fun sendButtonPress(x: Int, y: Int) {
        val payload = JSONObject().apply {
            put("Method", "BUTTON_PRESS")
            put("X", x)
            put("Y", y)
            put("Timestamp", System.currentTimeMillis())
        }
        sendRawJson(payload.toString(), "BUTTON_PRESS ($x, $y)")
    }

    /**
     * Emite el evento táctil inicial [BUTTON_DOWN] al tocar la pantalla.
     */
    fun sendButtonDown(x: Int, y: Int) {
        val payload = JSONObject().apply {
            put("Method", "BUTTON_DOWN")
            put("X", x)
            put("Y", y)
        }
        sendRawJson(payload.toString(), "BUTTON_DOWN ($x, $y)")
    }

    /**
     * Emite el evento táctil [BUTTON_UP] al levantar el dedo.
     */
    fun sendButtonUp(x: Int, y: Int) {
        val payload = JSONObject().apply {
            put("Method", "BUTTON_UP")
            put("X", x)
            put("Y", y)
        }
        sendRawJson(payload.toString(), "BUTTON_UP ($x, $y)")
    }

    /**
     * Emite [BUTTON_LONG_PRESS] al mantener presionado el botón.
     */
    fun sendButtonLongPress(x: Int, y: Int) {
        val payload = JSONObject().apply {
            put("Method", "BUTTON_LONG_PRESS")
            put("X", x)
            put("Y", y)
        }
        sendRawJson(payload.toString(), "BUTTON_LONG_PRESS ($x, $y)")
    }

    /**
     * Solicita cambio de carpeta o navegación en el deck.
     */
    fun navigateFolder(folderName: String) {
        val payload = JSONObject().apply {
            put("Method", "SET_FOLDER")
            put("Folder", folderName)
        }
        sendRawJson(payload.toString(), "SET_FOLDER: $folderName")
    }

    private fun sendRawJson(json: String, logTag: String) {
        val ws = activeWebSocket
        if (ws != null) {
            val sent = ws.send(json)
            if (sent) {
                emitLog(LogEntry.Direction.OUTGOING, "Tx: $logTag")
            } else {
                emitLog(LogEntry.Direction.SYSTEM, "Error al enviar: $logTag (Buffer lleno o desconectado)")
            }
        } else {
            emitLog(LogEntry.Direction.SYSTEM, "Desconectado: no se pudo enviar $logTag")
        }
    }

    private fun createWebSocketListener(host: String, port: Int) = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            retryCount = 0
            _connectionState.value = ConnectionState.Connected(host, port, latencyMs = 1L)
            emitLog(LogEntry.Direction.SYSTEM, "WebSocket conectado con éxito.")

            // 1. Handshake de registro de cliente Macro Deck 3
            val handshake = JSONObject().apply {
                put("Method", "CONNECTED")
                put("API_LEVEL", 30)
                put("ClientType", "Android")
                put("DeviceName", currentClientName)
                put("ClientId", clientId)
            }
            webSocket.send(handshake.toString())
            emitLog(LogEntry.Direction.OUTGOING, "Tx Handshake: CONNECTED")

            // 2. Solicitar configuración de cuadrícula actual
            val getConfig = JSONObject().apply {
                put("Method", "GET_CONFIG")
            }
            webSocket.send(getConfig.toString())

            // 3. Iniciar pings periódicos para medir latencia de red
            startLatencyMonitor(webSocket)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // Offload inmediato a Dispatchers.Default para evitar jank en UI
            clientScope.launch(Dispatchers.Default) {
                processIncomingPayload(text)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            emitLog(LogEntry.Direction.SYSTEM, "Servidor cerrando conexión ($code: $reason)")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _connectionState.value = ConnectionState.Disconnected
            emitLog(LogEntry.Direction.SYSTEM, "Conexión cerrada ($code)")
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            val errorMsg = t.message ?: "Fallo de conexión"
            _connectionState.value = ConnectionState.Error(errorMsg)
            emitLog(LogEntry.Direction.SYSTEM, "Error WebSocket: $errorMsg")
            scheduleReconnect()
        }
    }

    private suspend fun processIncomingPayload(payloadText: String) = withContext(Dispatchers.Default) {
        try {
            val json = JSONObject(payloadText)
            val method = json.optString("Method", json.optString("method", json.optString("type", "")))

            when {
                // Heartbeat / Ping respuesta
                method.equals("PONG", ignoreCase = true) || json.has("Latency") -> {
                    val currentState = _connectionState.value
                    if (currentState is ConnectionState.Connected) {
                        val pingTimestamp = json.optLong("PingTimestamp", 0L)
                        val latency = if (pingTimestamp > 0) {
                            (System.currentTimeMillis() - pingTimestamp).coerceAtLeast(1L)
                        } else {
                            currentState.latencyMs
                        }
                        _connectionState.value = currentState.copy(latencyMs = latency)
                    }
                }

                // Configuración completa de la cuadrícula
                method.equals("GET_CONFIG", ignoreCase = true) ||
                method.equals("CONFIGURATION", ignoreCase = true) ||
                method.equals("UPDATE_GRID", ignoreCase = true) ||
                json.has("Rows") || json.has("rows") -> {
                    val config = parseGridConfig(json)
                    _gridConfig.value = config
                    emitLog(LogEntry.Direction.INCOMING, "Rx Grid: ${config.columns}x${config.rows} (${config.buttons.size} botones)")
                }

                // Actualización de un único botón en tiempo real
                method.equals("UPDATE_BUTTON", ignoreCase = true) -> {
                    val buttonObj = json.optJSONObject("Button") ?: json
                    val button = parseSingleButton(buttonObj)
                    _buttonUpdate.emit(button)
                    emitLog(LogEntry.Direction.INCOMING, "Rx Botón (${button.positionX}, ${button.positionY}): ${button.label}")
                }

                else -> {
                    // Si el objeto contiene array "Buttons", procesarlo como grid
                    if (json.has("Buttons") || json.has("buttons")) {
                        val config = parseGridConfig(json)
                        _gridConfig.value = config
                    } else {
                        emitLog(LogEntry.Direction.INCOMING, "Rx JSON: ${payloadText.take(60)}...")
                    }
                }
            }
        } catch (e: Exception) {
            emitLog(LogEntry.Direction.SYSTEM, "Error parseando JSON: ${e.message}")
        }
    }

    private fun parseGridConfig(json: JSONObject): DeckGridConfig {
        val rows = json.optInt("Rows", json.optInt("rows", 3)).coerceIn(1, 10)
        val columns = json.optInt("Columns", json.optInt("columns", 5)).coerceIn(1, 10)
        val folder = json.optString("Folder", json.optString("folder", "Principal"))

        val buttonsArray = json.optJSONArray("Buttons") ?: json.optJSONArray("buttons") ?: JSONArray()
        val buttonsList = mutableListOf<DeckButton>()

        for (i in 0 until buttonsArray.length()) {
            val bObj = buttonsArray.optJSONObject(i) ?: continue
            buttonsList.add(parseSingleButton(bObj))
        }

        return DeckGridConfig(
            rows = rows,
            columns = columns,
            folderName = folder,
            buttons = buttonsList
        )
    }

    private fun parseSingleButton(json: JSONObject): DeckButton {
        val posX = json.optInt("Position_X", json.optInt("positionX", json.optInt("X", 0)))
        val posY = json.optInt("Position_Y", json.optInt("positionY", json.optInt("Y", 0)))
        val label = json.optString("Label", json.optString("label", ""))
        val icon = json.optString("Icon", json.optString("iconBase64", json.optString("icon", null)))
        val bg = json.optString("BackgroundColor", json.optString("backgroundColor", null))
        val labelColor = json.optString("LabelColor", json.optString("labelColor", null))
        val fontSize = json.optDouble("LabelFontSize", json.optDouble("fontSize", 14.0)).toFloat()
        val state = json.optBoolean("State", json.optBoolean("state", false))
        val action = json.optString("Action", json.optString("actionType", "PRESS"))

        return DeckButton(
            id = "${posX}_${posY}",
            positionX = posX,
            positionY = posY,
            label = label,
            iconBase64 = if (icon.isNullOrBlank() || icon == "null") null else icon,
            backgroundColorHex = if (bg.isNullOrBlank() || bg == "null") null else bg,
            labelColorHex = if (labelColor.isNullOrBlank() || labelColor == "null") null else labelColor,
            fontSize = fontSize,
            state = state,
            actionType = action
        )
    }

    private fun startLatencyMonitor(ws: WebSocket) {
        pingJob?.cancel()
        pingJob = clientScope.launch {
            while (true) {
                delay(5000)
                if (activeWebSocket != null) {
                    val ping = JSONObject().apply {
                        put("Method", "PING")
                        put("PingTimestamp", System.currentTimeMillis())
                    }
                    ws.send(ping.toString())
                }
            }
        }
    }

    private fun scheduleReconnect() {
        if (!autoReconnect) return
        pingJob?.cancel()
        reconnectJob?.cancel()

        reconnectJob = clientScope.launch {
            retryCount++
            // Backoff exponencial: 2s, 4s, 8s, max 15s
            val delayMs = (2000L * (1 shl (retryCount - 1).coerceAtMost(3))).coerceAtMost(15000L)
            emitLog(LogEntry.Direction.SYSTEM, "Reintentando conexión (#$retryCount) en ${delayMs / 1000}s...")
            delay(delayMs)
            connect(currentHost, currentPort, currentClientName, enableAutoReconnect = true)
        }
    }

    private fun emitLog(direction: LogEntry.Direction, message: String) {
        val entry = LogEntry(direction = direction, message = message)
        _logs.tryEmit(entry)
    }
}
