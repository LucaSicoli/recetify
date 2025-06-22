package com.example.recetify.ui.common

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView

@Composable
fun LoopingVideoPlayer(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current

    // recordamos el player para no reconstruirlo en cada recomposición
    val exoPlayer = remember(uri) {
        SimpleExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode    = Player.REPEAT_MODE_ONE
            playWhenReady = true
            volume        = 0f                  // silencio
            prepare()
        }
    }

    DisposableEffect(uri) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { innerCtx ->
            PlayerView(innerCtx).apply {
                player        = exoPlayer
                useController = false               // sin controles
                // escala el vídeo para llenar el view (recorta si hace falta)
                resizeMode    = AspectRatioFrameLayout.RESIZE_MODE_FILL
            }
        },
        modifier = modifier
    )
}