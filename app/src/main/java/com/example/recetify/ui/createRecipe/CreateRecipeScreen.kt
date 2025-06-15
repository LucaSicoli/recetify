package com.example.recetify.ui.createRecipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recetify.R
import com.example.recetify.data.remote.model.RecipeIngredientRequest
import com.example.recetify.data.remote.model.RecipeStepRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    viewModel: CreateRecipeViewModel,
    onClose: () -> Unit,
    onSaved: () -> Unit,
    onPublished: () -> Unit,
) {
    val uploading by viewModel.uploading.collectAsState()
    val submitting by viewModel.submitting.collectAsState()
    val error by viewModel.error.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var porciones by remember { mutableStateOf(1) }
    var tiempo by remember { mutableStateOf(1) }
    val ingredients = remember { mutableStateListOf<RecipeIngredientRequest>() }
    val steps = remember { mutableStateListOf<RecipeStepRequest>() }
    val etiquetas = listOf(
        "Desayuno", "Almuerzo", "Snack", "Merienda", "Cena",
        "Hamburguesas", "Pizzas", "Arroz",
        "Fideos", "Carnes y Pescados"
    )
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                },
                title = { /* vacío */ },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                OutlinedButton(
                    onClick = onSaved,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Guardar")
                }
                Button(
                    onClick = onPublished,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBC6154))
                ) {
                    Text("Publicar", color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF8F8F8))
        ) {
            // — Foto + botón “Agregar foto” —
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.photoUrl == null) {
                    IconButton(onClick = { /* abrir picker */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.placeholder_recipe),
                            contentDescription = "Agregar foto",
                            modifier = Modifier.size(80.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = viewModel.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                TextButton(
                    onClick = { /* abrir picker */ },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFFE0E0E0))
                ) {
                    Text("AGREGAR FOTO", fontWeight = FontWeight.Bold)
                }
            }

            // — Resto del formulario —
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre de la Receta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        placeholder = { Text("Breve descripción de la receta...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Porciones", modifier = Modifier.weight(1f))
                        IconButton(onClick = { if (porciones > 1) porciones-- }) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }
                        Text(porciones.toString(), fontSize = 18.sp)
                        IconButton(onClick = { porciones++ }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tiempo (min)", modifier = Modifier.weight(1f))
                        IconButton(onClick = { if (tiempo > 1) tiempo-- }) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }
                        Text(tiempo.toString(), fontSize = 18.sp)
                        IconButton(onClick = { tiempo++ }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Ingredientes (${ingredients.size} Items)",
                        fontWeight = FontWeight.SemiBold
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { /* abrir modal ingredientes */ },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar")
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Instrucciones (${steps.size} Pasos)",
                        fontWeight = FontWeight.SemiBold
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { /* abrir modal pasos */ },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar")
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("Etiquetas", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow {
                        items(etiquetas) { tag ->
                            val selected = selectedTags.contains(tag)
                            Text(
                                text = tag,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (selected) Color(0xFFBC6154)
                                        else Color(0xFFF2F2F2)
                                    )
                                    .clickable {
                                        selectedTags = if (selected)
                                            selectedTags - tag
                                        else
                                            selectedTags + tag
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (selected) Color.White else Color.Black
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "+",
                        fontSize = 20.sp,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F2))
                            .clickable { /* agregar nueva etiqueta */ }
                            .wrapContentSize()
                    )

                    Spacer(Modifier.height(32.dp))
                    if (error != null) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}