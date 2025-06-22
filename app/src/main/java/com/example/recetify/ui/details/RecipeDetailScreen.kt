package com.example.recetify.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.recetify.data.remote.model.toRecipeResponse
import com.example.recetify.data.remote.model.toRatingResponse
import com.example.recetify.ui.navigation.BottomNavBar

@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    navController: NavController,
    viewModel: RecipeDetailViewModel = viewModel()
) {
    val details          = viewModel.recipeWithDetails
    val loading          = viewModel.loading
    val showIngredients  = remember { mutableStateOf(true) }
    val currentStep      = remember { mutableStateOf(0) }

    LaunchedEffect(recipeId) {
        viewModel.fetchRecipe(recipeId)
    }

    Scaffold(
        bottomBar     = { BottomNavBar(navController) },
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
                // <-- Aquí cambias `toModel()` por `toRatingResponse()`
                val ratingResponses = details.ratings.map { it.toRatingResponse() }

                Column(modifier = Modifier.padding(bottom = 0.dp)) {
                    RecipeDetailContent(
                        receta          = details.toRecipeResponse(),
                        ratings         = ratingResponses,
                        padding         = PaddingValues(0.dp),
                        showIngredients = showIngredients,
                        currentStep     = currentStep,
                        navController   = navController,
                        onSendRating    = { comentario, puntos ->
                            viewModel.postRating(details.recipe.id, comentario, puntos)
                        }
                    )
                }
            }
        }
    }
}