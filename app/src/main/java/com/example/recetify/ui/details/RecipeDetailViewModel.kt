package com.example.recetify.ui.details

import android.app.Application
import android.net.ConnectivityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.RatingResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.example.recetify.data.RecipeDetailRepository
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.db.RecipeWithDetails
import com.example.recetify.data.remote.model.CreateRatingRequest

class RecipeDetailViewModel(app: Application) : AndroidViewModel(app) {
    // ➊ Obtenemos ConnectivityManager y Dao
    private val connectivity = app.getSystemService(ConnectivityManager::class.java)!!
    private val detailDao   = DatabaseProvider.getInstance(app).recipeDetailDao()

    // ➋ Creamos el repositorio que ya mezcla red ↔ cache
    private val repo = RecipeDetailRepository(
        detailDao    = detailDao,
        api          = RetrofitClient.api,
        connectivity = connectivity
    )

    // ➌ Exponemos un único estado con todo RecipeWithDetails
    var recipeWithDetails by mutableStateOf<RecipeWithDetails?>(null)
        private set

    var loading by mutableStateOf(true)
        private set

    fun fetchRecipe(recipeId: Long) {
        loading = true
        viewModelScope.launch {
            // ➍ Recolectamos el flujo (red + Room) y actualizamos UI
            repo.getRecipeDetail(recipeId)
                .collect { details ->
                    recipeWithDetails = details
                    loading = false
                }
        }
    }

    fun postRating(recipeId: Long, comentario: String, puntos: Int) {
        // (opcional) aquí podrías invocar al API y volver a refrescar
        viewModelScope.launch {
            RetrofitClient.api.addRating(CreateRatingRequest(recipeId, comentario, puntos))
            fetchRecipe(recipeId)
        }
    }
}