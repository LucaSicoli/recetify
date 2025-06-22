// app/src/main/java/com/example/recetify/MainActivity.kt
package com.example.recetify

import android.app.Application
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
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.SessionManager
import com.example.recetify.ui.common.NoConnectionScreen
import com.example.recetify.ui.common.rememberIsOnline
import com.example.recetify.ui.createRecipe.CreateRecipeScreen
import com.example.recetify.ui.createRecipe.CreateRecipeViewModel
import com.example.recetify.ui.createRecipe.CreateRecipeViewModelFactory
import com.example.recetify.ui.details.RecipeDetailScreen
import com.example.recetify.ui.home.HomeScreen
import com.example.recetify.ui.login.*
import com.example.recetify.ui.navigation.BottomNavBar
import com.example.recetify.ui.profile.ProfileScreen
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

    val passwordVm: PasswordResetViewModel = viewModel()
    val isOnline by rememberIsOnline()

    var offline by rememberSaveable { mutableStateOf(!isOnline) }
    LaunchedEffect(isOnline) { offline = !isOnline }

    // Estado en memoria para saber si el usuario está logueado
    var isAlumno by rememberSaveable { mutableStateOf(false) }

    // Escuchar token guardado y validar perfil
    LaunchedEffect(Unit) {
        SessionManager.tokenFlow(context).collect { token ->
            if (token.isNullOrBlank()) {
                // No hay token, usuario no logueado
                isAlumno = false
                SessionManager.clearToken()
                SessionManager.setVisitante(context)
            } else {
                try {
                    RetrofitClient.api.getMyProfile()
                    isAlumno = true
                } catch (e: Exception) {
                    // Token inválido o expirado
                    isAlumno = false
                    SessionManager.clearToken()
                    SessionManager.setVisitante(context)
                }
            }
        }
    }

    val startDestination = if (isAlumno) "home" else "login"

    Box(Modifier.fillMaxSize()) {
        NavHost(navController, startDestination = startDestination) {

            composable("login") {
                LoginScreen(
                    viewModel      = viewModel<LoginViewModel>(),
                    onLoginSuccess = { token ->
                        scope.launch {
                            SessionManager.setAlumno(context, token)
                            isAlumno = true
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    onForgot = { navController.navigate("forgot") }
                )
            }

            composable("forgot") {
                ForgotPasswordScreen(
                    viewModel = passwordVm,
                    onNext    = { navController.navigate("verify") }
                )
            }

            composable("verify") {
                VerifyCodeScreen(
                    viewModel = passwordVm,
                    onNext    = { navController.navigate("reset") }
                )
            }

            composable("reset") {
                ResetPasswordScreen(
                    viewModel = passwordVm,
                    onFinish  = {
                        navController.navigate("login") {
                            popUpTo("forgot") { inclusive = true }
                        }
                    }
                )
            }

            composable("home") {
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

            composable("profile") {
                if (!isAlumno) {
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }
                } else {
                    ProfileScreen(
                        navController = navController
                       /* onUnauthorized = {
                            SessionManager.clearToken()
                            isAlumno = false
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }*/
                    )
                }
            }

            composable("recipe/{id}") { backStackEntry ->
                backStackEntry.arguments
                    ?.getString("id")
                    ?.toLongOrNull()
                    ?.let { id ->
                        RecipeDetailScreen(recipeId = id, navController = navController)
                    }
            }

            composable("createRecipe") {
                val vm: CreateRecipeViewModel = viewModel(
                    factory = CreateRecipeViewModelFactory(
                        context.applicationContext as Application
                    )
                )
                CreateRecipeScreen(
                    viewModel   = vm,
                    onClose     = { navController.popBackStack() },
                    onSaved     = { navController.popBackStack() },
                    onPublished = { navController.popBackStack() }
                )
            }
        }

        // Mostrar BottomNavBar solo si estamos online y en home/recipe/createRecipe
        val backStackEntry by navController.currentBackStackEntryAsState()
        val route = backStackEntry?.destination?.route ?: ""
        if (!offline && (route == "home" || route.startsWith("recipe/") || route == "createRecipe" || route == "profile")) {
            Box(Modifier.align(Alignment.BottomCenter)) {
                BottomNavBar(navController)
            }
        }

        if (offline) {
            NoConnectionScreen(
                onContinueOffline = {
                    offline = false
                }
            )
        }
    }
}
