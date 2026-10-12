package com.example.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gestor de caché y decodificación asíncrona de imágenes Base64.
 * Macro Deck 3 envía iconos en formato Base64 (PNG/JPEG).
 * Para evitar caídas de fotogramas (jank) en el hilo principal de la UI,
 * la decodificación se ejecuta en [Dispatchers.Default] y los mapas de bits
 * resultantes se almacenan en un [LruCache] en memoria.
 */
object ImageCacheManager {

    // Asignamos 1/8 de la memoria máxima disponible de la JVM para el caché de Bitmaps
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val memoryCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // El tamaño se mide en kilobytes
            return bitmap.byteCount / 1024
        }
    }

    /**
     * Obtiene un [Bitmap] a partir de su cadena Base64.
     * Si ya se encuentra en caché, lo retorna de inmediato sin decodificar.
     * En caso contrario, realiza la decodificación en [Dispatchers.Default].
     */
    suspend fun decodeBase64(base64String: String?): Bitmap? {
        if (base64String.isNullOrBlank()) return null

        // 1. Verificar si existe en la caché en memoria (operación rápida O(1))
        memoryCache.get(base64String)?.let { cachedBitmap ->
            return cachedBitmap
        }

        // 2. Offload al hilo de cómputo en segundo plano
        return withContext(Dispatchers.Default) {
            try {
                // Eliminar posibles encabezados como "data:image/png;base64,"
                val cleanBase64 = if (base64String.contains(",")) {
                    base64String.substringAfter(",")
                } else {
                    base64String
                }

                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

                if (bitmap != null) {
                    memoryCache.put(base64String, bitmap)
                }
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Limpia la caché en memoria si se requiere liberar recursos.
     */
    fun clearCache() {
        memoryCache.evictAll()
    }
}

/**
 * Composable helper para recordar y decodificar asíncronamente un Bitmap
 * a partir de un String Base64 sin bloquear la composición ni causar recomposiciones lentas.
 */
@Composable
fun rememberBase64Bitmap(base64String: String?): Bitmap? {
    val bitmapState = remember(base64String) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(base64String) {
        if (base64String != null) {
            val decoded = ImageCacheManager.decodeBase64(base64String)
            bitmapState.value = decoded
        } else {
            bitmapState.value = null
        }
    }

    return bitmapState.value
}
