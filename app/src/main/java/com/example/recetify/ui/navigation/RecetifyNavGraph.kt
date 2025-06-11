package com.example.recetify.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.recetify.ui.home.HomeScreen
import com.example.recetify.ui.favorites.FavoritesScreen
import com.example.recetify.ui.profile.ProfileScreen
import com.example.recetify.ui.searchscreen.SearchScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Alignment
import androidx.navigation.compose.composable

@Composable
fun RecetifyNavGraph() {
    val navController = rememberNavController()
    val items = listOf("home", "search", "favorites", "profile")

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = screen) },
                        label = { Text(screen.replaceFirstChar { it.uppercase() }) },
                        selected = navController.currentDestination?.route == screen,
                        onClick = {
                            navController.navigate(screen) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen(navController = navController) }
            composable("search") { SearchScreen() }
            composable("favorites") { FavoritesScreen() }
            composable("profile") { ProfileScreen(navController) }

            // NUEVAS RUTAS
            composable("profile_info") {
                PlaceholderScreen("Información de perfil")
            }
            composable("my_recipes") {
                PlaceholderScreen("Mis recetas")
            }
            composable("saved_recipes") {
                PlaceholderScreen("Recetas guardadas")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla: $title", style = MaterialTheme.typography.headlineSmall)
    }
}
