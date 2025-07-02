// File: app/src/main/java/com/example/recetify/ui/profile/CustomTasteViewModel.kt
package com.example.recetify.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.db.CustomRecipeDao
import com.example.recetify.data.db.CustomRecipeEntity
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.remote.model.RecipeResponse
import com.example.recetify.data.remote.model.UserSavedRecipeDTO
import com.example.recetify.data.remote.model.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CustomTasteViewModel(app: Application) : AndroidViewModel(app) {
    private val dao: CustomRecipeDao =
        DatabaseProvider.getInstance(app).customRecipeDao()

    // guardamos aquí el e-mail, lo inicializamos en init { … }
    private var ownerEmail: String = ""

    // back-state de tu lista “Mi gusto”
    private val _customRecipes = MutableStateFlow<List<UserSavedRecipeDTO>>(emptyList())
    val customRecipes: StateFlow<List<UserSavedRecipeDTO>> = _customRecipes

    init {
        // 1) arrancamos un coroutine para leer el e-mail y luego cargar la lista
        viewModelScope.launch {
            ownerEmail = SessionManager.getCurrentUserEmail(app.applicationContext)
            // 2) luego coleccionamos el DAO y lo mapeamos al DTO
            dao.getAllForUser(ownerEmail).collect { list ->
                _customRecipes.value = list.map { e ->
                    UserSavedRecipeDTO(
                        id            = e.recipeId,
                        recipeId      = e.recipeId,
                        recipeNombre  = e.nombre,
                        fechaAgregado = e.fechaGuardado,
                        mediaUrls     = e.mediaUrls.orEmpty()
                    )
                }
            }
        }
    }

    /**
     * Agrega una receta a “Mi gusto”, pero no permite más de 10.
     */
    fun addCustom(rec: RecipeResponse, onError: (String) -> Unit) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                dao.countForUser(ownerEmail)
            }
            if (count >= 10) {
                onError("Ya tienes 10 recetas en “Mi gusto”. Borra alguna antes.")
                return@launch
            }
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            withContext(Dispatchers.IO) {
                // ——————————————————————————
                // Aquí serializas tu lista de ingredientes:
                val gson = Gson()
                val ingredientsJson = gson.toJson(rec.ingredients)
                // ——————————————————————————
                dao.insert(
                    CustomRecipeEntity(
                        recipeId       = rec.id,
                        ownerEmail     = ownerEmail,
                        nombre         = rec.nombre,
                        fechaGuardado  = now,
                        porciones      = rec.porciones,       // tu nuevo campo
                        ingredientsJson= ingredientsJson,     // aquí el JSON
                        mediaUrls      = rec.mediaUrls
                    )
                )
            }
        }
    }

    /**
     * Quita una receta de “Mi gusto”.
     */
    fun removeCustom(recipeId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(recipeId, ownerEmail)
        }
    }
}