package com.example.recetify.data.remote.model

data class CreateRatingRequest(
    val recipeId: Long,
    val comentario: String,
    val puntos: Int
)
