package com.example.recetify.data.remote.model

import java.util.UUID

data class RecipeStepRequest(
    val id: String = UUID.randomUUID().toString(), // Añadido para una key estable
    val numeroPaso: Int,
    val titulo: String?,
    val descripcion: String,
    val mediaUrls: List<String>? = null         // la URL que devuelve tu /api/images/upload
)