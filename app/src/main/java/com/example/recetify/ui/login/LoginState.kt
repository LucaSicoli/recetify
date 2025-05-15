package com.example.recetify.ui.login

data class LoginState(
    val email: String = "",
    val alias: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val token: String? = null
)
