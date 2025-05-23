package com.example.recetify.data.remote.model

data class RecipeResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val fotoPrincipal: String?,
    val tiempo: Int,
    val porciones: Int,
    val fechaCreacion: String,
    val estado: String
)

