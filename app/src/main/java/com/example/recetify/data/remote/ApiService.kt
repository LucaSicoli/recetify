// app/src/main/java/com/example/recetify/data/remote/ApiService.kt
package com.example.recetify.data.remote

import com.example.recetify.data.remote.model.CodeDTO
import com.example.recetify.data.remote.model.CreateRatingRequest
import com.example.recetify.data.remote.model.EmailDTO
import com.example.recetify.data.remote.model.JwtResponse
import com.example.recetify.data.remote.model.LoginRequest
import com.example.recetify.data.remote.model.RatingResponse
import com.example.recetify.data.remote.model.RecipeRequest
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.RecipeSummaryResponse
import com.example.recetify.data.remote.model.ResetDTO
import com.example.recetify.data.remote.model.RecipeStepRequest
import com.example.recetify.data.remote.model.RecipeIngredientRequest
import com.example.recetify.data.remote.model.UserResponse
import com.example.recetify.data.remote.model.UserSavedRecipeDTO
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    // —— Autenticación & recuperación de contraseña ——
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): JwtResponse

    @POST("api/auth/request-reset")
    suspend fun requestReset(@Body req: EmailDTO): Unit

    @POST("api/auth/verify-reset-code")
    suspend fun verifyResetCode(@Body req: CodeDTO): Unit

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body req: ResetDTO): Unit


    // —— Recetas ——
    @GET("recipes/{id}")
    suspend fun getRecipeById(@Path("id") id: Long): RecipeResponse

    @GET("recipes")
    suspend fun getAllRecipes(): List<RecipeResponse>

    /** Para el listado en Home, usamos el resumen con promedio y alias */
    @GET("recipes/summary")
    suspend fun getAllRecipesSummary(): List<RecipeSummaryResponse>

    /** Crear receta (requiere JWT) */
    @POST("recipes/create")
    suspend fun createRecipe(@Body req: RecipeRequest): RecipeResponse

    @POST("recipes/draft")
    suspend fun saveDraft(@Body req: RecipeRequest): RecipeResponse

    @PUT("recipes/{id}/draft/full")
    suspend fun syncDraftFull(
        @Path("id") id: Long,
        @Body req: RecipeRequest
    ): RecipeResponse    // ¡sigue siendo RecipeResponse!

    /** Listar borradores del usuario autenticado */
    @GET("recipes/drafts")
    suspend fun listDrafts(): List<RecipeSummaryResponse>

    @PUT("recipes/{id}/draft")
    suspend fun updateDraft(
        @Path("id") id: Long,
        @Body req: RecipeRequest
    ): RecipeResponse

    @GET("/recipes/saved")
    suspend fun listSavedRecipes(): List<UserSavedRecipeDTO>

    /** Publicar un borrador */
    @PUT("recipes/{id}/publish")
    suspend fun publishDraft(@Path("id") recipeId: Long): RecipeResponse

    // —— Ratings ——
    @GET("ratings/recipe/{recipeId}")
    suspend fun getRatingsForRecipe(@Path("recipeId") recipeId: Long): List<RatingResponse>

    @POST("ratings")
    suspend fun addRating(@Body req: CreateRatingRequest): RatingResponse

    @GET("recipes/created")
    suspend fun getMyPublishedRecipes(): List<RecipeSummaryResponse>

    @GET("/users/me")
    suspend fun getCurrentUser(): UserResponse

    @GET("ratings/count/me")
    suspend fun countMyReviews(): Int

    // —— Subida de imágenes ——
    @Multipart
    @POST("api/images/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): String
}