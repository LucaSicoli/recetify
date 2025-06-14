// data/remote/model/RecipeSummaryResponse.kt
package com.example.recetify.data.remote.model

data class RecipeSummaryResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val fotoPrincipal: String?,
    val tiempo: Long,
    val porciones: Int,
    val tipoPlato: String,
    val categoria: String,
    val usuarioCreadorAlias: String?,
    val promedioRating: Double?
)