package com.tupausa.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tupausa.TuPausaApplication
import com.tupausa.model.Usuario
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TuPausaApplication).usuarioRepository
    private val preferencesManager = (application as TuPausaApplication).preferencesManager

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData para manejar errores
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // LiveData para indicar si el login fue exitoso
    private val _loginSuccess = MutableLiveData<Usuario?>()
    val loginSuccess: LiveData<Usuario?> get() = _loginSuccess

    // Metodo para iniciar sesión
    fun login(correoElectronico: String, contrasena: String) {
        // Validar campos vacíos
        if (correoElectronico.isEmpty() || contrasena.isEmpty()) {
            _error.value = "Por favor completa todos los campos"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = repository.login(correoElectronico, contrasena)

                result.onSuccess { usuario ->
                    // Guardar sesión
                    preferencesManager.saveUserSession(usuario)

                    _loginSuccess.value = usuario
                    _isLoading.value = false
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Error al iniciar sesión"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error de red: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Verificar si ya hay sesión activa
    fun checkSession(): Usuario? {
        return if (preferencesManager.isLoggedIn()) {
            val userId = preferencesManager.getUserId()
            val userName = preferencesManager.getUserName()
            val userEmail = preferencesManager.getUserEmail()
            val userType = preferencesManager.getUserType()

            Usuario(
                idUsuario = userId,
                nombre = userName,
                correoElectronico = userEmail,
                contrasena = "",
                idTipoUsuario = userType
            )
        } else null
    }

    // ==========================================
    // NUEVO: Metodo para cerrar sesión
    // ==========================================
    fun logout() {
        repository.logout()
        _loginSuccess.value = null
    }

    // Metodo para limpiar el estado de error
    fun clearError() {
        _error.value = ""
    }
}