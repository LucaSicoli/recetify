package com.example.recetify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Shapes
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.FlowRow // Si estás usando Accompanist
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun FilterDialog(
    selectedTypes: List<String>,
    onTypeSelected: (String) -> Unit,
    ingredientFilter: String,
    onIngredientFilterChange: (String) -> Unit,
    ingredientCondition: String,
    onIngredientConditionChange: (String) -> Unit,
    usuario: String,
    onUsuarioChange: (String) -> Unit,
    selectedRating: Int,
    onRatingSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    onApplyFilter: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Encabezado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Filtra tu búsqueda", fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // TIPO
                Text("TIPO", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val opcionesTipo = listOf("Hamburguesas", "Pizzas", "Arroz", "Fideos", "Pescados")
                    opcionesTipo.forEach { tipo ->
                        FilterChip(
                            text = tipo,
                            selected = selectedTypes.contains(tipo),
                            onClick = { onTypeSelected(tipo) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // INGREDIENTES
                Text("INGREDIENTES", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DropdownSelector(
                        options = listOf("Contiene", "No contiene"),
                        selectedOption = ingredientCondition,
                        onOptionSelected = onIngredientConditionChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = ingredientFilter,
                        onValueChange = onIngredientFilterChange,
                        placeholder = { Text("Buscar ...") },
                        modifier = Modifier
//                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(25),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFFD36B5A)
                        )
                    )

                }

                Spacer(modifier = Modifier.height(12.dp))

                // USUARIO
                Text("USUARIO", fontWeight = FontWeight.Medium)
                TextField(
                    value = usuario,
                    onValueChange = onUsuarioChange,
                    placeholder = { Text("Buscar") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // RATING
                Text("RATING", fontWeight = FontWeight.Medium)
                Row {
                    (1..5).forEach { star ->
                        IconButton(onClick = { onRatingSelected(star) }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (star <= selectedRating) Color(0xFFE26D5A) else Color.LightGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BOTÓN
                Button(
                    onClick = onApplyFilter,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE26D5A))
                ) {
                    Text("FILTRAR", color = Color.White)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterDialogPreview() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEFEFEF))) {
        FilterDialog(
            selectedTypes = listOf("Pizzas"),
            onTypeSelected = {},
            ingredientFilter = "Queso",
            onIngredientFilterChange = {},
            ingredientCondition = "Contiene",
            onIngredientConditionChange = {},
            usuario = "Nico",
            onUsuarioChange = {},
            selectedRating = 4,
            onRatingSelected = {},
            onDismiss = {},
            onApplyFilter = {}
        )
    }
}

