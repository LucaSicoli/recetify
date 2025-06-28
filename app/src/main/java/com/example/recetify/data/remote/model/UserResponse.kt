package com.example.recetify.data.remote.model

// package com.example.recetify.data.remote.model

data class UserResponse(
    val id: Long,
    val alias: String,
    val email: String,
    val fechaCreacion: String,
    val urlFotoPerfil: String?      // ← nuevo campo opcional
)