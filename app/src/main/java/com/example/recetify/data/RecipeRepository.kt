// RecipeRepository.kt
package com.example.recetify.data

import android.content.Context
import android.net.ConnectivityManager
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.db.RecipeDao
import com.example.recetify.data.db.RecipeEntity
import com.example.recetify.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class RecipeRepository(
    private val dao: RecipeDao,
    private val api: ApiService,
    private val connectivity: ConnectivityManager
) {
    fun getAllRecipes(): Flow<List<RecipeEntity>> = flow {
        if (connectivity.activeNetwork != null) {
            val remote = api.getAllRecipesSummary()
            val entities = remote.map { resp ->
                RecipeEntity(id = resp.id, title = resp.nombre, instructions = resp.descripcion ?: "", author = resp.usuarioCreadorAlias ?: "")
            }
            // actualizo cache
            dao.clearAll()
            dao.insertAll(entities)
        }
        // emito siempre el contenido de la base local
        emitAll(dao.getAll())
    }

    suspend fun addLocal(recipe: RecipeEntity) = withContext(Dispatchers.IO) {
        dao.insert(recipe)
    }

    suspend fun deleteLocal(recipe: RecipeEntity) = withContext(Dispatchers.IO) {
        dao.delete(recipe)
    }
}