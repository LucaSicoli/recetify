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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recetify.data.remote.model.SessionManager
import com.example.recetify.ui.common.NoConnectionScreen
import com.example.recetify.ui.common.rememberIsOnline
import com.example.recetify.ui.createRecipe.CreateRecipeScreen
import com.example.recetify.ui.createRecipe.CreateRecipeViewModel
import com.example.recetify.ui.createRecipe.CreateRecipeViewModelFactory
import com.example.recetify.ui.createRecipe.EditRecipeScreen
import com.example.recetify.ui.details.LocalRecipeDetailScreen
import com.example.recetify.ui.details.RecipeDetailScreen
import com.example.recetify.ui.home.HomeScreen
import com.example.recetify.ui.login.*
import com.example.recetify.ui.navigation.BottomNavBar
import com.example.recetify.ui.profile.*
import com.example.recetify.ui.theme.RecetifyTheme
import kotlinx.coroutines.launch
// …otros imports…
import com.example.recetify.ui.search.SearchScreen

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
    val myRecipesVm: MyRecipesViewModel   = viewModel()
    val reviewCountVm: ReviewCountViewModel = viewModel()


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
                    onLoginSuccess = { token, email ->
                        scope.launch {
                            // ahora pasamos también el email:
                            SessionManager.setAlumno(context, token, email)
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
                    onForgot  = { navController.navigate("forgot") }
                )
            }

            // 2) Password reset flow
            composable("forgot") {
                ForgotPasswordScreen(
                    navController = navController,
                    viewModel = passwordVm,
                    onNext    = { navController.navigate("verify") },
                )
            }
            composable("verify") {
                VerifyCodeScreen(
                    viewModel = passwordVm,
                    onNext    = { navController.navigate("reset") },
                    navController = navController
                )
            }
            composable("reset") {
                ResetPasswordScreen(
                    viewModel = passwordVm,
                    onFinish  = {
                        navController.navigate("login?passwordChanged=1") {
                            popUpTo("forgot") { inclusive = true }
                        }
                    },
                    navController = navController
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

            composable("search") {
                SearchScreen(navController = navController)
            }


            composable(
                route = "recipe/{id}?photo={photo}",
                arguments = listOf(
                    navArgument("id")   { type = NavType.LongType },
                    navArgument("photo"){ type = NavType.StringType; defaultValue = "" }
                )
            ) { back ->
                val id    = back.arguments!!.getLong("id")
                // decodificamos la URL si vino no vacía
                val photo = back.arguments!!
                    .getString("photo")
                    ?.takeIf(String::isNotBlank)
                    ?.let { java.net.URLDecoder.decode(it, "UTF-8") }

                RecipeDetailScreen(
                    recipeId        = id,
                    profilePhotoUrl = photo,              // ← aquí le pasamos la foto
                    navController   = navController
                )
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
                    onSaved     = { navController.navigate("drafts") },
                    onPublished = { navController.navigate("myRecipes") },
                    onEditExisting = { existingRecipeId ->
                        // Navegar a EditRecipeScreen con la receta existente
                        navController.navigate("editRecipe/$existingRecipeId") {
                            // Limpiar el stack para evitar volver a crear receta
                            popUpTo("createRecipe") { inclusive = true }
                        }
                    }
                )
            }
            composable("editRecipe/{recipeId}") { backStack ->
                val recipeId = backStack.arguments
                    ?.getString("recipeId")
                    ?.toLongOrNull() ?: return@composable

                // Usamos el mismo ViewModel de creación, inyectado igual que en Create
                val vm: CreateRecipeViewModel =
                    viewModel(factory = CreateRecipeViewModelFactory(
                        LocalContext.current.applicationContext as Application
                    ))

                EditRecipeScreen(
                    recipeId    = recipeId,
                    viewModel   = vm,
                    onClose     = { navController.popBackStack() },
                    onSaved     = {
                        // 1) recarga borradores
                        draftVm.refresh()
                        // 2) recarga publicadas
                        myRecipesVm.refresh()
                        // 3) volvemos atrás
                        navController.navigate("profile") {
                            popUpTo("profile") { inclusive = false }
                        }
                    },
                    onPublished = {
                        draftVm.refresh()
                        myRecipesVm.refresh()
                        navController.navigate("profile") {
                            popUpTo("profile") { inclusive = false }
                        }
                    }
                )
            }

            // 5) Profile flow
            composable("profile") {
                // 1) observa la entrada actual del NavController:
                val backStackEntry by navController.currentBackStackEntryAsState()
                // 2) siempre que cambie (o sea, vuelvas aquí), recarga TODOS tus VMs:
                LaunchedEffect(backStackEntry) {
                    draftVm.refresh()
                    favVm.loadFavourites()
                    myRecipesVm.refresh()
                    reviewCountVm.loadCount()
                }
                ProfileScreen(
                    navController   = navController,
                    draftVm         = draftVm,
                    favVm           = favVm,
                    myRecipesVm     = myRecipesVm,
                    reviewCountVm   = reviewCountVm
                )
            }
            // endpoints para tus pantallas de perfil
            composable("drafts") {
                DraftsScreen(
                    draftVm = draftVm,
                    onDraftClick = { id ->
                        navController.navigate("editRecipe/$id")
                    }
                )
            }
            composable("saved") {
                SavedRecipesScreen(
                    onRecipeClick = { id ->
                        navController.navigate("recipe/$id")
                    },
                    onLocalRecipeClick = { localId ->
                        navController.navigate("localRecipe/$localId")
                    }
                )
            }

            composable(
                "localRecipe/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                // Aquí faltaba el navController
                LocalRecipeDetailScreen(
                    localRecipeId = id,
                    navController = navController   // ← pásalo aquí
                )
            }

            composable("myRecipes") {
                MyRecipesScreen(onRecipeClick = { id ->
                    navController.navigate("editRecipe/$id")
                })
            }
            composable("profileInfo") {
                ProfileInfoScreen(navController = navController)
            }

            composable("login?passwordChanged={passwordChanged}", arguments = listOf(
                navArgument("passwordChanged") { type = NavType.StringType; defaultValue = "0" }
            )) { backStackEntry ->
                val passwordChanged = backStackEntry.arguments?.getString("passwordChanged") == "1"
                LoginScreen(
                    viewModel      = viewModel<LoginViewModel>(),
                    onLoginSuccess = { token, email ->
                        scope.launch {
                            SessionManager.setAlumno(context, token, email)
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
                    onForgot  = { navController.navigate("forgot") },
                    passwordChanged = passwordChanged
                )
            }
        }

        // BottomNavBar en home/recipe/create/profile
        val backStackEntry by navController.currentBackStackEntryAsState()
        val route = backStackEntry?.destination?.route ?: ""
        if (!offline && (
                    route == "home" ||
                            route.startsWith("recipe/") ||
                            route == "search" ||
                            route == "createRecipe" ||
                            route == "profile" ||
                            route == "drafts" ||
                            route == "saved" ||
                            route == "myRecipes" ||
                            route == "profileInfo"
                    )
        ) {
            Box(Modifier.align(Alignment.BottomCenter)) {
                BottomNavBar(navController, isAlumno)
            }
        }

        // Overlay "Sin conexión"
        if (offline) {
            NoConnectionScreen(onContinueOffline = {
                // Setear modo visitante y navegar a home
                scope.launch {
                    SessionManager.setVisitante(context)
                    offline = false
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            })
        }
    }
}