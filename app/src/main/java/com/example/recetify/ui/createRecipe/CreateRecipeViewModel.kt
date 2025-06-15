// app/src/main/java/com/example/recetify/ui/createRecipe/CreateRecipeViewModel.kt
package com.example.recetify.ui.createRecipe

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.RecipeRepository
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeIngredientRequest
import com.example.recetify.data.remote.model.RecipeRequest
import com.example.recetify.data.remote.model.RecipeStepRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Manual provider para inyección del repositorio (sin Hilt).
 */
object RecipeRepositoryProvider {
    fun get(context: Context): RecipeRepository {
        val db = DatabaseProvider.getInstance(context)
        val dao = db.recipeDao()
        val connectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // RetrofitClient.api es una instancia de ApiService
        return RecipeRepository(
            dao = dao,
            api = RetrofitClient.api,
            connectivity = connectivity
        )
    }
}

/**
 * Factory para CreateRecipeViewModel, inyectando el repositorio.
 */
class CreateRecipeViewModelFactory(
    private val app: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRecipeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateRecipeViewModel(
                app = app,
                repo = RecipeRepositoryProvider.get(app)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * ViewModel para la pantalla de creación de recetas.
 */
class CreateRecipeViewModel(
    app: Application,
    private val repo: RecipeRepository
) : AndroidViewModel(app) {

    private val _uploading = MutableStateFlow(false)
    val uploading = _uploading.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting = _submitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Aquí guardamos la URL tras subir la foto
    var photoUrl: String? = null

    /**
     * Sube la foto seleccionada y guarda la URL devuelta.
     */
    fun uploadPhoto(file: File) = viewModelScope.launch {
        _uploading.value = true
        _error.value = null
        try {
            photoUrl = repo.uploadPhoto(file)
        } catch (t: Throwable) {
            _error.value = t.localizedMessage
        } finally {
            _uploading.value = false
        }
    }

    /**
     * Construye el RecipeRequest y lo envía al backend.
     */
    fun createRecipe(
        nombre: String,
        descripcion: String,
        tiempo: Int,
        porciones: Int,
        tipoPlato: String,
        categoria: String,
        ingredients: List<RecipeIngredientRequest>,
        steps: List<RecipeStepRequest>,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        _submitting.value = true
        _error.value = null
        try {
            val req = RecipeRequest(
                nombre = nombre,
                descripcion = descripcion,
                tiempo = tiempo,
                porciones = porciones,
                fotoPrincipal = photoUrl,
                tipoPlato = tipoPlato,
                categoria = categoria,
                ingredients = ingredients,
                steps = steps
            )
            repo.createRecipe(req)
            onSuccess()
        } catch (t: Throwable) {
            _error.value = t.localizedMessage
        } finally {
            _submitting.value = false
        }
    }
}