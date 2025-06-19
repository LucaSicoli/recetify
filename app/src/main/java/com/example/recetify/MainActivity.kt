package com.example.recetify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.recetify.ui.profile.MyRecipesScreen
import com.example.recetify.ui.profile.ProfileInfoScreen
import com.example.recetify.ui.profile.ProfileScreen
import com.example.recetify.ui.profile.SavedRecipesScreen
import com.example.recetify.ui.theme.RecetifyTheme

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}

@Composable
fun AppNavGraph() {
    // 1) NavController
    val navController = rememberNavController()
    // 2) ViewModels
    val passwordVm: PasswordResetViewModel = viewModel()
    val loginVm: LoginViewModel = viewModel()
    // 3) Conectividad
    val isOnline by rememberIsOnline()
    var showOfflineScreen by rememberSaveable { mutableStateOf(!isOnline) }
    LaunchedEffect(isOnline) { showOfflineScreen = !isOnline }

    Box(modifier = Modifier.fillMaxSize()) {
        // 4) NavHost
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = loginVm,
                    onLoginSuccess = { token ->
                        SessionManager.saveToken(token)
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onForgot = { navController.navigate("forgot") }
                )
            }
            composable("forgot") {
                ForgotPasswordScreen(
                    viewModel = passwordVm,
                    onNext = { navController.navigate("verify") }
                )
            }
            composable("verify") {
                VerifyCodeScreen(
                    viewModel = passwordVm,
                    onNext = { navController.navigate("reset") }
                )
            }
            composable("reset") {
                ResetPasswordScreen(
                    viewModel = passwordVm,
                    onFinish = {
                        navController.navigate("login") {
                            popUpTo("forgot") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(navController = navController)
            }
            composable("recipe/{id}") { back ->
                back.arguments
                    ?.getString("id")
                    ?.toLongOrNull()
                    ?.let { id ->
                        RecipeDetailScreen(recipeId = id, navController = navController)
                    }
            }

            composable("profile") {
                ProfileScreen(navController = navController)
            }
            composable("my_recipes") {
                MyRecipesScreen(onBack = { navController.popBackStack() })
            }
            composable("saved_recipes") {
                SavedRecipesScreen(onBack = { navController.popBackStack() })
            }
            composable("profile_info") {
                ProfileInfoScreen(onBack = { navController.popBackStack() })
            }

        }

        // 5) BottomNavBar solo en home/recipe y si hay conexión
        val backStack by navController.currentBackStackEntryAsState()
        val route = backStack?.destination?.route ?: ""
        if (!showOfflineScreen && (route == "home" || route.startsWith("recipe/") || route == "profile")) {
            Box(Modifier.align(Alignment.BottomCenter)) {
                BottomNavBar(navController = navController)
            }
        }

        // 6) Overlay "Sin conexión"
        if (showOfflineScreen) {
            NoConnectionScreen(
                onRetry = { /* RememberIsOnline se re-evalúa */ },
                onContinueOffline = { showOfflineScreen = false }
            )
        }
    }
}