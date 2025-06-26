// app/src/main/java/com/example/recetify/ui/createRecipe/CreateRecipeViewModel.kt
package com.example.recetify.ui.createRecipe

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.RecipeRepository
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeIngredientRequest
import com.example.recetify.data.remote.model.RecipeRequest
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.RecipeStepRequest
import com.example.recetify.util.FileUtil
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
    private val app: Application,
    private val editingRecipeId: Long? = null
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
    private val repo: RecipeRepository,
    private val editingRecipeId: Long? = null
) : AndroidViewModel(app) {

    private val _uploading = MutableStateFlow(false)
    val uploading = _uploading.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting = _submitting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // ← Cambiado a StateFlow para poder hacer collectAsState()
    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl = _photoUrl.asStateFlow()



   /* init {
        if (editingRecipeId != null) {
            viewModelScope.launch {
                val receta = RetrofitClient.api.getRecipeById(editingRecipeId)
                loadFromRecipe(receta)
            }
        }
    }*/
    /*fun loadFromRecipe(recipe: RecipeResponse) {
        // Inicializa los campos desde la receta
        nombre.value        = recipe.nombre
        descripcion.value   = recipe.descripcion.orEmpty()
        tiempo.value        = recipe.tiempo.toString()
        porciones.value     = recipe.porciones.toString()
        categoria.value     = recipe.categoria
        tipoPlato.value     = recipe.tipoPlato
        fotoPrincipalUrl    = recipe.fotoPrincipal

        ingredientes.clear()
        ingredientes.addAll(recipe.ingredients.map {
            IngredientInput(
                nombre = it.nombre,
                cantidad = it.cantidad.toString(),
                unidad = it.unidadMedida
            )
        })

        pasos.clear()
        pasos.addAll(recipe.steps.sortedBy { it.numeroPaso }.map {
            StepInput(
                titulo = it.titulo,
                descripcion = it.descripcion,
                url = it.urlMedia.orEmpty()
            )
        })
    }*/

    private suspend fun buildRecipeRequest(
        nombre: String,
        descripcion: String,
        tiempo: Int,
        porciones: Int,
        tipoPlato: String,
        categoria: String,
        ingredients: List<RecipeIngredientRequest>,
        steps: List<RecipeStepRequest>
    ): RecipeRequest {
        val uploadedSteps = steps.map { step ->
            val local = step.urlMedia
            if (!local.isNullOrBlank() && local.startsWith("content://")) {
                val file = File(FileUtil.from(getApplication(), Uri.parse(local)).path)
                val remoteUrl = repo.uploadPhoto(file)
                step.copy(urlMedia = remoteUrl)
            } else step
        }

        return RecipeRequest(
            nombre        = nombre,
            descripcion   = descripcion,
            tiempo        = tiempo,
            porciones     = porciones,
            fotoPrincipal = photoUrl.value,
            tipoPlato     = tipoPlato,
            categoria     = categoria,
            ingredients   = ingredients,
            steps         = uploadedSteps
        )
    }

    fun updateDraftRecipe(
        recipeId: Long,
        nombre: String,
        descripcion: String,
        tiempo: Int,
        porciones: Int,
        tipoPlato: String,
        categoria: String,
        ingredients: List<RecipeIngredientRequest>,
        steps: List<RecipeStepRequest>
    ) {
        viewModelScope.launch {
            val updated = buildRecipeRequest(nombre, descripcion, tiempo, porciones, tipoPlato, categoria, ingredients, steps)
            RetrofitClient.api.updateRecipe(recipeId, updated)
        }
    }

   /* fun publishDraftRecipe(recipeId: Long) {
        viewModelScope.launch {
            val updated = buildRecipeRequest().copy(estado = "PENDIENTE")
            RetrofitClient.api.updateRecipe(recipeId, updated)
        }
    }*/




    /**
     * Sube la foto seleccionada y guarda la URL devuelta.
     */
    fun uploadPhoto(file: File) = viewModelScope.launch {
        _uploading.value = true
        _error.value = null
        try {
            val url = repo.uploadPhoto(file)
            _photoUrl.value = url
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
        nombre: String, descripcion: String, tiempo: Int, porciones: Int,
        tipoPlato: String, categoria: String,
        ingredients: List<RecipeIngredientRequest>,
        steps: List<RecipeStepRequest>,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        _submitting.value = true
        _error.value = null
        try {
            // ➊ primero subo todas las fotos de pasos:
            val uploadedSteps = steps.map { step ->
                val local = step.urlMedia
                if (!local.isNullOrBlank() && local.startsWith("content://")) {
                    // convierto URI a File (igual que haces en el screen)
                    val file = File(FileUtil.from(getApplication(), Uri.parse(local)).path)
                    val remoteUrl = repo.uploadPhoto(file)
                    step.copy(urlMedia = remoteUrl)
                } else step
            }

            // ➋ armo el request usando la lista con URLs remotas
            val req = RecipeRequest(
                nombre        = nombre,
                descripcion   = descripcion,
                tiempo        = tiempo,
                porciones     = porciones,
                fotoPrincipal = _photoUrl.value,
                tipoPlato     = tipoPlato,
                categoria     = categoria,
                ingredients   = ingredients,
                steps         = uploadedSteps
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