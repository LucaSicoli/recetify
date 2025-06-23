package com.example.recetify.data.remote.model

import com.google.gson.annotations.SerializedName

data class RecipeSummaryResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    @SerializedName("mediaUrls")
    val mediaUrls: List<String>? = null,
    val tiempo: Long,
    val porciones: Int,
    val tipoPlato: String,
    val categoria: String,
    val usuarioCreadorAlias: String?,
    val promedioRating: Double?,
    val estadoAprobacion: String,            // <-- agregar en el DTO
    val estadoPublicacion: String
)