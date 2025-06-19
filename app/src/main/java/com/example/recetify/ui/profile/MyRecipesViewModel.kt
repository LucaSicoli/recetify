package com.example.recetify.ui.profile

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import kotlinx.coroutines.launch

class MyRecipesViewModel : ViewModel() {
    private val _recipes = mutableStateOf<List<RecipeResponse>>(emptyList())
    val recipes: State<List<RecipeResponse>> = _recipes

    private val api = RetrofitClient.api

    init {
        fetchMyRecipes()
    }

    private fun fetchMyRecipes() {
        viewModelScope.launch {
            try {
                val result = api.getMyRecipes()
                Log.d("MyRecipesVM", "Recetas obtenidas: ${result.size}")
                _recipes.value = result
            } catch (e: Exception) {
                Log.e("MyRecipesVM", "Error fetching my recipes", e)
            }
        }
    }
}