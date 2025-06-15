// app/src/main/java/com/example/recetify/data/RecipeDetailRepository.kt
package com.example.recetify.data

import android.net.ConnectivityManager
import com.example.recetify.data.db.RecipeDetailDao
import com.example.recetify.data.db.RecipeWithDetails
import com.example.recetify.data.remote.ApiService
import com.example.recetify.data.remote.model.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RecipeDetailRepository(
    private val detailDao: RecipeDetailDao,
    private val api: ApiService,
    private val connectivity: ConnectivityManager
) {
    /**
     * Siempre emite lo que hay en Room.
     * Si hay conexión, primero descarga desde la API y actualiza el caché,
     * luego emite el contenido de la base local (para que funcione offline).
     */
    fun getRecipeDetail(id: Long): Flow<RecipeWithDetails> = flow {
        if (connectivity.activeNetwork != null) {
            // 1) Descarga remota
            val remoteRecipe = api.getRecipeById(id)
            val remoteRatings    = api.getRatingsForRecipe(id)
            val remoteIngredients = remoteRecipe.ingredients
            val remoteSteps       = remoteRecipe.steps

            // 2) Guarda en Room en IO
            withContext(Dispatchers.IO) {
                detailDao.insertRecipe(remoteRecipe.toEntity())
                detailDao.insertRatings(remoteRatings.map { it.toEntity(id) })
                detailDao.insertIngredients(remoteIngredients.map { it.toEntity(id) })
                detailDao.insertSteps(remoteSteps.map { it.toEntity(id) })
            }
        }

        // 3) Emite siempre Room
        emitAll(detailDao.getRecipeWithDetails(id))
    }.flowOn(Dispatchers.IO)
}