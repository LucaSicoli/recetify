// app/src/main/java/com/example/recetify/ui/details/LocalRecipeDetailScreen.kt
package com.example.recetify.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.recetify.data.db.UserCustomRecipeDTO
import com.example.recetify.data.remote.model.*
import com.example.recetify.ui.navigation.BottomNavBar
import com.example.recetify.ui.profile.CustomTasteViewModel
import kotlinx.coroutines.flow.firstOrNull

/* ─────────────────────────────────────────────────────────── */

@Composable
fun LocalRecipeDetailScreen(
    localRecipeId: Long,
    navController: NavController,
    customVm: CustomTasteViewModel = viewModel()
) {
    val context     = androidx.compose.ui.platform.LocalContext.current

    /* 1 ─ Escuchamos la lista “Mi gusto” */
    val customList by customVm.customRecipes.collectAsState()

    /* 2 ─ Encontramos la receta pedida */
    val dto: UserCustomRecipeDTO? = remember(customList) {
        customList.filterIsInstance<UserCustomRecipeDTO>()
            .firstOrNull { it.recipeId == localRecipeId }
    }

    /* 3 ─ ¿Alumno? → BottomBar */
    val isAlumno by SessionManager
        .isAlumnoFlow(context)
        .collectAsState(initial = false)

    /* 4 ─ Alias del usuario (una sola lectura) */
    val userAlias by produceState<String?>(initialValue = null) {
        value = SessionManager.currentUserEmailFlow(context).firstOrNull() ?: "Yo"
    }

    Scaffold(
        containerColor = Color.White,                    // quita franja negra
        bottomBar      = { BottomNavBar(navController, isAlumno) }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (dto == null || userAlias == null) {
                CircularProgressIndicator()
            } else {
                /* 5 ─ Map DTO → RecipeResponse usando el mapper global */
                val receta = dto.toRecipeResponse(userAlias!!)

                /* 6 ─ Reutilizamos la UI remota 1:1 */
                RecipeDetailContent(
                    receta             = receta,
                    ratings            = emptyList(),
                    padding            = PaddingValues(0.dp),
                    showIngredients    = remember { mutableStateOf(true) },
                    currentStep        = remember { mutableStateOf(0) },
                    navController      = navController,
                    profileUrl         = null,
                    onSendRating       = { _, _ -> },
                    isFavorite         = true,
                    onToggleFavorite   = { /* sin favoritos locales */ },
                    onSaveEditedRecipe = { /* opcional */ }
                )
            }
        }
    }
}