package com.example.recetify

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class App : Application(), ImageLoaderFactory {

    companion object {
        /** Cache global de ExoPlayer para vídeos */
        lateinit var exoCache: SimpleCache
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // ─── ExoPlayer SimpleCache (vídeos) ───────────────────────────────────
        val exoCacheDir = File(cacheDir, "exo_cache")
        val evictor     = LeastRecentlyUsedCacheEvictor(200L * 1024 * 1024) // 200 MB para vídeos
        val dbProvider  = StandaloneDatabaseProvider(this)
        exoCache = SimpleCache(exoCacheDir, evictor, dbProvider)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .addNetworkInterceptor { chain ->
                        val response = chain.proceed(chain.request())
                        response.newBuilder()
                            .header("Cache-Control", "public, max-age=604800")
                            .build()
                    }
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .allowHardware(true)
            .allowRgb565(true)
            .crossfade(true)
            .logger(DebugLogger())
            .build()
    }
}