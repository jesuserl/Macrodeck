package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta

/**
 * Barra superior HUD de estado y control para el Cliente Web de Macro Deck 3.
 * Diseñada para flotar sobre el contenido y ocultarse automáticamente,
 * permitiendo apreciar el Deck en pantalla completa al 100%.
 */
@Composable
fun DeckTopBar(
    webUrl: String,
    autoHideEnabled: Boolean,
    onToggleAutoHide: (Boolean) -> Unit,
    onReloadClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onHideBarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(Color(0xFF0F1018).copy(alpha = 0.95f))
            .border(1.dp, Color(0xFF202336), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Lado Izquierdo: Título de Macro Deck y Badge de Cliente Web
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "MACRO DECK 3",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = " // Kiosk Web",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Centro: Píldora de URL Activa
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .background(Color(0xFF161824))
                .clickable { onOpenSettingsClick() }
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .testTag("top_bar_url_pill")
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = webUrl,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Lado Derecho: Acciones (Recargar, Fijar/Auto-ocultar, Ajustes, Ocultar ahora)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Botón Recargar Página
            IconButton(
                onClick = onReloadClick,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("top_bar_reload_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recargar Web Deck",
                    tint = NeonCyan,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Botón Fijar / Auto-ocultar barra
            IconButton(
                onClick = { onToggleAutoHide(!autoHideEnabled) },
                modifier = Modifier
                    .size(34.dp)
                    .testTag("top_bar_pin_btn")
            ) {
                Icon(
                    imageVector = if (autoHideEnabled) Icons.Outlined.PushPin else Icons.Default.PushPin,
                    contentDescription = if (autoHideEnabled) "Auto-ocultamiento activado" else "Barra fijada",
                    tint = if (autoHideEnabled) Color.White.copy(alpha = 0.6f) else NeonMagenta,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Botón Ajustes
            IconButton(
                onClick = onOpenSettingsClick,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("top_bar_settings_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(19.dp)
                )
            }

            // Botón Ocultar barra ahora a pantalla completa
            IconButton(
                onClick = onHideBarClick,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("top_bar_collapse_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Pantalla completa (ocultar barra)",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
