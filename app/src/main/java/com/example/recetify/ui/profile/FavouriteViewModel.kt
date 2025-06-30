package com.example.recetify.ui.profile

import android.app.Application
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.RecipeRepository
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.UserSavedRecipeDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavouriteViewModel(application: Application) : AndroidViewModel(application) {
    private val connectivity = application
        .getSystemService(ConnectivityManager::class.java)!!

    private val repo = RecipeRepository(
        dao = DatabaseProvider.getInstance(application).recipeDao(),
        api = RetrofitClient.api,
        connectivity = connectivity
    )

    private val _favourites = MutableStateFlow<List<UserSavedRecipeDTO>>(emptyList())
    val favourites: StateFlow<List<UserSavedRecipeDTO>> = _favourites.asStateFlow()

    init {
        loadFavourites()
    }

    fun removeFavorite(recipeId: Long) {
        viewModelScope.launch {
            try {
                // llama al endpoint DELETE /recipes/{id}/save
                RetrofitClient.api.unsaveRecipe(recipeId)
            } catch (_: Exception) { /* opcional: loguear error */ }
            // recarga la lista para que la UI se actualice
            loadFavourites()
        }
    }

    /** Carga o recarga las recetas guardadas */
    fun loadFavourites() {
        viewModelScope.launch {
            _favourites.value = try {
                repo.listSavedRecipes()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}