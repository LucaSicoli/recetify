// app/src/main/java/com/example/recetify/ui/createRecipe/CreateRecipeScreen.kt
package com.example.recetify.ui.createRecipe

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
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
import com.example.recetify.util.TheMealDBImages
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import com.example.recetify.util.listaIngredientesConEmoji
import android.content.Intent
import android.media.browse.MediaBrowser
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.viewinterop.AndroidView
import com.example.recetify.data.remote.model.RecipeRequest
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
//import androidx.databinding.tool.Context
import kotlinx.coroutines.launch


private val Accent = Color(0xFFBC6154)
private val GrayBg  = Color(0xFFF8F8F8)
private val Ladrillo = Color(0xFFBC6154) // Nuevo color para el chip seleccionado

private val Destacado = FontFamily(
    Font(R.font.sen_semibold, weight = FontWeight.ExtraBold)
)

@Composable
fun AutoResizeText(
    text: String,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    maxFontSize: TextUnit = 13.sp,
    minFontSize: TextUnit = 9.sp,
    textAlign: TextAlign = TextAlign.Center
) {
    var textSize by remember { mutableStateOf(maxFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()
    Box(modifier = modifier) {
        Text(
            text = text,
            fontFamily = fontFamily,
            fontWeight = fontWeight,
            color = color,
            fontSize = textSize,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            textAlign = textAlign,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { layoutCoordinates ->
                    if (!readyToDraw) {
                        val boxWidth = layoutCoordinates.size.width
                        val measured = textMeasurer.measure(
                            text = text,
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
                                fontSize = textSize
                            ),
                            maxLines = 1
                        )
                        val textWidthPx = measured.size.width
                        if (textWidthPx > boxWidth && textSize > minFontSize) {
                            textSize = TextUnit(textSize.value - 1, TextUnitType.Sp)
                        } else {
                            readyToDraw = true
                        }
                    }
                }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    viewModel: CreateRecipeViewModel,
    onClose:    () -> Unit,
    onSaved:    () -> Unit,
    onPublished:() -> Unit,
) {


    // --- Local & ViewModel state ---

    var localMediaUri by remember { mutableStateOf<Uri?>(null) }
    var isVideo       by remember { mutableStateOf(false) }
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
    var selectedStepIndex by remember { mutableStateOf<Int?>(null) }
    var editingStepIndex by remember { mutableStateOf<Int?>(null) }
    val categories = listOf("DESAYUNO","ALMUERZO","MERIENDA","CENA","SNACK","POSTRE")
    val tiposPlato = listOf("FIDEOS","PIZZA","HAMBURGUESA","ENSALADA","SOPA","PASTA","ARROZ","PESCADO","CARNE","POLLO","VEGETARIANO","VEGANO","SIN_TACC","RAPIDO","SALUDABLE")
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedCategory  by remember { mutableStateOf(false) }
    var selectedTipo by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedTipo by remember { mutableStateOf(false) }
    val steps       = remember { mutableStateListOf<RecipeStepRequest>() }
    val photoUrl   by viewModel.photoUrl.collectAsState(initial = null)
    val uploading  by viewModel.uploading.collectAsState(initial = false)
    val error      by viewModel.error.collectAsState(initial = null)

    var showFormError by remember { mutableStateOf(false) }
    var nombreError by remember { mutableStateOf(false) }
    var descripcionError by remember { mutableStateOf(false) }
    var categoriaError by remember { mutableStateOf(false) }
    var tipoError by remember { mutableStateOf(false) }
    var ingredientesError by remember { mutableStateOf(false) }
    var pasosError by remember { mutableStateOf(false) }


    var showMobileDataDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }


    val submitting = viewModel.submitting.collectAsState().value


    // Image picker
    val context  = LocalContext.current
    val anyLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        // 1️⃣ convierto el URI en File y subo siempre
        val file = FileUtil.from(context, uri)
        viewModel.uploadPhoto(file)  // puedes renombrar a uploadMedia si quieres

        // 2️⃣ guardo URI y tipo para previsualizar en pantalla
        context.contentResolver.getType(uri)?.let { mime ->
            isVideo = mime.startsWith("video/")
            localMediaUri = uri
        }
    }

    var draftId by remember { mutableStateOf<Long?>(null) }
    val draftResult   by viewModel.draftSaved.collectAsState()
    val publishResult by viewModel.publishResult.collectAsState()

    // ② Ahora sí puedes usar `context` dentro de tus LaunchedEffect
    LaunchedEffect(draftResult) {
        draftResult?.onSuccess { recipe ->
            draftId = recipe.id
            Toast.makeText(context, "Receta guardada exitosamente", Toast.LENGTH_SHORT).show()
            onSaved() // Redirige a borradores
        }?.onFailure {
            Toast.makeText(context, "Error guardando borrador: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(publishResult) {
        publishResult?.onSuccess {
            Toast.makeText(context, "Receta publicada exitosamente", Toast.LENGTH_SHORT).show()
            onPublished()
        }?.onFailure {
            Toast.makeText(context, "Error publicando: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }


    val stepMediaLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val uri = result.data?.data ?: return@rememberLauncherForActivityResult
        // aquí necesitarás saber qué paso estás editando;
        // puedes guardar el índice en un estado temporal:
        selectedStepIndex?.let { idx ->
            steps[idx] = steps[idx].copy(mediaUrls = listOf(uri.toString()))
        }
    }

    fun openPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        anyLauncher.launch(intent)
    }
    fun isUsingMobileData(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    @Composable
    fun MobileDataWarningDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Conexión con datos móviles") },
            text = { Text("Estás usando datos móviles. ¿Deseas continuar?") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("No")
                }
            }
        )
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
                    .clickable { openPicker() }
            ) {
                // —— Imagen o placeholder centrados —— (primero)
                when {
                    localMediaUri != null && isVideo -> VideoPlayer(localMediaUri!!)
                    localMediaUri != null             -> AsyncImage(
                        model = localMediaUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().align(Alignment.Center),
                        contentScale = ContentScale.Crop
                    )
                    photoUrl != null                   -> AsyncImage(
                        model = photoUrl!!,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().align(Alignment.Center),
                        contentScale = ContentScale.Crop
                    )
                    else -> Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clickable { openPicker() }
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
                        onValueChange = {
                            nombre = it
                            if (showFormError) nombreError = it.isBlank()
                        },
                        label = { Text("Nombre de la Receta", color = Gray, fontFamily = Destacado) },
                        textStyle = LocalTextStyle.current.copy(color = Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = nombreError
                    )
                    if (nombreError) {
                        Text("Campo obligatorio", color = Color.Red, fontSize = 12.sp)
                    }
                    // Descripción
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = {
                            descripcion = it
                            if (showFormError) descripcionError = it.isBlank()
                        },
                        label = { Text("Breve descripción…", color = Gray, fontFamily = Destacado) },
                        placeholder = { Text("") },
                        textStyle = LocalTextStyle.current.copy(color = Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        isError = descripcionError
                    )
                    if (descripcionError) {
                        Text("Campo obligatorio", color = Color.Red, fontSize = 12.sp)
                    }

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
                        Text("Categoría", fontFamily = Destacado, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DarkGray)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                val selected = selectedCategory == cat
                                OutlinedButton(
                                    onClick = { selectedCategory = cat },
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, Color.Black),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) Ladrillo else Color.White,
                                        contentColor = if (selected) Color.White else Color.Black
                                    ),
                                    modifier = Modifier
                                        .height(44.dp)
                                        .padding(end = 8.dp, bottom = 8.dp)
                                ) {
                                    Text(
                                        cat,
                                        fontFamily = Destacado,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        if (categoriaError) {
                            Text("Selecciona una categoría", color = Color.Red, fontSize = 12.sp)
                        }

                        // 4. Tipo de Plato
                        Text("Tipo de Plato", fontFamily = Destacado, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DarkGray)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tiposPlato.forEach { tipo ->
                                val selected = selectedTipo == tipo
                                OutlinedButton(
                                    onClick = { selectedTipo = tipo },
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, Color.Black),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) Ladrillo else Color.White,
                                        contentColor = if (selected) Color.White else Color.Black
                                    ),
                                    modifier = Modifier
                                        .height(44.dp)
                                        .padding(end = 8.dp, bottom = 8.dp)
                                ) {
                                    Text(
                                        tipo,
                                        fontFamily = Destacado,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        if (tipoError) {
                            Text("Selecciona un tipo de plato", color = Color.Red, fontSize = 12.sp)
                        }
                    }
// ── Divider justo después de todos los steppers ─────────────────
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
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.padding(end = 24.dp))
                                    }
                                }
                                // Contenido desplazable
                                Box(
                                    Modifier
                                        .offset { IntOffset(offsetX.value.toInt(), 0) }
                                ) {
                                    IngredientRow(idx, ing, onUpdate = { newIng ->
                                        ingredients[idx] = newIng
                                    }, onDelete = {
                                        dismissed = true
                                        ingredients.removeAt(idx)
                                    })
                                }
                            }
                        }
                    }
                    if (ingredientesError) {
                        Text("Agrega al menos un ingrediente", color = Color.Red, fontSize = 12.sp)
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
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    type = "*/*"
                                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                                }
                                stepMediaLauncher.launch(intent)
                            },
                            onDelete      = {
                                steps.removeAt(idx)
                                selectedStepIndex = null
                                steps.forEachIndexed { i, s -> steps[i] = s.copy(numeroPaso = i + 1) }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    if (pasosError) {
                        Text("Agrega al menos un paso", color = Color.Red, fontSize = 12.sp)
                    }

// ——————————
// Editor inline para nuevo paso con contador y visor de fotos
// ——————————
                    // ——————————
// Editor inline para nuevo paso con botones compactos
// ——————————


                        // --- DIALOGO DE PREVIEW ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // 1) crea el paso vacío
                                steps += RecipeStepRequest(
                                    numeroPaso  = steps.size + 1,
                                    titulo      = "",
                                    descripcion = "",
                                    mediaUrls   = emptyList()
                                )
                                // 2) marca ese índice como “en edición”
                                selectedStepIndex = steps.lastIndex
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
                            Text("Agregar paso", color = Color(0xFF006400), fontFamily = Destacado)
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

                    // Mensaje de error general
                    if (showFormError && (nombreError || descripcionError || categoriaError || tipoError || ingredientesError || pasosError)) {
                        Text(
                            "Por favor, completá todos los campos obligatorios",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val coroutineScope = rememberCoroutineScope()
                        // GUARDAR
                        OutlinedButton(
                            onClick = {
                                nombreError = nombre.isBlank()
                                descripcionError = descripcion.isBlank()
                                categoriaError = selectedCategory == null
                                tipoError = selectedTipo == null
                                ingredientesError = ingredients.isEmpty()
                                pasosError = steps.isEmpty()
                                showFormError = nombreError || descripcionError || categoriaError || tipoError || ingredientesError || pasosError
                                if (showFormError) return@OutlinedButton

                                val request = RecipeRequest(
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    tiempo = tiempo,
                                    porciones = porciones,
                                    mediaUrls = photoUrl?.let { listOf(it) } ?: emptyList(),
                                    tipoPlato = selectedTipo!!,
                                    categoria = selectedCategory!!,
                                    ingredients = ingredients.toList(),
                                    steps = steps.toList()
                                )


                                val continuar = {
                                    coroutineScope.launch {
                                        viewModel.saveDraftWithMedia(request, localMediaUri)
                                    }
                                    Unit // <<< asegura que sea () -> Unit
                                }

                                if (isUsingMobileData(context)) {
                                    pendingAction = continuar
                                    showMobileDataDialog = true
                                } else {
                                    continuar()
                                }
                            },
                            enabled = !submitting,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, Black)
                        ) {
                            Text("GUARDAR", color = Black, fontWeight = FontWeight.Bold)
                        }

                        // PUBLICAR
                        Button(
                            onClick = {
                                nombreError = nombre.isBlank()
                                descripcionError = descripcion.isBlank()
                                categoriaError = selectedCategory == null
                                tipoError = selectedTipo == null
                                ingredientesError = ingredients.isEmpty()
                                pasosError = steps.isEmpty()
                                showFormError = nombreError || descripcionError || categoriaError || tipoError || ingredientesError || pasosError
                                if (showFormError) return@Button

                                val continuar = {
                                    coroutineScope.launch {
                                        viewModel.createRecipe(
                                            nombre = nombre,
                                            descripcion = descripcion,
                                            tiempo = tiempo,
                                            porciones = porciones,
                                            tipoPlato = selectedTipo!!,
                                            categoria = selectedCategory!!,
                                            ingredients = ingredients.toList(),
                                            steps = steps.toList(),
                                            onSuccess = onPublished
                                        )
                                    }
                                    Unit // <- asegura que la lambda sea () -> Unit
                                }

                                Log.d("NetworkCheck", "¿Datos móviles?: ${isUsingMobileData(context)}")
                                if (isUsingMobileData(context)) {
                                    pendingAction = continuar
                                    showMobileDataDialog = true
                                } else {
                                    continuar()
                                }
                            },
                            enabled = !submitting,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Black)
                        ) {
                            if (submitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
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
                            mediaUrls  = null
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
    stepNumber:    Int,
    title:         String,
    description:   String,
    mediaUrls:     List<String>?,         // ahora plural
    onTitleChange: (String) -> Unit,
    onDescChange:  (String) -> Unit,
    onAddMedia:    () -> Unit,
    onDelete:      () -> Unit           // callback para eliminar el paso
) {
    var showMediaPreview by remember { mutableStateOf(false) }
    val firstMedia = mediaUrls?.firstOrNull()

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
            // —— Número y título del paso —————————————————
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

            // —— Descripción ——————————————————————————————
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

            // —— Media placeholder fijo 120 dp ——————————————
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0))
                    .clickable { onAddMedia() },
                contentAlignment = Alignment.Center
            ) {
                if (firstMedia == null) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar foto/video",
                        modifier = Modifier.size(40.dp),
                        tint = Accent
                    )
                } else {
                    val uri = Uri.parse(firstMedia)
                    val ctx = LocalContext.current
                    val mime = ctx.contentResolver.getType(uri).orEmpty()
                    if (mime.startsWith("video/")) {
                        VideoPlayer(uri)
                    } else {
                        AsyncImage(
                            model        = uri,
                            contentDescription = null,
                            modifier     = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // —— Botones “Agregar/Cambiar media” + “Eliminar” —————
            Row(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // 1) Botón Cambiar/Agregar media
                OutlinedButton(
                    onClick        = onAddMedia,
                    modifier       = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape          = RoundedCornerShape(8.dp),
                    border         = BorderStroke(1.dp, Accent),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Accent)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text       = if (firstMedia == null) "Agregar" else "Cambiar",
                        color      = Accent,
                        fontFamily = Destacado
                    )
                }

                // 2) Botón Eliminar paso
                OutlinedButton(
                    onClick        = onDelete,
                    modifier       = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape          = RoundedCornerShape(8.dp),
                    border         = BorderStroke(1.dp, Accent),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,  // o Icons.Default.Delete si lo prefieres
                        contentDescription = null,
                        tint = Accent
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar", color = Accent, fontFamily = Destacado)
                }
            }
        }
    }

    // —— Diálogo de preview —————————————————————————
    if (showMediaPreview && firstMedia != null) {
        AlertDialog(
            onDismissRequest = { showMediaPreview = false },
            confirmButton = {
                TextButton(onClick = { showMediaPreview = false }) {
                    Text("Cerrar", color = Accent)
                }
            },
            text = {
                val uri = Uri.parse(firstMedia)
                val ctx = LocalContext.current
                val mime = ctx.contentResolver.getType(uri).orEmpty()
                if (mime.startsWith("video/")) {
                    VideoPlayer(uri)
                } else {
                    AsyncImage(
                        model        = uri,
                        contentDescription = null,
                        modifier     = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        )
    }
}

@Composable
fun VideoPlayer(uri: Uri) {
    AndroidView(
        factory = { ctx ->
            // se llama sólo la primera vez
            PlayerView(ctx).apply {
                player = SimpleExoPlayer.Builder(ctx).build().also { exo ->
                    //exo.setMediaItem(MediaBrowser.MediaItem.fromUri(uri))
                    exo.prepare()
                    exo.playWhenReady = false
                }
            }
        },
        update = { view ->
            // cada vez que cambia `uri`, reconfigura el mismo player
            view.player?.run {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
private fun IngredientRow(
    index: Int,
    ingredient: RecipeIngredientRequest,
    onUpdate: (RecipeIngredientRequest) -> Unit,
    onDelete: () -> Unit // <--- nuevo parámetro
) {
    var cantidadText by remember { mutableStateOf(ingredient.cantidad.toInt().toString()) }
    var unidad      by remember { mutableStateOf(ingredient.unidadMedida) }
    var expanded    by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape     = RoundedCornerShape(6.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reemplazamos el emoji con la imagen de TheMealDB
            val imageUrl = TheMealDBImages.getIngredientImageUrlSmart(ingredient.nombre.orEmpty())
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
                        contentDescription = ingredient.nombre.orEmpty(),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Fallback al emoji si no hay imagen disponible
                Text(
                    text       = obtenerEmoji(ingredient.nombre.orEmpty()),
                    fontSize   = 24.sp,
                    color      = Color.Black,
                    fontFamily = Destacado
                )
            }
            Spacer(Modifier.width(8.dp))

            // Nombre
            Text(
                text       = ingredient.nombre.orEmpty(),
                modifier   = Modifier.weight(1f),
                fontSize   = 16.sp,
                fontWeight = FontWeight.Medium,
                color      = Color.Black,
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
                Icon(Icons.Default.Remove, contentDescription = null, tint = Red)
            }
            Spacer(Modifier.width(4.dp))

            // Caja de cantidad editable
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(36.dp)
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value         = cantidadText,
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
                        color      = Color.Black,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                        fontFamily = Destacado
                    ),
                    cursorBrush = SolidColor(Color.Black),
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { inner() }
                    }
                )
            }
            Spacer(Modifier.width(8.dp))

            // Caja de unidad
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
                    expanded         = expanded,
                    onDismissRequest = { expanded = false },
                    // ↓ aquí forzamos el fondo blanco y un radio
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(6.dp))
                ) {
                    listOf("un","g","kg","ml","l","tsp","tbsp","cup").forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u, color = Color.Black, fontFamily = Destacado) },
                            onClick = {
                                unidad   = u
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
            // --- Tacho de basura ---
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar ingrediente", tint = Color(0xFFD32F2F))
            }
        }

    }
}
