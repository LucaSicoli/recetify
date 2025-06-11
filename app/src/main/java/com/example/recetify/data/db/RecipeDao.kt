package com.example.recetify.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(recipes: List<RecipeEntity>): List<Long>

    @Query("DELETE FROM recipes")
    fun clearAll(): Int          // devuelve número de filas eliminadas

    @Query("SELECT * FROM recipes ORDER BY id DESC")
    fun getAll(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun findById(id: Long): Flow<RecipeEntity?>

    @Delete
    fun delete(recipe: RecipeEntity): Int
}