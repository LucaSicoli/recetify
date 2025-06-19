// data/db/StepEntity.kt
package com.example.recetify.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "steps",
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
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val recipeId: Long,
    val numeroPaso: Int,
    val titulo: String,
    val descripcion: String,
    val urlMedia: String?
)