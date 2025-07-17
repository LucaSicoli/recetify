package com.example.recetify.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraftsScreen(
    draftVm: DraftViewModel = viewModel(),
    onDraftClick: (Long) -> Unit = {}
) {
    // Refresca la lista cada vez que se entra a la pantalla
    LaunchedEffect(Unit) {
        draftVm.refresh()
    }
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

        // HEADER *sticky* siempre visible
        stickyHeader {
            val stuck by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
            Box(
                Modifier
                    .fillMaxWidth()
                    .offset(y = if (!stuck) (-24).dp else 0.dp)
                    .padding(horizontal = if (stuck) 0.dp else 24.dp)
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

        if (drafts.isEmpty()) {
            item {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 80.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = Color(0xFF5A6F8A),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No hay borradores que mostrar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Cards de borrador
            items(drafts, key = { it.id }) { draft ->
                Box(Modifier.padding(horizontal = 24.dp)) {
                    DraftRecipeCard(
                        draft = draft,
                        onClick = onDraftClick,
                        onDelete = { draftVm.deleteDraft(it) }
                    )
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun DraftRecipeCard(
    draft: RecipeSummaryResponse,
    onClick: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    val context = LocalContext.current
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
            elevation = CardDefaults.cardElevation(4.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // → reemplazamos altura fija por aspectRatio 16:9
                val mediaModifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))

                if (isVideo) {
                    LoopingVideoPlayer(
                        uri      = finalUrl.toUri(),
                        modifier = mediaModifier
                    )
                } else {
                    AsyncImage(
                        model           = finalUrl,
                        contentDescription = draft.nombre,
                        modifier        = mediaModifier,
                        contentScale    = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(8.dp))

                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text     = draft.nombre,
                            style    = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color    = Color.Black,
                            fontFamily = Destacado,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                onDelete(draft.id)
                                Toast.makeText(context, "Borrador eliminado", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar borrador",
                                tint = Color(0xFF5A6F8A) // Azul igual que el header
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = draft.descripcion.orEmpty(),
                        style    = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color    = Color.DarkGray
                    )
                }
            }
        }

        Box(
            Modifier
                .align(Alignment.TopStart)
                .background(
                    color = Color(0xFF5A6F8A),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "BORRADOR",
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Destacado
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
                        imageVector        = Icons.Filled.Book,
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