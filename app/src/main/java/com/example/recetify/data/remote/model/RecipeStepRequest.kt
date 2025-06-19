package com.example.recetify.data.remote.model

data class RecipeStepRequest(
    val numeroPaso: Int,
    val titulo: String?,
    val descripcion: String,
    val urlMedia: String?             // la URL que devuelve tu /api/images/upload
)