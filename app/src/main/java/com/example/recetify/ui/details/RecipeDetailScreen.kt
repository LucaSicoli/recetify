package com.example.recetify.ui.details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.recetify.data.remote.model.SessionManager
import com.example.recetify.data.remote.model.toRecipeResponse
import com.example.recetify.data.remote.model.toRatingResponse
import com.example.recetify.ui.navigation.BottomNavBar
import com.example.recetify.ui.profile.CustomTasteViewModel
import com.example.recetify.ui.profile.FavouriteViewModel
import com.example.recetify.ui.common.ReviewSubmittedDialog
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    navController: NavController,
    profilePhotoUrl: String?,
    viewModel: RecipeDetailViewModel = viewModel(),
    favVm: FavouriteViewModel = viewModel(), // inyectamos el VM de favoritos
    customVm: CustomTasteViewModel = viewModel(),
    from: String? = null,
    onNavigateWithLoading: ((String) -> Unit)? = null // <-- nuevo callback
) {
    val context = LocalContext.current
    val details = viewModel.recipeWithDetails
    val loading = viewModel.loading
    val showIngredients = remember { mutableStateOf(true) }
    val currentStep = remember { mutableStateOf(0) }
    val isAlumno by SessionManager
        .isAlumnoFlow(context)
        .collectAsState(initial = false)

    // Estados del comentario - cambiamos por el nuevo diálogo
    val showReviewSubmittedDialog = viewModel.showReviewSubmittedDialog
    val commentError = viewModel.commentError

    // Observamos la lista de guardados y calculamos si esta receta está guardada
    val savedList by favVm.favourites.collectAsState()
    val isFav = remember(savedList) { savedList.any { it.recipeId == recipeId } }

    // Snackbar solo para errores
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Efecto para mostrar errores en snackbar
    LaunchedEffect(commentError) {
        commentError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetCommentError()
            }
        }
    }

    LaunchedEffect(recipeId) {
        viewModel.fetchRecipe(recipeId)
        favVm.loadFavourites()
    }

    // Mostrar el diálogo de reseña enviada
    if (showReviewSubmittedDialog) {
        ReviewSubmittedDialog(
            onDismiss = { viewModel.dismissReviewDialog() }
        )
    }

    // Interceptar el botón de volver atrás (hardware y UI)
    BackHandler {
        if (onNavigateWithLoading != null) {
            if (from == "search") {
                onNavigateWithLoading("search")
            } else if (from == "saved") {
                onNavigateWithLoading("saved")
            } else {
                onNavigateWithLoading("home")
            }
        } else {
            if (from == "search") {
                navController.popBackStack("search", inclusive = false)
                navController.navigate("search")
            } else if (from == "saved") {
                navController.popBackStack("saved", inclusive = false)
                navController.navigate("saved")
            } else {
                navController.popBackStack("home", inclusive = false)
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    }

    // Mostrar el diálogo de límite de recetas personalizadas
    val showLimitDialog by customVm.showLimitDialog.collectAsState()
    if (showLimitDialog) {
        com.example.recetify.ui.common.CustomLimitDialog(
            onDismiss = { customVm.dismissLimitDialog() }
        )
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, isAlumno) },
        containerColor = Color.White,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    CustomSnackbar(snackbarData = snackbarData)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading || details == null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    com.example.recetify.ui.common.LoadingScreen()
                }
            } else {
                val ratingResponses = details.ratings.map { it.toRatingResponse() }

                Column(modifier = Modifier.padding(bottom = 0.dp)) {
                    RecipeDetailContent(
                        receta            = details.toRecipeResponse(),
                        ratings           = ratingResponses,
                        padding           = PaddingValues(0.dp),
                        showIngredients   = showIngredients,
                        currentStep       = currentStep,
                        navController     = navController,
                        profileUrl        = profilePhotoUrl,
                        onSendRating      = { c, p -> viewModel.postRating(details.recipe.id, c, p) },
                        isFavorite        = isFav,
                        onToggleFavorite  = { viewModel.toggleFavorite(details.recipe.id, isFav) { favVm.loadFavourites() } },
                        onSaveEditedRecipe = { editedRecipe ->
                            // guarda una copia local en "Mi gusto"
                            customVm.addCustom(
                                editedRecipe,
                                onError = { msg ->
                                    // por ejemplo, mostrar un Snackbar o Toast
                                }
                            )
                        },
                        isAlumno = isAlumno,
                        from = from, // <-- pasar el parámetro
                        onNavigateWithLoading = onNavigateWithLoading // <-- pasar callback
                    )
                }
            }
        }
    }
}