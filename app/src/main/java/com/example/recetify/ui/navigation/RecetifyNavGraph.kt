package com.example.recetify.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.recetify.ui.home.HomeScreen
import com.example.recetify.ui.favorites.FavoritesScreen
import com.example.recetify.ui.profile.ProfileScreen
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import com.example.recetify.ui.searchscreen.SearchScreen

@Composable
fun RecetifyNavGraph() {
    val navController = rememberNavController()
    val items = listOf("home", "search" ,"favorites", "profile")

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = screen) },
                        label = { Text(screen.capitalize()) },
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
            composable("home")     { HomeScreen() }
            composable("search")     { SearchScreen() }
            composable("favorites") { FavoritesScreen() }
            composable("profile")  { ProfileScreen() }
        }
    }
}
