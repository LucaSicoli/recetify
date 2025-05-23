package com.example.recetify.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.model.CodeDTO
import com.example.recetify.data.remote.model.EmailDTO
import com.example.recetify.data.remote.model.ResetDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordResetViewModel : ViewModel() {
    private val _state = MutableStateFlow(ResetState())
    val state: StateFlow<ResetState> = _state.asStateFlow()

    fun onEmailChange(e: String)        = _state.update { it.copy(email = e) }
    fun onCodeChange(c: String)         = _state.update { it.copy(code = c) }
    fun onNewPassChange(p: String)      = _state.update { it.copy(newPassword = p) }
    fun onConfirmPassChange(p: String)  = _state.update { it.copy(confirmPassword = p) }

    fun requestReset(onSuccess: ()->Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                RetrofitClient.api.requestReset(EmailDTO(state.value.email))
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = t.localizedMessage) }
            }
        }
    }

    fun verifyCode(
        code: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                RetrofitClient.api.verifyResetCode(
                    CodeDTO(state.value.email, code)
                )
                // guardamos el código que acaba de validarse
                _state.update { it.copy(isLoading = false, code = code) }
                onSuccess()
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = t.localizedMessage) }
            }
        }
    }


    fun resetPassword(onSuccess: ()->Unit) {
        viewModelScope.launch {
            if (state.value.newPassword != state.value.confirmPassword) {
                _state.update { it.copy(error = "Las contraseñas no coinciden") }
                return@launch
            }
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                RetrofitClient.api.resetPassword(
                    ResetDTO(
                        state.value.email,
                        state.value.code,
                        state.value.newPassword,
                        state.value.confirmPassword
                    )
                )
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = t.localizedMessage) }
            }
        }
    }
}
