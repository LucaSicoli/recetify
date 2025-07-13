// app/src/main/java/com/example/recetify/ui/createRecipe/EditRecipeScreen.kt
package com.example.recetify.ui.createRecipe

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.recetify.R
import com.example.recetify.data.remote.model.*
import com.example.recetify.util.*
import com.example.recetify.util.TheMealDBImages
import kotlinx.coroutines.launch

private val Accent    = Color(0xFFBC6154)
private val GrayBg    = Color(0xFFF8F8F8)
private val Destacado = FontFamily(Font(R.font.sen_semibold, weight = FontWeight.ExtraBold))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipeId: Long,
    viewModel: CreateRecipeViewModel,
    onClose:    () -> Unit,
    onSaved:    () -> Unit,
    onPublished:() -> Unit,
) {
    val context = LocalContext.current

    // UI state
    var localMediaUri        by remember { mutableStateOf<Uri?>(null) }
    var isVideo              by remember { mutableStateOf(false) }
    var showIngredientDialog by remember { mutableStateOf(false) }
    var showStepDialog       by remember { mutableStateOf(false) }
    var loaded by rememberSaveable { mutableStateOf(false) }

    var nombre        by rememberSaveable { mutableStateOf("") }
    var descripcion   by rememberSaveable { mutableStateOf("") }
    var porciones     by rememberSaveable { mutableStateOf(1) }
    var tiempo        by rememberSaveable { mutableStateOf(15) }
    var porcionesText by rememberSaveable { mutableStateOf("1") }
    var tiempoText    by rememberSaveable { mutableStateOf("15") }

    val categories = listOf("DESAYUNO","ALMUERZO","MERIENDA","CENA","SNACK","POSTRE")
    val tiposPlato = listOf(
        "FIDEOS","PIZZA","HAMBURGUESA","ENSALADA","SOPA","PASTA",
        "ARROZ","PESCADO","CARNE","POLLO","VEGETARIANO","VEGANO",
        "SIN_TACC","RAPIDO","SALUDABLE"
    )
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedCategory  by remember { mutableStateOf(false) }
    var selectedTipo     by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedTipo     by remember { mutableStateOf(false) }

    val ingredients       = remember { mutableStateListOf<RecipeIngredientRequest>() }
    var selectedStepIndex by remember { mutableStateOf<Int?>(null) }
    val steps             = remember { mutableStateListOf<RecipeStepRequest>() }

    // ViewModel state
    val uploading     by viewModel.uploading.collectAsState()
    val draftDetail   by viewModel.draftDetail.collectAsState()
    val draftResult   by viewModel.draftSaved.collectAsState()
    val publishResult by viewModel.publishResult.collectAsState()

    // Load draft once
    LaunchedEffect(recipeId) {
        viewModel.loadDraftDetail(recipeId)
    }
    // Prefill SOLO la primera vez que llega draftDetail
    LaunchedEffect(draftDetail) {
        val d = draftDetail ?: return@LaunchedEffect
        if (!loaded) {
            loaded = true
            nombre        = d.nombre.orEmpty()
            descripcion   = d.descripcion.orEmpty()
            porciones     = d.porciones
            porcionesText = d.porciones.toString()
            tiempo        = d.tiempo.toInt()
            tiempoText    = d.tiempo.toString()
            selectedCategory = d.categoria
            selectedTipo     = d.tipoPlato

            ingredients.clear()
            ingredients.addAll(d.ingredients.map {
                RecipeIngredientRequest(
                    ingredientId = null,
                    nombre       = it.nombre.orEmpty(),
                    cantidad     = it.cantidad,
                    unidadMedida = it.unidadMedida.orEmpty()
                )
            })

            steps.clear()
            steps.addAll(d.steps.map {
                RecipeStepRequest(
                    numeroPaso  = it.numeroPaso,
                    titulo      = it.titulo.orEmpty(),
                    descripcion = it.descripcion,
                    mediaUrls   = it.mediaUrls ?: emptyList()
                )
            })

            d.mediaUrls
                ?.firstOrNull()
                ?.let { url ->
                    localMediaUri = Uri.parse(url)
                    // Detectar vídeo vs imagen:
                    isVideo = url.endsWith(".mp4", ignoreCase = true)
                            || url.endsWith(".webm", ignoreCase = true)
                }
        }
    }
    LaunchedEffect(draftResult) {
        draftResult
            ?.onSuccess {
                Toast.makeText(context, "Borrador #${it.id} actualizado", Toast.LENGTH_SHORT).show()
                // recarga el borrador para limpiar el estado local
                viewModel.loadDraftDetail(recipeId)
                onSaved()
            }
            ?.onFailure {
                Toast.makeText(context, "Error actualizando: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    LaunchedEffect(publishResult) {
        publishResult
            ?.onSuccess {
                Toast.makeText(context, "¡Publicado!", Toast.LENGTH_SHORT).show()
                onPublished()
            }
            ?.onFailure {
                Toast.makeText(context, "Error publicando: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Image pickers
    val anyLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        FileUtil.from(context, uri).also { viewModel.uploadPhoto(it) }
        context.contentResolver.getType(uri)?.let { mime ->
            isVideo       = mime.startsWith("video/")
            localMediaUri = uri
        }
    }
    // 1. Cambiar el launcher para aceptar selección múltiple y acumular archivos en mediaUrls
    val stepMediaLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val clipData = result.data?.clipData
        val uri = result.data?.data
        selectedStepIndex?.let { idx ->
            val currentList = steps[idx].mediaUrls?.toMutableList() ?: mutableListOf()
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val itemUri = clipData.getItemAt(i).uri
                    if (!currentList.contains(itemUri.toString()))
                        currentList.add(itemUri.toString())
                }
            } else if (uri != null) {
                if (!currentList.contains(uri.toString()))
                    currentList.add(uri.toString())
            }
            steps[idx] = steps[idx].copy(mediaUrls = currentList)
        }
    }
    fun openPicker() {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*","video/*"))
        }.also(anyLauncher::launch)
    }

    Scaffold { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.Black)
        ) {
            // Header media
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clickable { openPicker() }
            ) {
                when {
                    localMediaUri != null && isVideo -> VideoPlayer(localMediaUri!!)
                    localMediaUri != null             -> AsyncImage(
                        model              = localMediaUri,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize().align(Alignment.Center)
                    )
                    else -> Row(
                        Modifier
                            .align(Alignment.Center)
                            .clickable { openPicker() }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.RamenDining, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp))
                        Icon(Icons.Default.Add,        contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
                if (uploading) {
                    CircularProgressIndicator(
                        Modifier.align(Alignment.BottomCenter).padding(bottom = 56.dp),
                        color = Accent
                    )
                }
                IconButton(
                    onClick   = onClose,
                    modifier  = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .background(Color.DarkGray, CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            }

            Surface(
                Modifier.offset(y = (-32).dp).fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nombre de la receta
                    OutlinedTextField(
                        value         = nombre,
                        onValueChange = { nombre = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Nombre de la Receta", color = Color.Gray, fontFamily = Destacado) },
                        textStyle     = LocalTextStyle.current.copy(color = Color.Black),
                        shape         = RoundedCornerShape(12.dp)
                    )
                    // Descripción
                    OutlinedTextField(
                        value         = descripcion,
                        onValueChange = { descripcion = it },
                        modifier      = Modifier.fillMaxWidth().height(120.dp),
                        label         = { Text("Breve descripción…", color = Color.Gray, fontFamily = Destacado) },
                        placeholder   = { Text("", color = Color.Black) },
                        textStyle     = LocalTextStyle.current.copy(color = Color.Black),
                        maxLines      = 4,
                        shape         = RoundedCornerShape(12.dp)
                    )

                    Divider(color = Color(0xFFE0E0E0), thickness = 4.dp)

                    // Steppers
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StepperCard(
                            label       = "Porciones",
                            valueText   = porcionesText,
                            onDecrement = {
                                val v = porcionesText.toIntOrNull() ?: 0
                                if (v > 1) { porcionesText = (v - 1).toString(); porciones = v - 1 }
                            },
                            onChange    = { str ->
                                if (str.all { it.isDigit() } || str.isEmpty()) {
                                    porcionesText = str; porciones = str.toIntOrNull() ?: 0
                                }
                            },
                            onIncrement = {
                                val v = porcionesText.toIntOrNull() ?: 0
                                porcionesText = (v + 1).toString(); porciones = v + 1
                            }
                        )
                        StepperCard(
                            label       = "Tiempo (min)",
                            valueText   = tiempoText,
                            onDecrement = {
                                val v = tiempoText.toIntOrNull() ?: 0
                                if (v > 1) { tiempoText = (v - 1).toString(); tiempo = v - 1 }
                            },
                            onChange    = { str ->
                                if (str.all { it.isDigit() } || str.isEmpty()) {
                                    tiempoText = str; tiempo = str.toIntOrNull() ?: 0
                                }
                            },
                            onIncrement = {
                                val v = tiempoText.toIntOrNull() ?: 0
                                tiempoText = (v + 1).toString(); tiempo = v + 1
                            }
                        )
                        DropdownCard(
                            label      = "Categoría",
                            options    = categories,
                            selected   = selectedCategory,
                            onExpand   = { expandedCategory = true },
                            expanded   = expandedCategory,
                            onSelect   = { selectedCategory = it; expandedCategory = false }
                        )
                        DropdownCard(
                            label      = "Tipo de Plato",
                            options    = tiposPlato,
                            selected   = selectedTipo,
                            onExpand   = { expandedTipo = true },
                            expanded   = expandedTipo,
                            onSelect   = { selectedTipo = it; expandedTipo = false }
                        )
                    }

                    Divider(color = Color(0xFFE0E0E0), thickness = 4.dp)

                    // Ingredientes
                    CollapsibleSection(
                        title = "Ingredientes",
                        count = ingredients.size,
                        onAdd = { showIngredientDialog = true }
                    ) {
                        ingredients.forEachIndexed { idx, ing ->
                            val offsetX = remember { Animatable(0f) }
                            val scope = rememberCoroutineScope()
                            var dismissed by remember { mutableStateOf(false) }

                            if (!dismissed) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    if (offsetX.value < -150f) {
                                                        scope.launch {
                                                            offsetX.animateTo(-500f, animationSpec = tween(300))
                                                            dismissed = true
                                                            ingredients.removeAt(idx)
                                                        }
                                                    } else {
                                                        scope.launch { offsetX.animateTo(0f, animationSpec = tween(300)) }
                                                    }
                                                },
                                                onHorizontalDrag = { change, dragAmount ->
                                                    val newOffset = (offsetX.value + dragAmount).coerceAtMost(0f)
                                                    scope.launch { offsetX.snapTo(newOffset) }
                                                }
                                            )
                                        }
                                ) {
                                    // Fondo rojo con tacho
                                    if (offsetX.value < -20f) {
                                        Box(
                                            Modifier
                                                .matchParentSize()
                                                .background(Color(0xFFD32F2F)),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color.White,
                                                modifier = Modifier.padding(end = 24.dp)
                                            )
                                        }
                                    }

                                    // Contenido desplazable
                                    Box(
                                        Modifier
                                            .offset { IntOffset(offsetX.value.toInt(), 0) }
                                    ) {
                                        IngredientRow(idx, ing, onUpdate = { new ->
                                            ingredients[idx] = new
                                        }, onDelete = {
                                            dismissed = true
                                            ingredients.removeAt(idx)
                                        })
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color(0xFFE0E0E0), thickness = 4.dp)

                    // Instrucciones
                    CollapsibleSection(
                        title = "Instrucciones",
                        count = steps.size,
                        onAdd = {
                            steps += RecipeStepRequest(
                                numeroPaso  = steps.size + 1,
                                titulo      = "",
                                descripcion = "",
                                mediaUrls   = emptyList()
                            )
                            selectedStepIndex = steps.lastIndex
                        }
                    ) {
                        steps.forEachIndexed { idx, step ->
                            StepCard(
                                stepNumber    = step.numeroPaso,
                                title         = step.titulo.orEmpty(),
                                description   = step.descripcion,
                                mediaUrls     = step.mediaUrls ?: emptyList(),
                                onTitleChange = { new -> steps[idx] = step.copy(titulo = new) },
                                onDescChange  = { new -> steps[idx] = step.copy(descripcion = new) },
                                onAddMedia    = {
                                    selectedStepIndex = idx
                                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                        type = "*/*"
                                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*","video/*"))
                                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                    }.also(stepMediaLauncher::launch)
                                },
                                onDelete      = {
                                    steps.removeAt(idx)
                                    selectedStepIndex = null
                                    steps.forEachIndexed { i, s -> steps[i] = s.copy(numeroPaso = i+1) }
                                },
                                onRemoveMedia = { mediaIdx ->
                                    val currentList = step.mediaUrls?.toMutableList() ?: mutableListOf()
                                    if (mediaIdx in currentList.indices) {
                                        currentList.removeAt(mediaIdx)
                                        steps[idx] = step.copy(mediaUrls = currentList)
                                    }
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    viewModel.error.collectAsState().value?.let { err ->
                        Text(err, color = MaterialTheme.colorScheme.error)
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick   = {
                                val req = RecipeRequest(
                                    nombre      = nombre,
                                    descripcion = descripcion,
                                    tiempo      = tiempo,
                                    porciones   = porciones,
                                    mediaUrls   = localMediaUri?.let{ listOf(it.toString()) } ?: emptyList(),
                                    tipoPlato   = selectedTipo!!,
                                    categoria   = selectedCategory!!,
                                    ingredients = ingredients.toList(),
                                    steps       = steps.toList()
                                )
                                viewModel.syncDraftFull(recipeId, req, localMediaUri)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape    = RoundedCornerShape(24.dp),
                            border   = BorderStroke(1.dp, Color.Black),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                            enabled  = !uploading
                        ) {
                            Text("GUARDAR", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick   = {
                                // Primero sincronizar los cambios, luego publicar
                                val req = RecipeRequest(
                                    nombre      = nombre,
                                    descripcion = descripcion,
                                    tiempo      = tiempo,
                                    porciones   = porciones,
                                    mediaUrls   = localMediaUri?.let{ listOf(it.toString()) } ?: emptyList(),
                                    tipoPlato   = selectedTipo!!,
                                    categoria   = selectedCategory!!,
                                    ingredients = ingredients.toList(),
                                    steps       = steps.toList()
                                )
                                // Usar syncDraftFull y luego publicar
                                viewModel.syncDraftFullAndPublish(recipeId, req, localMediaUri)
                            },
                            modifier  = Modifier.weight(1f).height(48.dp),
                            shape     = RoundedCornerShape(24.dp),
                            colors    = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            enabled   = !uploading
                        ) {
                            Text("PUBLICAR", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
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
                                        ingredients += RecipeIngredientRequest(
                                            ingredientId = null,
                                            nombre       = ing,
                                            cantidad     = 1.0,
                                            unidadMedida = "un"
                                        )
                                        showIngredientDialog = false
                                    }
                                    .background(Color.White) // filas blancas
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Reemplazamos el emoji con la imagen de TheMealDB
                                val imageUrl = TheMealDBImages.getIngredientImageUrlSmart(ing)
                                if (imageUrl != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE6EBF2)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = ing,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    // Fallback al emoji si no hay imagen disponible
                                    Text(obtenerEmoji(ing), fontSize = 20.sp)
                                }
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
        var t by remember { mutableStateOf("") }
        var d by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showStepDialog = false },
            title = { Text("Nuevo paso") },
            text = {
                Column {
                    OutlinedTextField(value = t, onValueChange = { t = it }, placeholder = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = d, onValueChange = { d = it }, placeholder = { Text("Descripción") }, modifier = Modifier.fillMaxWidth().height(80.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    steps += RecipeStepRequest(numeroPaso = steps.size + 1, titulo = t, descripcion = d, mediaUrls = null)
                    showStepDialog = false
                }) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { showStepDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// Reuse all helper composables:

@Composable
private fun StepperCard(
    label: String,
    valueText: String,
    onDecrement: ()->Unit,
    onChange: (String)->Unit,
    onIncrement: ()->Unit
) {
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
            // Etiqueta siempre negra
            Text(
                label,
                fontFamily = Destacado,
                fontWeight = FontWeight.SemiBold,
                color      = Color.Black
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.Red)
                }
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(36.dp)
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value           = valueText,
                        onValueChange   = onChange,
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle       = LocalTextStyle.current.copy(
                            color     = Color.Black,       // aquí el color negro
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush     = SolidColor(Color.Black)
                    )
                }
                IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Green)
                }
            }
        }
    }
}

@Composable
private fun DropdownCard(
    label: String,
    options: List<String>,
    selected: String?,
    onExpand: ()->Unit,
    expanded: Boolean,
    onSelect: (String)->Unit
) {
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
            // Etiqueta siempre en negro
            Text(
                label,
                fontFamily = Destacado,
                fontWeight = FontWeight.SemiBold,
                color      = Color.Black
            )
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(GrayBg)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
                    .clickable(onClick = onExpand)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Valor seleccionado en negro
                Text(
                    selected ?: "Seleccionar",
                    fontFamily = Destacado,
                    color      = Color.Black
                )
                DropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = onExpand,
                    // <<<<<<<<<<<<<<<<< Aquí forzamos el fondo blanco
                    modifier         = Modifier.background(Color.White)
                ) {
                    options.forEach { opt ->
                        DropdownMenuItem(
                            text    = { Text(opt, color = Color.Black) },
                            onClick = { onSelect(opt) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsibleSection(
    title: String,
    count: Int,
    onAdd: ()->Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF333333))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("$title ($count)", color = Color.White, fontFamily = Destacado)
    }
    Column(Modifier.padding(vertical = 8.dp)) {
        content()
        Card(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onAdd)
                .padding(8.dp),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF006400))
                Spacer(Modifier.width(8.dp))
                val singular = when (title) {
                    "Instrucciones" -> "paso"
                    "Ingredientes"  -> "ingrediente"
                    else            -> title.removeSuffix("s").lowercase()
                }
                Text("Agregar $singular", color = Color(0xFF006400), fontFamily = Destacado)
            }
        }
    }
}

@Composable
private fun IngredientRow(
    index: Int,
    ingredient: RecipeIngredientRequest,
    onUpdate: (RecipeIngredientRequest) -> Unit,
    onDelete: () -> Unit
) {
    var cantidadText by remember { mutableStateOf(ingredient.cantidad.toInt().toString()) }
    var unidad by remember { mutableStateOf(ingredient.unidadMedida) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color(0xFFE8E8E8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen del ingrediente
            val imageUrl = TheMealDBImages.getIngredientImageUrlSmart(ingredient.nombre.orEmpty())
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = ingredient.nombre.orEmpty(),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = obtenerEmoji(ingredient.nombre.orEmpty()),
                        fontSize = 24.sp
                    )
                }
            }

            // Nombre del ingrediente
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = ingredient.nombre.orEmpty(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    fontFamily = Destacado,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                // Controles de cantidad y unidad en una fila compacta
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    // Botón decrementar
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
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Campo cantidad
                    Card(
                        modifier = Modifier.width(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        BasicTextField(
                            value = cantidadText,
                            onValueChange = { new ->
                                if (new.all { it.isDigit() } || new.isEmpty()) {
                                    cantidadText = new
                                    val parsed = new.toIntOrNull() ?: 0
                                    onUpdate(ingredient.copy(cantidad = parsed.toDouble(), unidadMedida = unidad))
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                fontFamily = Destacado
                            ),
                            cursorBrush = SolidColor(Accent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }

                    // Botón incrementar
                    IconButton(
                        onClick = {
                            val current = cantidadText.toIntOrNull() ?: 0
                            cantidadText = (current + 1).toString()
                            onUpdate(ingredient.copy(cantidad = (current + 1).toDouble(), unidadMedida = unidad))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Selector de unidad
                    Card(
                        modifier = Modifier.width(60.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                unidad,
                                color = Color.Black,
                                fontFamily = Destacado,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White, shape = RoundedCornerShape(8.dp))
                            ) {
                                listOf("un","g","kg","ml","l","tsp","tbsp","cup").forEach { u ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                u,
                                                color = Color.Black,
                                                fontFamily = Destacado,
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            unidad = u
                                            expanded = false
                                            val qty = cantidadText.toIntOrNull() ?: 0
                                            onUpdate(ingredient.copy(cantidad = qty.toDouble(), unidadMedida = unidad))
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Botón eliminar
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar ingrediente",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNumber:    Int,
    title:         String,
    description:   String,
    mediaUrls:     List<String>?,
    onTitleChange: (String) -> Unit,
    onDescChange:  (String) -> Unit,
    onAddMedia:    () -> Unit,
    onDelete:      () -> Unit,
    onRemoveMedia: ((Int) -> Unit)? = null
) {
    val firstMedia = mediaUrls?.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con número de paso y título
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Columna para el número de paso
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(22.dp)) // Espacio para alinear con el label de título

                    Card(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Accent),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$stepNumber",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = Destacado
                            )
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Campo de título expandido
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Título del paso",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280),
                        fontFamily = Destacado,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = onTitleChange,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            decorationBox = { inner ->
                                if (title.isEmpty()) {
                                    Text(
                                        "Título del paso...",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 16.sp,
                                        fontFamily = Destacado
                                    )
                                }
                                inner()
                            },
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF1F2937),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = Destacado
                            ),
                            cursorBrush = SolidColor(Accent)
                        )
                    }
                }
            }

            // Descripción con mejor diseño
            Column {
                Text(
                    text = "Instrucciones del paso",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280),
                    fontFamily = Destacado,
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    BasicTextField(
                        value = description,
                        onValueChange = onDescChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(12.dp),
                        decorationBox = { inner ->
                            if (description.isEmpty()) {
                                Text(
                                    "Describe los pasos detalladamente...",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 14.sp,
                                    fontFamily = Destacado
                                )
                            }
                            inner()
                        },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF1F2937),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontFamily = Destacado
                        ),
                        cursorBrush = SolidColor(Accent)
                    )
                }
            }

            // Sección de medios mejorada
            if (!mediaUrls.isNullOrEmpty()) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Archivos adjuntos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280),
                            fontFamily = Destacado,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.width(8.dp))

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Accent.copy(alpha = 0.1f)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Text(
                                text = "${mediaUrls.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Accent,
                                fontFamily = Destacado
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(mediaUrls.size) { i ->
                            val uri = mediaUrls[i].toUri()
                            val ctx = LocalContext.current
                            val mime = ctx.contentResolver.getType(uri).orEmpty()

                            Card(
                                modifier = Modifier.size(120.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (mime.startsWith("video/")) {
                                        VideoPlayer(uri)
                                        // Overlay para indicar que es video
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(32.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.6f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = "Video",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    // Botón eliminar mejorado
                                    Card(
                                        onClick = { onRemoveMedia?.invoke(i) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(28.dp),
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Black.copy(alpha = 0.7f)
                                        ),
                                        elevation = CardDefaults.cardElevation(2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Eliminar archivo",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Línea separadora sutil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF3F4F6))
            )

            // Botones de acción rediseñados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón agregar media
                Card(
                    onClick = onAddMedia,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Accent.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, Accent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (firstMedia == null) "Agregar" else "Más archivos",
                            color = Accent,
                            fontFamily = Destacado,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Botón eliminar paso
                Card(
                    onClick = onDelete,
                    modifier = Modifier.width(80.dp).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar paso",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
