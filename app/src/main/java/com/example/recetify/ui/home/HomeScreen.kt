package com.example.recetify.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recetify.R
import com.example.recetify.data.db.RecipeEntity
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.SessionManager
import kotlinx.coroutines.launch
import java.net.URI

// Fuentes
private val Sen = FontFamily(
    Font(R.font.pacifico_regular, weight = FontWeight.Light)
)
private val Destacado = FontFamily(
    Font(R.font.sen_semibold, weight = FontWeight.ExtraBold)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    homeVm: HomeViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // Flow desde Room/Repo
    val recipes  by homeVm.recipes.collectAsState()
    val isLoading by homeVm.isLoading.collectAsState()

    val listState = rememberLazyListState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Cálculo de fade para el logo
    val fadePx = with(LocalDensity.current){ 80.dp.toPx() }
    val rawAlpha = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 0f
            else (1f - listState.firstVisibleItemScrollOffset / fadePx).coerceIn(0f,1f)
        }
    }.value
    val logoAlpha by animateFloatAsState(rawAlpha, tween(500))

    BackHandler { showLogoutDialog = true }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
        if (isLoading) {
            // ── Mientras carga ────────────────────────────────
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // ── Lista de recetas ───────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 0.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Logo ───────────────────────────────────────────
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.homecheflogo),
                            contentDescription = "Logo Recetify",
                            modifier = Modifier
                                .size(210.dp)
                                .align(Alignment.TopCenter)
                                .padding(top = 58.dp)
                                .graphicsLayer { alpha = logoAlpha },
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // ── Sticky Header “Destacados” ────────────────────
                stickyHeader {
                    val stuck by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .offset(y = if (!stuck) (-24).dp else 0.dp)
                            .padding(horizontal = if (stuck) 0.dp else 24.dp)
                            .background(Color.White)
                            .zIndex(10f)
                    ) {
                        FeaturedHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (stuck) 100.dp else 90.dp),
                            shape = if (stuck) RoundedCornerShape(0.dp) else RoundedCornerShape(8.dp)
                        )
                    }
                }

                // ── Items ─────────────────────────────────────────
                items(recipes, key = { it.id }) { recipe ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clickable { navController.navigate("recipe/${recipe.id}") }
                    ) {
                        RecipeCard(recipe)
                    }
                }
            }
        }
    }

    // ── Diálogo de “Cerrar sesión” ───────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title           = { Text("Cerrar sesión") },
            text            = { Text("¿Estás seguro de que querés cerrar la sesión y volver al login?") },
            confirmButton   = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch {
                        SessionManager.setVisitante(context)
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }) {
                    Text("Sí")
                }
            },
            dismissButton   = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
private fun FeaturedHeader(
    modifier: Modifier = Modifier,
    title: String = "DESTACADOS",
    subtitle: String = "del día",
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFCC5E5A),
                            Color(0xFFC6665A),
                            Color(0xFFE29587)
                        )
                    )
                )
        ) {
            Row(
                Modifier
                    .align(Alignment.Center)
                    .wrapContentWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontFamily = Destacado,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = Sen,
                        fontSize = 20.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: RecipeEntity,
    modifier: Modifier = Modifier
) {
    val base     = RetrofitClient.BASE_URL.trimEnd('/')
    val original = recipe.fotoPrincipal.orEmpty()
    val pathOnly = runCatching {
        val uri = URI(original)
        uri.rawPath + uri.rawQuery?.let { "?$it" }.orEmpty()
    }.getOrNull() ?: original
    val finalUrl = if (pathOnly.startsWith("/")) "$base$pathOnly" else "$base/$pathOnly"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = finalUrl,
                contentDescription = recipe.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(12.dp))

            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = recipe.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Black
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Chef",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = recipe.usuarioCreadorAlias.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Black)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFe29587),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "%,.1f".format(recipe.promedioRating ?: 0.0),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                    )

                    Spacer(Modifier.width(16.dp))

                    Icon(
                        Icons.Filled.Timer,
                        contentDescription = "Tiempo",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${recipe.tiempo} min",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = recipe.descripcion.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}