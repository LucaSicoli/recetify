// app/src/main/java/com/example/recetify/ui/profile/MyRecipesViewModel.kt
package com.example.recetify.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyRecipesViewModel(app: Application) : AndroidViewModel(app) {

    private val _recipes = MutableStateFlow<List<RecipeSummaryResponse>>(emptyList())
    val recipes: StateFlow<List<RecipeSummaryResponse>> = _recipes

    init {
        loadMyRecipes()
    }

    /** Trae TODAS las recetas del usuario autenticado (excepto borradores) */
    fun loadMyRecipes() {
        viewModelScope.launch {
            try {
                // Traemos todas las recetas del usuario (aprobadas, pendientes y rechazadas)
                val allRecipes = RetrofitClient.api.getMyPublishedRecipes() // O cambia a getAllMyRecipes() si renombras el endpoint
                _recipes.value = allRecipes
            } catch (e: Exception) {
                // Si falla, dejamos la lista vacía (y podrías loguear)
                _recipes.value = emptyList()
            }
        }
    }

    fun refresh() = loadMyRecipes()

}