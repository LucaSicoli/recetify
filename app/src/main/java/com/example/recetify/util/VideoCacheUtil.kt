package com.example.recetify.util

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

object VideoCacheUtil {
    /**
     * Descarga el vídeo desde `url` y lo guarda en cacheDir.
     * Si ya existe, devuelve directamente ese fichero.
     * Retorna un Uri file:// para reproducir offline.
     */
    suspend fun downloadToCache(url: String, context: Context): Uri = withContext(Dispatchers.IO) {
        // Usa el último segmento de la URL como nombre de fichero
        val name = Uri.parse(url).lastPathSegment ?: "video.mp4"
        val file = File(context.cacheDir, name)

        if (file.exists()) {
            return@withContext file.toUri()
        }

        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("Error al descargar vídeo: $resp")
            resp.body?.byteStream()?.use { input ->
                file.outputStream().buffered().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return@withContext file.toUri()
    }
}