package com.example.recetify.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.UserDto
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProfileViewModel : ViewModel() {

    var onUnauthorized: () -> Unit = {}

    var profileState by mutableStateOf<UserDto?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var recetasPublicadas by mutableStateOf(0)
        private set

    var recetasGuardadas by mutableStateOf(0)
        private set

    var resenasPublicadas by mutableStateOf(0)
        private set

    // NO llamar fetchProfile() en init para evitar error si onUnauthorized no está seteado aún

    fun fetchProfile() {
        viewModelScope.launch {
            isLoading = true
            try {
                val profile = RetrofitClient.api.getMyProfile()
                profileState = profile

                val resumen = RetrofitClient.api.getProfileSummary()
                recetasPublicadas = resumen.recetasPublicadas
                recetasGuardadas = resumen.recetasGuardadas
                resenasPublicadas = resumen.reseñasPublicadas

                errorMessage = null
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    onUnauthorized()
                } else {
                    errorMessage = "Error al cargar perfil: ${e.localizedMessage}"
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar perfil: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
