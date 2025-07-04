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
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.RecipeStepRequest
import com.example.recetify.util.FileUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _uploading     = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading.asStateFlow()

    private val _submitting    = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    private val _error         = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _photoUrl      = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl.asStateFlow()

    private val _draftSaved    = MutableStateFlow<Result<RecipeResponse>?>(null)
    val draftSaved: StateFlow<Result<RecipeResponse>?> = _draftSaved

    private val _publishResult = MutableStateFlow<Result<RecipeResponse>?>(null)
    val publishResult: StateFlow<Result<RecipeResponse>?> = _publishResult

    private val _draftDetail   = MutableStateFlow<RecipeResponse?>(null)
    val draftDetail: StateFlow<RecipeResponse?> = _draftDetail.asStateFlow()

    /** Sube foto o vídeo y guarda su URL en `_photoUrl` */
    fun uploadPhoto(file: File) = viewModelScope.launch {
        _uploading.value = true
        _error.value     = null
        try {
            val url = repo.uploadPhoto(file)
            _photoUrl.value = url
        } catch (t: Throwable) {
            _error.value = t.localizedMessage
        } finally {
            _uploading.value = false
        }
    }

    /** Carga un borrador completo por su ID */
    fun loadDraftDetail(id: Long) = viewModelScope.launch {
        _submitting.value = true
        _error.value      = null
        runCatching {
            repo.getDraftById(id)
        }.onSuccess { resp ->
            _draftDetail.value = resp
        }.onFailure {
            _error.value = it.localizedMessage
        }.also {
            _submitting.value = false
        }
    }

    /** Guarda un borrador SIN gestionar subida de media */
    fun saveDraft(request: RecipeRequest) = viewModelScope.launch {
        runCatching { repo.saveDraft(request) }
            .onSuccess { _draftSaved.value = Result.success(it) }
            .onFailure { _draftSaved.value = Result.failure(it) }
    }

    /** Guarda un borrador con subida de portada y media de pasos */
    fun saveDraftWithMedia(
        request: RecipeRequest,
        localMediaUri: Uri?
    ) = viewModelScope.launch {
        _submitting.value = true
        _error.value      = null
        try {
            val headerUrls = localMediaUri
                ?.takeIf { it.scheme == "content" }
                ?.let { uri ->
                    val file = File(FileUtil.from(getApplication(), uri).path)
                    listOf(repo.uploadPhoto(file))
                } ?: request.mediaUrls.orEmpty()

            val uploadedSteps = request.steps.map { step ->
                val locals = step.mediaUrls.orEmpty().filter { it.startsWith("content://") }
                if (locals.isNotEmpty()) {
                    val remoteUrls = locals.map { localUriString ->
                        val uri  = Uri.parse(localUriString)
                        val file = File(FileUtil.from(getApplication(), uri).path)
                        repo.uploadPhoto(file)
                    }
                    val preserved = step.mediaUrls.orEmpty()
                        .filterNot { it.startsWith("content://") }
                    step.copy(mediaUrls = preserved + remoteUrls)
                } else step
            }

            val newReq = request.copy(
                mediaUrls = headerUrls,
                steps     = uploadedSteps
            )
            val resp = repo.saveDraft(newReq)
            _draftSaved.value = Result.success(resp)
        } catch (t: Throwable) {
            _draftSaved.value = Result.failure(t)
        } finally {
            _submitting.value = false
        }
    }

    /**
     * **Nuevo**: Sincroniza TODO el borrador (portada, ingredientes y pasos)
     * Reutiliza el mismo endpoint de updateDraft, que devuelve RecipeResponse
     */
    fun syncDraftFull(
        id: Long,
        request: RecipeRequest,
        localMediaUri: Uri?
    ) = viewModelScope.launch {
        _submitting.value = true
        _error.value      = null
        try {
            // 1️ portada
            val headerUrls = localMediaUri
                ?.takeIf { it.scheme=="content" }
                ?.let { uri ->
                    listOf(repo.uploadPhoto(File(FileUtil.from(getApplication(),uri).path)))
                } ?: request.mediaUrls.orEmpty()

            // 2️ pasos
            val uploadedSteps = request.steps.map { step ->
                val locals = step.mediaUrls.orEmpty().filter { it.startsWith("content://") }
                if (locals.isEmpty()) return@map step
                val remotes = locals.map {
                    val uri = Uri.parse(it)
                    repo.uploadPhoto(File(FileUtil.from(getApplication(),uri).path))
                }
                val already = step.mediaUrls.orEmpty().filterNot { it.startsWith("content://") }
                step.copy(mediaUrls = already + remotes)
            }

            val finalReq = request.copy(
                mediaUrls = headerUrls,
                steps     = uploadedSteps
            )

            // en lugar de un método que devuelve Unit,
            // llamamos a updateDraft que devuelve RecipeResponse:
            val resp = repo.syncDraftFull(id, finalReq)
            _draftSaved.value = Result.success(resp)
        } catch (t: Throwable) {
            _draftSaved.value = Result.failure(t)
        } finally {
            _submitting.value = false
        }
    }

    /** Publica un borrador existente */
    fun publishDraft(id: Long) = viewModelScope.launch {
        runCatching { repo.publishDraft(id) }
            .onSuccess { _publishResult.value = Result.success(it) }
            .onFailure { _publishResult.value = Result.failure(it) }
    }

    /** Crea una receta y envía a publicar */
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
        _error.value      = null
        try {
            val uploadedSteps = steps.map { step ->
                val locals = step.mediaUrls.orEmpty().filter { it.startsWith("content://") }
                if (locals.isNotEmpty()) {
                    val remoteUrls = locals.map { localUri ->
                        val file = File(FileUtil.from(getApplication(), Uri.parse(localUri)).path)
                        repo.uploadPhoto(file)
                    }
                    val preserved = step.mediaUrls.orEmpty().filter { !it.startsWith("content://") }
                    step.copy(mediaUrls = preserved + remoteUrls)
                } else step
            }

            val headerUrls = _photoUrl.value?.let { listOf(it) } ?: emptyList()
            val req = RecipeRequest(
                nombre      = nombre,
                descripcion = descripcion,
                tiempo      = tiempo,
                porciones   = porciones,
                mediaUrls   = headerUrls,
                tipoPlato   = tipoPlato,
                categoria   = categoria,
                ingredients = ingredients,
                steps       = uploadedSteps
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