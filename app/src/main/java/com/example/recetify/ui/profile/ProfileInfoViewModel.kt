// ProfileInfoViewModel.kt
package com.example.recetify.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileInfoViewModel(app: Application) : AndroidViewModel(app) {
    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user

    init { loadUser() }

    private fun loadUser() = viewModelScope.launch {
        _user.value = try {
            RetrofitClient.api.getCurrentUser()
        } catch (e: Exception) {
            null
        }
    }
}