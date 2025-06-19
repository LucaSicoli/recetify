package com.example.recetify.data.remote.model

data class ProfileSummaryDTO(
    val userId: Long,
    val recetasPublicadas: Int,
    val recetasGuardadas: Int,
    val reseñasPublicadas: Int
)
