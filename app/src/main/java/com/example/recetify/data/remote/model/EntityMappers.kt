// app/src/main/java/com/example/recetify/data/remote/model/EntityMappers.kt
package com.example.recetify.data.remote.model

import com.example.recetify.data.db.*

/** ────────────────────────────────────────────────────────────────────────────
 *  De los modelos remotos (DTOs) a las entidades de Room
 *  ──────────────────────────────────────────────────────────────────────────── */

/** RecipeResponse → RecipeEntity */
fun RecipeResponse.toEntity(): RecipeEntity =
    RecipeEntity(
        id                  = id,
        nombre              = nombre,
        descripcion         = descripcion ?: "",
        fotoPrincipal       = fotoPrincipal,
        tiempo              = tiempo.toInt(),
        porciones           = porciones,
        tipoPlato           = tipoPlato,
        categoria           = categoria,
        usuarioCreadorAlias = usuarioCreadorAlias,
        promedioRating      = promedioRating
    )

/** IngredientDTO → IngredientEntity */
fun IngredientDTO.toEntity(recipeId: Long): IngredientEntity =
    IngredientEntity(
        recipeId     = recipeId,
        nombre       = nombre,
        cantidad     = cantidad,
        unidadMedida = unidadMedida
    )

/** StepDTO → StepEntity */
fun StepDTO.toEntity(recipeId: Long): StepEntity =
    StepEntity(
        recipeId    = recipeId,
        numeroPaso  = numeroPaso,
        titulo      = titulo,
        descripcion = descripcion,
        urlMedia    = urlMedia
    )

/** RatingResponse → RatingEntity */
fun RatingResponse.toEntity(recipeId: Long): RatingEntity =
    RatingEntity(
        recipeId   = recipeId,
        userAlias  = userAlias,
        puntos     = puntos,
        comentario = comentario,
        fecha      = fecha
    )



/** ────────────────────────────────────────────────────────────────────────────
 *  De las entidades de Room a modelos de UI / DTOs remotos
 *  ──────────────────────────────────────────────────────────────────────────── */

/** RecipeEntity → RecipeResponse (sin relaciones) */
fun RecipeEntity.toRecipeResponse(): RecipeResponse =
    RecipeResponse(
        id                  = id,
        nombre              = nombre,
        descripcion         = descripcion,
        fotoPrincipal       = fotoPrincipal,
        tiempo              = tiempo,
        porciones           = porciones,
        tipoPlato           = tipoPlato,
        categoria           = categoria,
        ingredients         = emptyList(),    // se rellenan más abajo
        steps               = emptyList(),
        fechaCreacion       = "",
        estado              = "",
        usuarioCreadorAlias = usuarioCreadorAlias,
        promedioRating      = promedioRating
    )

/**
 * RatingEntity → RatingResponse
 *
 * Ahora incluye recipeNombre.
 */
fun RatingEntity.toModel(): RatingResponse =
    RatingResponse(
        id        = this.localId,
        userAlias = this.userAlias,
        puntos    = this.puntos,
        comentario= this.comentario,
        fecha     = this.fecha
    )



/** ────────────────────────────────────────────────────────────────────────────
 *  Combina RecipeWithDetails → RecipeResponse completo + lista de RatingResponse
 *  ──────────────────────────────────────────────────────────────────────────── */

/**
 * RecipeWithDetails → RecipeResponse + colecciones internas transformadas
 *
 * NOTA: el composable recibe la lista de RatingResponse aparte,
 * así que aquí retornamos sólo el RecipeResponse "completo" con
 * ingredients y steps.
 */
fun RecipeWithDetails.toRecipeResponse(): RecipeResponse {
    // Ingredientes room → DTO remoto
    val ingrDTOs = ingredients.map { ing ->
        IngredientDTO(
            nombre       = ing.nombre,
            cantidad     = ing.cantidad,
            unidadMedida = ing.unidadMedida
        )
    }

    // Pasos room → DTO remoto
    val stepDTOs = steps.map { st ->
        StepDTO(
            numeroPaso  = st.numeroPaso,
            titulo      = st.titulo,
            descripcion = st.descripcion,
            urlMedia    = st.urlMedia
        )
    }

    return RecipeResponse(
        id                   = recipe.id,
        nombre               = recipe.nombre,
        descripcion          = recipe.descripcion,
        fotoPrincipal        = recipe.fotoPrincipal,
        tiempo               = recipe.tiempo,
        porciones            = recipe.porciones,
        tipoPlato            = recipe.tipoPlato,
        categoria            = recipe.categoria,
        ingredients          = ingrDTOs,
        steps                = stepDTOs,
        fechaCreacion        = "",
        estado               = "",
        usuarioCreadorAlias  = recipe.usuarioCreadorAlias,
        promedioRating       = recipe.promedioRating
    )
}