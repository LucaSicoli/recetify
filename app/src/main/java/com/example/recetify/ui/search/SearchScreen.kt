// File: app/src/main/java/com/example/recetify/ui/search/SearchScreen.kt
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import com.example.recetify.ui.home.Destacado
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recetify.data.remote.ApiService
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import com.example.recetify.ui.common.LoopingVideoPlayer
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(navController: NavController) {
    val api  = RetrofitClient.api as ApiService
    val repo = remember { SearchRepository(api) }
    val vm: SearchViewModel = viewModel(factory = SearchViewModel.provideFactory(repo, api))

    val results      by vm.results.collectAsState()
    val currentTipo  by vm.tipoPlato.collectAsState()
    val currentCat   by vm.categoria.collectAsState()
    val sortOrder    by vm.sortOrder.collectAsState()
    val savedIds     by vm.savedIds.collectAsState()

    var query        by remember { mutableStateOf("") }
    var showFilters  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.doSearch() }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // — Search bar —
        item {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value         = query,
                onValueChange = {
                    query = it
                    vm.updateName(it.ifBlank { null })
                    vm.doSearch()          // ← Aquí disparas la nueva búsqueda en caliente
                },
                placeholder   = { Text("Buscar receta…", fontFamily = Destacado) },
                leadingIcon   = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
                },
                trailingIcon  = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros", tint = Color.Black)
                    }
                },
                textStyle     = TextStyle(color = Color.Black),
                singleLine    = true,
                shape         = RoundedCornerShape(24.dp),
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        // — Tipo de Plato Chips —
        item {
            val tipos = listOf(
                "FIDEOS","PIZZA","HAMBURGUESA","ENSALADA","SOPA",
                "PASTA","ARROZ","PESCADO","CARNE","POLLO",
                "VEGETARIANO","VEGANO","SIN_TACC","RAPIDO","SALUDABLE"
            )
            LazyRow(
                contentPadding       = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tipos) { tipo ->
                    FilterChip(
                        selected   = currentTipo == tipo,
                        onClick    = {
                            vm.updateTipoPlato(if (currentTipo == tipo) null else tipo)
                            vm.doSearch()
                        },
                        label      = {
                            Text(
                                tipo,
                                color = if (currentTipo == tipo) Color.White else Color.Black,
                                fontFamily = Destacado
                            )
                        },
                        shape      = RoundedCornerShape(16.dp),
                        colors     = FilterChipDefaults.filterChipColors(
                            containerColor         = if (currentTipo == tipo) Color.Black else Color(0xFFF5F5F5),
                            selectedContainerColor = Color.Black,
                            selectedLabelColor     = Color.White
                        ),
                        modifier   = Modifier.height(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // — Categoría Chips —
        item {
            val categorias = listOf("DESAYUNO","ALMUERZO","MERIENDA","CENA","SNACK","POSTRE")
            LazyRow(
                contentPadding       = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias) { cat ->
                    FilterChip(
                        selected   = currentCat == cat,
                        onClick    = {
                            vm.updateCategoria(if (currentCat == cat) null else cat)
                            vm.doSearch()
                        },
                        label      = {
                            Text(
                                cat,
                                color = if (currentCat == cat) Color.White else Color.Black,
                                fontFamily = Destacado
                            )
                        },
                        shape      = RoundedCornerShape(16.dp),
                        colors     = FilterChipDefaults.filterChipColors(
                            containerColor         = if (currentCat == cat) Color.Black else Color(0xFFF5F5F5),
                            selectedContainerColor = Color.Black,
                            selectedLabelColor     = Color.White
                        ),
                        modifier   = Modifier.height(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // — Sticky header “Buscador” —
        stickyHeader {
            Surface(color = Color.White, tonalElevation = 4.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Buscador", style = MaterialTheme.typography.titleLarge, fontFamily = Destacado)
                    val isAlphaSort = (sortOrder == "name")
                    OutlinedButton(
                        onClick = {
                            vm.updateSort(if (isAlphaSort) "newest" else "name")
                            vm.doSearch()
                        },
                        shape   = RoundedCornerShape(16.dp),
                        border  = BorderStroke(1.dp, if (isAlphaSort) Color.Black else Color.LightGray),
                        colors  = ButtonDefaults.outlinedButtonColors(
                            // cuando está seleccionado:
                            containerColor = if (isAlphaSort) Color.Black else Color.Transparent,
                            contentColor   = if (isAlphaSort) Color.White else Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Ordenar alfabéticamente", fontFamily = Destacado)
                        val arrow = if (isAlphaSort) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown
                        // icono hereda contentColor, así que saldrá blanco o negro según el estado
                        Icon(arrow, contentDescription = null, Modifier.size(20.dp))
                    }
                }
            }
        }

        // — Resultados —
        items(results, key = { it.id }) { summary ->
            val isSaved = summary.id in savedIds
            SearchResultCard(
                summary       = summary,
                navController = navController,
                isSaved       = isSaved,
                onToggleSave  = { vm.toggleSave(summary.id) }
            )
            Spacer(Modifier.height(12.dp))
        }

        item { Spacer(Modifier.height(80.dp)) }
    }

    // — Filtros: sólo Ingrediente, Sin ingrediente, Usuario, Rating —
    if (showFilters) {
        SearchFiltersDialog(
            currentIngredient = vm.ingredient.collectAsState().value,
            currentExclude    = vm.exclude.collectAsState().value,
            currentUser       = vm.userAlias.collectAsState().value,
            currentRating     = vm.rating.collectAsState().value,
            onIngredientChange= { vm.updateIngredient(it) },
            onExcludeChange   = { vm.updateExclude(it) },
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
    val firstMedia = summary.mediaUrls?.firstOrNull().orEmpty()
    val isVideo    = firstMedia.endsWith(".mp4", true) || firstMedia.endsWith(".webm", true)

    val profileUrl = summary.usuarioFotoPerfil?.let { raw ->
        runCatching {
            val uri = URI(raw)
            val path = uri.rawPath + (uri.rawQuery?.let { "?$it" } ?: "")
            if (path.startsWith("/")) RetrofitClient.BASE_URL.trimEnd('/') + path else raw
        }.getOrNull() ?: raw
    }

    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .weight(1f)
                    .clickable {
                        val photoParam = profileUrl
                            ?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.name()) }
                            .orEmpty()
                        navController.navigate("recipe/${summary.id}?photo=$photoParam")
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    if (isVideo) {
                        LoopingVideoPlayer(
                            uri      = firstMedia.toUri(),
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        AsyncImage(
                            model              = firstMedia,
                            contentDescription = summary.nombre,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                        )
                    }
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
                        if (profileUrl.isNullOrBlank()) {
                            Icon(Icons.Outlined.Person, contentDescription = "Sin avatar", Modifier.size(20.dp))
                        } else {
                            AsyncImage(
                                model              = profileUrl,
                                contentDescription = summary.usuarioCreadorAlias,
                                modifier           = Modifier.size(20.dp).clip(CircleShape),
                                contentScale       = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(summary.usuarioCreadorAlias.orEmpty(), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${summary.tiempo} min", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            IconButton(onClick = onToggleSave) {
                Icon(
                    imageVector       = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    tint              = if (isSaved) Color(0xFFE63946) else Color.Gray,
                    contentDescription= "Guardar"
                )
            }
        }
    }
}