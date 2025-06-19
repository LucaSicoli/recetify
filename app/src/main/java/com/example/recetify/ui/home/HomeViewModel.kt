// app/src/main/java/com/example/recetify/ui/home/HomeViewModel.kt
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
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    // Para detectar si hay red
    private val connectivity =
        app.getSystemService(ConnectivityManager::class.java)!!

    private val dao  = DatabaseProvider.getInstance(app).recipeDao()
    private val repo = RecipeRepository(dao, RetrofitClient.api, connectivity)

    // Indicador de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Lista de recetas (actualiza _isLoading cuando emite)
    val recipes: StateFlow<List<RecipeEntity>> =
        repo.getAllRecipes()
            .onStart {
                // justo antes de pedir la lista, muestro loading
                _isLoading.value = true
            }
            .onEach {
                // tras recibir la primera emisión (cache o red), oculto loading
                _isLoading.value = false
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )
}