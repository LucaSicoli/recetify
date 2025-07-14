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
        ingredient: String?,        // include‐filter
        excludeIngredient: String?, // exclude‐filter
        userAlias: String?,
        sort: String
    ): List<RecipeSummaryResponse> = withContext(Dispatchers.IO) {
        if (connectivity.activeNetwork != null) {
            // 1) Traemos con include
            val includeList = runCatching {
                api.searchRecipes(name, type, ingredient, null, userAlias, sort)
            }.getOrDefault(emptyList())

            // 2) Si hay excludeIngredient, pedimos los que SÍ lo contienen
            val excludedIds = excludeIngredient
                .takeIf { !it.isNullOrBlank() }
                ?.let { excl ->
                    runCatching {
                        api.searchRecipes(name, type, excl, null, userAlias, sort)
                            .map { it.id }
                            .toSet()
                    }.getOrDefault(emptySet())
                } ?: emptySet()

            // 3) Restamos ambos conjuntos
            val finalList = includeList.filter { it.id !in excludedIds }

            // 4) Cacheamos sólo el resultado final
            dao.clearAll()
            dao.insertAll(finalList.map { it.toEntity() })

            finalList
        } else {
            // Offline: sólo filtros locales (sin excludeIngredient)
            dao.searchLocal(name, type, /* categoría = */ null, sort)
                .map { it.toSummaryResponse() }
        }
    }
}

/** convierte DTO → Entidad Room */
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

/** convierte Entidad Room → DTO */
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
    estadoPublicacion   = estadoPublicacion.orEmpty(),
    estado              = estadoAprobacion  // Corregido: mapear estadoAprobacion -> estado
)