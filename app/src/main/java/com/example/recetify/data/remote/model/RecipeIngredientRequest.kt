package com.example.recetify.data.remote.model

data class RecipeIngredientRequest(
    val ingredientId: Long? = null,   // si es ingrediente existente
    val nombre: String? = null,       // si crea uno nuevo
    val cantidad: Double,
    val unidadMedida: String
)