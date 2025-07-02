// app/src/main/java/com/example/recetify/data/db/Converters.kt
package com.example.recetify.data.db

import androidx.room.TypeConverter
import com.example.recetify.data.remote.model.IngredientDTO
import com.example.recetify.data.remote.model.StepDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Converters para que Room pueda serializar/des-serializar
 * listas de DTOs y de cadenas a/cadenas JSON en la BD.
 */
class Converters {

    /* Reutilizamos una única instancia de Gson */
    private val gson = Gson()

    /* ───────────── IngredientDTO ───────────── */

    @TypeConverter
    fun fromIngredientList(list: List<IngredientDTO>?): String =
        gson.toJson(list)

    @TypeConverter
    fun toIngredientList(json: String?): List<IngredientDTO> =
        if (json.isNullOrEmpty() || json == "null") emptyList()
        else gson.fromJson(json, object : TypeToken<List<IngredientDTO>>() {}.type)

    /* ───────────── StepDTO (NUEVO) ───────────── */

    @TypeConverter
    fun fromStepList(list: List<StepDTO>?): String =
        gson.toJson(list)

    @TypeConverter
    fun toStepList(json: String?): List<StepDTO> =
        if (json.isNullOrEmpty() || json == "null") emptyList()
        else gson.fromJson(json, object : TypeToken<List<StepDTO>>() {}.type)

    /* ───────────── List<String> (urls, etc.) ───────────── */

    @TypeConverter
    fun fromStringList(list: List<String>?): String =
        gson.toJson(list)

    @TypeConverter
    fun toStringList(json: String?): List<String> =
        if (json.isNullOrEmpty() || json == "null") emptyList()
        else gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
}