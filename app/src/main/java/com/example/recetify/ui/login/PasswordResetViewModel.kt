package com.example.recetify.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.RetrofitClient
import com.example.recetify.data.remote.RetrofitClient.api
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
    fun onConfirmPassChange(p: String)  = _state.update { it.copy(confirmPassword = p) }

    fun onNewPassChange(p: String) {
        _state.update {
            it.copy(
                newPassword = p,
                isLengthValid = p.length >= 8,
                hasUppercase = p.any { it.isUpperCase() },
                hasNumber = p.any { it.isDigit() },
                hasSpecialChar = p.any { !it.isLetterOrDigit() },
                error = null
            )
        }
    }

    fun requestReset(onSuccess: ()->Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Llamar a requestReset que ahora devuelve PasswordResetResponse
                val response = RetrofitClient.api.requestReset(EmailDTO(state.value.email))
                _state.update { it.copy(isLoading = false) }

                // Manejar la respuesta según el status
                when (response.status) {
                    com.example.recetify.data.remote.model.PasswordResetResponse.Status.SUCCESS -> {
                        // Éxito: continuar al siguiente paso
                        onSuccess()
                    }
                    com.example.recetify.data.remote.model.PasswordResetResponse.Status.USER_INACTIVE -> {
                        // Usuario inactivo: mostrar dialog
                        _state.update {
                            it.copy(showUserInactiveDialog = true)
                        }
                    }
                    com.example.recetify.data.remote.model.PasswordResetResponse.Status.USER_NOT_FOUND -> {
                        // Usuario no encontrado: mostrar mensaje de error
                        _state.update {
                            it.copy(error = "El mail ingresado está mal escrito o no existe")
                        }
                    }
                }
            } catch (t: Throwable) {
                // Manejar errores de red/conexión
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Error de conexión. Verificá tu internet e intentá nuevamente."
                    )
                }
            }
        }
    }

    fun dismissUserInactiveDialog() {
        _state.update { it.copy(showUserInactiveDialog = false) }
    }

    fun verifyCode(
        code: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                api.verifyResetCode(CodeDTO(state.value.email, code))
                _state.update { it.copy(isLoading = false, code = code) }
                onSuccess()
            } catch (t: Throwable) {
                _state.update { it.copy(isLoading = false, error = t.localizedMessage) }
                onError()
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

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearResetState() {
        _state.update {
            it.copy(
                newPassword = "",
                confirmPassword = "",
                isLengthValid = false,
                hasUppercase = false,
                hasNumber = false,
                hasSpecialChar = false,
                error = null
            )
        }
    }
}
