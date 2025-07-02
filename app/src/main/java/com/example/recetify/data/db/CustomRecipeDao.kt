// File: app/src/main/java/com/example/recetify/data/db/CustomRecipeDao.kt
package com.example.recetify.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomRecipeDao {

    /**
     * Flujo de las “recetas a mi gusto” de un usuario,
     * ordenadas de más reciente a más antigua.
     */
    @Query("""
    SELECT * FROM custom_recipes WHERE ownerEmail = :ownerEmail ORDER BY fechaGuardado DESC
""")
    fun getAllForUser(ownerEmail: String): Flow<List<CustomRecipeEntity>>

    /**
     * Número total de recetas guardadas por un usuario.
     * Método síncrono (Room lo ejecutará en background si lo llamas desde IO).
     */
    @Query("""
      SELECT COUNT(*) 
        FROM custom_recipes 
       WHERE ownerEmail = :ownerEmail
    """)
    fun countForUser(ownerEmail: String): Int

    /**
     * Inserta o reemplaza una receta “a mi gusto”.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: CustomRecipeEntity)

    /**
     * Borra la fila que tenga recipeId = :id y ownerEmail = :ownerEmail.
     * Devuelve el número de filas eliminadas.
     */
    @Query("""
      DELETE 
        FROM custom_recipes 
       WHERE recipeId = :id 
         AND ownerEmail = :ownerEmail
    """)
    fun delete(id: Long, ownerEmail: String): Int
}