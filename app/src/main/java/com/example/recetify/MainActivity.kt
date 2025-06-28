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
    profileInfoVm: ProfileInfoViewModel = viewModel()
) {
    val drafts    by draftVm.drafts.collectAsState()
    val favs      by favVm.favourites.collectAsState()
    val published by myRecipesVm.recipes.collectAsState(initial = emptyList())
    val user      by profileInfoVm.user.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

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
                if (user?.urlFotoPerfil?.isNotBlank() == true) {
                    // 1) construimos la URL completa tal como en HomeScreen
                    val base   = RetrofitClient.BASE_URL.trimEnd('/')
                    val remote = user!!.urlFotoPerfil!!
                    val path   = runCatching<String> {
                        val uri = URI(remote)
                        uri.rawPath + (uri.rawQuery?.let { "?$it" } ?: "")
                    }.getOrElse { remote }
                    val finalUrl = if (path.startsWith("/")) "$base$path" else remote

                    AsyncImage(
                        model = ImageRequest.Builder(context)
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
                        modifier           = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF2E3A59), CircleShape),
                        tint               = Color.Gray.copy(alpha = 0.4f)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text  = user?.alias.orEmpty().ifBlank { "Usuario" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text  = "Apasionada por la cocina",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(38.dp))

            // ─── STATS ───────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                StatItem(published.size, "Publicadas")
                Spacer(Modifier.width(12.dp))
                StatSeparator(height = 40.dp)
                Spacer(Modifier.width(12.dp))
                StatItem(favs.size,      "Favoritas")
                Spacer(Modifier.width(12.dp))
                StatSeparator(height = 40.dp)
                Spacer(Modifier.width(12.dp))
                StatItem(drafts.size,    "Borradores")
            }

            Spacer(Modifier.height(32.dp))

            // ─── OPCIONES ────────────────────────
            OptionRow("Información de perfil") { navController.navigate("profileInfo") }
            Spacer(Modifier.height(16.dp))
            OptionRow("Mis recetas publicadas") { navController.navigate("myRecipes") }
            Spacer(Modifier.height(16.dp))
            OptionRow("Mis recetas favoritas") { navController.navigate("saved") }
            Spacer(Modifier.height(16.dp))
            OptionRow("Mis borradores") { navController.navigate("drafts") }

            Spacer(Modifier.height(32.dp))

            // ─── CERRAR SESIÓN ────────────────────
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(8.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Cerrar sesión", color = Color.White)
            }
        }
    }

    // ─── DIALOGO LOGOUT ──────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title            = { Text("Cerrar sesión") },
            text             = { Text("¿Estás seguro de que querés cerrar sesión?") },
            confirmButton    = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch {
                        SessionManager.clearSession(context)
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
private fun OptionRow(title: String, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().height(64.dp).clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
private fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(label,    style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun StatSeparator(height: Dp) {
    Box(Modifier.width(1.dp).height(height).background(Color.LightGray))
}