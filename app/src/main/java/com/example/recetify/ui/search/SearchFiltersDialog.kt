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
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.recetify.ui.theme.Ladrillo
import com.example.recetify.util.TheMealDBImages
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.zIndex
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

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
    // --- Autocompletado de ingredientes ---
    var showDropdown by remember { mutableStateOf(false) }
    val allIngredients = remember { TheMealDBImages::class.java.getDeclaredField("ingredientImageMap").apply { isAccessible = true }.get(TheMealDBImages) as Map<String, String> }
    val ingredientList = remember { allIngredients.keys.sorted() }
    val filteredIngredients = remember(currentIngredient) {
        if (currentIngredient.isNullOrBlank() || currentIngredient.length < 2) emptyList()
        else ingredientList.filter { it.startsWith(currentIngredient.lowercase()) && it != currentIngredient.lowercase() }.take(6)
    }

    var showDropdownExclude by remember { mutableStateOf(false) }
    val filteredExcludeIngredients = remember(currentExclude) {
        if (currentExclude.isNullOrBlank() || currentExclude.length < 2) emptyList()
        else ingredientList.filter { it.startsWith(currentExclude.lowercase()) && it != currentExclude.lowercase() }.take(6)
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
                    Text("Filtrar búsqueda", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Ingrediente
                Text("INGREDIENTE", style = MaterialTheme.typography.labelLarge.copy(color = Color.Black))
                Box {
                    OutlinedTextField(
                        value = currentIngredient.orEmpty(),
                        onValueChange = {
                            onIngredientChange(it)
                            showDropdown = it.length >= 2
                        },
                        placeholder = { Text("Buscar…") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().zIndex(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { showDropdown = false }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    if (showDropdown && filteredIngredients.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 56.dp)
                                .zIndex(2f)
                                // Quitar el borde negro:
                                //.border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                                .heightIn(max = 168.dp), // 3 items de 56dp aprox
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                filteredIngredients.forEachIndexed { idx, suggestion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onIngredientChange(suggestion)
                                                showDropdown = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val imgUrl = TheMealDBImages.getIngredientImageUrl(suggestion)
                                        if (imgUrl != null) {
                                            AsyncImage(
                                                model = imgUrl,
                                                contentDescription = suggestion,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                alignment = Alignment.Center
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = suggestion.replaceFirstChar { it.uppercase() },
                                            color = Color.Black,
                                            fontSize = 16.sp
                                        )
                                    }
                                    // Línea divisoria fina entre sugerencias, excepto la última
                                    if (idx < filteredIngredients.lastIndex) {
                                        Divider(
                                            color = Color(0x22000000), // gris muy suave
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))

                // Sin ingrediente
                Text("SIN INGREDIENTE", style = MaterialTheme.typography.labelLarge.copy(color = Color.Black))
                Box {
                    OutlinedTextField(
                        value = currentExclude.orEmpty(),
                        onValueChange = {
                            onExcludeChange(it)
                            showDropdownExclude = it.length >= 2
                        },
                        placeholder = { Text("Excluir…") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().zIndex(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { showDropdownExclude = false }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                    if (showDropdownExclude && filteredExcludeIngredients.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 56.dp)
                                .zIndex(2f)
                                // Quitar el borde negro:
                                //.border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                                .heightIn(max = 168.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                filteredExcludeIngredients.forEachIndexed { idx, suggestion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onExcludeChange(suggestion)
                                                showDropdownExclude = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val imgUrl = TheMealDBImages.getIngredientImageUrl(suggestion)
                                        if (imgUrl != null) {
                                            AsyncImage(
                                                model = imgUrl,
                                                contentDescription = suggestion,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                alignment = Alignment.Center
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = suggestion.replaceFirstChar { it.uppercase() },
                                            color = Color.Black,
                                            fontSize = 16.sp
                                        )
                                    }
                                    // Línea divisoria fina entre sugerencias, excepto la última
                                    if (idx < filteredExcludeIngredients.lastIndex) {
                                        Divider(
                                            color = Color(0x22000000), // gris muy suave
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))

                // Usuario
                Text("USUARIO", style = MaterialTheme.typography.labelLarge.copy(color = Color.Black))
                OutlinedTextField(
                    value = currentUser.orEmpty(),
                    onValueChange = onUserChange,
                    placeholder = { Text("Buscar…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                Spacer(Modifier.height(14.dp))

                // Rating
                Text("RATING", style = MaterialTheme.typography.labelLarge.copy(color = Color.Black))
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
                            tint = if (currentRating != null && star <= currentRating) Ladrillo else Color.LightGray
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ladrillo)
                ) {
                    Text("APLICAR", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        onIngredientChange("")
                        onExcludeChange("")
                        onUserChange("")
                        onRatingSelect(null)
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(180.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Ladrillo
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        "RESET",
                        color = Ladrillo,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    )
                }
            }
        }
    }
}