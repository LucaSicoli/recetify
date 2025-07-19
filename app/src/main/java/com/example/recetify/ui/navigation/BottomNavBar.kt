package com.example.recetify.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.recetify.R
import com.example.recetify.data.remote.model.SessionManager
import com.example.recetify.ui.common.LogoutDialog
import kotlinx.coroutines.launch

@Composable
fun BottomNavBar(
    navController: NavController,
    isAlumno: Boolean,
    onNavWithLoading: ((String) -> Unit)? = null,
    showLoading: Boolean = false // <-- nuevo parámetro
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Si soy visitante, solo Home y Search. Si soy alumno, todo.
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
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            val navColors = NavigationBarItemDefaults.colors(
                unselectedIconColor = Color.Black,
                selectedIconColor = Color.White,
                indicatorColor = Color(0xFF00261C)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            if (item.icon != null) {
                                val isSelected = currentRoute == item.route
                                val animatedIconColor by animateColorAsState(
                                    targetValue = if (isSelected) Color.White else Color.Black
                                )
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.route,
                                    modifier = Modifier.size(26.dp),
                                    tint = animatedIconColor
                                )
                            }
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (!showLoading) {
                                if (item == NavItem.Logout) {
                                    showLogoutDialog = true
                                } else if (currentRoute != item.route) {
                                    if (onNavWithLoading != null && item != NavItem.Logout) {
                                        // CORRECCIÓN: Si el destino es "home", navega limpiando el stack igual que el BackHandler
                                        if (item.route == "home") {
                                            navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            onNavWithLoading(item.route)
                                        }
                                    } else {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !showLoading,
                        alwaysShowLabel = false,
                        colors = navColors
                    )
                }
            }
        }

        // Solo si es alumno, muestro el FAB de Chef
        if (isAlumno) {
            FloatingActionButton(
                onClick = {
                    if (!showLoading && currentRoute != NavItem.Chef.route) {
                        if (onNavWithLoading != null) {
                            onNavWithLoading(NavItem.Chef.route)
                        } else {
                            navController.navigate(NavItem.Chef.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    }
                },
                shape = CircleShape,
                containerColor = Color(0xFF00261C),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-20).dp)
                    .size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chef_hat),
                    contentDescription = "Chef",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    // LogoutDialog reutilizable para visitantes
    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                scope.launch {
                    SessionManager.clearSession(context)
                    navController.navigate("login") {
                        // Limpiar TODO el stack de navegación para evitar volver atrás
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }
}