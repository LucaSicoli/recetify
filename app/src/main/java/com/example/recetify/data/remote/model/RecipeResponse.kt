package com.example.recetify.data.remote.model

data class RecipeResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val fotoPrincipal: String?,
    val tiempo: Int,
    val porciones: Int,
    val tipoPlato: String,
    val categoria: String,
    val ingredients: List<IngredientDTO> = emptyList(),
    val steps: List<StepDTO> = emptyList(),
    val fechaCreacion: String,
    val estado: String,

    // ------> NUEVAS (con default para que no crashee aún sin backend):
    val usuarioCreadorAlias: String? = null,
    val promedioRating: Double?       = null
)
data class IngredientDTO(
    val nombre: String,
    val cantidad: Double,
    val unidadMedida: String
)

data class StepDTO(
    val numeroPaso: Int,
    val titulo: String,
    val descripcion: String,
    val urlMedia: String? = null
)