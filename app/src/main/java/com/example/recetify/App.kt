package com.example.recetify

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File

class App : Application() {

    companion object {
        /** Cache global de ExoPlayer para vídeos */
        lateinit var exoCache: SimpleCache
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // ─── Coil / OkHttp HTTP cache (imágenes y peticiones) ─────────────────
        // in your Application.onCreate()
        val httpCacheDir = File(cacheDir, "http_cache")
        val httpCache    = Cache(httpCacheDir, 50L * 1024 * 1024) // 50 MB

        val okHttp = OkHttpClient.Builder()
            .cache(httpCache)
            // force‐cache every image response for up to 7 days
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=604800")
                    .build()
            }
            .build()

        val loader = ImageLoader.Builder(this)
            .okHttpClient { okHttp }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)  // ignorar Cache-Control del servidor
            .build()

        Coil.setImageLoader(loader)
        // ─── ExoPlayer SimpleCache (vídeos) ───────────────────────────────────
        val exoCacheDir = File(cacheDir, "exo_cache")
        val evictor     = LeastRecentlyUsedCacheEvictor(200L * 1024 * 1024) // 200 MB para vídeos
        val dbProvider  = StandaloneDatabaseProvider(this)
        exoCache = SimpleCache(exoCacheDir, evictor, dbProvider)
    }
}