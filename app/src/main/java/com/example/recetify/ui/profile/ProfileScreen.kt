package com.example.recetify.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import com.example.recetify.data.remote.model.UserSavedRecipeDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    draftVm: DraftViewModel = viewModel(),
    favVm: FavouriteViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Borradores", "Guardadas")

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                // ── Pestaña Borradores ─────────────────────────
                0 -> DraftsList(
                    items       = draftVm.drafts.collectAsState().value,
                    onItemClick = { draft ->
                        // Ejemplo: navegar a la pantalla de edición pasándole el id
                        navController.navigate("createRecipe?draftId=${draft.id}")
                    }
                )
                // ── Pestaña Guardadas ─────────────────────────
                1 -> FavouritesList(
                    items = favVm.favourites.collectAsState().value
                )
            }
        }
    }
}

@Composable
private fun DraftsList(
    items: List<RecipeSummaryResponse>,
    onItemClick: (RecipeSummaryResponse) -> Unit
) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay borradores aún")
        }
    } else {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { draft ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(draft) }
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(draft.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            draft.descripcion.orEmpty(),
                            style   = MaterialTheme.typography.bodyMedium,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavouritesList(items: List<UserSavedRecipeDTO>) {
    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tienes recetas guardadas")
        }
    } else {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { fav ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(fav.recipeNombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Guardada el ${fav.fechaAgregado}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}