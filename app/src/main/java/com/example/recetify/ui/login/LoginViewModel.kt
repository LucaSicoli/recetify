
package com.example.recetify.ui.login
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recetify.data.remote.model.JwtResponse
import com.example.recetify.data.remote.model.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChanged(new: String) {
        _state.update { it.copy(email = new) }
    }

    fun onAliasChanged(new: String) {
        _state.update { it.copy(alias = new) }
    }

    fun onPasswordChanged(new: String) {
        _state.update { it.copy(password = new) }
    }

    fun togglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClicked() {

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response: JwtResponse = RetrofitClient.api.login(
                    LoginRequest(
                        email    = _state.value.email,
                        alias    = _state.value.alias,
                        password = _state.value.password
                    )
                )
                _state.update { it.copy(token = response.token, isLoading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = t.localizedMessage ?: "Error desconocido", isLoading = false) }
            }
        }
    }
}
