package com.example.recetify.data.remote.model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionPrefs by preferencesDataStore("session_prefs")
private val KEY_IS_ALUMNO = booleanPreferencesKey("is_alumno")

object SessionManager {
    private var jwtToken: String? = null
    fun getToken(): String? = jwtToken

    suspend fun setAlumno(context: Context, token: String) {
        jwtToken = token
        context.sessionPrefs.edit { it[KEY_IS_ALUMNO] = true }
    }

    suspend fun setVisitante(context: Context) {
        jwtToken = null
        context.sessionPrefs.edit { it[KEY_IS_ALUMNO] = false }
    }

    fun isAlumnoFlow(context: Context): Flow<Boolean> =
        context.sessionPrefs.data.map { it[KEY_IS_ALUMNO] ?: false }
}