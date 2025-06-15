package com.example.recetify.data.remote.model

data class AddFavoriteRequest(
    val user: IdWrapper,
    val recipe: IdWrapper
)

data class IdWrapper(val id: Long)