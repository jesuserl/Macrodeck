package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ServerProfile
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen

/**
 * Diálogo de configuración simplificado para el Cliente Web de Macro Deck 3.
 * Configuración de IP, puerto, URL personalizada, auto-ocultamiento y perfiles.
 */
@Composable
fun SettingsModal(
    serverHost: String,
    serverPort: Int,
    webCustomUrl: String,
    keepScreenOn: Boolean,
    autoHideTopBar: Boolean,
    savedProfiles: List<ServerProfile>,
    onSaveHost: (String) -> Unit,
    onSavePort: (Int) -> Unit,
    onSaveWebCustomUrl: (String) -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onToggleAutoHideTopBar: (Boolean) -> Unit,
    onReload: () -> Unit,
    onSelectProfile: (ServerProfile) -> Unit,
    onSaveCurrentProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val safeDismiss = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onDismiss()
    }

    BackHandler(onBack = safeDismiss)

    var hostInput by remember { mutableStateOf(serverHost) }
    var portInput by remember { mutableStateOf(serverPort.toString()) }
    var webUrlInput by remember { mutableStateOf(webCustomUrl) }
    var newProfileNameInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { safeDismiss() }
            .imePadding()
            .testTag("settings_modal"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF141622),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .border(1.dp, Color(0xFF2A2E44), RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { }
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CONFIGURACIÓN CLIENTE WEB MACRO DECK",
                            color = NeonCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Parámetros de conexión HTTP del PC y visualización en pantalla completa",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = safeDismiss,
                        modifier = Modifier.testTag("close_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Contenido principal en 2 columnas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Columna 1: Dirección IP, Puerto y URL
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF191B2B), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Dirección de tu Servidor Macro Deck",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = hostInput,
                                onValueChange = {
                                    hostInput = it
                                    onSaveHost(it)
                                },
                                label = { Text("IP de tu PC") },
                                placeholder = { Text("192.168.1.100") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color(0xFF333850),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(0.7f)
                                    .testTag("settings_ip_input")
                            )

                            OutlinedTextField(
                                value = portInput,
                                onValueChange = {
                                    portInput = it
                                    it.toIntOrNull()?.let { p -> onSavePort(p) }
                                },
                                label = { Text("Puerto") },
                                placeholder = { Text("8191") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color(0xFF333850),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(0.3f)
                                    .testTag("settings_port_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = webUrlInput,
                            onValueChange = {
                                webUrlInput = it
                                onSaveWebCustomUrl(it)
                            },
                            label = { Text("URL Personalizada (Opcional)") },
                            placeholder = { Text("ej. http://192.168.1.100:8191 o https://tu-tunel.ngrok.io") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = Color(0xFF333850),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tarjeta explicativa sobre el emulador web y redes locales
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF141724), RoundedCornerShape(8.dp))
                                .border(1.dp, NeonAmber.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recordatorio de Red: Las IPs locales (192.168.x.x) de tu hogar no son alcanzables directamente desde los servidores de Google AI Studio. Para usar la app con tu PC: descarga el APK en tu tablet o teléfono en tu misma red Wi-Fi, o usa un túnel (ngrok / Cloudflare).",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onReload()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF00222B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GUARDAR Y RECARGAR CLIENTE WEB", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Columna 2: Kiosk, Auto-ocultado y Perfiles
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF191B2B), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Visualización y Perfiles",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Switch Auto-hide Top Bar
                        SettingSwitchRow(
                            title = "Auto-ocultar Barra Superior",
                            subtitle = "Se oculta tras 3.5 segundos para pantalla completa total",
                            checked = autoHideTopBar,
                            onCheckedChange = onToggleAutoHideTopBar
                        )

                        // Switch Keep Screen On
                        SettingSwitchRow(
                            title = "Mantener Pantalla Encendida (Wake Lock)",
                            subtitle = "Evita que la pantalla se apague mientras usas el Deck",
                            checked = keepScreenOn,
                            onCheckedChange = onToggleKeepScreenOn
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Guardar nuevo perfil
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newProfileNameInput,
                                onValueChange = { newProfileNameInput = it },
                                placeholder = { Text("Nombre perfil (ej. Mi PC)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen,
                                    unfocusedBorderColor = Color(0xFF333850),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    onSaveCurrentProfile(newProfileNameInput)
                                    newProfileNameInput = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                                modifier = Modifier.testTag("save_profile_btn")
                            ) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Perfiles Guardados:",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(savedProfiles) { profile ->
                                ProfileItemCard(
                                    profile = profile,
                                    isActive = profile.host == hostInput && profile.port.toString() == portInput,
                                    onSelect = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        onSelectProfile(profile)
                                        hostInput = profile.host
                                        portInput = profile.port.toString()
                                        onDismiss()
                                    },
                                    onDelete = { onDeleteProfile(profile.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                uncheckedTrackColor = Color(0xFF282C40)
            )
        )
    }
}

@Composable
private fun ProfileItemCard(
    profile: ServerProfile,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1E283A) else Color(0xFF1F2236)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name, color = if (isActive) NeonCyan else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("http://${profile.host}:${profile.port}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
