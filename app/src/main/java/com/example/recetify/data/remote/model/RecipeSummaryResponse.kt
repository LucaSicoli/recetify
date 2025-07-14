// app/src/main/java/com/example/recetify/data/remote/model/RecipeSummaryResponse.kt
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
    @SerializedName("usuarioFotoPerfil")
    val usuarioFotoPerfil: String?,  // ← Aquí
    val promedioRating: Double?,
    val estadoPublicacion: String,   // PENÚLTIMO (coincide con backend)
    val estado: String               // ÚLTIMO (este es el estado de aprobación)
)