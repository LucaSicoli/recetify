package com.example.recetify.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        RatingEntity::class,
        CustomRecipeEntity::class
    ],
    version = 13, // Incrementamos la versión para incluir el nuevo campo estadoAprobacion
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeDetailDao(): RecipeDetailDao
    abstract fun customRecipeDao(): CustomRecipeDao
}