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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recetify.data.remote.model.*
import com.example.recetify.util.*
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import com.example.recetify.ui.home.Destacado
import com.example.recetify.ui.common.rememberConnectionState
import com.example.recetify.ui.common.ConnectionType
import com.example.recetify.ui.common.MobileDataWarningDialog

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

    // Detectar estado de conexión
    val connectionState by rememberConnectionState()
    var showMobileDataWarning by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Función para validar conexión antes de subir
    fun checkConnectionAndProceed(action: () -> Unit) {
        when {
            !connectionState.isConnected -> {
                // Sin conexión, mostrar error
                Toast.makeText(context, "No tienes conexión a internet", Toast.LENGTH_LONG).show()
            }
            connectionState.connectionType == ConnectionType.CELLULAR -> {
                // Solo datos móviles, mostrar advertencia
                pendingAction = action
                showMobileDataWarning = true
            }
            else -> {
                // WiFi u otra conexión, proceder directamente
                action()
            }
        }
    }

    // UI state
    var localMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isVideo by remember { mutableStateOf(false) }
    var showIngredientDialog by remember { mutableStateOf(false) }
    var showStepDialog by remember { mutableStateOf(false) }
    var loaded by rememberSaveable { mutableStateOf(false) }

    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var porciones by rememberSaveable { mutableStateOf(1) }
    var tiempo by rememberSaveable { mutableStateOf(15) }
    var porcionesText by rememberSaveable { mutableStateOf("1") }
    var tiempoText by rememberSaveable { mutableStateOf("15") }

    val categories = listOf("DESAYUNO", "ALMUERZO", "MERIENDA", "CENA", "SNACK", "POSTRE")
    val tiposPlato = listOf(
        "FIDEOS", "PIZZA", "HAMBURGUESA", "ENSALADA", "SOPA", "PASTA",
        "ARROZ", "PESCADO", "CARNE", "POLLO", "VEGETARIANO", "VEGANO",
        "SIN_TACC", "RAPIDO", "SALUDABLE"
    )
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTipo by rememberSaveable { mutableStateOf<String?>(null) }

    // Cambiar a remember simple en lugar de mutableStateListOf para mejor rendimiento
    var ingredients by remember { mutableStateOf(listOf<RecipeIngredientRequest>()) }
    var selectedStepIndex by remember { mutableStateOf<Int?>(null) }
    var steps by remember { mutableStateOf(listOf<RecipeStepRequest>()) }

    // ViewModel state
    val uploading by viewModel.uploading.collectAsState()
    val draftDetail by viewModel.draftDetail.collectAsState()
    val draftResult by viewModel.draftSaved.collectAsState()
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
            nombre = d.nombre.orEmpty()
            descripcion = d.descripcion.orEmpty()
            porciones = d.porciones
            porcionesText = d.porciones.toString()
            tiempo = d.tiempo.toInt()
            tiempoText = d.tiempo.toString()
            selectedCategory = d.categoria
            selectedTipo = d.tipoPlato

            ingredients = d.ingredients.map {
                RecipeIngredientRequest(
                    ingredientId = null,
                    nombre = it.nombre.orEmpty(),
                    cantidad = it.cantidad,
                    unidadMedida = it.unidadMedida.orEmpty()
                )
            }

            steps = d.steps.map {
                RecipeStepRequest(
                    numeroPaso = it.numeroPaso,
                    titulo = it.titulo.orEmpty(),
                    descripcion = it.descripcion,
                    mediaUrls = it.mediaUrls ?: emptyList()
                )
            }

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
                viewModel.loadDraftDetail(recipeId)
                onSaved()
            }
            ?.onFailure {
                Toast.makeText(context, "Error actualizando: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    LaunchedEffect(publishResult) {
        publishResult
            ?.onSuccess {
                Toast.makeText(context, "¡Publicado!", Toast.LENGTH_SHORT).show()
                onPublished()
            }
            ?.onFailure {
                Toast.makeText(context, "Error publicando: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    // Image pickers
    val anyLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        FileUtil.from(context, uri).also { viewModel.uploadPhoto(it) }
        context.contentResolver.getType(uri)?.let { mime ->
            isVideo = mime.startsWith("video/")
            localMediaUri = uri
        }
    }

    val stepMediaLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val clipData = result.data?.clipData
        val uri = result.data?.data
        selectedStepIndex?.let { idx ->
            if (idx < steps.size) {
                val currentStep = steps[idx]
                val currentList = currentStep.mediaUrls?.toMutableList() ?: mutableListOf()
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
                val updatedSteps = steps.toMutableList()
                updatedSteps[idx] = currentStep.copy(mediaUrls = currentList)
                steps = updatedSteps
            }
        }
    }

    fun openPicker() {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
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
            // Header media - usando VideoPlayer de CreateRecipeScreen
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clickable { openPicker() }
            ) {
                when {
                    localMediaUri != null && isVideo -> VideoPlayer(localMediaUri!!)
                    localMediaUri != null -> AsyncImage(
                        model = localMediaUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().align(Alignment.Center)
                    )

                    else -> Row(
                        Modifier
                            .align(Alignment.Center)
                            .clickable { openPicker() }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.RamenDining,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                if (uploading) {
                    CircularProgressIndicator(
                        Modifier.align(Alignment.BottomCenter).padding(bottom = 56.dp),
                        color = Color(0xFFBC6154)
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
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
                        value = nombre,
                        onValueChange = { nombre = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre de la Receta", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Descripción
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        label = { Text("Breve descripción…", color = Color.Gray) },
                        placeholder = { Text("", color = Color.Black) },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Divider(color = Color(0xFFE0E0E0), thickness = 2.dp)

                    // Steppers usando los componentes de CreateRecipeScreen
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Porciones
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Porciones",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = DarkGray
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val v = porcionesText.toIntOrNull() ?: 0
                                            if (v > 1) {
                                                porcionesText = (v - 1).toString()
                                                porciones = v - 1
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = null,
                                            tint = Red
                                        )
                                    }

                                    Box(
                                        Modifier
                                            .width(64.dp)
                                            .height(36.dp)
                                            .background(Color.White, RoundedCornerShape(6.dp))
                                            .border(
                                                1.dp,
                                                Color.LightGray,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value = porcionesText,
                                            onValueChange = { str ->
                                                if (str.all { it.isDigit() } || str.isEmpty()) {
                                                    porcionesText = str
                                                    porciones = str.toIntOrNull() ?: 0
                                                }
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle = LocalTextStyle.current.copy(
                                                color = Color.Black,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            ),
                                            cursorBrush = SolidColor(Color.Black),
                                            decorationBox = { inner ->
                                                Box(
                                                    Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) { inner() }
                                            }
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val v = porcionesText.toIntOrNull() ?: 0
                                            porcionesText = (v + 1).toString()
                                            porciones = v + 1
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Green
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Tiempo
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Tiempo (min)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = DarkGray
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            val v = tiempoText.toIntOrNull() ?: 0
                                            if (v > 1) {
                                                tiempoText = (v - 1).toString()
                                                tiempo = v - 1
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = null,
                                            tint = Red
                                        )
                                    }

                                    Box(
                                        Modifier
                                            .width(64.dp)
                                            .height(36.dp)
                                            .background(Color.White, RoundedCornerShape(6.dp))
                                            .border(
                                                1.dp,
                                                Color.LightGray,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value = tiempoText,
                                            onValueChange = { str ->
                                                if (str.all { it.isDigit() } || str.isEmpty()) {
                                                    tiempoText = str
                                                    tiempo = str.toIntOrNull() ?: 0
                                                }
                                            },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle = LocalTextStyle.current.copy(
                                                color = Color.Black,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            ),
                                            cursorBrush = SolidColor(Color.Black),
                                            decorationBox = { inner ->
                                                Box(
                                                    Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) { inner() }
                                            }
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val v = tiempoText.toIntOrNull() ?: 0
                                            tiempoText = (v + 1).toString()
                                            tiempo = v + 1
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Green
                                        )
                                    }
                                }
                            }
                        }

                        // Divider antes de las categorías
                        Divider(
                            color = Color(0xFFE0E0E0),
                            thickness = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        // 3. Categoría usando AutoResizeText de CreateRecipeScreen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF333333))
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Categoría",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Grid manual de 3 columnas para categorías
                        val categoryRows = categories.chunked(3)
                        categoryRows.forEach { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCategories.forEach { cat ->
                                    val selected = selectedCategory == cat
                                    OutlinedButton(
                                        onClick = { selectedCategory = cat },
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, Color.Black),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (selected) Color(0xFFBC6154) else Color.White,
                                            contentColor = if (selected) Color.White else Color.Black
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 4.dp,
                                            vertical = 8.dp
                                        )
                                    ) {
                                        AutoResizeText(
                                            text = cat,
                                            fontWeight = FontWeight.Medium,
                                            maxFontSize = 13.sp,
                                            minFontSize = 9.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                // Rellenar espacios vacíos en la última fila
                                repeat(3 - rowCategories.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Divider entre categoría y tipo de plato
                        Divider(
                            color = Color(0xFFE0E0E0),
                            thickness = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        // 4. Tipo de Plato
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF333333))
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Tipo de Plato",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Grid manual de 3 columnas para tipos de plato
                        val tipoRows = tiposPlato.chunked(3)
                        tipoRows.forEach { rowTipos ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowTipos.forEach { tipo ->
                                    val selected = selectedTipo == tipo
                                    OutlinedButton(
                                        onClick = { selectedTipo = tipo },
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, Color.Black),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (selected) Color(0xFFBC6154) else Color.White,
                                            contentColor = if (selected) Color.White else Color.Black
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 4.dp,
                                            vertical = 8.dp
                                        )
                                    ) {
                                        AutoResizeText(
                                            text = tipo,
                                            fontWeight = FontWeight.Medium,
                                            maxFontSize = 13.sp,
                                            minFontSize = 9.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                // Rellenar espacios vacíos en la última fila
                                repeat(3 - rowTipos.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Divider(color = Color(0xFFE0E0E0), thickness = 2.dp)

                    // Ingredientes usando IngredientRow de CreateRecipeScreen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF333333))
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ingredientes (${ingredients.size})", color = Color.White)
                    }

                    Column(Modifier.padding(vertical = 8.dp)) {
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
                                                            offsetX.animateTo(
                                                                -500f,
                                                                animationSpec = tween(300)
                                                            )
                                                            dismissed = true
                                                            val updatedIngredients =
                                                                ingredients.toMutableList()
                                                            updatedIngredients.removeAt(idx)
                                                            ingredients = updatedIngredients
                                                        }
                                                    } else {
                                                        scope.launch {
                                                            offsetX.animateTo(
                                                                0f,
                                                                animationSpec = tween(300)
                                                            )
                                                        }
                                                    }
                                                },
                                                onHorizontalDrag = { change, dragAmount ->
                                                    val newOffset =
                                                        (offsetX.value + dragAmount).coerceAtMost(0f)
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

                                    // Contenido desplazable - usando IngredientRow de CreateRecipeScreen
                                    Box(
                                        Modifier
                                            .offset { IntOffset(offsetX.value.toInt(), 0) }
                                    ) {
                                        IngredientRow(idx, ing, onUpdate = { new ->
                                            val updatedIngredients = ingredients.toMutableList()
                                            updatedIngredients[idx] = new
                                            ingredients = updatedIngredients
                                        }, onDelete = {
                                            dismissed = true
                                            val updatedIngredients = ingredients.toMutableList()
                                            updatedIngredients.removeAt(idx)
                                            ingredients = updatedIngredients
                                        })
                                    }
                                }
                            }
                        }

                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable { showIngredientDialog = true }
                                .padding(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF006400)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Agregar ingrediente",
                                    color = Color(0xFF006400),
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = Destacado
                                )
                            }
                        }
                    }

                    Divider(color = Color(0xFFE0E0E0), thickness = 2.dp)

                    // Instrucciones usando StepCard de CreateRecipeScreen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF333333))
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Instrucciones (${steps.size})", color = Color.White)
                    }

                    // Carrusel de pasos - mostrar uno a la vez
                    if (steps.isNotEmpty()) {
                        var currentStepIndex by remember { mutableStateOf(0) }

                        // Efecto para cambiar automáticamente al último paso cuando se agrega uno nuevo
                        LaunchedEffect(steps.size) {
                            if (steps.isNotEmpty()) {
                                currentStepIndex = steps.size - 1
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Indicador de página
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentStepIndex > 0) currentStepIndex--
                                    },
                                    enabled = currentStepIndex > 0
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Paso anterior",
                                        tint = if (currentStepIndex > 0) Color(0xFFBC6154) else Color.Gray
                                    )
                                }

                                Text(
                                    "Paso ${currentStepIndex + 1} de ${steps.size}",
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                IconButton(
                                    onClick = {
                                        if (currentStepIndex < steps.size - 1) currentStepIndex++
                                    },
                                    enabled = currentStepIndex < steps.size - 1
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "Paso siguiente",
                                        tint = if (currentStepIndex < steps.size - 1) Color(0xFFBC6154) else Color.Gray
                                    )
                                }
                            }

                            // Mostrar solo el paso actual
                            val currentStep = steps[currentStepIndex]
                            StepCard(
                                stepNumber = currentStep.numeroPaso,
                                title = currentStep.titulo.orEmpty(),
                                description = currentStep.descripcion,
                                mediaUrls = currentStep.mediaUrls ?: emptyList(),
                                onTitleChange = { new ->
                                    val updatedSteps = steps.toMutableList()
                                    updatedSteps[currentStepIndex] = currentStep.copy(titulo = new)
                                    steps = updatedSteps
                                },
                                onDescChange = { new ->
                                    val updatedSteps = steps.toMutableList()
                                    updatedSteps[currentStepIndex] = currentStep.copy(descripcion = new)
                                    steps = updatedSteps
                                },
                                onAddMedia = {
                                    selectedStepIndex = currentStepIndex
                                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                        type = "*/*"
                                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                    }.also(stepMediaLauncher::launch)
                                },
                                onDelete = {
                                    val updatedSteps = steps.toMutableList()
                                    updatedSteps.removeAt(currentStepIndex)
                                    // Reajustar números de paso
                                    val reindexedSteps = updatedSteps.mapIndexed { i, s ->
                                        s.copy(numeroPaso = i + 1)
                                    }
                                    steps = reindexedSteps
                                    selectedStepIndex = null
                                    // Ajustar índice currentStepIndex si es necesario
                                    if (currentStepIndex >= steps.size && steps.isNotEmpty()) {
                                        currentStepIndex = steps.size - 1
                                    } else if (steps.isEmpty()) {
                                        currentStepIndex = 0
                                    }
                                },
                                onRemoveMedia = { mediaIdx ->
                                    val currentList = currentStep.mediaUrls?.toMutableList() ?: mutableListOf()
                                    if (mediaIdx in currentList.indices) {
                                        currentList.removeAt(mediaIdx)
                                        val updatedSteps = steps.toMutableList()
                                        updatedSteps[currentStepIndex] = currentStep.copy(mediaUrls = currentList)
                                        steps = updatedSteps
                                    }
                                }
                            )
                        }
                    }

                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Limitar el número máximo de pasos para prevenir problemas de rendimiento
                                if (steps.size < 20) {
                                    val newStep = RecipeStepRequest(
                                        numeroPaso  = steps.size + 1,
                                        titulo      = "",
                                        descripcion = "",
                                        mediaUrls   = emptyList()
                                    )
                                    steps = steps + newStep
                                    selectedStepIndex = steps.size - 1
                                }
                            }
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
                            Text(
                                if (steps.size >= 20) "Máximo 20 pasos" else "Agregar paso",
                                color = if (steps.size >= 20) Color.Gray else Color(0xFF006400),
                                fontFamily = Destacado
                            )
                        }
                    }

                    viewModel.error.collectAsState().value?.let { err ->
                        Text(err, color = MaterialTheme.colorScheme.error)
                    }

                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    // Botones de acción
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "¿Listo para actualizar tu receta?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Guarda los cambios como borrador o publícala para que otros la vean",
                                fontSize = 14.sp,
                                color = Color(0xFF718096),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Botón GUARDAR (Borrador)
                                Card(
                                    onClick = {
                                        checkConnectionAndProceed {
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
                                        }
                                    },
                                    enabled = !viewModel.submitting.collectAsState().value,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White,
                                        disabledContainerColor = Color(0xFFF7F7F7)
                                    ),
                                    elevation = CardDefaults.cardElevation(6.dp),
                                    border = BorderStroke(2.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.BookmarkBorder,
                                            contentDescription = null,
                                            tint = Color(0xFF4A5568),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "GUARDAR",
                                            color = Color(0xFF4A5568),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Botón PUBLICAR (Principal)
                                Card(
                                    onClick = {
                                        checkConnectionAndProceed {
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
                                            viewModel.syncDraftFullAndPublish(recipeId, req, localMediaUri)
                                        }
                                    },
                                    enabled = !viewModel.submitting.collectAsState().value,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFBC6154),
                                        disabledContainerColor = Color(0xFFE2E8F0)
                                    ),
                                    elevation = CardDefaults.cardElevation(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val submitting = viewModel.submitting.collectAsState().value
                                        if (submitting) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    "ENVIANDO...",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        } else {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Send,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    "PUBLICAR",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo Ingrediente - usando el mismo de CreateRecipeScreen
    if (showIngredientDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val allIngredients = listaIngredientesConEmoji()
        val filteredIngredients = remember(searchQuery) {
            allIngredients.filter { it.contains(searchQuery, ignoreCase = true) }
        }

        AlertDialog(
            onDismissRequest = { showIngredientDialog = false },
            shape = RoundedCornerShape(12.dp),
            containerColor = Color.White,
            tonalElevation = 4.dp,
            title = {
                Text(
                    "Seleccionar ingrediente",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar…") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        singleLine = true
                    )
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp) // Limitar altura del diálogo
                    ) {
                        items(filteredIngredients) { ing ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newIngredient = RecipeIngredientRequest(
                                            ingredientId = null,
                                            nombre       = ing,
                                            cantidad     = 1.0,
                                            unidadMedida = "un"
                                        )
                                        ingredients = ingredients + newIngredient
                                        showIngredientDialog = false
                                    }
                                    .background(Color.White)
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                    Text(obtenerEmoji(ing), fontSize = 20.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    ing,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIngredientDialog = false }) {
                    Text("Cancelar", color = Color(0xFFBC6154))
                }
            }
        )
    }

    // Diálogo Paso - simplificado para usar solo con diálogo básico
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
                    if (steps.size < 20) {
                        val newStep = RecipeStepRequest(numeroPaso = steps.size + 1, titulo = t, descripcion = d, mediaUrls = null)
                        steps = steps + newStep
                    }
                    showStepDialog = false
                }) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { showStepDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de advertencia para datos móviles
    if (showMobileDataWarning) {
        MobileDataWarningDialog(
            onContinueWithMobileData = {
                showMobileDataWarning = false
                pendingAction?.invoke()
                pendingAction = null
            },
            onWaitForWifi = {
                showMobileDataWarning = false
                pendingAction = null
                Toast.makeText(context, "Conéctate a WiFi y vuelve a intentarlo", Toast.LENGTH_LONG).show()
            }
        )
    }
}
