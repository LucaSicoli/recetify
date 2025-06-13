package com.example.recetify.data.remote.model

data class RatingResponse(
    val id: Long,
    val userAlias: String,
    val recipeNombre: String,
    val puntos: Int,
    val comentario: String,
    val fecha: String
)
