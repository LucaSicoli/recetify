package com.example.recetify.ui.details

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.RatingResponse
import com.example.recetify.ui.details.RecipeDetailViewModel
import com.example.recetify.util.obtenerEmoji

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
        // ── Encabezado con “Reseñas” + promedio + total ───────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reseñas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = String.format("%.1f", averageRating),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Black
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Estrella Dorada",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "(${ratings.size} reseñas)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
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
        Divider(
            color = Color(0xFFE0E0E0),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )

        // ── Card: “Dejá tu comentario” ────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        color = Color.Black
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
                    OutlinedTextField(
                        value = textComment,
                        onValueChange = {
                            if (it.text.length <= maxChars) textComment = it
                        },
                        placeholder = { Text("Contanos qué te pareció la receta…") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4,
                        textStyle = TextStyle(color = Color.Black)
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
    padding: PaddingValues,
    showIngredients: MutableState<Boolean>,
    currentStep: MutableState<Int>,
    navController: NavController,
    viewModel: RecipeDetailViewModel
) {
    val primaryTextColor = Color(0xFF042628)
    val selectedButtonColor = Color(0xFF042628)
    val unselectedButtonColor = Color(0xFFE6EBF2)
    val unselectedTextColor = Color(0xFF042628)
    val ingredientCardColor = Color.White
    val ingredientIconBackground = Color(0xFFE6EBF2)
    val unitBackgroundColor = Color(0xFF995850)
    val unitTextColor = Color.White

    val ratings = viewModel.ratings
    val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
    val imgPath = receta.fotoPrincipal?.removePrefix("http://localhost:8080") ?: ""
    val fullUrl = "$baseUrl$imgPath"

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
            AsyncImage(
                model = fullUrl,
                contentDescription = receta.nombre,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
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
                Text(
                    text = receta.nombre,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = primaryTextColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = receta.descripcion ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryTextColor
                )

                Spacer(Modifier.height(16.dp))

                // Tiempo, creador y promedio
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Creador",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = receta.usuarioCreadorAlias.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Promedio",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "%,.1f".format(receta.promedioRating ?: 0.0),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
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
                                fontSize = 14.sp
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
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Mostrar lista de ingredientes o paso a paso ────────────────
                if (showIngredients.value) {
                    // Ingredientes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Ingredientes",
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryTextColor
                        )
                        Text(
                            text = "${receta.ingredients.size} Items",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryTextColor
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    receta.ingredients.forEach { ing ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ingredientCardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                        color = primaryTextColor
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
                        color = primaryTextColor
                    )
                    Spacer(Modifier.height(12.dp))
                    val pasos = receta.steps.sortedBy { it.numeroPaso }
                    if (currentStep.value in pasos.indices) {
                        val paso = pasos[currentStep.value]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = "${paso.numeroPaso}. ${paso.titulo}",
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                if (!paso.urlMedia.isNullOrBlank()) {
                                    Spacer(Modifier.height(8.dp))

                                    // 1) Tomamos la URL que vino del backend:
                                    val rawUrl = paso.urlMedia!!
                                    val imageUrl = when {
                                        rawUrl.startsWith("http://localhost:8080") ->
                                            rawUrl.replaceFirst("http://localhost:8080", baseUrl)

                                        rawUrl.startsWith(baseUrl) ->
                                            rawUrl

                                        else ->
                                            rawUrl
                                    }

                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                if (!paso.descripcion.isNullOrBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(paso.descripcion, color = primaryTextColor)
                                }
                                Spacer(Modifier.height(12.dp))
                                if (currentStep.value > 0) {
                                    // A partir del segundo paso, mostramos "Anterior" y "Siguiente"
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Button(
                                            onClick = { currentStep.value-- },
                                            enabled = currentStep.value > 0,
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
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = { currentStep.value++ },
                                            enabled = currentStep.value < pasos.lastIndex,
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
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    // Solo “Siguiente” en el primer paso
                                    Button(
                                        onClick = { currentStep.value++ },
                                        enabled = currentStep.value < pasos.lastIndex,
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
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Sección de reseñas + comentarios ─────────────────────────────
                ReviewsAndCommentSection(
                    ratings = ratings,
                    onSend = { comentarioTexto, puntosElegidos ->
                        viewModel.postRating(
                            recipeId = receta.id,
                            comentario = comentarioTexto,
                            puntos = puntosElegidos
                        )
                    }
                )
            }
        }
    }
}
