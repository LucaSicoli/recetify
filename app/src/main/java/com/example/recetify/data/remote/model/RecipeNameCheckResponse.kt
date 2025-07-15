package com.example.recetify.data.remote.model

import com.example.recetify.data.remote.model.RecipeSummaryResponse

/**
 * Modelo para la respuesta de validación de nombre de receta.
 */
data class RecipeNameCheckResponse(
    val exists: Boolean = false,
    val message: String = "",
    val existingRecipe: RecipeSummaryResponse? = null
)
