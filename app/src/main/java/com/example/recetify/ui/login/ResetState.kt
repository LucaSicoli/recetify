// src/main/java/com/example/recetify/ui/login/ResetState.kt
package com.example.recetify.ui.login

data class ResetState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,

    // Validaciones de contraseña
    val isLengthValid: Boolean = false,
    val hasUppercase: Boolean = false,
    val hasNumberOrSymbol: Boolean = false
)
