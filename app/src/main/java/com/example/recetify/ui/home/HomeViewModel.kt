package com.example.recetify.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.RecipeResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URI

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val _recipes = MutableStateFlow<List<RecipeResponse>>(emptyList())
    val recipes: StateFlow<List<RecipeResponse>> = _recipes

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // 1) Descarga JSON
            val fetched = try {
                RetrofitClient.api.getAllRecipes()
            } catch (e: Throwable) {
                emptyList<RecipeResponse>()
            }

            // 2) Precarga imágenes con Coil
            val context = getApplication<Application>() // <<=== tipado explícito
            val loader = ImageLoader(context)

            fetched.forEach { recipe ->
                val base     = RetrofitClient.BASE_URL.trimEnd('/')
                val original = recipe.fotoPrincipal.orEmpty()

                val pathOnly = runCatching {
                    val uri = URI(original)
                    uri.rawPath + uri.rawQuery?.let { "?$it" }.orEmpty()
                }.getOrNull() ?: original

                val finalUrl = if (pathOnly.startsWith("/")) "$base$pathOnly" else "$base/$pathOnly"

                val request = ImageRequest.Builder(context) // <<=== pasás el context explícito
                    .data(finalUrl)
                    .build()

                runCatching { loader.execute(request) } // precarga sin bloquear
            }

            // 3) Emitir datos listos
            _recipes.value = fetched
            _isLoading.value = false
        }
    }
}
