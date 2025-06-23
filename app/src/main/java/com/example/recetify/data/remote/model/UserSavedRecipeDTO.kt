package com.example.recetify.data.remote.model

import com.google.gson.annotations.SerializedName

data class UserSavedRecipeDTO(
    val id: Long,
    @SerializedName("recipeId")
    val recipeId: Long,
    @SerializedName("recipeNombre")
    val recipeNombre: String,
    @SerializedName("fechaAgregado")
    val fechaAgregado: String
)