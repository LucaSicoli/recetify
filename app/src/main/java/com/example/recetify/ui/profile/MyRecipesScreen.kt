// 3) MyRecipesScreen.kt
package com.example.recetify.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
import java.net.URI
import androidx.core.net.toUri

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyRecipesScreen(
    myRecipesVm: MyRecipesViewModel = viewModel(),
    onRecipeClick: (Long) -> Unit = {}
) {
    val recipes   by myRecipesVm.recipes.collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    // Cálculo de fade para el logo (no afecta al header)
    val fadePx = with(LocalDensity.current) { 80.dp.toPx() }
    val logoAlpha by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else (1f - listState.firstVisibleItemScrollOffset / fadePx).coerceIn(0f,1f)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Espacio superior (logo opcional)
        item { Spacer(Modifier.height(24.dp)) }

        // Sticky header “MIS PUBLICADAS”
        stickyHeader {
            val stuck by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
            Box(
                Modifier
                    .fillMaxWidth()
                    .offset(y = if (!stuck) (-24).dp else 0.dp)
                    .padding(horizontal = if (stuck) 0.dp else 24.dp)
                    // sin background blanco que tape el header
                    .zIndex(10f)
            ) {
                PublicHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (stuck) 100.dp else 90.dp),
                    title = "MIS PUBLICADAS",
                    shape = if (stuck) RoundedCornerShape(0.dp) else RoundedCornerShape(8.dp),
                )
            }
        }

        // Tus recetas publicadas
        items(recipes, key = { it.id }) { recipe ->
            Box(Modifier.padding(horizontal = 24.dp)) {
                PublishedRecipeCard(recipe) {
                    onRecipeClick(recipe.id)
                }
            }
        }
    }
}

@Composable
private fun PublishedRecipeCard(
    recipe: RecipeSummaryResponse,
    onClick: () -> Unit
) {
    val base     = RetrofitClient.BASE_URL.trimEnd('/')
    val original = recipe.mediaUrls?.firstOrNull().orEmpty()
    val pathOnly = runCatching {
        val uri = URI(original)
        uri.rawPath + (uri.rawQuery?.let { "?$it" } ?: "")
    }.getOrNull() ?: original
    val finalUrl = if (pathOnly.startsWith("/")) "$base$pathOnly" else "$base/$pathOnly"
    val isVideo  = finalUrl.endsWith(".mp4", true) || finalUrl.endsWith(".webm", true)

    Box(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Card(
            Modifier.fillMaxWidth(),
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
                        contentDescription = recipe.nombre,
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
                        text     = recipe.nombre,
                        style    = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = recipe.descripcion.orEmpty(),
                        style    = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Badge "PUBLICADA" pegado al borde superior izquierdo
        Box(
            Modifier
                .align(Alignment.TopStart)
                .background(
                    color = Color(0xFF5EAE5A),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "PUBLICADA",
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun PublicHeader(
    modifier: Modifier = Modifier,
    title: String,
    shape: Shape
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
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF8B6A00), // goldenrod oscuro
                            Color(0xFFFFD700)  // gold
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
                    imageVector        = Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text       = title,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    style      = MaterialTheme.typography.titleMedium.copy(
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 20.sp,
                        fontFamily = Destacado
                    )
                )
            }
        }
    }
}