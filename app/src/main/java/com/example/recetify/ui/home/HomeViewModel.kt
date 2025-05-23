// HomeViewModel.kt
package com.example.recetify.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.model.RecipeResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _recipes = MutableStateFlow<List<RecipeResponse>>(emptyList())
    val recipes: StateFlow<List<RecipeResponse>> = _recipes

    init {
        viewModelScope.launch {
            try {
                _recipes.value = RetrofitClient.api.getAllRecipes()
            } catch (e: Throwable) {
            }
        }
    }
}
