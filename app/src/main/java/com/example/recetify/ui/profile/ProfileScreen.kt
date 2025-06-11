package com.example.recetify.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile = viewModel.profileState
    val isLoading = viewModel.isLoading
    val error = viewModel.errorMessage

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (error != null) {
        Text("Error: $error", color = Color.Red)
        return
    }

    profile?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Mi Perfil", style = MaterialTheme.typography.headlineSmall, color = Color.Black)

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val imageUrl = it.urlFotoPerfil ?: "https://i.imgur.com/GzGfU5b.png"
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(it.alias, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                    Text(it.nombre ?: "Apasionado por la cocina", color = Color.Black)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(count = it.recetasPublicadas, label = "Recetas\nPublicadas")
                ProfileStat(count = it.recetasGuardadas, label = "Recetas\nGuardadas")
                ProfileStat(count = it.resenasPublicadas, label = "Reseñas\nPublicadas")
            }

            Spacer(Modifier.height(24.dp))

            ProfileOption("Información de perfil", Color(0xFFD96E63)) {
                navController.navigate("profile_info")
            }

            ProfileOption("Mis recetas", Color(0xFFAA5A4F)) {
                navController.navigate("my_recipes")
            }

            ProfileOption("Recetas guardadas", Color(0xFFADB9D3)) {
                navController.navigate("saved_recipes")
            }
        }
    }
}

@Composable
fun ProfileStat(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", style = MaterialTheme.typography.titleLarge, color = Color.Black)
        Text(label, textAlign = TextAlign.Center, color = Color.Black)
    }
}

@Composable
fun ProfileOption(title: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color, shape = CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}
