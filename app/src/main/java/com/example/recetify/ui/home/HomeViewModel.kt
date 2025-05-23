package com.example.recetify.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _recipes   = MutableStateFlow<List<RecipeResponse>>(emptyList())
    val recipes: StateFlow<List<RecipeResponse>> = _recipes

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            val fetched = try {
                RetrofitClient.api.getAllRecipes()
            } catch (e: Throwable) {
                emptyList<RecipeResponse>()
            }
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < 1_000) {
                delay(1_000 - elapsed)
            }
            // 3) Actualiza estado
            _recipes.value = fetched
            _isLoading.value = false
        }
    }
}
