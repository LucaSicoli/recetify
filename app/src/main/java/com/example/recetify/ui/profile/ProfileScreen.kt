package com.example.recetify.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.recetify.data.remote.model.SessionManager
import com.example.recetify.ui.common.RecipeCardDraft


@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile = viewModel.profileState
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) MaterialTheme.colorScheme.background else Color.White
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onUnauthorized = {
            SessionManager.clearToken()
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
        viewModel.fetchProfile()
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (error != null) {
        Text("Error: $error", color = MaterialTheme.colorScheme.error)
        return
    }

    profile?.let {
        Column(
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(24.dp)
        ) {
            // Título y botón para alternar vista
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (viewModel.showDrafts) "Mis Borradores" else "Mi Perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = viewModel::toggleView,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (viewModel.showDrafts) "Ver Perfil" else "Ver Borradores")
                }
            }

            Spacer(Modifier.height(24.dp))

            if (viewModel.showDrafts) {
                if (viewModel.draftRecipes.isEmpty()) {
                    Text("No tenés recetas en borrador aún.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        viewModel.draftRecipes.forEach { recipe ->
                            RecipeCardDraft(recipe = recipe, navController = navController)
                        }
                    }
                }
                return@Column
            }

            // FOTO DE PERFIL + INFO
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = it.urlFotoPerfil ?: "https://drive.google.com/uc?id=1BXSqhCJNCLiusAosGcoE36K24xdRFDfw/n",
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        it.alias,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        it.nombre ?: "Apasionado por la cocina",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ESTADÍSTICAS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(viewModel.recetasPublicadas, "Recetas\nPublicadas")
                ProfileStat(viewModel.recetasGuardadas, "Recetas\nGuardadas")
                ProfileStat(viewModel.resenasPublicadas, "Reseñas\nPublicadas")
            }

            Spacer(Modifier.height(32.dp))

            // EMAIL
            Text(
                "MAIL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextField(
                value = it.email,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = TextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(Modifier.height(12.dp))

            // ALIAS
            Text(
                "ALIAS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextField(
                value = it.alias,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = TextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(Modifier.height(32.dp))

            // BOTÓN CERRAR SESIÓN
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                border = BorderStroke(1.dp, SolidColor(MaterialTheme.colorScheme.error)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = backgroundColor,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que querés cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    SessionManager.clearToken()
                    showLogoutDialog = false
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun ProfileStat(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$count",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            label,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
