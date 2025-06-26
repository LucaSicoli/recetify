// app/src/main/java/com/example/recetify/ui/createRecipe/CreateRecipeScreen.kt
package com.example.recetify.ui.createRecipe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.rememberDismissState
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import com.example.recetify.util.listaIngredientesConEmoji

private val Accent = Color(0xFFBC6154)
private val GrayBg  = Color(0xFFF8F8F8)

private val Destacado = FontFamily(
    Font(R.font.sen_semibold, weight = FontWeight.ExtraBold)
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
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
    var editingStep by remember { mutableStateOf<RecipeStepRequest?>(null) }
    var nombre      by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var porciones   by rememberSaveable { mutableStateOf(1) }
    var tiempo      by rememberSaveable { mutableStateOf(15) }
    // justo junto a tus `var porciones by rememberSaveable…` y `var tiempo by rememberSaveable…`
    var porcionesText by rememberSaveable { mutableStateOf(porciones.toString()) }
    var tiempoText    by rememberSaveable { mutableStateOf(tiempo.toString()) }

    val ingredients = remember { mutableStateListOf<RecipeIngredientRequest>() }

    val categories = listOf("DESAYUNO","ALMUERZO","MERIENDA","CENA","SNACK","POSTRE")
    val tiposPlato = listOf("FIDEOS","PIZZA","HAMBURGUESA","ENSALADA","SOPA","PASTA","ARROZ","PESCADO","CARNE","POLLO","VEGETARIANO","VEGANO","SIN_TACC","RAPIDO","SALUDABLE")

    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedCategory  by remember { mutableStateOf(false) }

    var selectedTipo by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedTipo by remember { mutableStateOf(false) }
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

    Scaffold{ innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Black)
        ) {
            // Header image (toda el área clickeable)
            // Header image (toda el área clickeable)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clickable { launcher.launch("image/*") }
            ) {
                // —— Imagen o placeholder centrados —— (primero)
                when {
                    localImageUri != null -> AsyncImage(
                        model = localImageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        contentScale = ContentScale.Crop
                    )
                    photoUrl != null -> AsyncImage(
                        model = photoUrl!!,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        contentScale = ContentScale.Crop
                    )
                    else -> Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clickable { launcher.launch("image/*") }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.RamenDining,
                            contentDescription = "Agregar foto",
                            modifier = Modifier.size(80.dp),
                            tint = Color.White
                        )
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar foto",
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                }

                // —— Indicador de subida —— (segundo)
                if (uploading) {
                    CircularProgressIndicator(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 56.dp),
                        color = Accent
                    )
                }

                // —— Botón Volver —— (último, siempre encima)
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .background(DarkGray, shape = CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
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
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre de la Receta", color = Gray, fontFamily = Destacado) },
                        textStyle = LocalTextStyle.current.copy(color = Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),

                    )
                    // Descripción
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Breve descripción…", color = Gray, fontFamily = Destacado) },
                        placeholder = { Text("") },
                        textStyle = LocalTextStyle.current.copy(color = Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                    )

                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )


                    // STEPPERS
                    val stepBg = Color.Black
                    val stepFg = Color.White

                    // STEPPERS
                    // ── STEPPERS (Porciones / Tiempo / Categoria / Tipo) ─────────────────
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Porciones
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(6.dp),
                            border   = BorderStroke(1.dp, Color.LightGray),
                            colors   = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text("Porciones",
                                    fontFamily = Destacado,
                                    fontWeight  = FontWeight.SemiBold,
                                    fontSize    = 16.sp,
                                    color       = DarkGray
                                )
                                // aquí no hay Spacer ni spacing automático
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val v = porcionesText.toIntOrNull() ?: 0
                                            if (v > 1) {
                                                porcionesText = (v - 1).toString()
                                                porciones     = v - 1
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = Red)
                                    }

                                    Box(
                                        Modifier
                                            .width(64.dp)
                                            .height(36.dp)
                                            .background(Color.White, RoundedCornerShape(6.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value           = porcionesText,
                                            onValueChange   = { str ->
                                                // solo dígitos o vacío
                                                if (str.all { it.isDigit() } || str.isEmpty()) {
                                                    porcionesText = str
                                                    porciones     = str.toIntOrNull() ?: 0
                                                }
                                            },
                                            singleLine      = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle       = LocalTextStyle.current.copy(
                                                color      = Color.Black,
                                                fontSize   = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign  = TextAlign.Center
                                            ),
                                            cursorBrush     = SolidColor(Color.Black),
                                            decorationBox   = { inner ->
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { inner() }
                                            }
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val v = porcionesText.toIntOrNull() ?: 0
                                            porcionesText = (v + 1).toString()
                                            porciones     = v + 1
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Green)
                                    }
                                }
                            }
                        }

                        // 2. Tiempo
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(6.dp),
                            border   = BorderStroke(1.dp, Color.LightGray),
                            colors   = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text("Tiempo (min)",
                                    fontFamily = Destacado,
                                    fontWeight  = FontWeight.SemiBold,
                                    fontSize    = 16.sp,
                                    color       = DarkGray
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val v = tiempoText.toIntOrNull() ?: 0
                                            if (v > 1) {
                                                tiempoText = (v - 1).toString()
                                                tiempo     = v - 1
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = Red)
                                    }

                                    Box(
                                        Modifier
                                            .width(64.dp)
                                            .height(36.dp)
                                            .background(Color.White, RoundedCornerShape(6.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value           = tiempoText,
                                            onValueChange   = { str ->
                                                if (str.all { it.isDigit() } || str.isEmpty()) {
                                                    tiempoText = str
                                                    tiempo     = str.toIntOrNull() ?: 0
                                                }
                                            },
                                            singleLine      = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle       = LocalTextStyle.current.copy(
                                                color      = Color.Black,
                                                fontSize   = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign  = TextAlign.Center
                                            ),
                                            cursorBrush     = SolidColor(Color.Black),
                                            decorationBox   = { inner ->
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { inner() }
                                            }
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val v = tiempoText.toIntOrNull() ?: 0
                                            tiempoText = (v + 1).toString()
                                            tiempo     = v + 1
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Green)
                                    }
                                }
                            }
                        }

                        // 3. Categoría
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(6.dp),
                            colors    = CardDefaults.cardColors(containerColor = Color.White),
                            border    = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text("Categoría",
                                    fontFamily = Destacado,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 16.sp,
                                    color = DarkGray
                                )
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GrayBg)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                                        .clickable { expandedCategory = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(selectedCategory ?: "Seleccionar", color = DarkGray, fontFamily = Destacado)
                                    DropdownMenu(
                                        expanded = expandedCategory,
                                        onDismissRequest = { expandedCategory = false }
                                    ) {
                                        categories.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    selectedCategory = cat
                                                    expandedCategory = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Tipo de Plato
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = RoundedCornerShape(6.dp),
                            colors    = CardDefaults.cardColors(containerColor = Color.White),
                            border    = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text("Tipo de Plato",
                                    fontFamily = Destacado,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 16.sp,
                                    color = DarkGray
                                )
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GrayBg)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                                        .clickable { expandedTipo = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(selectedTipo ?: "Seleccionar", color = DarkGray, fontFamily = Destacado)
                                    DropdownMenu(
                                        expanded = expandedTipo,
                                        onDismissRequest = { expandedTipo = false }
                                    ) {
                                        tiposPlato.forEach { tipo ->
                                            DropdownMenuItem(
                                                text = { Text(tipo) },
                                                onClick = {
                                                    selectedTipo = tipo
                                                    expandedTipo = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
// ── Divider justo después de todos los steppers ─────────────────────
                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    // Ingredientes header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF333333))
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ingredientes (${ingredients.size})",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = Destacado,

                        )
                    }
                    // Ingredientes list
                    ingredients.forEachIndexed { idx, ing ->
                        IngredientRow(
                            index = idx,
                            ingredient = ing,
                            onUpdate = { newIng -> ingredients[idx] = newIng },
                            onDelete = { ingredients.removeAt(idx) }
                        )
                    }

                    // Agregar ingrediente
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showIngredientDialog = true }
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        shape     = RoundedCornerShape(12.dp),
                        colors    = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF006400))
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar ingrediente", color = Color(0xFF006400), fontWeight = FontWeight.Medium, fontFamily = Destacado,)
                        }
                    }

                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )


                    // Instrucciones header
                    // Instrucciones header (nuevo estilo)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF333333))
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Instrucciones (${steps.size})",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = Destacado,

                        )
                    }
