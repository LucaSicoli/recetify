package com.example.recetify.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_recipes")
data class CustomRecipeEntity(
    @PrimaryKey val recipeId: Long,
    val ownerEmail: String,
    val nombre: String,
    val fechaGuardado: String,
    val porciones: Int,                         // nuevo
    val ingredientsJson: String?,               // almacenas JSON
    val mediaUrls: List<String>?
)