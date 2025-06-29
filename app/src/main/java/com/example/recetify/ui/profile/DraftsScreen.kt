package com.example.recetify.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import com.example.recetify.ui.common.LoopingVideoPlayer
import com.example.recetify.ui.home.Destacado
import com.example.recetify.ui.home.Sen
import java.net.URI
import androidx.core.net.toUri

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraftsScreen(
    draftVm: DraftViewModel = viewModel(),
    onDraftClick: (Long) -> Unit = {}
) {
    val drafts by draftVm.drafts.collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Espacio superior
        item { Spacer(Modifier.height(24.dp)) }

        // Sticky header
        stickyHeader {
            val stuck by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
            Box(
                Modifier
                    .fillMaxWidth()
                    .offset(y = if (!stuck) (-24).dp else 0.dp)
                    .padding(horizontal = if (stuck) 0.dp else 24.dp)
                    .background(Color.White)
                    .zIndex(10f)
            ) {
                DraftsHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (stuck) 100.dp else 90.dp),
                    title = "MIS BORRADORES",
                    shape = if (stuck) RoundedCornerShape(0.dp) else RoundedCornerShape(8.dp)
                )
            }
        }

        // Cards de borrador
        items(drafts, key = { it.id }) { draft ->
            Box(Modifier.padding(horizontal = 24.dp)) {
                DraftRecipeCard(draft = draft, onClick = onDraftClick)
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DraftRecipeCard(
    draft: RecipeSummaryResponse,
    onClick: (Long) -> Unit
) {
    // 1) Normalizamos la URL igual que en HomeScreen
    val base     = RetrofitClient.BASE_URL.trimEnd('/')
    val original = draft.mediaUrls?.firstOrNull().orEmpty()
    val pathOnly = runCatching {
        val uri = URI(original)
        uri.rawPath + (uri.rawQuery?.let { "?$it" } ?: "")
    }.getOrNull() ?: original
    val finalUrl = if (pathOnly.startsWith("/")) "$base$pathOnly" else "$base/$pathOnly"
    val isVideo  = finalUrl.endsWith(".mp4", true) || finalUrl.endsWith(".webm", true)

    Box(
        Modifier
            .fillMaxWidth()
            .clickable { onClick(draft.id) }
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                if (isVideo) {
                    LoopingVideoPlayer(
                        uri = finalUrl.toUri(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    )
                } else {
                    AsyncImage(
                        model              = finalUrl,
                        contentDescription = draft.nombre,
                        modifier           = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        contentScale       = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(12.dp))

                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text      = draft.nombre,
                        style     = MaterialTheme.typography.titleLarge,
                        maxLines  = 1,
                        overflow  = TextOverflow.Ellipsis,
                        color     = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text      = draft.descripcion.orEmpty(),
                        style     = MaterialTheme.typography.bodyMedium,
                        maxLines  = 2,
                        overflow  = TextOverflow.Ellipsis,
                        color     = Color.DarkGray
                    )
                }
            }
        }

        // === Etiqueta BORRADOR pegada al borde superior izquierdo ===
        Box(
            Modifier
                .align(Alignment.TopStart)
                .background(
                    color = Color(0xFFE95E5A),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "BORRADOR",
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun DraftsHeader(
    modifier: Modifier = Modifier,
    title: String = "BORRADORES",
    subtitle: String = "",
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Card(
        modifier  = modifier,
        shape     = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF5A6F8A))
        ) {
            Box(Modifier.align(Alignment.Center)) {
                Row(
                    modifier              = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Star,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text       = title,
                        maxLines   = 1,
                        overflow   = TextOverflow.Clip,
                        style      = MaterialTheme.typography.titleMedium.copy(
                            color        = Color.White,
                            fontFamily   = Destacado,
                            fontWeight   = FontWeight.Bold,
                            fontSize     = 20.sp,
                            platformStyle= PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                    if (subtitle.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = subtitle,
                            maxLines   = 1,
                            overflow   = TextOverflow.Clip,
                            style      = MaterialTheme.typography.bodyMedium.copy(
                                color        = Color.White.copy(alpha = 0.9f),
                                fontFamily   = Sen,
                                fontSize     = 20.sp,
                                platformStyle= PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }
            }
        }
    }
}