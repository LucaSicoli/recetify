// app/src/main/java/com/example/recetify/ui/details/RecipeDetailViewModel.kt
package com.example.recetify.ui.details

import android.app.Application
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.RecipeDetailRepository
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.CreateRatingRequest
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.db.RecipeWithDetails
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.compose.runtime.*

class RecipeDetailViewModel(app: Application) : AndroidViewModel(app) {
    private val connectivity = app.getSystemService(ConnectivityManager::class.java)!!
    private val detailDao   = DatabaseProvider.getInstance(app).recipeDetailDao()
    private val repo = RecipeDetailRepository(
        detailDao    = detailDao,
        api          = RetrofitClient.api,
        connectivity = connectivity
    )

    // Estado de la receta con todo detalle
    var recipeWithDetails by mutableStateOf<RecipeWithDetails?>(null)
        private set

    var loading by mutableStateOf(true)
        private set

    // Estado para manejar la confirmación del comentario
    var commentSubmitted by mutableStateOf(false)
        private set

    var commentError by mutableStateOf<String?>(null)
        private set

    /** Carga la receta (y sus ratings) desde Repo (red ↔ caché) */
    fun fetchRecipe(recipeId: Long) {
        loading = true
        viewModelScope.launch {
            repo.getRecipeDetail(recipeId)
                .collect { details ->
                    recipeWithDetails = details
                    loading = false
                }
        }
    }

    /** Publicar un rating y recargar detalle */
    fun postRating(recipeId: Long, comentario: String, puntos: Int) {
        viewModelScope.launch {
            try {
                commentError = null
                RetrofitClient.api.addRating(CreateRatingRequest(recipeId, comentario, puntos))
                commentSubmitted = true
                // Recargar los datos para obtener los comentarios actualizados
                fetchRecipe(recipeId)
            } catch (e: Exception) {
                commentError = "Error al enviar el comentario: ${e.message}"
            }
        }
    }

    // Función para resetear el estado de confirmación
    fun resetCommentSubmitted() {
        commentSubmitted = false
        commentError = null
    }

    /**
     * Alterna el estado favorito:
     * - Si ya está guardada → la quita
     * - Si no → la guarda
     */
    fun toggleFavorite(recipeId: Long, currentlyFavorite: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                if (currentlyFavorite) {
                    RetrofitClient.api.unsaveRecipe(recipeId)
                } else {
                    RetrofitClient.api.saveRecipe(recipeId)
                }
            } catch (_: Exception) { /* ignoro errores simples */ }
            // notificar que terminó (la UI recargará la lista de favoritos)
            onDone()
        }
    }
}