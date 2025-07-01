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

    // filtros y orden…
    private val _name       = MutableStateFlow<String?>(null)
    private val _type       = MutableStateFlow<String?>(null)
    private val _ingredient = MutableStateFlow<String?>(null)
    private val _exclude    = MutableStateFlow<String?>(null)
    private val _userAlias  = MutableStateFlow<String?>(null)
    private val _sort       = MutableStateFlow("name")
    private val _rating     = MutableStateFlow<Int?>(null)

    private val _results = MutableStateFlow<List<RecipeSummaryResponse>>(emptyList())
    val results: StateFlow<List<RecipeSummaryResponse>> = _results.asStateFlow()

    // Saved IDs de favoritos
    private val _savedIds = MutableStateFlow<Set<Long>>(emptySet())
    val savedIds: StateFlow<Set<Long>> = _savedIds.asStateFlow()

    init {
        // Carga favoritos al iniciar
        viewModelScope.launch {
            try {
                val list: List<UserSavedRecipeDTO> = api.listSavedRecipes()
                _savedIds.value = list.map { it.recipeId }.toSet()
            } catch (_: Exception) { /* ignore */ }
        }
    }

    // Lógica de búsqueda
    fun doSearch() = viewModelScope.launch {
        val fetched = try {
            repo.search(
                name              = _name.value,
                type              = _type.value,
                ingredient        = _ingredient.value,
                excludeIngredient = _exclude.value,
                userAlias         = _userAlias.value,
                sort              = _sort.value
            )
        } catch (_: Throwable) {
            emptyList()
        }
        _results.value = _rating.value
            ?.let { threshold ->
                fetched.filter { it.promedioRating ?: 0.0 >= threshold }
            }
            ?: fetched
    }

    // Toggle favorito
    fun toggleSave(recipeId: Long) = viewModelScope.launch {
        val currently = recipeId in _savedIds.value
        try {
            if (currently) api.unsaveRecipe(recipeId)
            else          api.saveRecipe(recipeId)
            // actualiza localmente
            _savedIds.value = if (currently) {
                _savedIds.value - recipeId
            } else {
                _savedIds.value + recipeId
            }
        } catch (_: Exception) { /* manejar error si quieres */ }
    }

    // Actualizadores de filtros…
    fun updateName(v: String?)        { _name.value = v }
    fun updateType(v: String?)        { _type.value = v }
    fun updateIngredient(v: String?)  { _ingredient.value = v }
    fun updateExclude(v: String?)     { _exclude.value = v }
    fun updateUserAlias(v: String?)   { _userAlias.value = v }
    fun updateSort(v: String)         { _sort.value = v }
    fun updateRating(v: Int?)         { _rating.value = v }

    val type: StateFlow<String?>       get() = _type
    val sortOrder: StateFlow<String>   get() = _sort
    val ingredient: StateFlow<String?> get() = _ingredient
    val userAlias: StateFlow<String?>  get() = _userAlias
    val rating: StateFlow<Int?>        get() = _rating

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