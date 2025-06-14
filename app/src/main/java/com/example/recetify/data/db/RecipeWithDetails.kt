// data/db/RecipeWithDetails.kt
package com.example.recetify.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    ) val ingredients: List<IngredientEntity> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    ) val steps: List<StepEntity> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    ) val ratings: List<RatingEntity> = emptyList()
)