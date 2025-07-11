package com.example.recetify.data

import android.net.ConnectivityManager
import com.example.recetify.data.db.RecipeDao
import com.example.recetify.data.db.RecipeEntity
import com.example.recetify.data.remote.ApiService
import com.example.recetify.data.remote.model.RecipeIngredientRequest
import com.example.recetify.data.remote.model.RecipeRequest
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.RecipeStepRequest
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import com.example.recetify.data.remote.model.UserSavedRecipeDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RecipeRepository(
    private val dao: RecipeDao,
    private val api: ApiService,
    private val connectivity: ConnectivityManager
) {

    suspend fun uploadPhoto(file: File): String = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión" }
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(
            name     = "file",
            filename = file.name,
            body     = requestBody
        )
        api.uploadImage(part)
    }

    // app/src/main/java/com/example/recetify/data/RecipeRepository.kt

    /** Carga un borrador completo (o receta) por su ID usando el endpoint /recipes/{id} */
    suspend fun getDraftById(id: Long): RecipeResponse =
        api.getRecipeById(id)

    suspend fun saveDraft(req: RecipeRequest): RecipeResponse = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión" }
        api.saveDraft(req)
    }

    suspend fun listDrafts(): List<RecipeSummaryResponse> = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión" }
        api.listDrafts()
    }


    suspend fun listSavedRecipes(): List<UserSavedRecipeDTO> = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión" }
        api.listSavedRecipes()
    }

    suspend fun syncDraftFull(id: Long, request: RecipeRequest): RecipeResponse {
        return api.syncDraftFull(id, request)
    }

    suspend fun publishDraft(id: Long): RecipeResponse = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión" }
        api.publishDraft(id)
    }

    suspend fun createRecipe(req: RecipeRequest): RecipeResponse = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión de red" }
        api.createRecipe(req)
    }

    suspend fun deleteDraft(id: Long) = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión" }
        api.deleteDraft(id)
    }

    fun getPublishedRecipes(): Flow<List<RecipeEntity>> =
        dao.getByEstadoPublicacion("PUBLICADO")

    suspend fun updatePortionsAndIngredients(
        recipe: RecipeResponse
    ): RecipeResponse = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión" }

        // Mapear ingredientes
        val ingrReq = recipe.ingredients.map { ing ->
            RecipeIngredientRequest(
                nombre       = ing.nombre,
                cantidad     = ing.cantidad,
                unidadMedida = ing.unidadMedida
            )
        }

        // Mapear pasos (ajusta los campos al tuyo)
        val stepReq = recipe.steps.map { paso ->
            RecipeStepRequest(
                numeroPaso   = paso.numeroPaso,
                titulo       = paso.titulo,
                descripcion  = paso.descripcion.orEmpty(),
                mediaUrls    = paso.mediaUrls.orEmpty()
            )
        }

        // Construyo el RecipeRequest completo con todos los campos no nulos
        val req = RecipeRequest(
            nombre       = recipe.nombre,
            descripcion  = recipe.descripcion.orEmpty(),
            tiempo       = recipe.tiempo,
            porciones    = recipe.porciones,
            mediaUrls    = recipe.mediaUrls,
            tipoPlato    = recipe.tipoPlato,
            categoria    = recipe.categoria,
            ingredients  = ingrReq,
            steps        = stepReq
        )

        api.syncDraftFull(recipe.id, req)
    }

    fun getAllRecipes(): Flow<List<RecipeEntity>> = flow {
        if (connectivity.activeNetwork != null) {
            val list = api.getAllRecipesSummary()
            val entities = list.map { s ->
                RecipeEntity(
                    id                  = s.id,
                    nombre              = s.nombre,
                    descripcion         = s.descripcion,
                    mediaUrls           = s.mediaUrls,
                    tiempo              = s.tiempo.toInt(),
                    porciones           = s.porciones,
                    tipoPlato           = s.tipoPlato,
                    categoria           = s.categoria,
                    usuarioCreadorAlias = s.usuarioCreadorAlias,
                    promedioRating      = s.promedioRating,
                    estadoAprobacion    = "",
                    estadoPublicacion   = ""
                )
            }
            dao.clearAll()
            dao.insertAll(entities)
        }
        emitAll(dao.getAll())
    }.flowOn(Dispatchers.IO)
}