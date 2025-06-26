// 2) SavedRecipesScreen.kt
package com.example.recetify.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SavedRecipesScreen(
    favVm: FavouriteViewModel = viewModel(),
    onRecipeClick: (Long) -> Unit = {}
) {
    val favs by favVm.favourites.collectAsState()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(favs, key = { it.id }) { fav ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable { onRecipeClick(fav.id) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(fav.recipeNombre, style = MaterialTheme.typography.titleMedium)
                    Text("Guardada: ${fav.fechaAgregado}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}