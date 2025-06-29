package com.example.recetify.ui.profile

import java.net.URI
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import com.example.recetify.ui.common.LoopingVideoPlayer
import androidx.core.net.toUri

@Composable
fun DraftsScreen(
    draftVm: DraftViewModel = viewModel(),
    onDraftClick: (Long) -> Unit = {}
) {
    // Recogemos el StateFlow inicializado a lista vacía para evitar errores
    val drafts = draftVm.drafts.collectAsState(initial = emptyList()).value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        items(
            items = drafts,
            key = { it.id }
        ) { draft ->
            DraftRecipeCard(draft = draft, onClick = onDraftClick)
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
    // Aquí está la clave: SIEMPRE anteponemos base + "/" + pathOnly
    val finalUrl = if (pathOnly.startsWith("/")) {
        "$base$pathOnly"
    } else {
        "$base/$pathOnly"
    }

    // 2) Detectamos si es vídeo
    val isVideo = finalUrl.endsWith(".mp4", ignoreCase = true) ||
            finalUrl.endsWith(".webm", ignoreCase = true)

    Box(
        modifier = Modifier
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
                    // Vídeo en bucle igual que en HomeScreen
                    LoopingVideoPlayer(
                        uri = finalUrl.toUri(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    )
                } else {
                    // Imagen estática
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
                        text     = draft.nombre,
                        style    = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        color    = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = draft.descripcion.orEmpty(),
                        style    = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        color    = Color.DarkGray
                    )
                }
            }
        }

        // Tag "BORRADOR" superpuesto en la esquina
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = 8.dp)
                .background(
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text  = "BORRADOR",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Black)
            )
        }
    }
}