package com.example.recetify.ui.details

import android.R.id.input
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recetify.R
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.RatingResponse
import com.example.recetify.util.obtenerEmoji
import java.net.URI
import android.net.Uri
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import com.example.recetify.ui.common.LoopingVideoPlayer
import coil.compose.AsyncImage


private val Destacado = FontFamily(
    Font(R.font.sen_semibold, weight = FontWeight.ExtraBold)
)
/**
 * Selector de estrellas para puntaje (1..5).
 */
@Composable
fun StarRatingSelector(
    ratingPoints: Int,
    onRatingChanged: (Int) -> Unit
) {
    Row {
        for (i in 1..5) {
            IconButton(onClick = { onRatingChanged(i) }, modifier = Modifier.size(28.dp)) {
                if (i <= ratingPoints) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Estrella llena",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.StarBorder,
                        contentDescription = "Estrella vacía",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Muestra la sección de reseñas + formulario “Dejá tu comentario”.
 */
@Composable
fun ReviewsAndCommentSection(
    ratings: List<RatingResponse>,
    onSend: (comentario: String, puntos: Int) -> Unit
) {
    // Estado para expandir/colapsar la lista de reseñas
    var expanded by remember { mutableStateOf(false) }
    val previewCount = 2
    val displayList = if (expanded || ratings.size <= previewCount) {
        ratings
    } else {
        ratings.take(previewCount)
    }

    // Cálculo de promedio (1 decimal)
    val averageRating = remember(ratings) {
        if (ratings.isEmpty()) 0.0 else ratings.map { it.puntos }.average()
    }

    // Estado para comentario y estrellas en el formulario
    var textComment by remember { mutableStateOf(TextFieldValue("")) }
    var commentStars by remember { mutableStateOf(0) }
    val maxChars = 100

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Spacer(Modifier.height(24.dp))

        // ── Encabezado con “Reseñas” + promedio + total ───────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.9f)
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reseñas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                color = Color.Black,
                fontFamily = Destacado,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = String.format("%.1f", averageRating),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                color = Color.Black
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Estrella Dorada",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "(${ratings.size} reseñas)",
                style = MaterialTheme.typography.bodySmall, fontSize = 16.sp,
                color = Color.Gray,
                fontFamily = Destacado
            )
        }

        // ── Lista de reseñas (hasta 2 si no expandido) ────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            displayList.forEachIndexed { index, rating ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                CommentCard(rating = rating)
            }
            if (ratings.size > previewCount) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Ver menos" else "Ver más",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF042628)
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Cerrar lista" else "Abrir lista",
                        tint = Color(0xFF042628)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Card: “Dejá tu comentario” ────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Encabezado con “Dejá tu comentario” + estrellas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dejá tu comentario",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black,
                        fontFamily = Destacado
                    )
                    Spacer(Modifier.weight(1f))
                    for (i in 1..5) {
                        IconButton(
                            onClick = { commentStars = i },
                            modifier = Modifier.size(28.dp)
                        ) {
                            if (i <= commentStars) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Estrella llena",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.StarBorder,
                                    contentDescription = "Estrella vacía",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Cuadro de texto con contador de caracteres
                Box(modifier = Modifier.fillMaxWidth()) {
                    StyledBasicField(
                        value = textComment,
                        onValueChange = {
                            if (it.text.length <= maxChars) textComment = it
                        },
                        placeholder = { Text("Contanos qué te pareció la receta…", color = Color.Gray)},
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    Text(
                        text = "${textComment.text.length}/$maxChars",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color.Gray,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Botón “Enviar” alineado a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onSend(textComment.text.trim(), commentStars)
                            textComment = TextFieldValue("")
                            commentStars = 0
                        },
                        enabled = textComment.text.isNotBlank() && commentStars > 0
                    ) {
                        Text("Enviar")
                    }
                }
            }
        }
    }
}

/**
 * Composable completo para mostrar el detalle de una receta,
 * incluyendo portada, botones de Ingredientes/Instrucciones,
 * lista de ingredientes o pasos, y al final ReviewsAndCommentSection.
 */
