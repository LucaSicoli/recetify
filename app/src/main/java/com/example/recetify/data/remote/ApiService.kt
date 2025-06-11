package com.example.recetify.data.remote
import com.example.recetify.data.remote.model.CodeDTO
import com.example.recetify.data.remote.model.EmailDTO
import com.example.recetify.data.remote.model.JwtResponse
import com.example.recetify.data.remote.model.LoginRequest
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.ResetDTO
import com.example.recetify.data.remote.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): JwtResponse

    @POST("api/auth/request-reset")
    suspend fun requestReset(@Body req: EmailDTO): Unit

    @POST("api/auth/verify-reset-code")
    suspend fun verifyResetCode(@Body req: CodeDTO): Unit

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body req: ResetDTO): Unit

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Long): Response<UserDto>

    @GET("users/me")
    suspend fun getMyProfile(): UserDto


    @GET("users/{id}/profile")
    suspend fun getUserProfile(@Path("id") id: Long): UserDto

    @GET("recipes")
    suspend fun getAllRecipes(): List<RecipeResponse>

    @GET("recipes/mine")
    suspend fun getMyRecipes(): List<RecipeResponse>

}


