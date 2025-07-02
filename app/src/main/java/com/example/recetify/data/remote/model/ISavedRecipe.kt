// File: com/example/recetify/data/remote/model/ISavedRecipe.kt
package com.example.recetify.data.remote.model

interface ISavedRecipe {
    val id: Long
    val recipeId: Long
    val recipeNombre: String
    val fechaAgregado: String
    val mediaUrls: List<String>
}