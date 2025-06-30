package com.example.recetify.ui.home

import android.app.Application
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.RecipeRepository
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.db.RecipeEntity
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    // Para detectar si hay red
    private val connectivity =
        app.getSystemService(ConnectivityManager::class.java)!!

    private val dao  = DatabaseProvider.getInstance(app).recipeDao()
    private val repo = RecipeRepository(dao, RetrofitClient.api, connectivity)

    // Indicador de carga para la lista de recetas
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 1) Lista de recetas cacheadas (Room) que oculta el loading tras la primera emisión
    val recipes: StateFlow<List<RecipeEntity>> =
        repo.getAllRecipes()
            .onStart {
                _isLoading.value = true
            }
            .onEach {
                _isLoading.value = false
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )

    // 2) Lista de resúmenes remotos (para foto de perfil, alias, rating…)
    private val _summaries = MutableStateFlow<List<RecipeSummaryResponse>>(emptyList())
    val summaries: StateFlow<List<RecipeSummaryResponse>> = _summaries.asStateFlow()

    init {
        // Carga inicial de los resúmenes desde la API
        viewModelScope.launch {
            try {
                val list = RetrofitClient.api.getAllRecipesSummary()
                _summaries.value = list
            } catch (e: Exception) {
                // Aquí podrías loguear o manejar el error
            }
        }
    }
}