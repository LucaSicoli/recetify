package com.example.recetify.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

class RecipeDetailViewModel : ViewModel() {

    var recipe by mutableStateOf<RecipeResponse?>(null)
        private set

    var loading by mutableStateOf(true)
        private set

    fun fetchRecipe(id: Long) {
        viewModelScope.launch {
            loading = true
            val start = System.currentTimeMillis()
            try {
                recipe = RetrofitClient.api.getRecipeById(id)
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
}
