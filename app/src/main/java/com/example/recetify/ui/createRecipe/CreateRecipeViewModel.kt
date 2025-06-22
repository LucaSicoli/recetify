// app/src/main/java/com/example/recetify/ui/createRecipe/CreateRecipeViewModel.kt
package com.example.recetify.ui.createRecipe

import android.app.Application
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
import com.example.recetify.data.remote.model.RecipeStepRequest
import com.example.recetify.util.FileUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

object RecipeRepositoryProvider {
    fun get(context: Application): RecipeRepository {
        val db = DatabaseProvider.getInstance(context)
        val dao = db.recipeDao()
        val connectivity =
            context.getSystemService(Application.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        return RecipeRepository(
            dao = dao,
            api = RetrofitClient.api,
            connectivity = connectivity
        )
    }
}

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

class CreateRecipeViewModel(
    app: Application,
    private val repo: RecipeRepository
) : AndroidViewModel(app) {

    private val _uploading   = MutableStateFlow(false)
    val uploading           = _uploading.asStateFlow()

    private val _submitting  = MutableStateFlow(false)
    val submitting          = _submitting.asStateFlow()

    private val _error       = MutableStateFlow<String?>(null)
    val error               = _error.asStateFlow()

    private val _photoUrl    = MutableStateFlow<String?>(null)
    val photoUrl            = _photoUrl.asStateFlow()

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
            // ➊ Para cada paso, subimos todas las URIs locales en mediaUrls
            val uploadedSteps = steps.map { step ->
                // extraemos sólo las URIs que haya que subir
                val locals = step.mediaUrls.orEmpty()
                    .filter { it.startsWith("content://") }
                if (locals.isNotEmpty()) {
                    // subimos cada URI y recogemos sus URLs remotas
                    val remoteUrls = locals.map { localUri ->
                        val file = File(FileUtil.from(getApplication(), Uri.parse(localUri)).path)
                        repo.uploadPhoto(file)
                    }
                    // conservamos cualquier URL ya remota que ya estuviera en mediaUrls
                    val preserved = step.mediaUrls.orEmpty()
                        .filter { !it.startsWith("content://") }
                    step.copy(mediaUrls = preserved + remoteUrls)
                } else {
                    step
                }
            }

            // ➋ Creamos el RecipeRequest con las URLs remotas en cada paso
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