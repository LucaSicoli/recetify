package com.example.recetify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.SessionManager
import com.example.recetify.ui.home.HomeScreen
import com.example.recetify.ui.login.*
import com.example.recetify.ui.navigation.BottomNavBar
import com.example.recetify.ui.theme.RecetifyTheme
import com.example.recetify.ui.home.RecipeDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    }
}


@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    val passwordVm: PasswordResetViewModel = viewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = "login", modifier = Modifier.fillMaxSize()) {
            composable("login") {
                val loginVm = viewModel<LoginViewModel>()
                LoginScreen(
                    viewModel = loginVm,
                    onLoginSuccess = {
                        SessionManager.authToken = it
                        nav.navigate("home") { popUpTo("login") { inclusive = true } }
                    },
                    onForgot = { nav.navigate("forgot") }
                )
            }
            composable("forgot") { ForgotPasswordScreen(viewModel = passwordVm, onNext = { nav.navigate("verify") }) }
            composable("verify") { VerifyCodeScreen(viewModel = passwordVm, onNext = { nav.navigate("reset") }) }
            composable("reset") {
                ResetPasswordScreen(viewModel = passwordVm, onFinish = {
                    nav.navigate("login") { popUpTo("forgot") { inclusive = true } }
                })
            }
            composable("home") { HomeScreen(navController = nav) }
            composable("recipe/{id}") {
                val id = it.arguments?.getString("id")
                if (id != null) RecipeDetailScreen(id)
            }
        }

        val backStack by nav.currentBackStackEntryAsState()
        if (backStack?.destination?.route == "home") {
            Box(Modifier.align(Alignment.BottomCenter)) {
                BottomNavBar(navController = nav)
            }
        }
    }
}
