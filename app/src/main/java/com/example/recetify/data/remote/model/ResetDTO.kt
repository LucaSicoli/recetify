package com.example.recetify.data.remote.model

data class ResetDTO(
    val email: String,
    val code: String,
    val newPassword: String,
    val confirmPassword: String
)