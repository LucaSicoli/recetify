package com.example.recetify.data.remote.model

data class UserSavedRecipeDTO (
    val id: Long,
    val recipeId: Long,
    val recipeNombre: String,
    val fechaAgregado: String // o LocalDateTime si usás deserialización especial
)