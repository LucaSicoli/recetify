package com.example.recetify.data.db

import com.example.recetify.data.remote.model.ISavedRecipe
import com.example.recetify.data.remote.model.IngredientDTO
import com.example.recetify.data.remote.model.StepDTO

// app/src/main/java/com/example/recetify/data/db/UserCustomRecipeDTO.kt
data class UserCustomRecipeDTO(
    override val id: Long,
    override val recipeId: Long,
    override val recipeNombre: String,
    override val fechaAgregado: String,
    override val mediaUrls: List<String>,
    val porciones: Int,
    val tiempo: Int,
    val ingredients: List<IngredientDTO>,
    val steps: List<StepDTO> = emptyList()                      // ✅ NUEVO
) : ISavedRecipe