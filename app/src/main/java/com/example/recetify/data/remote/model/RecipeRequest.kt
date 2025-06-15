package com.example.recetify.data.remote.model

data class RecipeRequest(
    val nombre: String,
    val descripcion: String,
    val tiempo: Int,
    val porciones: Int,
    val fotoPrincipal: String?,        // URL resultante de subir la foto principal
    val tipoPlato: String,             // debe coincidir con tu enum TipoPlato (p.ej. "POSTRE")
    val categoria: String,             // igual, nombre de tu enum Categoria
    val ingredients: List<RecipeIngredientRequest> = emptyList(),
    val steps: List<RecipeStepRequest> = emptyList()
)