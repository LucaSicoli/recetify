// data/db/RecipeDao.kt
package com.example.recetify.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(recipes: List<RecipeEntity>): List<Long>

    @Query("DELETE FROM recipes")
    fun clearAll(): Int

    @Query("SELECT * FROM recipes ORDER BY id DESC")
    fun getAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE estadoPublicacion = :estado")
    fun getByEstadoPublicacion(estado: String): Flow<List<RecipeEntity>>

    @Query("""
    SELECT * FROM recipes
    WHERE (:name       IS NULL OR LOWER(nombre) LIKE '%' || LOWER(:name) || '%')
      AND (:type       IS NULL OR tipoPlato = :type)
      AND (:categoria  IS NULL OR categoria = :categoria)
    ORDER BY
      CASE WHEN :sort = 'name'    THEN nombre END ASC,
      CASE WHEN :sort = 'newest'  THEN id END DESC
  """)
    fun searchLocal(
        name: String?,
        type: String?,
        categoria: String?,
        sort: String
    ): List<RecipeEntity>
}