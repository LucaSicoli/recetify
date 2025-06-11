package com.example.recetify.data.remote

import com.example.recetify.data.remote.model.UserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApiService {
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Long): Response<UserDto>

    @GET("/users/{id}/profile")
    suspend fun getUserProfile(@Path("id") userId: Long): UserDto

    @GET("/api/usuarios/{id}")
    suspend fun obtenerUsuarioPorId(@Path("id") id: Long): Response<UserDto>


}
