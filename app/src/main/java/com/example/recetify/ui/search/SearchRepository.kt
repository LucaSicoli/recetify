// File: SearchRepository.kt
package com.example.recetify.ui.search

import android.net.ConnectivityManager
import com.example.recetify.data.db.RecipeDao
import com.example.recetify.data.db.RecipeEntity
import com.example.recetify.data.remote.ApiService
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(
    private val api: ApiService,
    private val dao: RecipeDao,
    private val connectivity: ConnectivityManager
) {
    suspend fun search(
        name: String?,
        type: String?,
        ingredient: String?,        // de momento no usado en offline
        excludeIngredient: String?, // idem
        userAlias: String?,         // idem
        sort: String
    ): List<RecipeSummaryResponse> = withContext(Dispatchers.IO) {
        if (connectivity.activeNetwork != null) {
            // Online: fetch + cache
            val remote = runCatching {
                api.searchRecipes(name, type, ingredient, excludeIngredient, userAlias, sort)
            }.getOrDefault(emptyList())
            val entities = remote.map { it.toEntity() }
            dao.clearAll()
            dao.insertAll(entities)
            remote
        } else {
            // Offline: solo filtros de nombre, tipo, categoría y orden
            dao.searchLocal(name, type, /* categoría = */ null, sort)
                .map { it.toSummaryResponse() }
        }
    }
}

/** convert DTO → Entidad Room */
private fun RecipeSummaryResponse.toEntity() = RecipeEntity(
    id                  = id,
    nombre              = nombre,
    descripcion         = descripcion,
    mediaUrls           = mediaUrls,
    tiempo              = tiempo.toInt(),
    porciones           = porciones,
    tipoPlato           = tipoPlato,
    categoria           = categoria,
    usuarioCreadorAlias = usuarioCreadorAlias,
    promedioRating      = promedioRating,
    estadoAprobacion    = "",
    estadoPublicacion   = ""
)

/** convert Entidad Room → DTO */
private fun RecipeEntity.toSummaryResponse() = RecipeSummaryResponse(
    id                  = id,
    nombre              = nombre,
    descripcion         = descripcion,
    mediaUrls           = mediaUrls,
    tiempo              = tiempo.toLong(),
    porciones           = porciones,
    tipoPlato           = tipoPlato,
    categoria           = categoria,
    usuarioCreadorAlias = usuarioCreadorAlias,
    usuarioFotoPerfil   = null,
    promedioRating      = promedioRating,
    estadoAprobacion    = estadoAprobacion,
    estadoPublicacion   = estadoPublicacion ?: ""
)