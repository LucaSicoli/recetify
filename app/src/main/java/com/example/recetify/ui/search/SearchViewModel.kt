// File: app/src/main/java/com/example/recetify/ui/search/SearchViewModel.kt
package com.example.recetify.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.ApiService
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import com.example.recetify.data.remote.model.UserSavedRecipeDTO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repo: SearchRepository,
    private val api: ApiService
) : ViewModel() {

    private val _name        = MutableStateFlow<String?>(null)
    private val _tipoPlato   = MutableStateFlow<String?>(null)
    private val _categoria   = MutableStateFlow<String?>(null)
    private val _ingredient  = MutableStateFlow<String?>(null)
    private val _exclude     = MutableStateFlow<String?>(null)
    private val _userAlias   = MutableStateFlow<String?>(null)
    private val _sort        = MutableStateFlow("newest")
    private val _rating      = MutableStateFlow<Int?>(null)

    private val _results = MutableStateFlow<List<RecipeSummaryResponse>>(emptyList())
    val results: StateFlow<List<RecipeSummaryResponse>> = _results

    private val _savedIds = MutableStateFlow<Set<Long>>(emptySet())
    val savedIds: StateFlow<Set<Long>> = _savedIds

    init {
        viewModelScope.launch {
            // Wrap in runCatching so failures don’t crash the ViewModel
            runCatching {
                api.listSavedRecipes()
                    .map(UserSavedRecipeDTO::recipeId)
                    .toSet()
            }.onSuccess { ids ->
                _savedIds.value = ids
            }.onFailure {
                // offline or unauthorized → just leave _savedIds empty
            }
        }
    }

    fun doSearch() = viewModelScope.launch {
        val fetched = runCatching {
            repo.search(
                name              = _name.value,
                type              = _tipoPlato.value,
                ingredient        = _ingredient.value,
                excludeIngredient = _exclude.value,
                userAlias         = _userAlias.value,
                sort              = _sort.value
            )
        }.getOrDefault(emptyList())    // on failure, fall back to an empty list

        val byRating = _rating.value
            ?.let { th -> fetched.filter { (it.promedioRating ?: 0.0) >= th } }
            ?: fetched

        val byCategory = _categoria.value
            ?.let { cat -> byRating.filter { it.categoria.equals(cat, ignoreCase = true) } }
            ?: byRating

        _results.value = byCategory
    }

    fun toggleSave(recipeId: Long) = viewModelScope.launch {
        val currently = recipeId in _savedIds.value

        // Optimistic update
        _savedIds.update { set ->
            if (currently) set - recipeId else set + recipeId
        }

        runCatching {
            if (currently) api.unsaveRecipe(recipeId)
            else          api.saveRecipe(recipeId)
        }.onFailure {
            // Revertir en caso de error
            _savedIds.update { set ->
                if (currently) set + recipeId else set - recipeId
            }
        }
    }

    // ── Funciones de actualización de filtros ─────────────────

    fun updateName(v: String?)           { _name.value = v }
    fun updateTipoPlato(v: String?)     { _tipoPlato.value = v }
    fun updateCategoria(v: String?)     { _categoria.value = v }
    fun updateIngredient(v: String?)    { _ingredient.value = v }
    fun updateExclude(v: String?)       { _exclude.value = v }
    fun updateUserAlias(v: String?)     { _userAlias.value = v }
    fun updateSort(v: String)           { _sort.value = v }
    fun updateRating(v: Int?)           { _rating.value = v }

    // ── Exposición de los StateFlows ────────────────────────

    val tipoPlato: StateFlow<String?>    get() = _tipoPlato
    val categoria: StateFlow<String?>    get() = _categoria
    val ingredient: StateFlow<String?>   get() = _ingredient
    val exclude: StateFlow<String?>      get() = _exclude
    val userAlias: StateFlow<String?>    get() = _userAlias
    val sortOrder: StateFlow<String>     get() = _sort
    val rating: StateFlow<Int?>          get() = _rating

    companion object {
        fun provideFactory(repo: SearchRepository, api: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass == SearchViewModel::class.java)
                    return SearchViewModel(repo, api) as T
                }
            }
    }
}