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
import com.example.recetify.ui.profile.*
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
    val context       = LocalContext.current
    val navController = rememberNavController()
    val scope         = rememberCoroutineScope()

    // ViewModels únicos
    val passwordVm: PasswordResetViewModel = viewModel()
    val draftVm: DraftViewModel           = viewModel()
    val favVm: FavouriteViewModel         = viewModel()

    // Estados de sesión y conexión
    val isAlumno   by SessionManager.isAlumnoFlow(context).collectAsState(initial = false)
    val isLoggedIn by SessionManager.isLoggedInFlow(context).collectAsState(initial = false)
    val isOnline   by rememberIsOnline()
    var offline by rememberSaveable { mutableStateOf(!isOnline) }
    LaunchedEffect(isOnline) { offline = !isOnline }

    Box(Modifier.fillMaxSize()) {
        NavHost(navController, startDestination = "login") {

            // 1) Login / Visitor
            composable("login") {
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
                    onVisitor = {
                        scope.launch {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    onForgot = { navController.navigate("forgot") }
                )
            }

            // 2) Password reset flow
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

            // 3) Home & Detail
            composable("home") {
                if (!isLoggedIn) {
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                } else {
                    // IMPORTANTE: pasar por nombre
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

            // 4) Create Recipe
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

            // 5) Profile flow
            composable("profile") {
                // refresca drafts y favs
                LaunchedEffect(Unit) {
                    draftVm.refresh()
                    favVm.loadFavourites()
                }
                ProfileScreen(navController = navController)
            }
            // endpoints para tus pantallas de perfil
            composable("drafts") {
                DraftsScreen(onDraftClick = { id ->
                    navController.navigate("recipe/$id")
                })
            }
            composable("saved") {
                SavedRecipesScreen(onRecipeClick = { id ->
                    navController.navigate("recipe/$id")
                })
            }
            composable("myRecipes") {
                MyRecipesScreen(onRecipeClick = { id ->
                    navController.navigate("recipe/$id")
                })
            }
            composable("profileInfo") {
                ProfileInfoScreen(navController = navController)
            }
        }

        // BottomNavBar en home/recipe/create/profile
        val backStackEntry by navController.currentBackStackEntryAsState()
        val route = backStackEntry?.destination?.route ?: ""
        if (!offline && (
                    route == "home" ||
                            route.startsWith("recipe/") ||
                            route == "createRecipe" ||
                            route == "profile"
                    )
        ) {
            Box(Modifier.align(Alignment.BottomCenter)) {
                BottomNavBar(navController, isAlumno)
            }
        }

        // Overlay "Sin conexión"
        if (offline) {
            NoConnectionScreen(onContinueOffline = { offline = false })
        }
    }
}