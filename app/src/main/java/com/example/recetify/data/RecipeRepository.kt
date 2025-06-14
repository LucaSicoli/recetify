// data/RecipeRepository.kt
package com.example.recetify.data

import android.net.ConnectivityManager
import com.example.recetify.data.db.RecipeDao
import com.example.recetify.data.db.RecipeEntity
import com.example.recetify.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*


class RecipeRepository(
    private val dao: RecipeDao,
    private val api: ApiService,
    private val connectivity: ConnectivityManager
) {
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