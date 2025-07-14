package com.example.recetify.data.remote.model

data class PasswordResetResponse(
    val status: Status,
    val message: String
) {
    enum class Status {
        SUCCESS,
        USER_NOT_FOUND,
        USER_INACTIVE
    }
}