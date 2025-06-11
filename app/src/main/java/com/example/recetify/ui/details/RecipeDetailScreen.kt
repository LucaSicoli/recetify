package com.example.recetify.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    navController: NavController,
    viewModel: RecipeDetailViewModel = viewModel()
) {
    val recipe = viewModel.recipe
    val ratings = viewModel.ratings
    val loading = viewModel.loading
    val scope = rememberCoroutineScope()

    val showIngredients = remember { mutableStateOf(true) }
    val currentStep   = remember { mutableStateOf(0) }

    LaunchedEffect(recipeId) {
        viewModel.fetchRecipe(recipeId)
    }

    Scaffold(containerColor = Color.White) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                recipe?.let { receta ->
                    Column(modifier = Modifier.padding(bottom = 100.dp)) {
                        RecipeDetailContent(
                            receta = receta,
                            padding = PaddingValues(0.dp),
                            showIngredients = showIngredients,
                            currentStep   = currentStep,
                            navController = navController,
                            viewModel     = viewModel
                        )
                    }
                }
            }
        }
    }
}