@Composable
fun RecipeDetailContent(
    receta: RecipeResponse,
    ratings: List<RatingResponse>,
    padding: PaddingValues,
    showIngredients: MutableState<Boolean>,
    currentStep: MutableState<Int>,
    navController: NavController,
    profileUrl: String?,
    onSendRating: (comentario: String, puntos: Int) -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveEditedRecipe: (RecipeResponse) -> Unit
) {
    val primaryTextColor = Color(0xFF042628)
    val selectedButtonColor = Color(0xFF042628)
    val unselectedButtonColor = Color(0xFFE6EBF2)
    val unselectedTextColor = Color(0xFF042628)
    val ingredientCardColor = Color.White
    val ingredientIconBackground = Color(0xFFE6EBF2)
    val unitBackgroundColor = Color(0xFF995850)
    val unitTextColor = Color.White
    var showPortionDialog by remember { mutableStateOf(false) }
    var currentPortions by remember { mutableStateOf(receta.porciones) }
    var adjustedIngredients by remember { mutableStateOf(receta.ingredients) }
    var portionInput         by remember { mutableStateOf(currentPortions.toString()) }

    val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
    // normalizo igual que en Home
    // AHORA: tomamos el primer elemento de mediaUrls (o cadena vacía)
    val originalMain = receta.mediaUrls.orEmpty().firstOrNull().orEmpty()
    val pathMain = runCatching {
        val uri = URI(originalMain)
        uri.rawPath + uri.rawQuery?.let { "?$it" }.orEmpty()
    }.getOrNull() ?: originalMain
    val fullUrl = if (pathMain.startsWith("/")) "$baseUrl$pathMain" else pathMain

    val finalProfileUrl = profileUrl
        ?.let { raw ->
            val pPath = runCatching {
                val uri = URI(raw)
                uri.rawPath + uri.rawQuery?.let { "?$it" }.orEmpty()
            }.getOrDefault(raw)
            if (pPath.startsWith("/")) "$baseUrl$pPath" else raw
        }

    Column(
        modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Imagen de portada y botón Volver ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            if (fullUrl.endsWith(".mp4", ignoreCase = true) ||
                fullUrl.endsWith(".webm", ignoreCase = true)
            ) {
                // Vídeo en loop, silencio y sin controles
                LoopingVideoPlayer(
                    uri = Uri.parse(fullUrl),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Imagen estática
                AsyncImage(
                    model = fullUrl,
                    contentDescription = receta.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { onToggleFavorite() },
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Quitar favorito" else "Agregar favorito",
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }
        }

        // ── Superficie con bordes redondeados que se superpone ─────────────
        Surface(
            modifier = Modifier.offset(y = (-24).dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            tonalElevation = 0.dp
        ) {
            Column(Modifier.padding(24.dp)) {
                // Título y descripción de la receta
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = receta.nombre,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = primaryTextColor,
                        fontFamily = Destacado
                    )
                    IconButton(onClick = { showPortionDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar porciones",
                            tint = primaryTextColor
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = receta.descripcion ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryTextColor
                )

                Spacer(Modifier.height(16.dp))

                // Tiempo, creador y promedio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!finalProfileUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = finalProfileUrl,
                            contentDescription = "Avatar del chef",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Chef",
                            modifier = Modifier.size(32.dp)
                        )
                    }


                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = receta.usuarioCreadorAlias.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = receta.promedioRating?.let {
                            if (it % 1.0 == 0.0) "${it.toInt()}" else String.format("%.1f", it)
                        } ?: "–",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFe29587))
                    )

                    Spacer(Modifier.width(16.dp))

                    // dentro de RecipeDetailContent, en el Row de tiempo/creador/promedio:
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Tiempo",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${receta.tiempo} min",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.width(16.dp))
// Añadimos icono y texto de porciones:
                    Icon(
                        imageVector = Icons.Default.People, // o cualquier icono que te guste
                        contentDescription = "Porciones",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${receta.porciones} porciones",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Botones para alternar Ingredientes / Instrucciones
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(unselectedButtonColor)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Pestaña "Ingredientes"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                                .background(
                                    if (showIngredients.value)
                                        selectedButtonColor
                                    else
                                        Color.Transparent
                                )
                                .clickable {
                                    showIngredients.value = true
                                    currentStep.value = 0
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ingredientes",
                                color = if (showIngredients.value) Color.White else unselectedTextColor,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                fontSize = 14.sp,
                                fontFamily = Destacado
                            )
                        }

                        // Pestaña "Instrucciones"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                .background(
                                    if (!showIngredients.value)
                                        selectedButtonColor
                                    else
                                        Color.Transparent
                                )
                                .clickable {
                                    showIngredients.value = false
                                    currentStep.value = 0
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Instrucciones",
                                color = if (!showIngredients.value) Color.White else unselectedTextColor,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                fontSize = 14.sp,
                                fontFamily = Destacado
                            )
                        }
                    }
                }


                Spacer(Modifier.height(16.dp))

                // ── Mostrar lista de ingredientes o paso a paso ────────────────
                if (showIngredients.value) {
                    // Ingredientes
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)    // centra todo el Row
                            .fillMaxWidth(0.9f)                     // ocupa el 90% del ancho
                            .padding(vertical = 8.dp),              // un poco de espacio arriba/abajo
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ingredientes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp                // mismo tamaño que en reseñas
                            ),
                            color      = primaryTextColor,
                            fontFamily = Destacado
                        )
                        Text(
                            text = "(${receta.ingredients.size} Items)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 16.sp                // igual que en “(n reseñas)”
                            ),
                            color      = primaryTextColor,
                            fontFamily = Destacado
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    adjustedIngredients.forEach { ing ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ingredientCardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)

                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(ingredientIconBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(obtenerEmoji(ing.nombre), fontSize = 20.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = ing.nombre,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryTextColor,
                                        fontFamily = Destacado
                                    )
                                }
                                androidx.compose.material3.Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = unitBackgroundColor
                                ) {
                                    Text(
                                        text = "${ing.cantidad} ${ing.unidadMedida}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        color = unitTextColor,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Instrucciones paso a paso
                    Text(
                        text = "Instrucciones",
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryTextColor,
                        fontFamily = Destacado
                    )
                    Spacer(Modifier.height(12.dp))

                    val pasos = receta.steps.sortedBy { it.numeroPaso }
                    val lastIndex = pasos.lastIndex

                    if (currentStep.value in pasos.indices) {
                        val paso = pasos[currentStep.value]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                // Título del paso
                                Text(
                                    text = "${paso.numeroPaso}. ${paso.titulo}",
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor,
                                    fontFamily = Destacado
                                )

                                // Imagen del paso, con borde redondeado de 8dp
                                // Sólo si la lista no está vacía
                                if (!paso.mediaUrls.isNullOrEmpty()) {
                                    Spacer(Modifier.height(8.dp))

                                    // Cogemos la primera URL (o la que tú quieras)
                                    val originalStep = paso.mediaUrls!!.first()

                                    // Normalizamos la URL igual que antes
                                    val pathStep = runCatching {
                                        val uri = URI(originalStep)
                                        uri.rawPath + uri.rawQuery?.let { "?$it" }.orEmpty()
                                    }.getOrNull() ?: originalStep
                                    val stepUrl = if (pathStep.startsWith("/")) "$baseUrl$pathStep" else pathStep

                                    if (stepUrl.endsWith(".mp4", ignoreCase = true) ||
                                        stepUrl.endsWith(".webm", ignoreCase = true)
                                    ) {
                                        // Vídeo: loop, sin sonido y sin controles
                                        LoopingVideoPlayer(
                                            uri = Uri.parse(stepUrl),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        // Imagen estática
                                        AsyncImage(
                                            model = stepUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                // Descripción del paso
                                if (!paso.descripcion.isNullOrBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(paso.descripcion, color = primaryTextColor)
                                }

                                Spacer(Modifier.height(12.dp))

                                // Navegación entre pasos
                                when {
                                    // Primer paso: solo “Siguiente”
                                    currentStep.value == 0 -> {
                                        Button(
                                            onClick = { currentStep.value++ },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF042628),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text(
                                                text = "Paso Siguiente",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                fontFamily = Destacado
                                            )
                                        }
                                    }
                                    // Último paso: solo “Anterior”
                                    currentStep.value == lastIndex -> {
                                        Button(
                                            onClick = { currentStep.value-- },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF042628),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text(
                                                text = "Paso Anterior",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                fontFamily = Destacado
                                            )
                                        }
                                    }
                                    // Pasos intermedios: ambos botones
                                    else -> {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Button(
                                                onClick = { currentStep.value-- },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF042628),
                                                    contentColor = Color.White
                                                )
                                            ) {
                                                Text(
                                                    text = "Paso Anterior",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { currentStep.value++ },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF042628),
                                                    contentColor = Color.White
                                                )
                                            ) {
                                                Text(
                                                    text = "Paso Siguiente",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Sección de reseñas + comentarios ─────────────────────────────
                ReviewsAndCommentSection(
                    ratings = ratings,
                    onSend  = onSendRating
                )
            }
            if (showPortionDialog) {
                AlertDialog(
                    onDismissRequest = { showPortionDialog = false },
                    title = { Text("¿Cuántas porciones?") },
                    text = {
                        OutlinedTextField(
                            value = portionInput,
                            onValueChange = { portionInput = it.filter(Char::isDigit) },
                            label = { Text("Porciones") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Row {
                            TextButton(onClick = {
                                // recalcula y cierra
                                val newPortions = portionInput.toIntOrNull() ?: receta.porciones
                                val factor      = newPortions.toFloat() / receta.porciones
                                adjustedIngredients = receta.ingredients.map { ing ->
                                    ing.copy(cantidad = ing.cantidad * factor)
                                }
                                currentPortions   = newPortions
                                showPortionDialog = false
                            }) {
                                Text("Aplicar")
                            }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = {
                                // recalcula, cierra y guarda
                                val newPortions = portionInput.toIntOrNull() ?: receta.porciones
                                val factor      = newPortions.toFloat() / receta.porciones
                                val updatedIngredients = receta.ingredients.map { ing ->
                                    ing.copy(cantidad = ing.cantidad * factor)
                                }
                                val edited = receta.copy(
                                    porciones   = newPortions,
                                    ingredients = updatedIngredients
                                )
                                onSaveEditedRecipe(edited)
                                showPortionDialog = false
                            }) {
                                Text("Guardar")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPortionDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

