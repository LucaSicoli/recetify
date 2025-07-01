package com.example.recetify.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recetify.data.remote.ApiService
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(navController: NavController) {
    // Inicializamos API, repo y VM
    val api  = RetrofitClient.api as ApiService
    val repo = remember { SearchRepository(api) }
    val vm: SearchViewModel = viewModel(
        factory = SearchViewModel.provideFactory(repo, api)
    )
    val scope = rememberCoroutineScope()

    // Collectamos los estados
    val results     by vm.results.collectAsState()
    val currentType by vm.type.collectAsState()
    val sortOrder   by vm.sortOrder.collectAsState()
    val savedIds    by vm.savedIds.collectAsState()

    var query       by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }

    // Búsqueda inicial
    LaunchedEffect(Unit) { vm.doSearch() }

    LazyColumn(Modifier.fillMaxSize()) {
        // — Buscador —
        item {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value         = query,
                onValueChange = {
                    query = it
                    vm.updateName(it.ifBlank { null })
                },
                placeholder   = { Text("Buscar receta…") },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = null)
                    }
                },
                singleLine = true,
                shape      = RoundedCornerShape(24.dp),
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        // — Chips de categoría (enum Categoria) —
        item {
            val categories = listOf("Desayuno","Almuerzo","Merienda","Cena","Snack","Postre")
            LazyRow(
                contentPadding       = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected   = currentType == cat,
                        onClick    = {
                            vm.updateType(cat)
                            vm.doSearch()
                        },
                        label      = { Text(cat) },
                        shape      = RoundedCornerShape(16.dp),
                        colors     = FilterChipDefaults.filterChipColors(containerColor = Color(0xFFF5F5F5)),
                        modifier   = Modifier.height(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // — Sticky header “Destacados” —
        stickyHeader {
            Surface(color = Color.White, tonalElevation = 4.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Destacados", style = MaterialTheme.typography.titleLarge)
                    val arrow = if (sortOrder == "name") Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp
                    OutlinedButton(
                        onClick = {
                            vm.updateSort(if (sortOrder=="name") "newest" else "name")
                            vm.doSearch()
                        },
                        shape         = RoundedCornerShape(16.dp),
                        border        = BorderStroke(1.dp, Color(0xFFE6EBF2)),
                        colors        = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFE6EBF2),
                            contentColor   = Color(0xFF042628)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Nombre")
                        Icon(arrow, contentDescription = null, Modifier.size(20.dp))
                    }
                }
            }
        }

        // — Resultados —
        items(results) { summary ->
            SearchResultCard(
                summary       = summary,
                navController = navController,
                isSaved       = summary.id in savedIds,
                onToggleSave  = { vm.toggleSave(summary.id) }
            )
            Spacer(Modifier.height(12.dp))
        }

        item { Spacer(Modifier.height(80.dp)) }
    }

    // — Diálogo de filtros (igual que antes) —
    if (showFilters) {
        SearchFiltersDialog(
            tipoOptions       = listOf("Hamburguesas","Pizzas","Arroz","Fideos","Carnes y Pescados"),
            selectedTipo      = currentType,
            currentIngredient = vm.ingredient.collectAsState().value,
            currentUser       = vm.userAlias.collectAsState().value,
            currentRating     = vm.rating.collectAsState().value,
            onTipoSelect      = { vm.updateType(it) },
            onIngredientChange= { vm.updateIngredient(it) },
            onUserChange      = { vm.updateUserAlias(it) },
            onRatingSelect    = { vm.updateRating(it) },
            onApply           = {
                vm.doSearch()
                showFilters = false
            },
            onDismiss         = { showFilters = false }
        )
    }
}

@Composable
private fun SearchResultCard(
    summary: RecipeSummaryResponse,
    navController: NavController,
    isSaved: Boolean,
    onToggleSave: () -> Unit
) {
    Card(
        modifier   = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                val photoParam = summary.usuarioFotoPerfil
                    ?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.name()) }
                    .orEmpty()
                navController.navigate("recipe/${summary.id}?photo=$photoParam")
            },
        shape      = RoundedCornerShape(16.dp),
        elevation  = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors     = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen + badge de rating
            Box {
                AsyncImage(
                    model              = summary.mediaUrls?.firstOrNull().orEmpty(),
                    contentDescription = summary.nombre,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .background(
                            color = Color(0xFFCB6E6C),
                            shape = RoundedCornerShape(topEnd = 4.dp, bottomStart = 12.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        String.format("%.1f★", summary.promedioRating ?: 0.0),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    summary.nombre,
                    style    = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model              = summary.usuarioFotoPerfil,
                        contentDescription = summary.usuarioCreadorAlias,
                        modifier           = Modifier
                            .size(20.dp)
                            .clip(CircleShape),
                        contentScale       = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        summary.usuarioCreadorAlias.orEmpty(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${summary.tiempo} min",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            IconButton(onClick = onToggleSave) {
                Icon(
                    imageVector       = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription= "Guardar",
                    tint              = if (isSaved) Color(0xFFE63946) else Color.Gray
                )
            }
        }
    }
}