//                    steps.forEachIndexed { idx, step ->
//                        // launcher específico para este paso
//                        val stepImageLauncher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
//                            uri?.let {
//                                // sólo actualizamos la foto de este paso
//                                steps[idx] = step.copy(urlMedia = it.toString())
//                            }
//                        }
//
//                        StepCard(
//                            stepNumber    = step.numeroPaso,
//                            title         = step.titulo.orEmpty(),
//                            description   = step.descripcion,
//                            urlMedia      = step.urlMedia,         // <<< aquí
//                            onTitleChange = { steps[idx] = step.copy(titulo = it) },
//                            onDescChange  = { steps[idx] = step.copy(descripcion = it) },
//                            onAddPhoto    = { stepImageLauncher.launch("image/*") }
//                        )
//                        Spacer(Modifier.height(8.dp))
//                    }
                    steps.forEachIndexed { idx, step ->
                        val stepImageLauncher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
                            uri?.let {
                                steps[idx] = step.copy(urlMedia = it.toString())
                            }
                        }

                        val dismissState = rememberDismissState(
                            confirmStateChange = {
                                if (it == DismissValue.DismissedToStart) {
                                    steps.removeAt(idx)
                                    true
                                } else false
                            }
                        )

                        SwipeToDismiss(
                            state = dismissState,
                            directions = setOf(DismissDirection.EndToStart),
                            background = {
                                if (dismissState.dismissDirection != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Red)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                                    }
                                }
                            },
                            dismissContent = {
                                StepCard(
                                    stepNumber    = step.numeroPaso,
                                    title         = step.titulo.orEmpty(),
                                    description   = step.descripcion,
                                    urlMedia      = step.urlMedia,
                                    onTitleChange = { steps[idx] = step.copy(titulo = it) },
                                    onDescChange  = { steps[idx] = step.copy(descripcion = it) },
                                    onAddPhoto    = { stepImageLauncher.launch("image/*") }
                                )
                            }
                        )

                        Spacer(Modifier.height(8.dp))
                    }


