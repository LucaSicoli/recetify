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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.recetify.util.listaIngredientesConEmoji

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
    var localImageUri       by remember { mutableStateOf<Uri?>(null) }
    var showIngredientDialog by remember { mutableStateOf(false) }
    var showStepDialog       by remember { mutableStateOf(false) }
    var editingStep by remember { mutableStateOf<RecipeStepRequest?>(null) }
    var nombre      by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var porciones   by rememberSaveable { mutableStateOf(1) }
    var tiempo      by rememberSaveable { mutableStateOf(15) }

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
                        shape = RoundedCornerShape(12.dp)
                    )

                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )


                    // STEPPERS
                    val stepBg = Color.Black
                    val stepFg = Color.White

                    // STEPPERS
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Porciones
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
                                Text("Porciones",
                                    color      = Color.Black,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 16.sp,
                                    fontFamily = Destacado
                                )
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (porciones > 1) porciones-- },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Disminuir",
                                            tint = Color.Red     // signo “–” en rojo
                                        )
                                    }
                                    Box(
                                        Modifier
                                            .width(64.dp)
                                            .height(36.dp)
                                            .background(White, RoundedCornerShape(6.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value         = porciones.toString(),
                                            onValueChange = { str ->
                                                porciones = str.filter(Char::isDigit).toIntOrNull() ?: porciones
                                            },
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(
                                                color      = Color.Black,
                                                fontSize   = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign  = TextAlign.Center
                                            ),
                                            cursorBrush = SolidColor(Color.Black),
                                            decorationBox = { inner ->
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { inner() }
                                            }
                                        )
                                    }
                                    IconButton(
                                        onClick = { porciones++ },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Aumentar",
                                            tint = Color(0xFF00C853)  // signo “+” en verde
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Tiempo
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
                                Text("Tiempo (min)",
                                    color      = Color.Black,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 16.sp,
                                    fontFamily = Destacado

                                )
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (tiempo > 1) tiempo-- },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Disminuir",
                                            tint = Color.Red
                                        )
                                    }
                                    Box(
                                        Modifier
                                            .width(64.dp)
                                            .height(36.dp)
                                            .background(White, RoundedCornerShape(6.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        BasicTextField(
                                            value         = tiempo.toString(),
                                            onValueChange = { str ->
                                                tiempo = str.filter(Char::isDigit).toIntOrNull() ?: tiempo
                                            },
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(
                                                color      = Color.Black,
                                                fontSize   = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign  = TextAlign.Center
                                            ),
                                            cursorBrush = SolidColor(Color.Black),
                                            decorationBox = { inner ->
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { inner() }
                                            }
                                        )
                                    }
                                    IconButton(
                                        onClick = { tiempo++ },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Aumentar",
                                            tint = Color(0xFF00C853)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(
                        color = Color(0xFFE0E0E0),
                        thickness = 1.dp,
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
                        thickness = 1.dp,
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
                            onTitleChange = { newTitle ->
                                steps[idx] = step.copy(titulo = newTitle)
                            },
                            onDescChange  = { newDesc ->
                                steps[idx] = step.copy(descripcion = newDesc)
                            },
                            onAddPhoto    = { launcher.launch("image/*") },
                            attachments   = if (step.urlMedia != null) 1 else 0
                        )
                        Spacer(Modifier.height(8.dp))
                    }

// ——————————
// Editor inline para nuevo paso
// ——————————
                    editingStep?.let { draft ->
                        Card(
                            modifier  = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            shape     = RoundedCornerShape(12.dp),
                            colors    = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value         = draft.titulo.orEmpty(),
                                    onValueChange = { editingStep = draft.copy(titulo = it) },
                                    label         = { Text("Nombre del Paso") },
                                    singleLine    = true,
                                    modifier      = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value         = draft.descripcion,
                                    onValueChange = { editingStep = draft.copy(descripcion = it) },
                                    label         = { Text("Descripción del Paso") },
                                    modifier      = Modifier.fillMaxWidth().height(100.dp),
                                    maxLines      = 4
                                )
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { launcher.launch("image/*") },
                                        shape   = RoundedCornerShape(8.dp),
                                        border  = BorderStroke(1.dp, Accent)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Accent)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Foto", color = Accent)
                                    }
                                    Row {
                                        TextButton(onClick = { editingStep = null }) {
                                            Text("Cancelar")
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Button(onClick = {
                                            steps += draft.copy(numeroPaso = steps.size + 1)
                                            editingStep = null
                                        }) {
                                            Text("Agregar Paso")
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

// ——————————
// Botón para iniciar edición de un nuevo paso
// ——————————
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
                        thickness = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )


                    // Etiquetas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .padding(vertical = 12.dp),     // un poco más de altura
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Etiquetas",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,            // un pelín más grande
                            textAlign = TextAlign.Center,
                            fontFamily = Destacado,
                        )
                    }

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),        // más cerca del título
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp)
                    ) {
                        etiquetas.forEach { tag ->
                            val sel = selectedTags.contains(tag)
                            Text(
                                tag,
                                Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (sel) Accent.copy(alpha = 0.15f) else Color(0xFFF2F2F2))
                                    .clickable {
                                        selectedTags =
                                            if (sel) selectedTags - tag
                                            else selectedTags + tag
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (sel) Accent else Color.Black
                            )
                        }
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F2))
                                .clickable { /* nueva etiqueta */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontSize = 16.sp)
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
            var searchQuery by remember { mutableStateOf("") }
            val allIngredients = listaIngredientesConEmoji()
            // Filtramos por el texto ingresado:
            val filteredIngredients = remember(searchQuery) {
                allIngredients.filter { it.contains(searchQuery, ignoreCase = true) }
            }

            AlertDialog(
                onDismissRequest = { showIngredientDialog = false },
                title   = { Text("Seleccionar ingrediente") },
                text    = {
                    Column {
                        // 1) Campo de búsqueda
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder   = { Text("Buscar…") },
                            modifier      = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine    = true
                        )
                        // 2) Lista filtrada
                        LazyColumn {
                            items(filteredIngredients) { ing ->
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
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showIngredientDialog = false }) {
                        Text("Cancelar")
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
                    placeholder   = { Text("Nombre del Paso", color = Color.White) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
            }
            OutlinedTextField(
                value         = description,
                onValueChange = onDescChange,
                placeholder   = { Text("Descripción de los pasos…", color = Color.White) },
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