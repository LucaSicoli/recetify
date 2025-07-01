package com.example.recetify.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.UserSavedRecipeDTO
import com.example.recetify.ui.common.LoopingVideoPlayer
import com.example.recetify.ui.home.Destacado
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedRecipesScreen(
    favVm: FavouriteViewModel = viewModel(),
    onRecipeClick: (Long) -> Unit = {}
) {
    val saved by favVm.favourites.collectAsState()
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(24.dp)) }

        // Sticky header con degradado fucsia y corazón
        stickyHeader {
            val stuck by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
            Box(
                Modifier
                    .fillMaxWidth()
                    .offset(y = if (!stuck) (-24).dp else 0.dp)
                    .padding(horizontal = if (stuck) 0.dp else 24.dp)
                    .zIndex(10f)
            ) {
                SavedHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (stuck) 100.dp else 90.dp),
                    title = "MIS FAVORITAS",
                    shape = if (stuck) RoundedCornerShape(0.dp) else RoundedCornerShape(8.dp)
                )
            }
        }

        // Tarjetas de guardadas
        items(saved, key = { it.id }) { item ->
            Box(Modifier.padding(horizontal = 24.dp)) {
                SavedRecipeCard(
                    item = item,
                    onClick = { onRecipeClick(item.recipeId) },
                    onUnsave = { favVm.removeFavorite(item.recipeId) }
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SavedRecipeCard(
    item: UserSavedRecipeDTO,
    onClick: () -> Unit,
    onUnsave: () -> Unit
) {
    val base     = RetrofitClient.BASE_URL.trimEnd('/')
    val original = item.mediaUrls.firstOrNull().orEmpty()
    val pathOnly = runCatching {
        val uri = URI(original)
        uri.rawPath + (uri.rawQuery?.let { "?$it" } ?: "")
    }.getOrNull() ?: original
    val finalUrl = if (pathOnly.startsWith("/")) "$base$pathOnly" else pathOnly
    val isVideo  = finalUrl.endsWith(".mp4", true) || finalUrl.endsWith(".webm", true)

    // Formateo de la fecha de guardado
    val formatter = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("es"))
    }
    val niceDate = remember(item.fechaAgregado) {
        runCatching {
            LocalDateTime.parse(item.fechaAgregado)
                .format(formatter)
        }.getOrNull() ?: item.fechaAgregado
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // Tarjeta principal
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
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
                        contentDescription = item.recipeNombre,
                        modifier        = mediaModifier,
                        contentScale    = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(8.dp))

                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text     = item.recipeNombre,
                        style    = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "Guardada: $niceDate",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            }
        }

        // Badge "GUARDADA"
        Box(
            Modifier
                .align(Alignment.TopStart)
                .background(
                    color = Color(0xFFCC3366),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "GUARDADA",
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Botón de “dislike” con fondo oscuro y área mayor
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(40.dp)                                      // área clickable más grande
                .background(Color(0x88000000), shape = CircleShape) // fondo semitransparente
                .clickable(onClick = onUnsave),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Filled.Favorite,
                contentDescription = "Quitar favorito",
                tint               = Color(0xFFE91E63),
                modifier           = Modifier.size(24.dp)        // tamaño del icono
            )
        }
    }
}

@Composable
fun SavedHeader(
    modifier: Modifier = Modifier,
    title: String,
    shape: Shape
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFCC3366), // fucsia más suave
                            Color(0xFF993355)  // fuc// fucsia claro
                        )
                    )
                )
        ) {
            Row(
                Modifier
                    .align(Alignment.Center)
                    .wrapContentWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color    = Color.White,
                        fontFamily = Destacado,
                        fontSize   = 20.sp
                    )
                )
            }
        }
    }
}