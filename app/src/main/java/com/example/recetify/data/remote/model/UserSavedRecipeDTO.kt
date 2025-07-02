// app/src/main/java/com/example/recetify/data/remote/model/UserSavedRecipeDTO.kt
package com.example.recetify.data.remote.model

import com.google.gson.annotations.SerializedName

data class UserSavedRecipeDTO(
    override val id: Long,
    override val recipeId: Long,
    override val recipeNombre: String,
    override val fechaAgregado: String,
    override val mediaUrls: List<String>
) : ISavedRecipe