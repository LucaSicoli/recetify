package com.example.recetify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recetify.ui.login.*
import com.example.recetify.ui.onboarding.*
import com.example.recetify.ui.theme.RecetifyTheme

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
    }
}

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    val passwordVm: PasswordResetViewModel = viewModel()

    // Empieza por el onboarding (pantalla 01)
    NavHost(navController = nav, startDestination = "start01") {
        // Onboarding screens
        composable("start01") { Start01Screen(navController = nav) }
        composable("start02") { Start02Screen(navController = nav) }
        composable("start03") { Start03Screen(navController = nav) }

        // Login flow
        composable("login") {
            LoginScreen(
                viewModel = viewModel(),
                onLoginSuccess = { token ->
                    nav.navigate("home") { popUpTo("login") { inclusive = true } }
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
                    nav.navigate("login") { popUpTo("forgot") { inclusive = true } }
                }
            )
        }

        composable("home") {
            HomeScreen()
        }
    }
}

@Composable
fun HomeScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Text(
            text = "¡Bienvenido a Recetify!",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}