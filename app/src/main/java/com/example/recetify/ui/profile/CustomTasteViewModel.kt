// File: app/src/main/java/com/example/recetify/ui/profile/CustomTasteViewModel.kt
package com.example.recetify.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.db.CustomRecipeDao
import com.example.recetify.data.db.CustomRecipeEntity
import com.example.recetify.data.db.DatabaseProvider
import com.example.recetify.data.db.UserCustomRecipeDTO
import com.example.recetify.data.remote.model.ISavedRecipe
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
    private val _customRecipes = MutableStateFlow<List<ISavedRecipe>>(emptyList())
    val customRecipes: StateFlow<List<ISavedRecipe>> = _customRecipes

    private val _showLimitDialog = MutableStateFlow(false)
    val showLimitDialog: StateFlow<Boolean> = _showLimitDialog

    init {
        viewModelScope.launch {
            ownerEmail = SessionManager.getCurrentUserEmail(app.applicationContext)
            dao.getAllForUser(ownerEmail).collect { list ->
                //  dentro de init { dao.getAllForUser(ownerEmail).collect { … } }
                _customRecipes.value = list.map { e ->
                    UserCustomRecipeDTO(
                        id            = e.recipeId,
                        recipeId      = e.recipeId,
                        recipeNombre  = e.nombre,
                        fechaAgregado = e.fechaGuardado,
                        mediaUrls     = e.mediaUrls.orEmpty(),
                        porciones     = e.porciones,
                        tiempo        = e.tiempo,
                        ingredients   = e.ingredients,
                        steps         = e.steps                           // ✅ NUEVO
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
            val count = withContext(Dispatchers.IO) { dao.countForUser(ownerEmail) }
            if (count >= 10) {
                _showLimitDialog.value = true
                onError("Ya tienes 10 recetas en “Mi gusto”. Borra alguna antes.")
                return@launch
            }
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            val uniqueId = System.currentTimeMillis() // O UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
            withContext(Dispatchers.IO) {
                dao.insert(
                    CustomRecipeEntity(
                        recipeId      = uniqueId,
                        ownerEmail    = ownerEmail,
                        nombre        = rec.nombre,
                        fechaGuardado = now,
                        porciones     = rec.porciones,
                        tiempo        = rec.tiempo,                       // ya estaba
                        ingredients   = rec.ingredients,
                        steps         = rec.steps,                        // ✅ NUEVO
                        mediaUrls     = rec.mediaUrls.orEmpty()
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

    fun dismissLimitDialog() {
        _showLimitDialog.value = false
    }
}