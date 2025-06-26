// 3) MyRecipesScreen.kt
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
fun MyRecipesScreen(
    myRecipesVm: MyRecipesViewModel = viewModel(),
    onRecipeClick: (Long) -> Unit = {}
) {
    val recipes by myRecipesVm.recipes.collectAsState()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(recipes, key = { it.id }) { rec ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable { onRecipeClick(rec.id) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(rec.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(rec.descripcion.orEmpty(), style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                }
            }
        }
    }
}