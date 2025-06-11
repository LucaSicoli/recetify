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
import android.app.Application
import androidx.navigation.NavHostController
import kotlin.text.Typography.dagger
import com.example.recetify.ui.profile.SavedRecipesScreen
import com.example.recetify.ui.favorites.FavoritesScreen
import com.example.recetify.ui.profile.ProfileScreen
import com.example.recetify.ui.profile.MyRecipesScreen
import com.example.recetify.ui.profile.ProfileInfoScreen

//import dagger.hilt.android.HiltAndroidApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecetifyTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
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

                // Pantalla de login
                composable("login") {
                    val loginVm: LoginViewModel = viewModel()
                    LoginScreen(
                        viewModel = loginVm,
                        onLoginSuccess = { token ->
                            SessionManager.authToken = token
                            nav.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onForgot = { nav.navigate("forgot") }
                    )
                }

                // Recuperar contraseña - paso 1
                composable("forgot") {
                    ForgotPasswordScreen(
                        viewModel = passwordVm,
                        onNext = { nav.navigate("verify") }
                    )
                }

                // Recuperar contraseña - paso 2
                composable("verify") {
                    VerifyCodeScreen(
                        viewModel = passwordVm,
                        onNext = { nav.navigate("reset") }
                    )
                }

                // Recuperar contraseña - paso 3 (resetear)
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

                // Pantalla principal (post login)
                composable("home") {
                    HomeScreen(navController = nav)
                }

                // Perfil del usuario
                composable("profile") {
                    ProfileScreen(navController = nav)
                }

                // Recetas guardadas
                composable("saved_recipes") {
                    SavedRecipesScreen(onBack = { nav.popBackStack() })
                }

                // Mis recetas
                composable("my_recipes") {
                    MyRecipesScreen(onBack = { nav.popBackStack() })
                }

                // Información del perfil
                composable("profile_info") {
                    ProfileInfoScreen(onBack = { nav.popBackStack() })
                }
            } }