// ——————————
// Editor inline para nuevo paso con contador y visor de fotos
// ——————————
                    editingStep?.let { draft ->
                        var showImagePreview by remember { mutableStateOf(false) }

                        // ← Aquí, justo antes de tu Card, declara el launcher de paso:
                        val stepImageLauncher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
                            uri?.let {
                                // Actualiza sólo el draft de este paso, manteniendo el resto intacto
                                editingStep = draft.copy(urlMedia = it.toString())
                            }
                        }

                        Card(
                            modifier  = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape     = RoundedCornerShape(12.dp),
                            colors    = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // --- TÍTULO ---
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8F8F8), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                                        .padding(12.dp)
                                ) {
                                    BasicTextField(
                                        value         = draft.titulo.orEmpty(),
                                        onValueChange = { editingStep = draft.copy(titulo = it) },
                                        singleLine    = true,
                                        textStyle     = LocalTextStyle.current.copy(
                                            color      = Color.Black,
                                            fontFamily = Destacado,
                                            fontSize   = 16.sp
                                        ),
                                        decorationBox = { inner ->
                                            if (draft.titulo.isNullOrEmpty()) {
                                                Text("Nombre del Paso", color = Gray, fontFamily = Destacado)
                                            }
                                            inner()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // --- DESCRIPCIÓN ---
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .background(Color(0xFFF8F8F8), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                                        .padding(12.dp)
                                ) {
                                    BasicTextField(
                                        value         = draft.descripcion,
                                        onValueChange = { editingStep = draft.copy(descripcion = it) },
                                        textStyle     = LocalTextStyle.current.copy(
                                            color      = Color.Black,
                                            fontFamily = Destacado,
                                            fontSize   = 14.sp
                                        ),
                                        decorationBox = { inner ->
                                            if (draft.descripcion.isEmpty()) {
                                                Text("Descripción del Paso", color = Gray, fontFamily = Destacado)
                                            }
                                            inner()
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                if (!draft.urlMedia.isNullOrBlank()) {
                                    Column(
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Imagen cargada:",
                                            color = Color.Gray,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = Destacado
                                        )
                                        AsyncImage(
                                            model = draft.urlMedia,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                // --- AVISO DE FOTOS ---
                                draft.urlMedia?.let { url ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Fotos: 1",
                                            fontFamily = Destacado,
                                            color = Accent
                                        )
                                        IconButton(onClick = { showImagePreview = true }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_chef_hat),
                                                contentDescription = "Ver foto",
                                                tint = Accent
                                            )
                                        }
                                    }
                                }

                                // --- BOTONES ---
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { stepImageLauncher.launch("image/*") },
                                        shape   = RoundedCornerShape(8.dp),
                                        border  = BorderStroke(1.dp, Accent),
                                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = Accent)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Foto", fontFamily = Destacado)
                                    }

                                    TextButton(onClick = { editingStep = null }) {
                                        Text("Cancelar", color = Accent, fontFamily = Destacado)
                                    }

                                    Button(
                                        onClick = {
                                            steps += draft.copy(numeroPaso = steps.size + 1)
                                            editingStep = null
                                        },
                                        shape  = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                                    ) {
                                        Text("Agregar", color = Color.White, fontFamily = Destacado)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))



                        // --- DIALOGO DE PREVIEW ---
                        if (showImagePreview) {
                            AlertDialog(
                                onDismissRequest = { showImagePreview = false },
                                confirmButton = {
                                    TextButton(onClick = { showImagePreview = false }) {
                                        Text("Cerrar", color = Accent)
                                    }
                                },
                                text = {
                                    AsyncImage(
                                        model = draft.urlMedia,
                                        contentDescription = "Foto del paso",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            )
                        }
                    }
                    if (editingStep == null) {
                        Card(
                            modifier  = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingStep = RecipeStepRequest(
                                        numeroPaso = steps.size + 1,
                                        titulo     = "",
                                        descripcion= "",
                                        urlMedia   = null
                                    )
                                }
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            shape     = RoundedCornerShape(12.dp),
                            colors    = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF006400))
                                Spacer(Modifier.width(8.dp))
                                Text("Agregar paso", color = Color(0xFF006400), fontWeight = FontWeight.Medium, fontFamily = Destacado)
                            }
                        }
                    }

                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )



                    error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // GUARDAR (opcional: local, borrador…)
                        OutlinedButton(
                            onClick   = onSaved,
                            enabled   = !viewModel.submitting.collectAsState().value,
                            modifier  = Modifier.weight(1f).height(48.dp),
                            shape     = RoundedCornerShape(24.dp),
                            border    = BorderStroke(1.dp, Black)
                        ) {
                            Text("GUARDAR", color = Black, fontWeight = FontWeight.Bold)
                        }

                        // PUBLICAR (esto es lo que dispara la petición REST)
                        Button(
                            onClick = {
                                // valida que todos los campos obligatorios estén llenos
                                if (nombre.isBlank() || descripcion.isBlank()
                                    || selectedCategory == null || selectedTipo == null
                                    || ingredients.isEmpty() || steps.isEmpty()
                                ) {
                                    // muestra algún error al usuario…
                                    return@Button
                                }
                                viewModel.createRecipe(
                                    nombre      = nombre,
                                    descripcion = descripcion,
                                    tiempo      = tiempo,
                                    porciones   = porciones,
                                    tipoPlato   = selectedTipo!!,
                                    categoria   = selectedCategory!!,
                                    ingredients = ingredients.toList(),
                                    steps       = steps.toList(),
                                    onSuccess   = onPublished
                                )
                            },
                            enabled   = !viewModel.submitting.collectAsState().value,
                            modifier  = Modifier.weight(1f).height(48.dp),
                            shape     = RoundedCornerShape(24.dp),
                            colors    = ButtonDefaults.buttonColors(containerColor = Black)
                        ) {
                            val submitting = viewModel.submitting.collectAsState().value
                            if (submitting) {
                                CircularProgressIndicator(
                                    modifier   = Modifier.size(20.dp),
                                    color      = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("PUBLICAR", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }
        }

        // Dialogo Ingrediente
        if (showIngredientDialog) {
            var searchQuery by remember { mutableStateOf("") }
            val allIngredients = listaIngredientesConEmoji()
            val filteredIngredients = remember(searchQuery) {
                allIngredients.filter { it.contains(searchQuery, ignoreCase = true) }
            }

            AlertDialog(
                onDismissRequest = { showIngredientDialog = false },
                // Le ponemos un shape y fondo blanco
                shape = RoundedCornerShape(12.dp),
                containerColor = Color.White,
                tonalElevation = 4.dp,
                // El texto y los botones en negro
                title = {
                    Text(
                        "Seleccionar ingrediente",
                        color = Color.Black,
                        fontFamily = Destacado,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White) // opcional, para asegurarnos
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar…", fontFamily = Destacado) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true
                        )
                        LazyColumn {
                            items(filteredIngredients) { ing ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            ingredients.add(
                                                RecipeIngredientRequest(
                                                    ingredientId = null,
                                                    nombre       = ing,
                                                    cantidad     = 1.0,
                                                    unidadMedida = "un"
                                                )
                                            )

                                            showIngredientDialog = false
                                        }
                                        .background(Color.White) // filas blancas
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(obtenerEmoji(ing), fontSize = 20.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        ing,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontFamily = Destacado
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showIngredientDialog = false }) {
                        Text("Cancelar", color = Accent)
                    }
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

// Componente tarjeta de paso
@Composable
private fun StepCard(
    stepNumber:   Int,
    title:        String,
    description:  String,
    urlMedia:     String?,           // ahora recibe la URL
    onTitleChange:(String)->Unit,
    onDescChange: (String)->Unit,
    onAddPhoto:   ()->Unit
) {
    var showImagePreview by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape     = RoundedCornerShape(6.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // —— Número y título
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(24.dp)
                        .background(Accent.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$stepNumber", color = Accent, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F8F8), RoundedCornerShape(6.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    BasicTextField(
                        value         = title,
                        onValueChange = onTitleChange,
                        singleLine    = true,
                        textStyle     = LocalTextStyle.current.copy(
                            color      = Color.Black,
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        decorationBox = { inner ->
                            if (title.isEmpty()) Text("Nombre del Paso", color = Color.Gray)
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // —— Descripción
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFF8F8F8), RoundedCornerShape(6.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value         = description,
                    onValueChange = onDescChange,
                    textStyle     = LocalTextStyle.current.copy(
                        color    = Color.Black,
                        fontSize = 14.sp
                    ),
                    decorationBox = { inner ->
                        if (description.isEmpty()) Text("Descripción de los pasos…", color = Color.Gray)
                        inner()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // —— Mini‐preview si hay foto
            urlMedia?.let {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Imagen paso:", color = Color.Gray, fontSize = 12.sp)
                    AsyncImage(
                        model = it,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showImagePreview = true },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // —— Botones foto / ver foto
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
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Accent)
                    Spacer(Modifier.width(4.dp))
                    Text("FOTO", color = Accent, fontFamily = Destacado)
                }

                urlMedia?.let {
                    TextButton(onClick = { showImagePreview = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chef_hat),
                            contentDescription = "Ver foto",
                            tint = Accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Ver foto", color = Accent, fontFamily = Destacado)
                    }
                }
            }
        }
    }

    // —— diálogo de preview
    if (showImagePreview) {
        AlertDialog(
            onDismissRequest = { showImagePreview = false },
            confirmButton = {
                TextButton(onClick = { showImagePreview = false }) {
                    Text("Cerrar", color = Accent)
                }
            },
            text = {
                AsyncImage(
                    model = urlMedia,
                    contentDescription = "Foto del paso",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        )
    }
}

// ── Fila de ingrediente: emoji, nombre, – / cantidad / + y menú de unidades ────────────────
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IngredientRow(
    index: Int,
    ingredient: RecipeIngredientRequest,
    onUpdate: (RecipeIngredientRequest) -> Unit,
    onDelete: () -> Unit
) {
    var cantidadText by remember { mutableStateOf(ingredient.cantidad.toInt().toString()) }
    var unidad by remember { mutableStateOf(ingredient.unidadMedida) }
    var expanded by remember { mutableStateOf(false) }

    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToStart) {
                onDelete()
            }
            true
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            // ✅ Solo se muestra el fondo rojo si realmente se está deslizando
            if (dismissState.dismissDirection != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                }
            }
        },
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji (asumí que tenés esta función en tu proyecto)
                    Text(
                        text = obtenerEmoji(ingredient.nombre.orEmpty()),
                        fontSize = 24.sp,
                        color = Color.Black,
                        fontFamily = Destacado
                    )
                    Spacer(Modifier.width(8.dp))

                    // Nombre ingrediente
                    Text(
                        text = ingredient.nombre.orEmpty(),
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        fontFamily = Destacado
                    )
                    Spacer(Modifier.width(8.dp))

                    // Botón “−”
                    IconButton(
                        onClick = {
                            val current = cantidadText.toIntOrNull() ?: 1
                            if (current > 1) {
                                cantidadText = (current - 1).toString()
                                onUpdate(ingredient.copy(cantidad = (current - 1).toDouble(), unidadMedida = unidad))
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = Color.Red)
                    }
                    Spacer(Modifier.width(4.dp))

                    // Caja editable cantidad
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(36.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = cantidadText,
                            onValueChange = { new ->
                                if (new.all(Char::isDigit)) {
                                    cantidadText = new
                                    val parsed = new.toIntOrNull() ?: 0
                                    onUpdate(ingredient.copy(cantidad = parsed.toDouble(), unidadMedida = unidad))
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontFamily = Destacado
                            ),
                            cursorBrush = SolidColor(Color.Black),
                            decorationBox = { inner ->
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { inner() }
                            }
                        )
                    }
                    Spacer(Modifier.width(8.dp))

                    // Selector unidad
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(36.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                            .clickable { expanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(unidad, color = Color.Black, fontFamily = Destacado)

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White, shape = RoundedCornerShape(6.dp))
                        ) {
                            listOf("un", "g", "kg", "ml", "l", "tsp", "tbsp", "cup").forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u, color = Color.Black, fontFamily = Destacado) },
                                    onClick = {
                                        unidad = u
                                        expanded = false
                                        val qty = cantidadText.toIntOrNull() ?: 0
                                        onUpdate(ingredient.copy(cantidad = qty.toDouble(), unidadMedida = unidad))
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(4.dp))

                    // Botón “+”
                    IconButton(
                        onClick = {
                            val current = cantidadText.toIntOrNull() ?: 0
                            cantidadText = (current + 1).toString()
                            onUpdate(ingredient.copy(cantidad = (current + 1).toDouble(), unidadMedida = unidad))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00C853))
                    }
                }
            }
        }
    )
}