package com.example.recetify.ui.drafts.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.*
import com.example.recetify.util.FileUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

data class EditDraftRecipeUiState(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String = "",
    val tiempo: Int = 0,
    val porciones: Int = 1,
    val tipoPlato: String = "",
    val categoria: String = "",
    val fotoPrincipal: String? = null,
    val ingredients: List<RecipeIngredientRequest> = emptyList(),
    val steps: List<RecipeStepRequest> = emptyList()
)
class EditDraftRecipeViewModel(
    private val recipeId: Long,
    private val app: Application
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(EditDraftRecipeUiState())
    val uiState: StateFlow<EditDraftRecipeUiState> = _uiState

    init {
        loadRecipe()
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            try {
                val recipe = RetrofitClient.api.getRecipeById(recipeId)
                _uiState.value = EditDraftRecipeUiState(
                    id = recipe.id,
                    nombre = recipe.nombre,
                    descripcion = recipe.descripcion.orEmpty(),
                    tiempo = recipe.tiempo,
                    porciones = recipe.porciones,
                    tipoPlato = recipe.tipoPlato.orEmpty(),
                    categoria = recipe.categoria.orEmpty(),
                    fotoPrincipal = recipe.fotoPrincipal,
                    ingredients = recipe.ingredients.map {
                        RecipeIngredientRequest(
                            nombre = it.nombre,
                            cantidad = it.cantidad,
                            unidadMedida = it.unidadMedida
                        )
                    },
                    steps = recipe.steps.map {
                        RecipeStepRequest(it.numeroPaso, it.titulo, it.descripcion, it.urlMedia)
                    }
                )
            } catch (e: Exception) {
                // TODO: manejar error
            }
        }
    }

    // --- Setters idénticos a antes ---
    fun updateNombre(n: String) { _uiState.value = _uiState.value.copy(nombre = n) }
    fun updateDescripcion(d: String) { _uiState.value = _uiState.value.copy(descripcion = d) }
    fun updateTiempo(t: Int) { _uiState.value = _uiState.value.copy(tiempo = t) }
    fun updatePorciones(p: Int) { _uiState.value = _uiState.value.copy(porciones = p) }
    fun updateTipoPlato(tp: String) { _uiState.value = _uiState.value.copy(tipoPlato = tp) }
    fun updateCategoria(cat: String) { _uiState.value = _uiState.value.copy(categoria = cat) }

    fun updateIngredientes(ings: List<RecipeIngredientRequest>) {
        _uiState.value = _uiState.value.copy(ingredients = ings)
    }

    fun updatePasos(pasos: List<RecipeStepRequest>) {
        _uiState.value = _uiState.value.copy(steps = pasos)
    }

    fun setFotoPrincipalUri(uri: Uri) {
        viewModelScope.launch {
            val file = File(FileUtil.from(app, uri).path)
            val remoteUrl = uploadPhoto(file)
            _uiState.value = _uiState.value.copy(fotoPrincipal = remoteUrl)
        }
    }

    fun updateDraftRecipe(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val request = buildRecipeRequest()
            RetrofitClient.api.updateRecipe(recipeId, request)
            onSuccess()
        }
    }

    fun publishDraftRecipe(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val request = buildRecipeRequest().copy(/* estado = "PENDIENTE" si aplica */)
            RetrofitClient.api.updateRecipe(recipeId, request)
            onSuccess()
        }
    }

    private suspend fun buildRecipeRequest(): RecipeRequest {
        val uploadedSteps = _uiState.value.steps.map { step ->
            val local = step.urlMedia
            if (!local.isNullOrBlank() && local.startsWith("content://")) {
                val file = File(FileUtil.from(app, Uri.parse(local)).path)
                val remoteUrl = uploadPhoto(file)
                step.copy(urlMedia = remoteUrl)
            } else step
        }

        return RecipeRequest(
            nombre = _uiState.value.nombre,
            descripcion = _uiState.value.descripcion,
            tiempo = _uiState.value.tiempo,
            porciones = _uiState.value.porciones,
            fotoPrincipal = _uiState.value.fotoPrincipal,
            tipoPlato = _uiState.value.tipoPlato,
            categoria = _uiState.value.categoria,
            ingredients = _uiState.value.ingredients,
            steps = uploadedSteps
        )
    }

    private suspend fun uploadPhoto(file: File): String {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = file.name,
            body = requestBody
        )
        return RetrofitClient.api.uploadImage(part)
    }
}

class EditDraftRecipeViewModelFactory(
    private val recipeId: Long,
    private val app: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditDraftRecipeViewModel(recipeId, app) as T
    }
}
