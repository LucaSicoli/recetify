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
import androidx.navigation.compose.*
import com.example.recetify.data.remote.model.SessionManager
import com.example.recetify.ui.common.NoConnectionScreen
import com.example.recetify.ui.common.rememberIsOnline
import com.example.recetify.ui.details.RecipeDetailScreen
import com.example.recetify.ui.home.HomeScreen
import com.example.recetify.ui.login.*
import com.example.recetify.ui.navigation.BottomNavBar
import com.example.recetify.ui.theme.RecetifyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Fullscreen edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
    val context       = LocalContext.current
    val navController = rememberNavController()
    val scope         = rememberCoroutineScope()

    // 1) Flujos de sesión y conexión
    val isAlumno by SessionManager.isAlumnoFlow(context).collectAsState(initial = false)
    val isOnline by rememberIsOnline()
    var offline by rememberSaveable { mutableStateOf(!isOnline) }
    LaunchedEffect(isOnline) { offline = !isOnline }

    Box(Modifier.fillMaxSize()) {
        // 2) NavHost siempre arranca en "login"
        NavHost(navController, startDestination = "login") {

            composable("login") {
                // Si ya tenemos sesión, navegamos directo a home
                if (isAlumno) {
                    LaunchedEffect(Unit) {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                } else {
                    LoginScreen(
                        viewModel      = viewModel<LoginViewModel>(),
                        onLoginSuccess = { token ->
                            scope.launch {
                                SessionManager.setAlumno(context, token)
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        },
                        onForgot = { navController.navigate("forgot") }
                    )
                }
            }

            composable("forgot") {
                ForgotPasswordScreen(
                    viewModel = viewModel<PasswordResetViewModel>(),
                    onNext    = { navController.navigate("verify") }
                )
            }
            composable("verify") {
                VerifyCodeScreen(
                    viewModel = viewModel<PasswordResetViewModel>(),
                    onNext    = { navController.navigate("reset") }
                )
            }
            composable("reset") {
                ResetPasswordScreen(
                    viewModel = viewModel<PasswordResetViewModel>(),
                    onFinish  = {
                        navController.navigate("login") {
                            popUpTo("forgot") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
                // Si perdimos sesión, volvemos a login
                if (!isAlumno) {
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                } else {
                    HomeScreen(navController = navController)
                }
            }

            composable("recipe/{id}") { back ->
                back.arguments
                    ?.getString("id")
                    ?.toLongOrNull()
                    ?.let { id ->
                        RecipeDetailScreen(recipeId = id, navController = navController)
                    }
            }
        }

        // 3) Extraer ruta actual para BottomNavBar
        val backStackEntry by navController.currentBackStackEntryAsState()
        val route = backStackEntry?.destination?.route ?: ""

        // 4) Mostrar BottomNavBar sólo en home/recipe y online
        if (!offline && (route == "home" || route.startsWith("recipe/"))) {
            Box(Modifier.align(Alignment.BottomCenter)) {
                BottomNavBar(navController)
            }
        }

        // 5) Overlay "Sin conexión"
        if (offline) {
            NoConnectionScreen(
                onRetry           = { /* rememberIsOnline reacciona */ },
                onContinueOffline = { offline = false }
            )
        }
    }
}