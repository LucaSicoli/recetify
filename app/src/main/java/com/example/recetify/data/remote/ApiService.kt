package com.example.recetify.data.remote
import com.example.recetify.data.remote.model.CodeDTO
import com.example.recetify.data.remote.model.CreateRatingRequest
import com.example.recetify.data.remote.model.EmailDTO
import com.example.recetify.data.remote.model.JwtResponse
import com.example.recetify.data.remote.model.LoginRequest
import com.example.recetify.data.remote.model.ProfileSummaryDTO
import com.example.recetify.data.remote.model.RatingResponse
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.ResetDTO
import com.example.recetify.data.remote.model.UserDto
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

    @GET("recipes/{id}")
    suspend fun getRecipeById(@Path("id") id: Long): RecipeResponse

    @GET("recipes")
    suspend fun getAllRecipes(): List<RecipeResponse>

    @GET("ratings/recipe/{recipeId}")
    suspend fun getRatingsForRecipe(@Path("recipeId") id: Long): List<RatingResponse>

    @POST("ratings")
    suspend fun addRating(@Body req: CreateRatingRequest): RatingResponse

    @GET("recipes/summary")
    suspend fun getAllRecipesSummary(): List<RecipeResponse>

    @GET("users/me")
    suspend fun getMyProfile(): UserDto


    @GET("recipes/my-recipes")
    suspend fun getMyRecipes(): List<RecipeResponse>

    @GET("user-saved-recipes/user/{userId}")
    suspend fun getSavedRecipesByUser(@Path("userId") userId: Long): List<RecipeResponse>

    @GET("ratings/user/{userId}")
    suspend fun getRatingsByUser(@Path("userId") userId: Long): List<RatingResponse>


    @GET("users/profile-summary")
    suspend fun getProfileSummary(): ProfileSummaryDTO

}

