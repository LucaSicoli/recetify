package com.example.recetify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recetify.ui.login.ForgotPasswordScreen
import com.example.recetify.ui.login.LoginScreen
import com.example.recetify.ui.login.PasswordResetViewModel
import com.example.recetify.ui.login.ResetPasswordScreen
import com.example.recetify.ui.login.VerifyCodeScreen
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
    // Esta es la única instancia de PasswordResetViewModel
    val passwordVm: PasswordResetViewModel = viewModel()

    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = viewModel(), // tu VM de login
                onLoginSuccess = { token ->
                    nav.navigate("home") { popUpTo("login") { inclusive = true } }
                },
                onForgot = {
                    nav.navigate("forgot")
                }
            )
        }

        composable("forgot") {
            ForgotPasswordScreen(
                viewModel = passwordVm,    // le pasas la VM hoisteada
                onNext = { nav.navigate("verify") }
            )
        }

        composable("verify") {
            VerifyCodeScreen(
                viewModel = passwordVm,    // misma VM
                onNext = { nav.navigate("reset") }
            )
        }

        composable("reset") {
            ResetPasswordScreen(
                viewModel = passwordVm,    // misma VM
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
        androidx.compose.material3.Text(
            text = "¡Bienvenido a Recetify!",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
