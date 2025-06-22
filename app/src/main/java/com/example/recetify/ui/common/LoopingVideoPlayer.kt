// app/src/main/java/com/example/recetify/ui/common/LoopingVideoPlayer.kt
package com.example.recetify.ui.common

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.recetify.App
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink

@Composable
fun LoopingVideoPlayer(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current

    // 1) Creamos las fábricas de data source
    val httpFactory = DefaultHttpDataSource.Factory()
        .setUserAgent(com.google.android.exoplayer2.util.Util.getUserAgent(ctx, "Recetify"))
    val upstreamFactory = DefaultDataSource.Factory(ctx, httpFactory)
    val cacheSinkFactory = CacheDataSink.Factory()
        .setCache(App.exoCache)
        .setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE)
    val cacheFactory = CacheDataSource.Factory()
        .setCache(App.exoCache)
        .setUpstreamDataSourceFactory(upstreamFactory)
        .setCacheWriteDataSinkFactory(cacheSinkFactory)
        .setFlags(
            CacheDataSource.FLAG_BLOCK_ON_CACHE or
                    CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
        )

    // 2) Recordamos el player para no recrearlo en cada recomposición
    val exoPlayer = remember(uri) {
        SimpleExoPlayer.Builder(ctx).build().apply {
            repeatMode    = Player.REPEAT_MODE_ONE
            volume        = 0f
            // Preparamos la fuente usando la fábrica cacheada
            val source = ProgressiveMediaSource.Factory(cacheFactory)
                .createMediaSource(MediaItem.fromUri(uri))
            setMediaSource(source)
            prepare()
            playWhenReady = true
        }
    }

    // 3) Liberamos cuando el Composable desaparece
    DisposableEffect(uri) {
        onDispose { exoPlayer.release() }
    }

    // 4) La vista
    AndroidView(
        factory = { innerCtx ->
            PlayerView(innerCtx).apply {
                useController = false
                resizeMode    = AspectRatioFrameLayout.RESIZE_MODE_FILL
                // inicialmente lo conectamos
                player = exoPlayer
            }
        },
        update = { view ->
            // en cada recomposición (uri cambió), lo volvemos a conectar
            view.player = exoPlayer
        },
        modifier = modifier
    )
}