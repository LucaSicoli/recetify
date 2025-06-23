package com.example.recetify.ui.profile

import android.app.Application
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.RecipeRepository
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar los borradores del usuario autenticado.
 * Carga la lista de RecipeSummaryResponse desde la API y la expone como StateFlow.
 */
class DraftViewModel(application: Application) : AndroidViewModel(application) {

    private val connectivity = application
        .getSystemService(ConnectivityManager::class.java)!!

    private val repo = RecipeRepository(
        dao = DatabaseProvider.getInstance(application).recipeDao(),
        api = RetrofitClient.api,
        connectivity = connectivity
    )

    private val _drafts = MutableStateFlow<List<RecipeSummaryResponse>>(emptyList())
    /** Lista de borradores del usuario, cada uno con alias, rating y estados */
    val drafts: StateFlow<List<RecipeSummaryResponse>> = _drafts

    init {
        // Al iniciar, recuperamos la lista de borradores
        refresh()
    }

    /**
     * Refresca la lista de borradores consultando la API.
     * En caso de error (por ejemplo, sin conexión), emite lista vacía.
     */
    fun refresh() {
        viewModelScope.launch {
            _drafts.value = try {
                repo.listDrafts()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}