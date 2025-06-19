// app/src/main/java/com/example/recetify/data/db/AppDatabase.kt
package com.example.recetify.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        RatingEntity::class
    ],
    version = 2,             // ← aumentamos de 1 a 2
    exportSchema = false     // ← opcional para no exportar JSON de esquema
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeDetailDao(): RecipeDetailDao
}