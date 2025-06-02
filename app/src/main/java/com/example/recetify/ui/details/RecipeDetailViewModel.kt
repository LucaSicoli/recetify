package com.example.recetify.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.RatingResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.example.recetify.data.remote.model.CreateRatingRequest

class RecipeDetailViewModel : ViewModel() {

    var recipe by mutableStateOf<RecipeResponse?>(null)
        private set

    var loading by mutableStateOf(true)
        private set

    var ratings by mutableStateOf<List<RatingResponse>>(emptyList())
        private set

    fun fetchRecipe(recipeId: Long) {
        loading = true
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            try {
                recipe = RetrofitClient.api.getRecipeById(recipeId)
                ratings = RetrofitClient.api.getRatingsForRecipe(recipeId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                val elapsed = System.currentTimeMillis() - start
                val remaining = 400 - elapsed
                if (remaining > 0) delay(remaining)
                loading = false
            }
        }
    }

    fun postRating(recipeId: Long, comentario: String, puntos: Int) {
        viewModelScope.launch {
            try {
                val nuevo = CreateRatingRequest(
                    recipeId = recipeId,
                    comentario = comentario,
                    puntos = puntos
                )
                RetrofitClient.api.addRating(nuevo)
                ratings = RetrofitClient.api.getRatingsForRecipe(recipeId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
