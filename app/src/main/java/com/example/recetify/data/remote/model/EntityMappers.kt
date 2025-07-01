package com.example.recetify.data.remote.model

import com.example.recetify.data.db.*

// ── Remoto → Room ───────────────────────────────────────────────

fun RecipeResponse.toEntity(): RecipeEntity = RecipeEntity(
    id                  = id,
    nombre              = nombre,
    descripcion         = descripcion.orEmpty(),
    mediaUrls           = mediaUrls.orEmpty(),
    tiempo              = tiempo,
    porciones           = porciones,
    tipoPlato           = tipoPlato,
    categoria           = categoria,
    usuarioCreadorAlias = usuarioCreadorAlias,
    promedioRating      = promedioRating,
    estadoAprobacion    = estado,             // usas “estado” para aprobación
    estadoPublicacion   = estadoPublicacion         // o el string que quieras
)

fun IngredientDTO.toEntity(recipeId: Long): IngredientEntity =
    IngredientEntity(
        recipeId     = recipeId,
        nombre       = nombre,
        cantidad     = cantidad,
        unidadMedida = unidadMedida
    )

fun StepDTO.toEntity(recipeId: Long): StepEntity =
    StepEntity(
        recipeId    = recipeId,
        numeroPaso  = numeroPaso,
        titulo      = titulo,
        descripcion = descripcion,
        mediaUrls   = mediaUrls ?: emptyList()
    )

fun RatingResponse.toEntity(recipeId: Long): RatingEntity =
    RatingEntity(
        recipeId   = recipeId,
        userAlias  = userAlias,
        puntos     = puntos,
        comentario = comentario,
        fecha      = fecha
    )

// ── Room → Remoto / UI ─────────────────────────────────────────

fun RecipeWithDetails.toRecipeResponse(): RecipeResponse {
    val ingrDTOs = ingredients.map { ing ->
        IngredientDTO(ing.nombre, ing.cantidad, ing.unidadMedida)
    }
    val stepDTOs = steps.map { st ->
        StepDTO(st.numeroPaso, st.titulo, st.descripcion, st.mediaUrls)
    }
    return RecipeResponse(
        id                  = recipe.id,
        nombre              = recipe.nombre,
        descripcion         = recipe.descripcion,
        mediaUrls           = recipe.mediaUrls,
        tiempo              = recipe.tiempo,
        porciones           = recipe.porciones,
        tipoPlato           = recipe.tipoPlato,
        categoria           = recipe.categoria,
        ingredients         = ingrDTOs,
        steps               = stepDTOs,
        fechaCreacion       = "",                            // o la fecha real
        estado              = recipe.estadoAprobacion ?: "", // nunca null
        estadoPublicacion   = recipe.estadoPublicacion ?: "",
        usuarioCreadorAlias = recipe.usuarioCreadorAlias,
        promedioRating      = recipe.promedioRating
    )
}

fun RecipeSummaryResponse.toEntity(): RecipeEntity =
    RecipeEntity(
        id                  = this.id,
        nombre              = this.nombre,
        descripcion         = this.descripcion,
        mediaUrls           = this.mediaUrls,
        tiempo              = this.tiempo.toInt(),
        porciones           = this.porciones,
        tipoPlato           = this.tipoPlato,
        categoria           = this.categoria,
        usuarioCreadorAlias = this.usuarioCreadorAlias,
        promedioRating      = this.promedioRating,
        estadoAprobacion    = this.estadoAprobacion,
        estadoPublicacion   = this.estadoPublicacion
    )

// ← Nuevo mapper para UI de ratings
fun RatingEntity.toRatingResponse(): RatingResponse =
    RatingResponse(
        id = recipeId,
        userAlias  = userAlias,
        puntos     = puntos,
        comentario = comentario,
        fecha      = fecha
    )
