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
import com.example.recetify.util.TheMealDBImages
import java.net.URI
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.LocalTextStyle

import androidx.compose.ui.graphics.SolidColor

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.example.recetify.ui.theme.Ladrillo
import kotlin.math.floor
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.shadow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.*

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.pager.HorizontalPagerIndicator


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
    onSend: (comentario: String, puntos: Int) -> Unit,
    isAlumno: Boolean,
    shakeComment: Boolean = false,
    commentError: Boolean = false
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

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeComment) {
        if (shakeComment) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 500
                    for (i in 0..4) {
                        (if (i % 2 == 0) -16f else 16f) at (i * 100)
                    }
                    0f at 500
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFF0F0F0),
                    shape = RoundedCornerShape(8.dp)
                )
                .defaultMinSize(minHeight = 64.dp)     // al menos 56dp de alto
                .padding(horizontal = 16.dp),           // sólo padding horizontal
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Text(
                text = "Reseñas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                ),
                color      = Color.Black,
                fontFamily = Destacado
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = averageRating.formatSmart(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 16.sp
                    ),
                    color      = Color.Black,
                    fontFamily = Destacado
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Estrella Dorada",
                    tint         = Color(0xFFFFD700),
                    modifier     = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "(${ratings.size} ${if (ratings.size == 1) "reseña" else "reseñas"})",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp),
                    color = Color.Gray,
                    fontFamily = Destacado
                )
            }
        }

        // ── Lista de reseñas (hasta 2 si no expandido) ────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            displayList.forEachIndexed { index, rating ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                // Reducimos apenas el ancho para que la sombra no se corte
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
                    CommentCard(rating = rating)
                }
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
        val screenWidth = LocalConfiguration.current.screenWidthDp
        val starSize = when {
            screenWidth < 340 -> 24.dp
            screenWidth < 400 -> 28.dp
            else -> 32.dp
        }
        val commentFontSize = when {
            screenWidth < 340 -> 16.sp
            screenWidth < 400 -> 18.sp
            else -> 20.sp
        }
        val cardPadding = when {
            screenWidth < 340 -> 8.dp
            screenWidth < 400 -> 12.dp
            else -> 16.dp
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(cardPadding)) {
                // 1. Título
                Text(
                    text = "Dejanos tu opinión",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = Color.Black,
                    fontFamily = Destacado,
                    fontSize = commentFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 10.dp) // Menos espacio debajo del título
                )
                // 2. Selector de estrellas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp), // Sin padding extra
                    horizontalArrangement = Arrangement.Center, // Centrado y más compacto
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { commentStars = i },
                            modifier = Modifier.size(starSize).padding(horizontal = 2.dp) // Menos separación
                        ) {
                            Icon(
                                imageVector = if (i <= commentStars) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = if (i <= commentStars) "Estrella llena" else "Estrella vac��a",
                                tint = if (i <= commentStars) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier.size(starSize)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp)) // Menos espacio entre estrellas y caja de texto
                // 3. Caja de texto más compacta
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp) // Aumenta la altura de la caja de comentario
                        .offset(x = shakeOffset.value.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        StyledBasicField(
                            value = textComment,
                            onValueChange = {
                                if (it.text.length <= maxChars) textComment = it
                            },
                            placeholder = null,
                            maxLines = 8, // Permite más líneas visibles
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp)) // Menos espacio antes del botón
                // 4. Botón centrado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            onSend(textComment.text.trim(), commentStars)
                            textComment = TextFieldValue("")
                            commentStars = 0
                        },
                        enabled = textComment.text.isNotBlank() && commentStars > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBC6154), // color accent
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "ENVIAR RESEÑA",
                            fontFamily    = Destacado,
                            fontWeight    = FontWeight.Bold,
                        )
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
    onSaveEditedRecipe: (RecipeResponse) -> Unit,
    isAlumno: Boolean,
    from: String? = null,
    onNavigateWithLoading: ((String) -> Unit)? = null // <-- nuevo callback
) {
    val primaryTextColor = Color(0xFF042628)
    val selectedButtonColor = Color(0xFF042628)
    val unselectedButtonColor = Color(0xFFE6EBF2)
    val unselectedTextColor = Color.Gray
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

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val pasoFontSize = when {
        screenWidth < 340 -> 16.sp
        screenWidth < 400 -> 16.sp
        else -> 16.sp
        }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var baritaRoja by remember { mutableStateOf(false) }
    var shakeComment by remember { mutableStateOf(false) }
    var commentError by remember { mutableStateOf(false) }

    val baritaShakeOffset = remember { Animatable(0f) }
    LaunchedEffect(baritaRoja) {
        if (baritaRoja) {
            baritaShakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 500
                    for (i in 0..4) {
                        (if (i % 2 == 0) -8f else 8f) at (i * 100)
                    }
                    0f at 500
                }
            )
        }
    }

    Box {
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Carrusel de portada y botón Volver ────────��──────────────────────
            val mediaList = receta.mediaUrls.orEmpty().map { url ->
                val path = runCatching {
                    val uri = URI(url)
                    uri.rawPath + uri.rawQuery?.let { "?${it}" }.orEmpty()
                }.getOrNull() ?: url
                if (path.startsWith("/")) "$baseUrl$path" else path
            }
            val pagerState = rememberPagerState()
            if (mediaList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    HorizontalPager(
                        count = mediaList.size,
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val url = mediaList[page]
                        if (url.endsWith(".mp4", ignoreCase = true) || url.endsWith(".webm", ignoreCase = true)) {
                            LoopingVideoPlayer(
                                uri = Uri.parse(url),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            AsyncImage(
                                model = url,
                                contentDescription = receta.nombre,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    // Indicador de páginas
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp),
                        activeColor = Color.White,
                        inactiveColor = Color.LightGray
                    )
                    // Botón volver en el carrusel de imágenes
                    IconButton(
                        onClick = {
                            if (onNavigateWithLoading != null) {
                                if (from == "search") {
                                    onNavigateWithLoading("search")
                                } else {
                                    onNavigateWithLoading("home")
                                }
                            } else {
                                if (from == "search") {
                                    navController.popBackStack("search", inclusive = false)
                                    navController.navigate("search")
                                } else {
                                    navController.popBackStack("home", inclusive = false)
                                    navController.navigate("home")
                                }
                            }
                        },
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
                    // Botón favorito
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
                // Autoplay: avanzar cada 3 segundos
                LaunchedEffect(pagerState.currentPage, mediaList.size) {
                    if (mediaList.size > 1) {
                        delay(3000)
                        val nextPage = (pagerState.currentPage + 1) % mediaList.size
                        pagerState.animateScrollToPage(nextPage)
                    }
                }
            } else {
                // Fallback si no hay media
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Receta sin foto principal",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
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
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = receta.nombre,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF042628),
                            fontFamily = Destacado,
                            maxLines = 3,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(end = 48.dp) // deja espacio para el lápiz
                        )
                        IconButton(
                            onClick = {
                                if (!isAlumno) {
                                    baritaRoja = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "No se puede personalizar una receta en modo invitado",
                                            duration = SnackbarDuration.Short
                                        )
                                        kotlinx.coroutines.delay(3000)
                                        baritaRoja = false
                                    }
                                } else {
                                    showPortionDialog = true
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = baritaShakeOffset.value.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Ajustar porciones/ingredientes",
                                tint = if (baritaRoja) Color.Red else Color(0xFF042628)
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
                            text = receta.promedioRating?.formatSmart() ?: "–",
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

                        Spacer(Modifier.width(10.dp))
// Añadimos icono y texto de porciones:
                        Icon(
                            imageVector = Icons.Default.People, // o cualquier icono que te guste
                            contentDescription = "Porciones",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$currentPortions ${if (currentPortions == 1) "porción" else "porciones"}",
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
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth()
                                // 1) fondo gris claro y esquinas redondeadas
                                .background(
                                    color = Color(0xFFF0F0F0),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                // 2) padding interno para separar del borde
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .height(40.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment   = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ingredientes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 16.sp
                                ),
                                color      = primaryTextColor,
                                fontFamily = Destacado
                            )
                            Text(
                                text = "(${receta.ingredients.size} ${if (receta.ingredients.size == 1) "item" else "items"})",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 16.sp
                                ),
                                color = Color.Gray,
                                fontFamily = Destacado
                            )
                        }
                        Spacer(Modifier.height(4.dp)) // Reducido de 8.dp a 4.dp para menor espacio
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
                                        // Solo la imagen, sin background circular
                                        val imageUrl = TheMealDBImages.getIngredientImageUrlSmart(ing.nombre)
                                        if (imageUrl != null) {
                                            AsyncImage(
                                                model = imageUrl,
                                                contentDescription = ing.nombre,
                                                modifier = Modifier.size(40.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
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
                                            text = "${ing.cantidad.formatSmart()} ${ing.unidadMedida}",
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
                        val pasos = receta.steps.sortedBy { it.numeroPaso }
                        val lastIndex = pasos.lastIndex
                        val mostrarNavegacion = pasos.size > 1

                        // Selector de pasos arriba del card, igual que en CreateRecipeScreen
                        if (pasos.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // primero pones el fondo con esquinas redondeadas
                                    .background(
                                        color = Color(0xFFF0F0F0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    // luego el padding interior
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { if (currentStep.value > 0) currentStep.value-- },
                                    enabled = currentStep.value > 0,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Anterior")
                                }

                                Spacer(Modifier.width(16.dp))

                                Text(
                                    text = "Paso ${currentStep.value + 1} de ${pasos.size}",
                                    fontSize = pasoFontSize,
                                    fontFamily = Destacado,
                                    color = Color(0xFF6B7280)
                                )

                                Spacer(Modifier.width(16.dp))

                                IconButton(
                                    onClick = { if (currentStep.value < pasos.lastIndex) currentStep.value++ },
                                    enabled = currentStep.value < pasos.lastIndex,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Siguiente")
                                }
                            }
                        }

                        if (currentStep.value in pasos.indices) {
                            val paso = pasos[currentStep.value]
                            // --- StepCard igual que en CreateRecipeScreen ---
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(1.dp), // Bajado de 6.dp a 5.dp
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
                                            Spacer(Modifier.height(22.dp))
                                            // Botón redondo solo con flecha, sin Card interna
                                            Card(
                                                modifier = Modifier.size(48.dp),
                                                shape = CircleShape,
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFBC6154)),
                                                elevation = CardDefaults.cardElevation(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${paso.numeroPaso}",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 18.sp,
                                                        fontFamily = Destacado
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        // Campo de título expandido (solo lectura)
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
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color.White,
                                                shadowElevation = 1.dp
                                            ) {
                                                Text(
                                                    text = paso.titulo ?: "",
                                                    color = Color(0xFF1F2937),
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontFamily = Destacado,
                                                    modifier = Modifier.padding(12.dp)
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
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color.White,
                                            shadowElevation = 1.dp
                                        ) {
                                            Text(
                                                text = paso.descripcion ?: "",
                                                color = Color(0xFF1F2937),
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp,
                                                fontFamily = Destacado,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                    // Sección de medios (solo imagen/video, no edición)
                                    if (!paso.mediaUrls.isNullOrEmpty()) {
                                        val mediaList = paso.mediaUrls!!.map { url ->
                                            val path = runCatching {
                                                val uri = java.net.URI(url)
                                                uri.rawPath + uri.rawQuery?.let { "?$it" }.orEmpty()
                                            }.getOrNull() ?: url
                                            if (path.startsWith("/")) "$baseUrl$path" else path
                                        }
                                        val pagerState = rememberPagerState()
                                        // val coroutineScope = rememberCoroutineScope() // Ya no se usa
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                        ) {
                                            HorizontalPager(
                                                count = mediaList.size,
                                                state = pagerState,
                                                modifier = Modifier.fillMaxSize()
                                            ) { page ->
                                                val url = mediaList[page]
                                                if (url.endsWith(".mp4", ignoreCase = true) || url.endsWith(".webm", ignoreCase = true)) {
                                                    LoopingVideoPlayer(
                                                        uri = android.net.Uri.parse(url),
                                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                                                    )
                                                } else {
                                                    coil.compose.AsyncImage(
                                                        model = url,
                                                        contentDescription = null,
                                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                    )
                                                }
                                            }
                                            HorizontalPagerIndicator(
                                                pagerState = pagerState,
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(8.dp),
                                                activeColor = Color(0xFFBC6154),
                                                inactiveColor = Color.LightGray
                                            )
                                            // Flechas eliminadas: ya no hay IconButton para avanzar/retroceder
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Sección de reseñas + comentarios ─────────────────────────────
                    ReviewsAndCommentSection(
                        ratings = ratings,
                        onSend = { comentario, puntos ->
                            if (!isAlumno) {
                                shakeComment = true
                                commentError = true
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "No se puede dejar una reseña en modo invitado",
                                        duration = SnackbarDuration.Short
                                    )
                                    kotlinx.coroutines.delay(2000)
                                    commentError = false
                                }
                            } else {
                                onSendRating(comentario, puntos)
                            }
                        },
                        isAlumno = isAlumno,
                        shakeComment = shakeComment,
                        commentError = commentError
                    )
                }
                if (showPortionDialog) {
                    var selectedTab by remember { mutableStateOf(0) }
                    var tempPortionInput by remember { mutableStateOf(portionInput) }
                    var tempIngredients by remember { mutableStateOf(adjustedIngredients) }
                    
                    AlertDialog(
                        onDismissRequest = { showPortionDialog = false },
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color.White,
                        tonalElevation = 4.dp,
                        title = {
                            Text(
                                "Ajustar receta",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        },
                        text = {
                            Column(Modifier.padding(top = 8.dp)) {
                                // Pestañas
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(
                                        onClick = { selectedTab = 0 },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (selectedTab == 0) Ladrillo else Color.Transparent,
                                            contentColor = if (selectedTab == 0) Color.White else Ladrillo
                                        )
                                    ) {
                                        Text("Porciones", fontWeight = FontWeight.Medium)
                                    }
                                    TextButton(
                                        onClick = { selectedTab = 1 },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = if (selectedTab == 1) Ladrillo else Color.Transparent,
                                            contentColor = if (selectedTab == 1) Color.White else Ladrillo
                                        )
                                    ) {
                                        Text("Ingredientes", fontWeight = FontWeight.Medium)
                                    }
                                }
                                
                                when (selectedTab) {
                                    0 -> {
                                        // Pestaña Porciones
                        OutlinedTextField(
                                            value = tempPortionInput,
                                            onValueChange = { tempPortionInput = it.filter(Char::isDigit) },
                                            label = { Text("Porciones", color = Color.Black) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            textStyle = LocalTextStyle.current.copy(color = Color.Black)
                                        )
                                    }
                                    1 -> {
                                        // Pestaña Ingredientes
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(300.dp)
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            tempIngredients.forEachIndexed { index, ingredient ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFF0E0DC)),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Emoji del ingrediente
                                                        //Text(
                                                        //    obtenerEmoji(ingredient.nombre),
                                                        //    fontSize = 20.sp
                                                        //)
                                                        //Spacer(Modifier.width(8.dp))

                                                        // Nombre del ingrediente
                                                        Text(
                                                            ingredient.nombre,
                                                            modifier = Modifier.weight(1f),
                                                            fontWeight = FontWeight.Medium,
                                                            color = Color.Black
                                                        )
                                                        
                                                        // Campo de cantidad editable
                                                        Box(
                                                            modifier = Modifier
                                                                .width(80.dp)
                                                                .height(36.dp)
                                                                .background(Color(0xFFF8F8F8), RoundedCornerShape(8.dp))
                                                                .border(1.dp, Ladrillo, RoundedCornerShape(8.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            BasicTextField(
                                                                value = ingredient.cantidad.toString(),
                                                                onValueChange = { newValue ->
                                                                    val originalCantidad = receta.ingredients[index].cantidad
                                                                    val newCantidad = newValue.toDoubleOrNull() ?: ingredient.cantidad
                                                                    val factor = if (originalCantidad > 0) newCantidad / originalCantidad else 1.0
                                                                    tempIngredients = tempIngredients.mapIndexed { i, ing ->
                                                                        val base = receta.ingredients[i].cantidad
                                                                        ing.copy(cantidad = (base * factor))
                                                                    }
                                                                    currentPortions = floor(receta.porciones * factor).toInt().coerceAtLeast(1)
                                                                    tempPortionInput = currentPortions.toString()
                                                                },
                                                                singleLine = true,
                                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                                textStyle = LocalTextStyle.current.copy(
                                                                    color = Color.Black,
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    textAlign = TextAlign.Center
                                                                ),
                                                                cursorBrush = SolidColor(Ladrillo),
                                                                decorationBox = { inner ->
                                                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { inner() }
                                                                }
                                                            )
                                                        }
                                                        
                                                        Spacer(Modifier.width(8.dp))
                                                        
                                                        // Unidad de medida
                                                        Text(
                                                            ingredient.unidadMedida,
                                                            fontSize = 14.sp,
                                                            color = Ladrillo
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Row {
                                TextButton(
                                    onClick = {
                                        // Aplicar cambios sin guardar
                                        when (selectedTab) {
                                            0 -> {
                                                val newPortions = tempPortionInput.toIntOrNull() ?: receta.porciones
                                                val factor = newPortions.toFloat() / receta.porciones
                                adjustedIngredients = receta.ingredients.map { ing ->
                                    ing.copy(cantidad = ing.cantidad * factor)
                                }
                                                currentPortions = newPortions
                                                portionInput = tempPortionInput
                                            }
                                            1 -> {
                                                adjustedIngredients = tempIngredients
                                                // Recalcular porciones basado en el ingrediente modificado (usando el primer ingrediente como referencia)
                                                val factor = if (receta.ingredients.isNotEmpty() && tempIngredients.isNotEmpty()) {
                                                    tempIngredients[0].cantidad / receta.ingredients[0].cantidad
                                                } else 1.0
                                                currentPortions = floor(receta.porciones * factor).toInt().coerceAtLeast(1)
                                                portionInput = currentPortions.toString()
                                            }
                                        }
                                    showPortionDialog = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Ladrillo, contentColor = Color.White)
                                ) {
                                    Text("Aplicar", fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(8.dp))
                                TextButton(
                                    onClick = {
                                        // Guardar cambios
                                        when (selectedTab) {
                                            0 -> {
                                                val newPortions = tempPortionInput.toIntOrNull() ?: receta.porciones
                                                val factor = newPortions.toFloat() / receta.porciones
                                val updatedIngredients = receta.ingredients.map { ing ->
                                    ing.copy(cantidad = ing.cantidad * factor)
                                }
                                val edited = receta.copy(
                                                porciones = newPortions,
                                    ingredients = updatedIngredients
                                )
                                onSaveEditedRecipe(edited)
                                            }
                                            1 -> {
                                                val factor = if (receta.ingredients.isNotEmpty() && tempIngredients.isNotEmpty()) {
                                                    tempIngredients[0].cantidad / receta.ingredients[0].cantidad
                                                } else 1.0
                                                val newPortions = floor(receta.porciones * factor).toInt().coerceAtLeast(1)
                                                val edited = receta.copy(
                                                    porciones = newPortions,
                                                    ingredients = tempIngredients
                                                )
                                                onSaveEditedRecipe(edited)
                                                adjustedIngredients = tempIngredients
                                                currentPortions = newPortions
                                                portionInput = newPortions.toString()
                                            }
                                        }
                                    showPortionDialog = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Ladrillo, contentColor = Color.White)
                                ) {
                                    Text("Guardar", fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showPortionDialog = false },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Ladrillo)
                            ) {
                                Text("Cancelar", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }
        }
        // SnackbarHost personalizado con estilo similar a Toast
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter),
            snackbar = { snackbarData ->
                CustomSnackbar(snackbarData = snackbarData)
            }
        )
    }
}

@Composable
fun CustomSnackbar(snackbarData: SnackbarData) {
    val visual = snackbarData.visuals
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .border(2.dp, Color.Black, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.logo_chef),
                contentDescription = null,
                tint = Color(0xFF042628),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = visual.message,
                color = Color(0xFF042628),
                fontFamily = Destacado,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

// Función de extensión para formatear números
fun Double.formatSmart(): String = if (this % 1.0 == 0.0) this.toInt().toString() else String.format("%.1f", this)
fun Float.formatSmart(): String = if (this % 1.0f == 0.0f) this.toInt().toString() else String.format("%.1f", this)
