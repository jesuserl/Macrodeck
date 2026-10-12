package com.example.ui

import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeckButton
import com.example.ui.theme.DeckBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.util.rememberBase64Bitmap

/**
 * Componente interactivo para cada celda de botón de Macro Deck 3.
 * Implementa detección de toque inmediata (Touch Down / Up) mediante [detectTapGestures],
 * escalado de pulsación táctil con animación spring, carga asíncrona de mapas de bits Base64
 * y bordes dinámicos de neón según el estado activo.
 */
@Composable
fun DeckButtonView(
    button: DeckButton?,
    positionX: Int,
    positionY: Int,
    isGloballyPressed: Boolean,
    onDown: (x: Int, y: Int) -> Unit,
    onUp: (x: Int, y: Int) -> Unit,
    onLongPress: (x: Int, y: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLocallyPressed by remember { mutableStateOf(false) }
    val isPressed = isLocallyPressed || isGloballyPressed

    // Animación de rebote físico táctil al presionar
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "btn_press_scale"
    )

    // Decodificar icono Base64 asíncronamente en segundo plano
    val iconBitmap = rememberBase64Bitmap(button?.iconBase64)

    // Determinar color de fondo
    val baseBgColor = remember(button?.backgroundColorHex, button?.state) {
        button?.backgroundColorHex?.let { parseHexColor(it) } ?: Color(0xFF1B1E2C)
    }

    // Color del borde dinámico
    val borderColor = when {
        isPressed -> NeonCyan
        button?.state == true -> NeonMagenta
        else -> DeckBorder
    }

    val borderWidth = if (isPressed || button?.state == true) 2.dp else 1.dp

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(borderWidth, borderColor),
        colors = CardDefaults.cardColors(containerColor = baseBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 1.dp else 4.dp),
        modifier = modifier
            .fillMaxSize()
            .scale(scale)
            .testTag("deck_button_${positionX}_${positionY}")
            .pointerInput(positionX, positionY) {
                detectTapGestures(
                    onPress = {
                        isLocallyPressed = true
                        onDown(positionX, positionY)
                        tryAwaitRelease()
                        isLocallyPressed = false
                        onUp(positionX, positionY)
                    },
                    onLongPress = {
                        onLongPress(positionX, positionY)
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isPressed) 0.12f else 0.04f),
                            Color.Black.copy(alpha = 0.25f)
                        )
                    )
                )
                .padding(6.dp)
        ) {
            // Indicador tenue de coordenadas (X, Y) en la esquina superior izquierda
            Text(
                text = "${positionX},${positionY}",
                color = Color.White.copy(alpha = 0.28f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Contenido central: Icono e etiqueta
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp, bottom = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (iconBitmap != null) {
                    Image(
                        bitmap = iconBitmap.asImageBitmap(),
                        contentDescription = button?.label ?: "Botón Macro Deck",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                } else if (!button?.label.isNullOrBlank()) {
                    // Si no tiene icono cargado pero sí etiqueta, mostramos un glifo sutil si el espacio lo permite
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = if (button?.state == true) 0.9f else 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Etiqueta del botón
                val labelText = button?.label ?: ""
                if (labelText.isNotBlank()) {
                    val labelColor = remember(button?.labelColorHex) {
                        button?.labelColorHex?.let { parseHexColor(it) } ?: Color.White
                    }

                    Text(
                        text = labelText,
                        color = labelColor,
                        fontSize = (button?.fontSize ?: 12f).coerceIn(10f, 16f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            // Indicador de estado activado (pequeño punto de luz LED en la esquina superior derecha)
            if (button?.state == true) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonMagenta)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}

/**
 * Parsea con seguridad colores hexadecimales en formatos #RGB, #RRGGBB o #AARRGGBB.
 */
private fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.trim()
        val parsed = AndroidColor.parseColor(if (clean.startsWith("#")) clean else "#$clean")
        Color(parsed)
    } catch (_: Exception) {
        Color(0xFF1B1E2C)
    }
}
