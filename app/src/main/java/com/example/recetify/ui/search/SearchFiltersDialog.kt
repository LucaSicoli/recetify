package com.example.recetify.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun SearchFiltersDialog(
    tipoOptions: List<String>,
    selectedTipo: String?,
    currentIngredient: String?,
    currentUser: String?,
    currentRating: Int?,
    onTipoSelect: (String) -> Unit,
    onIngredientChange: (String) -> Unit,
    onUserChange: (String) -> Unit,
    onRatingSelect: (Int?) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                // Header
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filtrar búsqueda", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Tipo
                Text("TIPO", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tipoOptions) { tipo ->
                        FilterChip(
                            selected = tipo == selectedTipo,
                            onClick = { onTipoSelect(tipo) },
                            label = { Text(tipo) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("INGREDIENTE", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentIngredient.orEmpty(),
                    onValueChange = onIngredientChange,
                    placeholder = { Text("Buscar…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Text("USUARIO", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentUser.orEmpty(),
                    onValueChange = onUserChange,
                    placeholder = { Text("Buscar…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Text("RATING", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Row {
                    (1..5).forEach { star ->
                        IconButton(onClick = {
                            onRatingSelect(if (currentRating == star) null else star)
                        }) {
                            Icon(
                                imageVector = if (currentRating != null && star <= currentRating)
                                    Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = null
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("APLICAR")
                }
            }
        }
    }
}