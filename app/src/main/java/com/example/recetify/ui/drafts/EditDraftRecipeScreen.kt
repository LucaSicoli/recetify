package com.example.recetify.ui.drafts

import android.app.Application
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recetify.data.remote.model.RecipeIngredientRequest
import com.example.recetify.data.remote.model.RecipeStepRequest
import com.example.recetify.ui.drafts.viewmodel.EditDraftRecipeViewModel
import com.example.recetify.ui.drafts.viewmodel.EditDraftRecipeViewModelFactory

@Composable
fun EditDraftRecipeScreen(
    recipeId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: EditDraftRecipeViewModel = viewModel(
        factory = EditDraftRecipeViewModelFactory(recipeId, context.applicationContext as Application)
    )

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val naranja = Color(0xFFC6665A)

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.setFotoPrincipalUri(it) }
    }

    // Estado para mostrar Ingredientes o Pasos
    val showIngredients = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFFF9FAFB)) // fondo clarito
    ) {
        // Imagen principal con botón volver superpuesto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            AsyncImage(
                model = uiState.fotoPrincipal,
                contentDescription = "Foto principal",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        }

        Surface(
            modifier = Modifier
                .offset(y = (-24).dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            tonalElevation = 4.dp
        ) {
            Column(Modifier.padding(24.dp)) {
                // Campos básicos
                TextField(
                    value = uiState.nombre,
                    onValueChange = { viewModel.updateNombre(it) },
                    label = { Text("Nombre de la receta") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                TextField(
                    value = uiState.descripcion,
                    onValueChange = { viewModel.updateDescripcion(it) },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextField(
                        value = uiState.tiempo.toString(),
                        onValueChange = { viewModel.updateTiempo(it.toIntOrNull() ?: 0) },
                        label = { Text("Tiempo (min)") },
                        modifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = uiState.porciones.toString(),
                        onValueChange = { viewModel.updatePorciones(it.toIntOrNull() ?: 1) },
                        label = { Text("Porciones") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Button(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cambiar imagen principal")
                }

                Spacer(Modifier.height(24.dp))

                // Botones tipo pestañas para Ingredientes / Pasos
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE6EBF2))
                ) {
                    Row(Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                                .background(if (showIngredients.value) Color(0xFF042628) else Color.Transparent)
                                .clickable { showIngredients.value = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Ingredientes",
                                color = if (showIngredients.value) Color.White else Color(0xFF042628),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                .background(if (!showIngredients.value) Color(0xFF042628) else Color.Transparent)
                                .clickable { showIngredients.value = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Pasos",
                                color = if (!showIngredients.value) Color.White else Color(0xFF042628),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (showIngredients.value) {
                    val ingredientes = uiState.ingredients.toMutableStateList()
                    ingredientes.forEachIndexed { index, ingrediente ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = ingrediente.nombre.orEmpty(),
                                    onValueChange = {
                                        ingredientes[index] = ingrediente.copy(nombre = it)
                                        viewModel.updateIngredientes(ingredientes)
                                    },
                                    label = { Text("Ingrediente ${index + 1}") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = ingrediente.cantidad.toString(),
                                        onValueChange = {
                                            val cantidad = it.toDoubleOrNull() ?: 0.0
                                            ingredientes[index] = ingrediente.copy(cantidad = cantidad)
                                            viewModel.updateIngredientes(ingredientes)
                                        },
                                        label = { Text("Cantidad") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = ingrediente.unidadMedida,
                                        onValueChange = {
                                            ingredientes[index] = ingrediente.copy(unidadMedida = it)
                                            viewModel.updateIngredientes(ingredientes)
                                        },
                                        label = { Text("Unidad") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            ingredientes.add(RecipeIngredientRequest(nombre = "", cantidad = 0.0, unidadMedida = ""))
                            viewModel.updateIngredientes(ingredientes)
                        },
                        modifier = Modifier.fillMaxWidth(),


                    ) {
                        Text("Agregar ingrediente")
                    }
                } else {
                    val pasos = uiState.steps.toMutableStateList()
                    pasos.forEachIndexed { index, paso ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = paso.titulo.orEmpty(),
                                    onValueChange = {
                                        pasos[index] = paso.copy(titulo = it)
                                        viewModel.updatePasos(pasos)
                                    },
                                    label = { Text("Paso ${index + 1} - Título") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = paso.descripcion,
                                    onValueChange = {
                                        pasos[index] = paso.copy(descripcion = it)
                                        viewModel.updatePasos(pasos)
                                    },
                                    label = { Text("Descripción") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            pasos.add(
                                RecipeStepRequest(
                                    numeroPaso = pasos.size + 1,
                                    titulo = "",
                                    descripcion = "",
                                    urlMedia = null
                                )
                            )
                            viewModel.updatePasos(pasos)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Agregar paso")
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            viewModel.updateDraftRecipe {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar")
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = {
                            viewModel.publishDraftRecipe {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Publicar")
                    }
                }
            }
        }
    }
}
