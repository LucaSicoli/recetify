// app/src/main/java/com/example/recetify/data/db/CustomRecipeEntity.kt
package com.example.recetify.data.db

import androidx.room.Entity
import com.example.recetify.data.remote.model.IngredientDTO
import com.example.recetify.data.remote.model.StepDTO            // ✅

@Entity(
    tableName   = "custom_recipes",
    primaryKeys = ["recipeId", "ownerEmail"]
)
data class CustomRecipeEntity(
    val recipeId: Long,
    val ownerEmail: String,
    val nombre: String,
    val fechaGuardado: String,
    val porciones: Int,
    val tiempo: Int,
    val ingredients: List<IngredientDTO>,
    val steps: List<StepDTO> = emptyList(),                     // ✅ NUEVO
    val mediaUrls: List<String>? = emptyList()
)