// NavItem.kt
package com.example.recetify.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Cottage
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val route: String, val icon: ImageVector?) {
    object Home      : NavItem("home",    Icons.Outlined.Cottage)
    object Search    : NavItem("search",  Icons.Outlined.Search)
    object Chef      : NavItem("createRecipe",    null)
    object Favorites : NavItem("saved",   Icons.Outlined.FavoriteBorder)
    object Profile   : NavItem("profile", Icons.Outlined.Person)
    object Logout   : NavItem("login", Icons.Outlined.ExitToApp)

    companion object {
        val items = listOf(Home, Search, Chef, Favorites, Profile)
    }
}