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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recetify.data.remote.model.RecipeResponse

@Composable
fun HomeScreen(homeVm: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val recipes   by homeVm.recipes.collectAsState()
    val isLoading by homeVm.isLoading.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        when {
            isLoading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            recipes.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay recetas aprobadas aún",
                        color = Color.Black
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recipes) { r ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row {
                                AsyncImage(
                                    model = r.fotoPrincipal,
                                    contentDescription = r.nombre,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .fillMaxHeight()
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(vertical = 8.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(r.nombre, style = MaterialTheme.typography.titleMedium)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Timer,
                                            contentDescription = null
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "${r.tiempo} min",
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                        Text(
                                            text = "Estado: ${r.estado}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
