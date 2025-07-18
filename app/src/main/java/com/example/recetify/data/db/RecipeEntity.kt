// data/db/RecipeEntity.kt
package com.example.recetify.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("recipes")
data class RecipeEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val descripcion: String?,
    val mediaUrls: List<String>?,
    val tiempo: Int,
    val porciones: Int,
    val tipoPlato: String,
    val categoria: String,
    val usuarioCreadorAlias: String?,
    val promedioRating: Double?,
    val estadoAprobacion: String   = "",
    val estadoPublicacion: String? = "",
    val fechaCreacion: String? = null // Nuevo campo para fecha de creación
)