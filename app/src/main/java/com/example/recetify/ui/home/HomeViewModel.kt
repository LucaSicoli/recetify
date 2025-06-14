// ui/home/HomeViewModel.kt
package com.example.recetify.ui.home

import android.app.Application
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.RecipeRepository
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.db.RecipeEntity
import com.example.recetify.data.remote.RetrofitClient
import kotlinx.coroutines.flow.*

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val connectivity =
        app.getSystemService(ConnectivityManager::class.java)!!

    private val dao  = DatabaseProvider.getInstance(app).recipeDao()
    private val repo = RecipeRepository(dao, RetrofitClient.api, connectivity)

    val recipes: StateFlow<List<RecipeEntity>> =
        repo.getAllRecipes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )
}