package com.example.recetify.ui.details

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recetify.ui.common.LoadingScreen
import com.example.recetify.data.remote.model.RecipeResponse

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    viewModel: RecipeDetailViewModel = viewModel()
) {
    val showIngredients = remember { mutableStateOf(true) }
    val currentStep = remember { mutableStateOf(0) }

    // Llamar a la API una sola vez
    LaunchedEffect(recipeId) {
        viewModel.fetchRecipe(recipeId.toLong())
    }

    val receta = viewModel.recipe
    val isLoading = viewModel.loading

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFFFFF) // o MaterialTheme.colorScheme.background
    ) {
        if (isLoading) {
            LoadingScreen()
        } else {
            receta?.let {
                RecipeDetailContent(
                    receta = it,
                    padding = PaddingValues(bottom = 80.dp),
                    showIngredients = showIngredients,
                    currentStep = currentStep
                )
            }
        }
    }
}
