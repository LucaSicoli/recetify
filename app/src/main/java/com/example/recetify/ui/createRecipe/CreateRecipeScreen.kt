// app/src/main/java/com/example/recetify/ui/createRecipe/CreateRecipeScreen.kt
package com.example.recetify.ui.createRecipe

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.core.net.toUri
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
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        anyLauncher.launch(intent)
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
                        thickness = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )


                    // STEPPERS
                    // ── STEPPERS (Porciones / Tiempo / Categoria / Tipo) ─────────────────
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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

                        // Divider antes de las categorías
                        Divider(
                            color = Color(0xFFE0E0E0),
                            thickness = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        // 3. Categoría
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
                                textAlign = TextAlign.Center,
                                fontFamily = Destacado
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
                                            containerColor = if (selected) Ladrillo else Color.White,
                                            contentColor = if (selected) Color.White else Color.Black
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                    ) {
                                        AutoResizeText(
                                            text = cat,
                                            fontFamily = Destacado,
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
                        if (categoriaError) {
                            Text("Selecciona una categoría", color = Color.Red, fontSize = 12.sp)
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
                                textAlign = TextAlign.Center,
                                fontFamily = Destacado
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
                                            containerColor = if (selected) Ladrillo else Color.White,
                                            contentColor = if (selected) Color.White else Color.Black
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                    ) {
                                        AutoResizeText(
                                            text = tipo,
                                            fontFamily = Destacado,
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
                        if (tipoError) {
                            Text("Selecciona un tipo de plato", color = Color.Red, fontSize = 12.sp)
                        }
                    }
// ── Divider justo después de todos los steppers ─────────────────
                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 2.dp,
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
                        thickness = 2.dp,
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
                                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                }
                                stepMediaLauncher.launch(intent)
                            },
                            onDelete      = {
                                steps.removeAt(idx)
                                selectedStepIndex = null
                                steps.forEachIndexed { i, s -> steps[i] = s.copy(numeroPaso = i + 1) }
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
                        thickness = 2.dp,
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

                    // Botones de acción mejorados
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
                            // Título de la sección
                            Text(
                                text = "¿Listo para compartir tu receta?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748),
                                fontFamily = Destacado,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Guarda como borrador o publícala para que otros la vean",
                                fontSize = 14.sp,
                                color = Color(0xFF718096),
                                fontFamily = Destacado,
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
                                        // 1️⃣ Validar campos
                                        nombreError = nombre.isBlank()
                                        descripcionError = descripcion.isBlank()
                                        categoriaError = selectedCategory == null
                                        tipoError = selectedTipo == null
                                        ingredientesError = ingredients.isEmpty()
                                        pasosError = steps.isEmpty()
                                        showFormError = nombreError || descripcionError || categoriaError || tipoError || ingredientesError || pasosError
                                        if (showFormError) return@Card

                                        // 2️⃣ Construir el RecipeRequest
                                        val request = RecipeRequest(
                                            nombre      = nombre,
                                            descripcion = descripcion,
                                            tiempo      = tiempo,
                                            porciones   = porciones,
                                            mediaUrls   = photoUrl?.let { listOf(it) } ?: emptyList(),
                                            tipoPlato   = selectedTipo!!,
                                            categoria   = selectedCategory!!,
                                            ingredients = ingredients.toList(),
                                            steps       = steps.toList()
                                        )

                                        // 3️⃣ Llamar al ViewModel para guardar borrador
                                        viewModel.saveDraftWithMedia(request, localMediaUri)
                                    },
                                    enabled = !viewModel.submitting.collectAsState().value,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp), // Aumentar altura
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White,
                                        disabledContainerColor = Color(0xFFF7F7F7)
                                    ),
                                    elevation = CardDefaults.cardElevation(6.dp),
                                    border = BorderStroke(2.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column( // Cambiar a Column para mejor layout vertical
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
                                            fontSize = 14.sp, // Reducir ligeramente el tamaño
                                            fontFamily = Destacado,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Botón PUBLICAR (Principal)
                                Card(
                                    onClick = {
                                        nombreError = nombre.isBlank()
                                        descripcionError = descripcion.isBlank()
                                        categoriaError = selectedCategory == null
                                        tipoError = selectedTipo == null
                                        ingredientesError = ingredients.isEmpty()
                                        pasosError = steps.isEmpty()
                                        showFormError = nombreError || descripcionError || categoriaError || tipoError || ingredientesError || pasosError
                                        if (showFormError) return@Card

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
                                    enabled = !viewModel.submitting.collectAsState().value,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp), // Misma altura
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Accent,
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
                                                    fontFamily = Destacado,
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
                                                    fontFamily = Destacado,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
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

@Composable
fun VideoPlayer(uri: Uri) {
    AndroidView(
        factory = { ctx ->
            // se llama sólo la primera vez
            PlayerView(ctx).apply {
                player = SimpleExoPlayer.Builder(ctx).build().also { exo ->
                    exo.setMediaItem(MediaItem.fromUri(uri))
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
