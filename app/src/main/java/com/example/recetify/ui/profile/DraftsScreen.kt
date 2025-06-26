// 1) DraftsScreen.kt
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
fun DraftsScreen(
    draftVm: DraftViewModel = viewModel(),
    onDraftClick: (Long) -> Unit = {}
) {
    val drafts by draftVm.drafts.collectAsState()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(drafts, key = { it.id }) { draft ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable { onDraftClick(draft.id) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(draft.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(draft.descripcion.orEmpty(), style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                }
            }
        }
    }
}