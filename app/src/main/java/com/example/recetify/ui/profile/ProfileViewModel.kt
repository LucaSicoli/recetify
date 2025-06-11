package com.example.recetify.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var profileState by mutableStateOf<UserDto?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            isLoading = true
            try {
                val profile = RetrofitClient.api.getMyProfile()
                profileState = profile
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Error al cargar perfil: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}

