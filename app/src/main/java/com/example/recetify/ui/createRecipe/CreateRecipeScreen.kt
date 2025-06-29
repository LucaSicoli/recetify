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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.recetify.data.remote.model.RecipeRequest
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView


private val Accent = Color(0xFFBC6154)
private val GrayBg  = Color(0xFFF8F8F8)

private val Destacado = FontFamily(
    Font(R.font.sen_semibold, weight = FontWeight.ExtraBold)
)


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
            Toast.makeText(context, "Borrador #${recipe.id} guardado", Toast.LENGTH_SHORT).show()
        }?.onFailure {
            Toast.makeText(context, "Error guardando borrador: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(publishResult) {
        publishResult?.onSuccess {
            Toast.makeText(context, "¡Borrador publicado!", Toast.LENGTH_SHORT).show()
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
                        IngredientRow(idx, ing, onUpdate = { newIng ->
                            ingredients[idx] = newIng
                        })
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

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                // 1️⃣ Validar campos (puedes extraerlo a función si quieres)
                                if (nombre.isBlank() || descripcion.isBlank()
                                    || selectedCategory == null || selectedTipo == null
                                    || ingredients.isEmpty() || steps.isEmpty()
                                ) {
                                    // aquí podrías mostrar un Toast o Snackbar de “faltan campos”
                                    return@OutlinedButton
                                }

                                // 2️⃣ Construir el RecipeRequest igual que en createRecipe
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
                            enabled   = !viewModel.submitting.collectAsState().value,
                            modifier  = Modifier
                                .weight(1f)
                                .height(48.dp),
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
                        text       = if (firstMedia == null) "Agregar media" else "Cambiar media",
                        color      = Accent,
                        fontFamily = Destacado
                    )
                }

                // 2) Botón Eliminar paso
                Button(
                    onClick        = onDelete,
                    modifier       = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape          = RoundedCornerShape(8.dp),
                    colors         = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor   = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar paso", fontFamily = Destacado)
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
    onUpdate: (RecipeIngredientRequest) -> Unit
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
            // Emoji
            Text(
                text       = obtenerEmoji(ingredient.nombre.orEmpty()),
                fontSize   = 24.sp,
                color      = Color.Black,
                fontFamily = Destacado
            )
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
                Icon(Icons.Default.Remove, contentDescription = null, tint = Color.Red)
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
        }

    }
}
