package com.example.recetify.data.remote.model

data class LoginRequest(
    val email: String,
    val alias: String,
    val password: String
)
