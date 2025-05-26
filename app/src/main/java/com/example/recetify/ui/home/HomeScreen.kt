// HomeScreen.kt
package com.example.recetify.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import java.net.URI

@Composable
fun HomeScreen(
    homeVm: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val recipes   by homeVm.recipes.collectAsState()
    val isLoading by homeVm.isLoading.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (recipes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay recetas aprobadas aún")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes, key = { it.id }) { RecipeCard(it) }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeResponse, modifier: Modifier = Modifier) {
    val base     = RetrofitClient.BASE_URL.trimEnd('/')
    val original = recipe.fotoPrincipal.orEmpty()
    val pathOnly = runCatching {
        val uri = URI(original)
        uri.rawPath + uri.rawQuery?.let { "?$it" }.orEmpty()
    }.getOrNull() ?: original
    val finalUrl = if (pathOnly.startsWith("/")) "$base$pathOnly" else "$base/$pathOnly"

    Card(
        modifier  = modifier.fillMaxWidth().height(100.dp),
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row {
            AsyncImage(
                model          = finalUrl,
                contentDescription = recipe.nombre,
                modifier       = Modifier.width(100.dp).fillMaxHeight(),
                contentScale   = ContentScale.Crop
            )
            Spacer(Modifier.width(8.dp))
            Column(
                Modifier
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(recipe.nombre, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("${recipe.tiempo} min", Modifier.weight(1f))
                    Text("Estado: ${recipe.estado}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
