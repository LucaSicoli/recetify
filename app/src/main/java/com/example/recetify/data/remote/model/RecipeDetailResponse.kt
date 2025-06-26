package com.example.recetify.data.remote.model
import com.example.recetify.data.remote.model.IngredientDTO
import com.example.recetify.data.remote.model.StepDTO
// RecipeDetailResponse.kt

data class RecipeDetailResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val fotoPrincipal: String?,
    val tiempo: Long,
    val porciones: Int,
    val tipoPlato: String,
    val categoria: String,
    val ingredients: List<IngredientDTO>,
    val steps: List<StepDTO>,
    val fechaCreacion: String,
    val estado: String,
    val usuarioCreadorAlias: String?,
    val promedioRating: Double?
)


