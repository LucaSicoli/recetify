// app/src/main/java/com/example/recetify/util/FileUtil.kt
package com.example.recetify.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

object FileUtil {
    @Throws(Exception::class)
    fun from(context: Context, uri: Uri): File {
        // 1) Abrimos el stream
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI: $uri")

        // 2) Deducimos el MIME type y la extensión
        val mime = context.contentResolver.getType(uri)
            ?: "application/octet-stream"
        val ext = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mime)
            ?: "jpg"   // fallback

        // 3) Creamos el temp file con la extensión correcta
        val file = File.createTempFile("upload_", ".$ext", context.cacheDir)

        // 4) Copiamos los bytes
        FileOutputStream(file).use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }
        return file
    }
}