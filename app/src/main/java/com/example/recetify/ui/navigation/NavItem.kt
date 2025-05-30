// NavItem.kt
package com.example.recetify.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val route: String, val icon: ImageVector?) {
    object Home      : NavItem("home", Icons.Filled.Home)
    object Search    : NavItem("search", Icons.Filled.Search)
    object Chef      : NavItem("chef", null) // <- ahora sí
    object Favorites : NavItem("favorites", Icons.Filled.Favorite)
    object Profile   : NavItem("profile", Icons.Filled.Person)

    companion object {
        val items = listOf(Home, Search, Chef, Favorites, Profile)
    }
}