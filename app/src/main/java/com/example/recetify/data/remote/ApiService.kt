package com.example.recetify.data.remote
import com.example.recetify.data.remote.model.JwtResponse
import com.example.recetify.data.remote.model.LoginRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): JwtResponse

}
