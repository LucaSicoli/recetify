package com.example.recetify.data.remote.model

data class RatingResponse(
    val id: Long,
    val userAlias: String,
    val puntos: Int,
    val comentario: String,
    val fecha: String,
    val estadoAprobacion: String? = "PENDIENTE" // Hacer nullable para compatibilidad con datos antiguos
)