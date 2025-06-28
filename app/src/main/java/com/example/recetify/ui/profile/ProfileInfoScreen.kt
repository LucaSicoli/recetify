package com.example.recetify.ui.profile

import java.net.URI
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.recetify.data.remote.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInfoScreen(
    navController: NavController,
    vm: ProfileInfoViewModel = viewModel()
) {
    val userState = vm.user.collectAsState()
    val user = userState.value
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Información de perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (user == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar
                    if (!user.urlFotoPerfil.isNullOrBlank()) {
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
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFBC6154), CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Avatar",
                            tint         = Color.Gray.copy(alpha = 0.4f),
                            modifier     = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFBC6154), CircleShape)
                        )
                    }

                    // Alias & Email
                    Text("Alias:", style = MaterialTheme.typography.labelLarge)
                    Text(user.alias, style = MaterialTheme.typography.bodyLarge)

                    Text("Email:", style = MaterialTheme.typography.labelLarge)
                    Text(user.email, style = MaterialTheme.typography.bodyLarge)

                    Spacer(Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { /* Navegar a editar perfil */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Editar perfil")
                    }
                }
            }
        }
    }
}