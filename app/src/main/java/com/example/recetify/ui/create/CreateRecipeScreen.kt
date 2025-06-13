package com.example.recetify.ui.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.recetify.R
import com.example.recetify.ui.navigation.BottomNavBar
import kotlinx.coroutines.launch
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


data class Ingredient(val emoji: String, val name: String, var unit: String = "")
data class InstructionStep(var title: String = "", var description: String = "", var imageUri: String? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(navController: NavController) {
    val ingredients = remember { mutableStateListOf<Ingredient>() }
    val steps = remember { mutableStateListOf<InstructionStep>() }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSelector by remember { mutableStateOf(false) }

    var recipeName by remember { mutableStateOf("") }
    var recipeDescription by remember { mutableStateOf("") }
    var portions by remember { mutableStateOf("2") }
    var time by remember { mutableStateOf("2") }

    val availableIngredients = listOf(
        Ingredient("🥔", "Papa"),
        Ingredient("🥑", "Palta"),
        Ingredient("🍅", "Tomate")
    )

    val selectedTags = remember { mutableStateListOf<String>() }
    val etiquetas = listOf("Desayuno", "Almuerzo", "Snack", "Merienda", "Cena", "Hamburguesas", "Pizzas", "Arroz", "Fideos", "Carnes y Pescados")

    if (showSelector) {
        ModalBottomSheet(
            onDismissRequest = { showSelector = false },
            sheetState = sheetState
        ) {
            IngredientSelectorSheet(
                availableIngredients = availableIngredients,
                onSelect = {
                    ingredients.add(it)
                    showSelector = false
                },
                onClose = { showSelector = false }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
                .padding(bottom = 80.dp) // deja espacio para la navbar fija
        ) {
            // Imagen de portada con botón de cerrar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_placeholder_image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clickable {
                        // Acción para agregar imagen
                    }
                )

                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(12.dp)
                        .background(Color(0x66000000), shape = RoundedCornerShape(50))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))




            OutlinedTextField(
                value = recipeName,
                onValueChange = { recipeName = it },
                placeholder = { Text("Nombre de la Receta", color = Color(0xFFB3B3C3)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = Color(0xFF1E2D3D)),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFB3B3C3),
                    focusedBorderColor = Color(0xFFB3B3C3),
                    unfocusedTextColor = Color(0xFF1E2D3D),
                    focusedTextColor = Color(0xFF1E2D3D)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))


// Descripción
            OutlinedTextField(
                value = recipeDescription,
                onValueChange = { recipeDescription = it },
                placeholder = { Text("Breve descripción de la receta...", color = Color(0xFFB3B3C3)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(120.dp),
                textStyle = LocalTextStyle.current.copy(color = Color(0xFF1E2D3D), fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE3E3EE),
                    focusedBorderColor = Color(0xFFE3E3EE),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedTextColor = Color(0xFF1E2D3D),
                    focusedTextColor = Color(0xFF1E2D3D)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

// Porciones y Tiempo
            // Porciones y Tiempo
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Porciones", fontWeight = FontWeight.Bold, color = Color(0xFF0F1C24))
                    OutlinedTextField(
                        value = portions,
                        onValueChange = { portions = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color(0xFF1E2D3D)),
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = Color(0xFF1E2D3D)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray,
                            unfocusedTextColor = Color(0xFF1E2D3D),
                            focusedTextColor = Color(0xFF1E2D3D)
                        )
                    )
                }

                Spacer(Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Tiempo", fontWeight = FontWeight.Bold, color = Color(0xFF8A8A9E))
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color(0xFF1E2D3D)),
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF1E2D3D)
                            )
                        }
                        ,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray,
                            unfocusedTextColor = Color(0xFF1E2D3D),
                            focusedTextColor = Color(0xFF1E2D3D)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(
                color = Color(0xFFE3E3EE),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
            // INGREDIENTES
            Text("Ingredientes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(8.dp))

            ingredients.forEachIndexed { index, ing ->
                var cantidad by remember { mutableStateOf("") }
                var unidad by remember { mutableStateOf("un") }
                var expanded by remember { mutableStateOf(false) }
                val unidades = listOf("un", "gr", "ml", "kg", "cc")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(text = ing.emoji, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = ing.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E2D3D)
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = cantidad,
                            onValueChange = {
                                cantidad = it
                                ingredients[index] = ing.copy(unit = "$it $unidad")
                            },
                            modifier = Modifier
                                .width(60.dp)
                                .height(50.dp),
                            placeholder = { Text("2") },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 11.sp,
                                color = Color.Black
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.DarkGray
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            Button(
                                onClick = { expanded = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF99585B)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text(unidad, color = Color.White)
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                unidades.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it) },
                                        onClick = {
                                            unidad = it
                                            ingredients[index] = ing.copy(unit = "$cantidad $unidad")
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { ingredients.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }



            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                TextButton(
                    onClick = { showSelector = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.textButtonColors()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFCF4E4E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Ingrediente", color = Color.Black)
                }
            }


            Spacer(Modifier.height(24.dp))
            Divider(
                color = Color(0xFFE3E3EE),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
            // INSTRUCCIONES
            Text("Instrucciones", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(8.dp))

            steps.forEachIndexed { index, step ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0xFFE67A6D), shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            OutlinedTextField(
                                value = step.title,
                                onValueChange = { steps[index] = step.copy(title = it) },
                                label = { Text("Nombre del Paso") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { steps.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar paso")
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = step.description,
                            onValueChange = { steps[index] = step.copy(description = it) },
                            label = { Text("Descripción de los pasos a seguir...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAEAEA)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("AGREGAR FOTO", color = Color.Black)
                            }
                            step.imageUri?.let {
                                Text(text = "1 Archivo adjunto", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                TextButton(
                    onClick = { steps.add(InstructionStep()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.textButtonColors()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFCF4E4E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar", color = Color.Black)
                }
            }
            Spacer(Modifier.height(24.dp))
            Divider(
                color = Color(0xFFE3E3EE),
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
            // ETIQUETAS
            Text("Etiquetas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 24.dp))
            Text(
                text = "Estas ayudan a que otros usuarios encuentren tus recetas más fácilmente",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                color = Color.Gray
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                etiquetas.forEach { tag ->
                    val selected = tag in selectedTags
                    AssistChip(
                        onClick = {
                            if (selected) selectedTags.remove(tag)
                            else selectedTags.add(tag)
                        },
                        label = { Text(tag) },
                        shape = RoundedCornerShape(50),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) Color(0xFFFDE6E3) else Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.weight(1f),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("GUARDAR", color = Color.Black)
                }
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67A6D)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("PUBLICAR", color = Color.White)
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomNavBar(navController = navController)
        }
    }
}