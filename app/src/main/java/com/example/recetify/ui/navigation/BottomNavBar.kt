package com.example.recetify.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.recetify.R

@Composable
fun BottomNavBar(
    navController: NavController,
    isAlumno: Boolean
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    // Si soy visitante, sólo Home y Search. Si soy alumno, todo.
    val items = if (isAlumno) {
        listOf(NavItem.Home, NavItem.Search, NavItem.Chef, NavItem.Favorites, NavItem.Profile)
    } else {
        listOf(NavItem.Home, NavItem.Search, NavItem.Logout)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
    ) {
        Surface(
            modifier        = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .align(Alignment.BottomCenter),
            color           = Color.White,
            shadowElevation = 0.dp,
            tonalElevation  = 0.dp,
            shape           = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            val navColors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Black
            )
            Row(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            if (item.icon != null) {
                                Icon(
                                    imageVector     = item.icon,
                                    contentDescription = item.route,
                                    modifier        = Modifier.size(26.dp)
                                )
                            }
                        },
                        selected = currentRoute == item.route,
                        onClick  = {
                            if (item == NavItem.Logout) {
                                navController.navigate(NavItem.Logout.route) {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        },
                        alwaysShowLabel = false,
                        colors          = navColors
                    )
                }
            }
        }

        // Sólo si es alumno, muestro el FAB de Chef
        if (isAlumno) {
            FloatingActionButton(
                onClick        = {
                    if (currentRoute != NavItem.Chef.route) {
                        navController.navigate(NavItem.Chef.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                shape          = CircleShape,
                containerColor = Color(0xFF00261C),
                contentColor   = Color.White,
                modifier       = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
                    .size(64.dp)
            ) {
                Icon(
                    painter             = painterResource(id = R.drawable.ic_chef_hat),
                    contentDescription  = "Chef",
                    modifier            = Modifier.size(32.dp)
                )
            }
        }
    }
}