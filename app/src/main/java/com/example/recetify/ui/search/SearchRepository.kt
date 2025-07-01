package com.example.recetify.ui.search

import com.example.recetify.data.remote.ApiService
import com.example.recetify.data.remote.model.RecipeSummaryResponse

class SearchRepository(
    private val api: ApiService
) {
    suspend fun search(
        name: String?,
        type: String?,
        ingredient: String?,
        excludeIngredient: String?,
        userAlias: String?,
        sort: String
    ): List<RecipeSummaryResponse> =
        api.searchRecipes(name, type, ingredient, excludeIngredient, userAlias, sort)
}