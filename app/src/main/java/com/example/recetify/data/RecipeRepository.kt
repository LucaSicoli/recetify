// app/src/main/java/com/example/recetify/data/RecipeRepository.kt
package com.example.recetify.data

import android.net.ConnectivityManager
import com.example.recetify.data.db.RecipeDao
import com.example.recetify.data.db.RecipeEntity
import com.example.recetify.data.remote.ApiService
import com.example.recetify.data.remote.model.RecipeRequest
import com.example.recetify.data.remote.model.RecipeResponse
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

    /**
     * Sube una imagen al servidor y devuelve la URL pública.
     */
    suspend fun uploadPhoto(file: File): String {
        val requestBody = file
            .asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(
            name     = "file",
            filename = file.name,
            body     = requestBody
        )
        return api.uploadImage(part)
    }

    suspend fun saveDraft(req: RecipeRequest): RecipeResponse = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión" }
        api.saveDraft(req)
    }

    suspend fun listDrafts(): List<RecipeResponse> = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión" }
        api.listDrafts()
    }

    suspend fun publishDraft(id: Long): RecipeResponse = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión" }
        api.publishDraft(id)
    }

    /**
     * Envía el RecipeRequest al backend y devuelve la receta creada.
     */
    suspend fun createRecipe(req: RecipeRequest): RecipeResponse = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión de red" }
        api.createRecipe(req)
    }

    fun getPublishedRecipes(): Flow<List<RecipeEntity>> =
        dao.getByEstadoPublicacion("PUBLICADO")

    /**
     * Descarga el listado de recetas (resumen) y actualiza el caché en Room.
     * Luego emite siempre el contenido de Room, para soporte offline.
     */
    fun getAllRecipes(): Flow<List<RecipeEntity>> = flow {
        if (connectivity.activeNetwork != null) {
            // 1) Llamo al nuevo endpoint
            val list = api.getAllRecipesSummary()
            // 2) Mapeo cada RecipeSummaryResponse a RecipeEntity
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
                    estadoAprobacion    = "",    // si no lo traes aquí, déjalo vacío o añade el campo en el DTO
                    estadoPublicacion   = null   // idem
                )
            }
            dao.clearAll()
            dao.insertAll(entities)
        }
        // 3) Emito siempre lo que hay en cache
        emitAll(dao.getAll())
    }.flowOn(Dispatchers.IO)
}