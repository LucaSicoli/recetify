// app/src/main/java/com/example/recetify/ui/createRecipe/CreateRecipeScreen.kt
package com.example.recetify.ui.createRecipe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recetify.R
import com.example.recetify.data.remote.model.RecipeIngredientRequest
import com.example.recetify.data.remote.model.RecipeStepRequest
import com.example.recetify.util.FileUtil
import com.example.recetify.util.obtenerEmoji
import android.webkit.MimeTypeMap

private val Accent = Color(0xFFBC6154)
private val GrayBg  = Color(0xFFF8F8F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    viewModel: CreateRecipeViewModel,
    onClose:    () -> Unit,
    onSaved:    () -> Unit,
    onPublished:() -> Unit,
) {
    // --- Local & ViewModel state ---
    var localImageUri       by remember { mutableStateOf<Uri?>(null) }
    var showIngredientDialog by remember { mutableStateOf(false) }
    var showStepDialog       by remember { mutableStateOf(false) }

    var nombre      by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var porciones   by rememberSaveable { mutableStateOf(1) }
    var tiempo      by rememberSaveable { mutableStateOf(1) }

    val ingredients = remember { mutableStateListOf<RecipeIngredientRequest>() }
    val steps       = remember { mutableStateListOf<RecipeStepRequest>() }
    val etiquetas   = listOf(
        "Desayuno","Almuerzo","Snack","Merienda","Cena",
        "Hamburguesas","Pizzas","Arroz","Fideos","Carnes y Pescados"
    )
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    val photoUrl   by viewModel.photoUrl.collectAsState(initial = null)
    val uploading  by viewModel.uploading.collectAsState(initial = false)
    val submitting by viewModel.submitting.collectAsState(initial = false)
    val error      by viewModel.error.collectAsState(initial = null)

    // Image picker
    val context  = LocalContext.current
    val launcher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let {
            localImageUri = it
            viewModel.uploadPhoto(FileUtil.from(context, it))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                },
                title  = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.White, tonalElevation = 8.dp) {
                OutlinedButton(
                    onClick = onSaved,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(8.dp),
                    shape  = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Accent)
                ) {
                    Text("GUARDAR", color = Accent, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onPublished,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(8.dp),
                    shape  = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Text("PUBLICAR", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(GrayBg)
        ) {
            // Header image (toda el área clickeable)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    localImageUri != null -> AsyncImage(
                        model = localImageUri, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                    )
                    photoUrl     != null -> AsyncImage(
                        model = photoUrl!!, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                    )
                    else -> Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Agregar foto",
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                }
                if (uploading) {
                    CircularProgressIndicator(
                        Modifier.align(Alignment.BottomCenter).padding(bottom = 56.dp),
                        color = Accent
                    )
                }
            }

            // Form surface
            Surface(
                Modifier.offset(y = (-32).dp).fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nombre
                    OutlinedTextField(
                        value         = nombre,
                        onValueChange = { nombre = it },
                        label         = { Text("Nombre de la Receta", color = Accent) },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp)
                    )
                    // Descripción
                    OutlinedTextField(
                        value         = descripcion,
                        onValueChange = { descripcion = it },
                        placeholder   = { Text("Breve descripción…", color = Color.Gray) },
                        modifier      = Modifier.fillMaxWidth().height(120.dp),
                        maxLines      = 4,
                        shape         = RoundedCornerShape(12.dp)
                    )
                    // Steppers
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        StepperField("Porciones", porciones, { if (porciones>1) porciones-- }, { porciones++ })
                        StepperField("Tiempo (min)", tiempo, { if (tiempo>1) tiempo-- }, { tiempo++ })
                    }
                    // Ingredientes header
                    Text("Ingredientes (${ingredients.size})",
                        fontWeight = FontWeight.SemiBold,
                        color      = Accent,
                        fontSize   = 18.sp
                    )
                    // Ingredientes list
                    ingredients.forEachIndexed { idx, ing ->
                        IngredientRow(idx, ing, onUpdate = { newIng ->
                            ingredients[idx] = newIng
                        })
                    }
                    // Agregar ingrediente
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable { showIngredientDialog = true },
                        shape     = RoundedCornerShape(12.dp),
                        border    = BorderStroke(1.dp, Accent),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Accent)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar ingrediente", color = Accent, fontWeight = FontWeight.Medium)
                        }
                    }
                    // Instrucciones
                    Text("Instrucciones (${steps.size})",
                        fontWeight = FontWeight.SemiBold,
                        color      = Accent,
                        fontSize   = 18.sp
                    )
                    steps.forEachIndexed { idx, step ->
                        StepCard(
                            stepNumber    = step.numeroPaso,
                            title         = step.titulo.orEmpty(),
                            description   = step.descripcion,
                            onTitleChange = { steps[idx] = step.copy(titulo = it) },
                            onDescChange  = { steps[idx] = step.copy(descripcion = it) },
                            onAddPhoto    = { launcher.launch("image/*") },
                            attachments   = if (step.urlMedia != null) 1 else 0
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable { showStepDialog = true },
                        shape     = RoundedCornerShape(12.dp),
                        border    = BorderStroke(1.dp, Accent),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Accent)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar paso", color = Accent, fontWeight = FontWeight.Medium)
                        }
                    }
                    // Etiquetas
                    Text("Etiquetas", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(etiquetas) { tag ->
                            val sel = selectedTags.contains(tag)
                            Text(
                                tag,
                                Modifier.clip(RoundedCornerShape(16.dp))
                                    .background(if (sel) Accent.copy(alpha = 0.15f) else Color(0xFFF2F2F2))
                                    .clickable {
                                        selectedTags = if (sel) selectedTags - tag else selectedTags + tag
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (sel) Accent else Color.Black
                            )
                        }
                        item {
                            Box(
                                Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF2F2F2))
                                    .clickable { /* nueva etiqueta */ },
                                contentAlignment = Alignment.Center
                            ) { Text("+", fontSize = 16.sp) }
                        }
                    }
                    error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }
        }

        // Dialogo Ingrediente
        if (showIngredientDialog) {
            var selectedIng by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showIngredientDialog = false },
                title   = { Text("Seleccionar ingrediente") },
                text    = {
                    LazyColumn {
                        items(listOf("Tomate","Queso","Harina","Huevos","Leche")) { ing ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ingredients += RecipeIngredientRequest(
                                            ingredientId = null,
                                            nombre       = ing,
                                            cantidad     = 1.0,
                                            unidadMedida = "un"
                                        )
                                        showIngredientDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(obtenerEmoji(ing), fontSize = 20.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(ing, fontSize = 16.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showIngredientDialog = false }) { Text("Cancelar") }
                }
            )
        }

        // Dialogo Paso
        if (showStepDialog) {
            var stepTitle by remember { mutableStateOf("") }
            var stepDesc  by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showStepDialog = false },
                title   = { Text("Nuevo paso") },
                text    = {
                    Column {
                        OutlinedTextField(
                            value         = stepTitle,
                            onValueChange = { stepTitle = it },
                            placeholder   = { Text("Nombre del Paso") },
                            modifier      = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value         = stepDesc,
                            onValueChange = { stepDesc = it },
                            placeholder   = { Text("Descripción del paso") },
                            modifier      = Modifier.fillMaxWidth().height(80.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        steps += RecipeStepRequest(
                            numeroPaso = steps.size + 1,
                            titulo     = stepTitle,
                            descripcion= stepDesc,
                            urlMedia   = null
                        )
                        showStepDialog = false
                    }) { Text("Agregar") }
                },
                dismissButton = {
                    TextButton(onClick = { showStepDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

// Componente Stepper
@Composable
private fun StepperField(
    label: String,
    value: Int,
    onDec: () -> Unit,
    onInc: () -> Unit
) {
    Column {
        Text(label, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDec, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = Accent)
            }
            Box(
                Modifier
                    .width(40.dp)
                    .height(32.dp)
                    .border(1.dp, Accent, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("$value", fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onInc, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Accent)
            }
        }
    }
}

// Componente tarjeta de paso
@Composable
private fun StepCard(
    stepNumber:   Int,
    title:        String,
    description:  String,
    onTitleChange:(String)->Unit,
    onDescChange: (String)->Unit,
    onAddPhoto:   ()->Unit,
    attachments:  Int
) {
    Card(
        shape     = RoundedCornerShape(12.dp),
        border    = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(24.dp)
                        .background(Accent.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$stepNumber", color = Accent, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value         = title,
                    onValueChange = onTitleChange,
                    placeholder   = { Text("Nombre del Paso", color = Color.Gray) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
            }
            OutlinedTextField(
                value         = description,
                onValueChange = onDescChange,
                placeholder   = { Text("Descripción de los pasos…", color = Color.Gray) },
                modifier      = Modifier.fillMaxWidth().height(80.dp),
                maxLines      = 3
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAddPhoto,
                    shape   = RoundedCornerShape(8.dp),
                    border  = BorderStroke(1.dp, Accent)
                ) {
                    Text("AGREGAR FOTO", color = Accent)
                }
                Text(
                    "$attachments archivo${if (attachments != 1) "s" else ""}",
                    color = Color.Gray
                )
            }

        }
    }
}

// ── Fila de ingrediente: emoji, nombre, – / cantidad / + y menú de unidades ────────────────
@Composable
private fun IngredientRow(
    index: Int,
    ingredient: RecipeIngredientRequest,
    onUpdate: (RecipeIngredientRequest) -> Unit
) {
    // estado local para cantidad y unidad
    var cantidad by remember { mutableStateOf(ingredient.cantidad.toInt()) }
    var unidad by remember { mutableStateOf(ingredient.unidadMedida) }

    // opciones de unidad; ajusta a tus necesidades
    val unidades = listOf("un", "g", "kg", "ml", "l", "tsp", "tbsp", "cup")

    // para desplegar el menú
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // emoji
            Text(
                text = obtenerEmoji(ingredient.nombre.orEmpty()),
                fontSize = 24.sp
            )
            Spacer(Modifier.width(8.dp))
            // nombre
            Text(
                text = ingredient.nombre.orEmpty(),
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(8.dp))
            // decrement
            IconButton(onClick = {
                if (cantidad > 1) {
                    cantidad--
                    onUpdate(ingredient.copy(cantidad = cantidad.toDouble(), unidadMedida = unidad))
                }
            }) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = Accent)
            }
            // cantidad
            Text(
                text = "$cantidad",
                modifier = Modifier
                    .width(32.dp)
                    .wrapContentHeight(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            // increment
            IconButton(onClick = {
                cantidad++
                onUpdate(ingredient.copy(cantidad = cantidad.toDouble(), unidadMedida = unidad))
            }) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Accent)
            }
            Spacer(Modifier.width(8.dp))
            // menú de unidad
            Box {
                Text(
                    text = unidad,
                    modifier = Modifier
                        .border(BorderStroke(1.dp, Accent), RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    unidades.forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u) },
                            onClick = {
                                unidad = u
                                expanded = false
                                onUpdate(ingredient.copy(cantidad = cantidad.toDouble(), unidadMedida = unidad))
                            }
                        )
                    }
                }
            }
        }
    }
}