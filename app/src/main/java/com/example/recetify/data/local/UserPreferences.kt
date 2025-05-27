package com.example.recetify.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

// extensión sobre Context para obtener el DataStore
private val Context.userPrefsDataStore by preferencesDataStore("user_prefs")

class UserPreferences(context: Context) {

    private val ds = context.userPrefsDataStore

    companion object {
        private val KEY_ALIAS    = stringPreferencesKey("alias")
        private val KEY_EMAIL    = stringPreferencesKey("email")
        private val KEY_PASSWORD = stringPreferencesKey("password")
        private val KEY_REMEMBER = booleanPreferencesKey("remember_me")
    }

    // flujos de lectura
    val aliasFlow    = ds.data.map { it[KEY_ALIAS] }
    val emailFlow    = ds.data.map { it[KEY_EMAIL] }
    val passwordFlow = ds.data.map { it[KEY_PASSWORD] }
    val rememberFlow = ds.data.map { it[KEY_REMEMBER] ?: false }

    // escritura
    suspend fun saveLoginData(alias: String, email: String, password: String, remember: Boolean) {
        ds.edit { prefs ->
            prefs[KEY_ALIAS]    = alias
            prefs[KEY_EMAIL]    = email
            prefs[KEY_PASSWORD] = password
            prefs[KEY_REMEMBER] = remember
        }
    }

    // limpia todo
    suspend fun clearLoginData() {
        ds.edit { it.clear() }
    }
}
