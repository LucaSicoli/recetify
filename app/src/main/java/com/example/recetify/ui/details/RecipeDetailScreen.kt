package com.example.recetify.ui.details

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

@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    navController: NavController,
    profilePhotoUrl: String?,
    viewModel: RecipeDetailViewModel = viewModel(),
    favVm: FavouriteViewModel = viewModel(), // inyectamos el VM de favoritos
    customVm: CustomTasteViewModel = viewModel()

) {
    val context = LocalContext.current
    val details = viewModel.recipeWithDetails
    val loading = viewModel.loading
    val showIngredients = remember { mutableStateOf(true) }
    val currentStep = remember { mutableStateOf(0) }
    val isAlumno by SessionManager
        .isAlumnoFlow(context)
        .collectAsState(initial = false)

    // Observamos la lista de guardados y calculamos si esta receta está guardada
    val savedList by favVm.favourites.collectAsState()
    val isFav = remember(savedList) { savedList.any { it.recipeId == recipeId } }

    LaunchedEffect(recipeId) {
        viewModel.fetchRecipe(recipeId)
        favVm.loadFavourites()
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, isAlumno) },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading || details == null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
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
                        }
                    )
                }
            }
        }
    }
}