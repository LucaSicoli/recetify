// app/src/main/java/com/example/recetify/MainActivity.kt
package com.example.recetify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.recetify.data.remote.model.SessionManager
import com.example.recetify.ui.common.NoConnectionScreen
import com.example.recetify.ui.common.rememberIsOnline
import com.example.recetify.ui.details.RecipeDetailScreen
import com.example.recetify.ui.home.HomeScreen
import com.example.recetify.ui.login.ForgotPasswordScreen
import com.example.recetify.ui.login.LoginScreen
import com.example.recetify.ui.login.LoginViewModel
import com.example.recetify.ui.login.PasswordResetViewModel
import com.example.recetify.ui.login.ResetPasswordScreen
import com.example.recetify.ui.login.VerifyCodeScreen
import com.example.recetify.ui.navigation.BottomNavBar
import com.example.recetify.ui.theme.RecetifyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fullscreen edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        setContent {
            RecetifyTheme {
                AppNavGraph()
            }
        }
    }
}

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()

    // 1) ¿Ya guardamos token (alumno o visitante)?
    val isAlumno by SessionManager.isAlumnoFlow(context)
        .collectAsState(initial = false)

    // 2) ViewModels compartidos
    val loginVm: LoginViewModel = viewModel()
    val passwordVm: PasswordResetViewModel = viewModel()

    // 3) Estado de la red
    val isOnline by rememberIsOnline()
    var offline by rememberSaveable { mutableStateOf(!isOnline) }
    LaunchedEffect(isOnline) { offline = !isOnline }

    Box(Modifier.fillMaxSize()) {
        // NavHost
        NavHost(
            navController = nav,
            startDestination = if (isAlumno) "home" else "login"
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = loginVm,
                    onLoginSuccess = { token ->
                        scope.launch {
                            SessionManager.setAlumno(context, token)
                            nav.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    onForgot = { nav.navigate("forgot") }
                )
            }
            composable("forgot") {
                ForgotPasswordScreen(
                    viewModel = passwordVm,
                    onNext = { nav.navigate("verify") }
                )
            }
            composable("verify") {
                VerifyCodeScreen(
                    viewModel = passwordVm,
                    onNext = { nav.navigate("reset") }
                )
            }
            composable("reset") {
                ResetPasswordScreen(
                    viewModel = passwordVm,
                    onFinish = {
                        nav.navigate("login") {
                            popUpTo("forgot") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(navController = nav)
            }
            composable("recipe/{id}") { back ->
                back.arguments
                    ?.getString("id")
                    ?.toLongOrNull()
                    ?.let { id ->
                        RecipeDetailScreen(recipeId = id, navController = nav)
                    }
            }
        }

        // Extraer la ruta actual
        val backStackEntry by nav.currentBackStackEntryAsState()
        val route = backStackEntry?.destination?.route ?: ""

        // BottomNavBar (solo en home/recipe y online)
        if (!offline && (route == "home" || route.startsWith("recipe/"))) {
            Box(Modifier.align(Alignment.BottomCenter)) {
                BottomNavBar(nav)
            }
        }

        // Overlay "Sin conexión"
        if (offline) {
            NoConnectionScreen(
                onRetry = { /* el hook rememberIsOnline reaccionará */ },
                onContinueOffline = { offline = false }
            )
        }
    }
}