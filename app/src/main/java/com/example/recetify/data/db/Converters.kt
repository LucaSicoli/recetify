// app/src/main/java/com/example/recetify/data/db/Converters.kt
package com.example.recetify.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromList(value: List<String>?): String? =
        value?.let { gson.toJson(it) }

    @TypeConverter
    fun toList(value: String?): List<String>? =
        value?.let {
            gson.fromJson<List<String>>(it, object : TypeToken<List<String>>(){}.type)
        }
}