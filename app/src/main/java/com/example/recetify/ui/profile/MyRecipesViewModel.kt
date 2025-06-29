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

    /** Trae las recetas PUBLICADAS del usuario autenticado */
    fun loadMyRecipes() {
        viewModelScope.launch {
            try {
                // Usamos RetrofitClient.api, que es tu ApiService
                val fetched = RetrofitClient.api.getMyPublishedRecipes()
                _recipes.value = fetched
            } catch (e: Exception) {
                // Si falla, dejamos la lista vacía (y podrías loguear)
                _recipes.value = emptyList()
            }
        }
    }

    fun refresh() = loadMyRecipes()

}