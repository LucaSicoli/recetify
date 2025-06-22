package com.example.recetify.data.remote.model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionPrefs by preferencesDataStore("session_prefs")
private val KEY_IS_ALUMNO = booleanPreferencesKey("is_alumno")

// SessionManager.kt
object SessionManager {
    private var jwtToken: String? = null
    private val Context.sessionPrefs by preferencesDataStore("session_prefs")
    private val KEY_IS_ALUMNO    = booleanPreferencesKey("is_alumno")
    private val KEY_JWT_TOKEN    = stringPreferencesKey("jwt_token")

    suspend fun setAlumno(context: Context, token: String) {
        this.jwtToken = token
        context.sessionPrefs.edit {
            it[KEY_IS_ALUMNO] = true
            it[KEY_JWT_TOKEN] = token
        }
    }

    suspend fun setVisitante(context: Context) {
        jwtToken = null
        context.sessionPrefs.edit {
            it[KEY_IS_ALUMNO] = false
            it.remove(KEY_JWT_TOKEN)
        }
    }

    fun getToken(): String? = jwtToken

    fun clearToken() {
        jwtToken = null
    }

    fun isAlumnoFlow(context: Context): Flow<Boolean> =
        context.sessionPrefs.data.map { it[KEY_IS_ALUMNO] ?: false }

    fun tokenFlow(context: Context): Flow<String?> =
        context.sessionPrefs.data.map { it[KEY_JWT_TOKEN] }
}