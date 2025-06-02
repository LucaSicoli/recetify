package com.example.recetify.data.remote.model

object SessionManager {
    private var token: String? = null
    fun saveToken(t: String) { token = t }
    fun getToken(): String? = token
}