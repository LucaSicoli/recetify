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
    private val api: ApiService,               // ← Recibe únicamente el ApiService
    private val connectivity: ConnectivityManager
) {

    /**
     * Sube una imagen al servidor y devuelve la URL pública.
     */
    suspend fun uploadPhoto(file: File): String {
        val requestBody = file
            .asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = file.name,
            body = requestBody
        )
        return api.uploadImage(part)
    }

    /**
     * Envía el RecipeRequest al backend y devuelve la receta creada.
     * Lanza excepción si no hay conexión.
     */
    suspend fun createRecipe(req: RecipeRequest): RecipeResponse = withContext(Dispatchers.IO) {
        check(connectivity.activeNetwork != null) { "Sin conexión de red" }
        api.createRecipe(req)
    }

    /**
     * Descarga el listado de recetas (resumen) y actualiza el caché en Room.
     * Luego emite siempre el contenido de Room, para soporte offline.
     */
    fun getAllRecipes(): Flow<List<RecipeEntity>> =
        flow {
            if (connectivity.activeNetwork != null) {
                val summary = api.getAllRecipesSummary()
                val entities = summary.map { s ->
                    RecipeEntity(
                        id                  = s.id,
                        nombre              = s.nombre,
                        descripcion         = s.descripcion,
                        fotoPrincipal       = s.fotoPrincipal,
                        tiempo              = s.tiempo.toInt(),
                        porciones           = s.porciones,
                        tipoPlato           = s.tipoPlato,
                        categoria           = s.categoria,
                        usuarioCreadorAlias = s.usuarioCreadorAlias,
                        promedioRating      = s.promedioRating
                    )
                }
                dao.clearAll()
                dao.insertAll(entities)
            }
            emitAll(dao.getAll())
        }
            .flowOn(Dispatchers.IO)
}