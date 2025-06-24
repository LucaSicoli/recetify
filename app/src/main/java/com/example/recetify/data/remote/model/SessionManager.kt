package com.example.recetify.data.remote.model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1) Extensión para obtener el DataStore
private val Context.sessionPrefs by preferencesDataStore(name = "session_prefs")

// 2) Tus claves de Preferences
private val KEY_IS_ALUMNO   = booleanPreferencesKey("is_alumno")
private val KEY_IS_VISITOR  = booleanPreferencesKey("is_visitor")
private val KEY_JWT_TOKEN   = stringPreferencesKey("jwt_token")

object SessionManager {
    private var jwtToken: String? = null

    /** Guarda token alumno */
    suspend fun setAlumno(context: Context, token: String) {
        jwtToken = token
        context.sessionPrefs.edit { prefs ->
            prefs[KEY_IS_ALUMNO]  = true
            prefs[KEY_IS_VISITOR] = false
            prefs[KEY_JWT_TOKEN]  = token
        }
    }

    /** Marca visitante (sin token) */
    suspend fun setVisitante(context: Context) {
        jwtToken = null
        context.sessionPrefs.edit { prefs ->
            prefs[KEY_IS_ALUMNO]  = false
            prefs[KEY_IS_VISITOR] = true
            prefs.remove(KEY_JWT_TOKEN)       // ← aquí se usa remove
        }
    }

    /** Borra TODO: alumno, visitante y token */
    suspend fun clearSession(context: Context) {
        jwtToken = null
        context.sessionPrefs.edit { prefs ->
            prefs.remove(KEY_IS_ALUMNO)
            prefs.remove(KEY_IS_VISITOR)
            prefs.remove(KEY_JWT_TOKEN)
        }
    }

    /** Token en memoria */
    fun getToken(): String? = jwtToken

    /** Flow del token */
    fun tokenFlow(context: Context): Flow<String?> =
        context.sessionPrefs.data.map { prefs ->
            prefs[KEY_JWT_TOKEN]
        }

    /** Flow: ¿es alumno? */
    fun isAlumnoFlow(context: Context): Flow<Boolean> =
        context.sessionPrefs.data.map { prefs ->
            prefs[KEY_IS_ALUMNO] ?: false
        }

    /** Flow: ¿está logueado (alumno o visitante)? */
    fun isLoggedInFlow(context: Context): Flow<Boolean> =
        context.sessionPrefs.data.map { prefs ->
            (prefs[KEY_IS_ALUMNO]  == true) ||
                    (prefs[KEY_IS_VISITOR] == true)
        }
}