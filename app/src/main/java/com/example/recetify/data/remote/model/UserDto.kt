package com.example.recetify.data.remote.model


data class UserDto(
    val id: Long,
    val email: String,
    val alias: String,
    val nombre: String?,
    val rol: String?,
    val urlFotoPerfil: String?,
    val recetasGuardadas: Int = 0,
    val recetasPublicadas: Int = 0,
    val resenasPublicadas: Int = 0,
    val descripcion: String? = null // <--- AGREGALO ACÁ
)

