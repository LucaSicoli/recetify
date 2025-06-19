// data/db/RatingEntity.kt
package com.example.recetify.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "ratings",
    foreignKeys = [
        ForeignKey(
            entity        = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns  = ["recipeId"],
            onDelete      = CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class RatingEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val recipeId: Long,
    val userAlias: String,
    val puntos: Int,
    val comentario: String,
    val fecha: String
)