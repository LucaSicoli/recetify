package com.example.recetify.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URI

class FavoritesViewModel(app: Application) : AndroidViewModel(app) {

    private val _allRecipes = MutableStateFlow<List<RecipeResponse>>(emptyList())
    val allRecipes: StateFlow<List<RecipeResponse>> = _allRecipes

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortField = MutableStateFlow("Nombre")
    val sortField: StateFlow<String> = _sortField

    private val _isAscending = MutableStateFlow(true)
    val isAscending: StateFlow<Boolean> = _isAscending

    private val _selectedTab = MutableStateFlow(0) // 0 = Mis Recetas, 1 = Favoritos
    val selectedTab: StateFlow<Int> = _selectedTab

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val filteredAndSortedRecipes: StateFlow<List<RecipeResponse>> = combine(
        _allRecipes, _searchQuery, _sortField, _isAscending
    ) { recipes, query, field, ascending ->
        val filtered = recipes.filter {
            it.nombre.contains(query, ignoreCase = true) ||
                    it.nombre.contains(query, ignoreCase = true)
        }
        val sorted = when (field) {
            "Nombre" -> if (ascending) filtered.sortedBy { it.nombre } else filtered.sortedByDescending { it.nombre }
            "Autor" -> if (ascending) filtered.sortedBy { it.usuarioCreadorAlias } else filtered.sortedByDescending { it.usuarioCreadorAlias }
            "Calificación" -> if (ascending) filtered.sortedBy { it.promedioRating ?: 0.0 } else filtered.sortedByDescending { it.promedioRating ?: 0.0 }
            else -> filtered // si el campo no coincide con ninguno, no se ordena
        }

        sorted
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadRecipesForTab(_selectedTab.value)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortField(field: String) {
        _sortField.value = field
    }

    fun toggleOrder() {
        _isAscending.value = !_isAscending.value
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
        loadRecipesForTab(index)
    }

    fun refresh() {
        loadRecipesForTab(_selectedTab.value)
    }

    private fun loadRecipesForTab(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val fetched = try {
                when (index) {
                    0 -> RetrofitClient.api.getAllRecipes()      //getMyRecipes()
                    1 -> RetrofitClient.api.getAllRecipes()      //getFavoriteRecipes()
                    else -> emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }

            // Preload imágenes con Coil
            val loader = ImageLoader(getApplication())
            val base = RetrofitClient.BASE_URL.trimEnd('/')

            fetched.forEach { recipe ->
                val original = recipe.fotoPrincipal.orEmpty()
                val pathOnly = runCatching {
                    val uri = URI(original)
                    uri.rawPath + uri.rawQuery?.let { "?$it" }.orEmpty()
                }.getOrNull() ?: original
                val finalUrl = if (pathOnly.startsWith("/")) "$base$pathOnly" else "$base/$pathOnly"

                val context = getApplication<Application>().applicationContext
                val request = ImageRequest.Builder(context)
                    .data(finalUrl)
                    .build()
                runCatching { loader.execute(request) }

            }

            _allRecipes.value = fetched
            _isLoading.value = false
        }
    }
}
