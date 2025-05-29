package com.example.recetify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recetify.data.remote.model.SessionManager
import com.example.recetify.ui.home.HomeScreen
import com.example.recetify.ui.home.HomeViewModel
import com.example.recetify.ui.login.ForgotPasswordScreen
import com.example.recetify.ui.login.LoginScreen
import com.example.recetify.ui.login.LoginViewModel
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
                    color    = MaterialTheme.colorScheme.background
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

    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            val loginVm: LoginViewModel = viewModel()
            LoginScreen(
                viewModel      = loginVm,
                onLoginSuccess = { token ->
                    SessionManager.authToken = token
                    nav.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgot       = { nav.navigate("forgot") }
            )
        }

        composable("forgot") {
            ForgotPasswordScreen(
                viewModel = passwordVm,
                onNext    = { nav.navigate("verify") }
            )
        }

        composable("verify") {
            VerifyCodeScreen(
                viewModel = passwordVm,
                onNext    = { nav.navigate("reset") }
            )
        }

        composable("reset") {
            ResetPasswordScreen(
                viewModel  = passwordVm,
                onFinish   = {
                    nav.navigate("login") {
                        popUpTo("forgot") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen() // ya inyecta su propio HomeViewModel
        }
    }
}
