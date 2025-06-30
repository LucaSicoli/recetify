// app/src/main/java/com/example/recetify/data/remote/model/UserSavedRecipeDTO.kt
package com.example.recetify.data.remote.model

import com.google.gson.annotations.SerializedName

data class UserSavedRecipeDTO(
    val id: Long,
    @SerializedName("recipeId")
    val recipeId: Long,
    @SerializedName("recipeNombre")
    val recipeNombre: String,
    @SerializedName("fechaAgregado")
    val fechaAgregado: String,
    @SerializedName("mediaUrls")
    val mediaUrls: List<String>  // <-- nuevo campo
)