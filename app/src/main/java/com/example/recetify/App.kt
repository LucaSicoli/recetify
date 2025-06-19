package com.example.recetify

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 50 MB para cache de HTTP (coils + OkHttp)
        val cacheDir = File(cacheDir, "http_cache")
        val cache    = Cache(cacheDir, 50L * 1024 * 1024)

        val okHttp = OkHttpClient.Builder()
            .cache(cache)
            .build()

        val loader = ImageLoader.Builder(this)
            .okHttpClient { okHttp }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .build()

        Coil.setImageLoader(loader)
    }
}