// File: app/src/main/java/com/example/recetify/ui/search/SearchFiltersDialog.kt
package com.example.recetify.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.recetify.ui.theme.Ladrillo
import com.example.recetify.util.listaIngredientesConEmoji

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
    // obtenemos lista de ingredientes
    val allIngredients = remember { listaIngredientesConEmoji() }

    // ** lógica de sugerencias actualizada **
    val ingredientSuggestions = remember(currentIngredient, allIngredients) {
        currentIngredient
            .orEmpty()
            .takeIf { it.isNotBlank() }
            ?.let { text ->
                allIngredients.filter { it.contains(text, ignoreCase = true) }
                    .take(3)
            } ?: emptyList()
    }

    val excludeSuggestions = remember(currentExclude, allIngredients) {
        currentExclude
            .orEmpty()
            .takeIf { it.isNotBlank() }
            ?.let { text ->
                allIngredients.filter { it.contains(text, ignoreCase = true) }
                    .take(3)
            } ?: emptyList()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFF0E0DC)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp)) {
                // Header
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Filtrar búsqueda",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Ingrediente
                Text(
                    "INGREDIENTE",
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.Black)
                )
                OutlinedTextField(
                    value = currentIngredient.orEmpty(),
                    onValueChange = onIngredientChange,
                    placeholder = { Text("Buscar…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                // mostrar sugerencias de ingrediente
                ingredientSuggestions.forEach { suggestion ->
                    Text(
                        suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIngredientChange(suggestion) }
                            .padding(vertical = 4.dp),
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(14.dp))

                // Sin ingrediente
                Text(
                    "SIN INGREDIENTE",
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.Black)
                )
                OutlinedTextField(
                    value = currentExclude.orEmpty(),
                    onValueChange = onExcludeChange,
                    placeholder = { Text("Excluir…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                // mostrar sugerencias de exclusión
                excludeSuggestions.forEach { suggestion ->
                    Text(
                        suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExcludeChange(suggestion) }
                            .padding(vertical = 4.dp),
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(14.dp))

                // Usuario
                Text(
                    "USUARIO",
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.Black)
                )
                OutlinedTextField(
                    value = currentUser.orEmpty(),
                    onValueChange = onUserChange,
                    placeholder = { Text("Buscar…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(14.dp))

                // Rating
                Text(
                    "RATING",
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.Black)
                )
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
                                },
                            tint = if (currentRating != null && star <= currentRating)
                                Ladrillo else Color.LightGray
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onApply,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ladrillo)
                ) {
                    Text(
                        "APLICAR",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}