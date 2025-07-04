package com.example.recetify.ui.profile

import java.net.URI
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    navController: NavController,
    draftVm: DraftViewModel = viewModel(),
    favVm: FavouriteViewModel = viewModel(),
    myRecipesVm: MyRecipesViewModel = viewModel(),
    reviewCountVm: ReviewCountViewModel       = viewModel()
) {
    // 1) Listas
    val drafts    by draftVm.drafts.collectAsState()
    val favs      by favVm.favourites.collectAsState()
    val published by myRecipesVm.recipes.collectAsState(initial = emptyList())

    // 2) Usuario
    val profileInfoVm: ProfileInfoViewModel = viewModel()
    val userState = profileInfoVm.user.collectAsState()
    val user = userState.value

    val ctx = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val reviewCount by reviewCountVm.count.collectAsState()
    LaunchedEffect(Unit) { reviewCountVm.loadCount() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color    = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 72.dp, bottom = 24.dp)
        ) {
            // ─── HEADER ─────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Avatar dinámico
                if (user != null && !user.urlFotoPerfil.isNullOrBlank()) {
                    // Normalizar URL igual que en HomeScreen
                    val base   = RetrofitClient.BASE_URL.trimEnd('/')
                    val remote = user.urlFotoPerfil!!
                    val path   = runCatching {
                        val uri = URI(remote)
                        uri.rawPath + (uri.rawQuery?.let { "?$it" } ?: "")
                    }.getOrDefault(remote)
                    val finalUrl = if (path.startsWith("/")) "$base$path" else remote

                    AsyncImage(
                        model = ImageRequest.Builder(ctx)
                            .data(finalUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF2E3A59), CircleShape)
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Default.AccountCircle,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF2E3A59), CircleShape),
                        tint = Color.Gray.copy(alpha = 0.4f)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text  = user?.alias.orEmpty().ifBlank { "Usuario" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = user?.descripcion
                            .takeIf { !it.isNullOrBlank() }
                            .orEmpty()
                            .ifBlank { "Sin descripción" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(38.dp))

            // ─── STATS (4) ───────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                StatItem(published.size, "Publicadas", Modifier.weight(1f))
                StatSeparator(height = 40.dp, modifier = Modifier.weight(0.05f))
                StatItem(drafts.size,    "Borradores", Modifier.weight(1f))
                StatSeparator(height = 40.dp, modifier = Modifier.weight(0.05f))
                StatItem(favs.size,      "Favoritas", Modifier.weight(1f))
                StatSeparator(height = 40.dp, modifier = Modifier.weight(0.05f))
                StatItem(reviewCount,    "Reseñas", Modifier.weight(1f))
            }

            Spacer(Modifier.height(32.dp))

            // ─── OPCIONES (4) ────────────────────────
            OptionRow(
                color = Color(0xFFBC6154),
                title = "Información de perfil"
            ) {
                navController.navigate("profileInfo")
            }
            Spacer(Modifier.height(16.dp))

            OptionRow(
                color = Color(0xFF2E7D32),
                title = "Mis recetas publicadas"
            ) {
                navController.navigate("myRecipes")
            }
            Spacer(Modifier.height(16.dp))

            OptionRow(
                color = Color(0xFFCC3366),
                title = "Mis recetas favoritas"
            ) {
                navController.navigate("saved")
            }
            Spacer(Modifier.height(16.dp))

            OptionRow(
                color = Color(0xFF5A6F8A),
                title = "Mis borradores"
            ) {
                navController.navigate("drafts")
            }

            Spacer(Modifier.height(32.dp))

            // ─── BOTÓN CERRAR SESIÓN ───────────────────
            Button(
                onClick  = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape  = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Cerrar sesión", color = Color.White)
            }
        }
    }

    // ─── DIÁLOGO CONF. LOGOUT ───────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title            = { Text("Cerrar sesión") },
            text             = { Text("¿Estás seguro de que querés cerrar sesión?") },
            confirmButton    = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch {
                        SessionManager.clearSession(ctx)
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }) { Text("Sí") }
            },
            dismissButton    = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("No") }
            }
        )
    }
}

@Composable
private fun StatItem(count: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text  = count.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text      = label,
            style     = MaterialTheme.typography.bodySmall,
            maxLines  = 2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatSeparator(
    width: Dp = 1.dp,
    height: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .width(width)
            .height(height)
            .background(Color.LightGray)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionRow(
    color: Color,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier           = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text     = title,
                style    = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector        = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint               = Color.Gray,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}