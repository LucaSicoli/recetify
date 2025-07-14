package com.example.recetify.util

import com.example.recetify.data.remote.RetrofitClient
import java.net.URI

object ImageUrlUtils {

    /**
     * Normaliza una URL de imagen del servidor para asegurar que sea válida
     * @param rawUrl URL cruda que puede venir del servidor
     * @return URL normalizada lista para usar con Coil
     */
    fun normalizeImageUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null

        return try {
            val base = RetrofitClient.BASE_URL.trimEnd('/')

            // Si ya es una URL completa, la devolvemos tal como está
            if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                rawUrl
            } else {
                // Si es una ruta relativa, la completamos con el base URL
                val cleanPath = if (rawUrl.startsWith("/")) rawUrl else "/$rawUrl"
                "$base$cleanPath"
            }
        } catch (e: Exception) {
            // En caso de error, intentamos construir una URL simple
            val base = RetrofitClient.BASE_URL.trimEnd('/')
            if (rawUrl.startsWith("/")) "$base$rawUrl" else "$base/$rawUrl"
        }
    }

    /**
     * Normaliza una URL de perfil de usuario
     */
    fun normalizeProfileUrl(rawUrl: String?): String? = normalizeImageUrl(rawUrl)

    /**
     * Normaliza URLs de media (imágenes y videos de recetas)
     */
    fun normalizeMediaUrl(rawUrl: String?): String? = normalizeImageUrl(rawUrl)

    /**
     * Obtiene la primera URL de media de una lista
     */
    fun getFirstMediaUrl(mediaUrls: List<String>?): String? {
        return mediaUrls?.firstOrNull()?.let { normalizeMediaUrl(it) }
    }
}
