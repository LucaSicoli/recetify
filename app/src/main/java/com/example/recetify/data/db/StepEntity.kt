// app/src/main/java/com/example/recetify/data/db/StepEntity.kt
package com.example.recetify.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity        = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns  = ["recipeId"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
@TypeConverters(Converters::class)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val recipeId: Long,
    val numeroPaso: Int,
    val titulo: String,
    val descripcion: String,
    val mediaUrls: List<String>?         // varios medios por paso
)