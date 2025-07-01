// File: app/src/main/java/com/example/recetify/ui/search/SearchRepository.kt
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
        runCatching {
            api.searchRecipes(name, type, ingredient, excludeIngredient, userAlias, sort)
        }.getOrDefault(emptyList())
}