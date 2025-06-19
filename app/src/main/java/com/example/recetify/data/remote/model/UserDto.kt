package com.example.recetify.data.remote.model

data class UserDto(
    val id: Long,
    val email: String,
    val alias: String,
    val nombre: String?,
    val urlFotoPerfil: String?,
    val recetasPublicadas: Int?,
    val recetasGuardadas: Int?,
    val resenasPublicadas: Int?
)

