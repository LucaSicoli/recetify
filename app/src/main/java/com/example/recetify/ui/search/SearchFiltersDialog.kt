// File: app/src/main/java/com/example/recetify/ui/search/SearchFiltersDialog.kt
package com.example.recetify.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun SearchFiltersDialog(
    currentIngredient: String?,
    currentExclude: String?,
    currentUser: String?,
    currentRating: Int?,
    onIngredientChange: (String) -> Unit,
    onExcludeChange: (String) -> Unit,
    onUserChange: (String) -> Unit,
    onRatingSelect: (Int?) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
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

                // Ingrediente
                Text("INGREDIENTE", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = currentIngredient.orEmpty(),
                    onValueChange = onIngredientChange,
                    placeholder = { Text("Buscar…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // Sin ingrediente
                Text("SIN INGREDIENTE", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = currentExclude.orEmpty(),
                    onValueChange = onExcludeChange,
                    placeholder = { Text("Excluir…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // Usuario
                Text("USUARIO", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = currentUser.orEmpty(),
                    onValueChange = onUserChange,
                    placeholder = { Text("Buscar…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // Rating
                Text("RATING", style = MaterialTheme.typography.labelMedium)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (currentRating != null && star <= currentRating)
                                Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    onRatingSelect(if (currentRating == star) null else star)
                                }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B))
                ) {
                    Text("APLICAR", color = Color.Black)
                }
            }
        }
    }
}