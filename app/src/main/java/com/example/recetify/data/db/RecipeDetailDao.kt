// data/db/RecipeDetailDao.kt
package com.example.recetify.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecipe(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIngredients(list: List<IngredientEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSteps(list: List<StepEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRatings(list: List<RatingEntity>): List<Long>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeWithDetails(recipeId: Long): Flow<RecipeWithDetails>
}