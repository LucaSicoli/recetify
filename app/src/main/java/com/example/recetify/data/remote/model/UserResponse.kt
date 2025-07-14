// app/src/main/java/com/example/recetify/data/remote/model/UserResponse.kt
package com.example.recetify.data.remote.model

data class UserResponse(
    val id: Long,
    val alias: String,
    val email: String,
    val fechaCreacion: String,
    val urlFotoPerfil: String?,
    val descripcion: String?,
    val activo: Boolean = true  // ← Nuevo campo con valor por defecto
)