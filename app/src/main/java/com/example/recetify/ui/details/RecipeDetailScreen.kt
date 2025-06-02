package com.example.recetify.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val currentStep = remember { mutableIntStateOf(0) }

    var newComment by remember { mutableStateOf("") }
    var ratingPoints by remember { mutableFloatStateOf(3f) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        viewModel.fetchRecipe(recipeId)
    }

    Scaffold(
        containerColor = Color.White
    ) { padding ->
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
                recipe?.let {
                    Column(modifier = Modifier.padding(bottom = 100.dp)) {
                        RecipeDetailContent(
                            receta = it,
                            padding = PaddingValues(0.dp),
                            showIngredients = showIngredients,
                            currentStep = currentStep,
                            navController = navController,
                            viewModel = viewModel
                        )

                        if (ratings.isNotEmpty()) {
                            Divider(Modifier.padding(vertical = 16.dp))
                            Text(
                                text = "Reseñas",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            ratings.forEach { rating ->
                                RatingItem(rating)
                            }
                        }

                        // Sección para crear comentario, siempre visible
                        Divider(Modifier.padding(vertical = 16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)), // gris claro
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        )
                        {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (ratings.isEmpty()) "¡Sé el primero en comentar!" else "Dejá tu comentario",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.Black
                                )

                                Spacer(Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = newComment,
                                    onValueChange = { newComment = it },
                                    label = { Text("Comentario") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "Puntaje: ${ratingPoints.toInt()}⭐",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Slider(
                                    value = ratingPoints,
                                    onValueChange = { ratingPoints = it },
                                    valueRange = 1f..5f,
                                    steps = 3
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                viewModel.postRating(
                                                    recipeId = recipeId,
                                                    comentario = newComment,
                                                    puntos = ratingPoints.toInt()
                                                )
                                                newComment = ""
                                                ratingPoints = 3f
                                                showSuccess = true
                                            }
                                        },
                                        enabled = newComment.isNotBlank()
                                    ) {
                                        Text("Enviar")
                                    }
                                }

                                if (showSuccess) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Comentario enviado exitosamente 🎉",
                                        color = Color(0xFF4CAF50),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
