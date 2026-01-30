package com.tupausa.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tupausa.TuPausaApplication
import com.tupausa.model.Usuario
import com.tupausa.utils.Constants
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TuPausaApplication).usuarioRepository
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error
    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    // Validar campos
    private fun isValidNombre(nombre: String): Boolean {
        return nombre.matches(Constants.NAME_REGEX.toRegex())
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Constants.EMAIL_REGEX.toRegex())
    }

    private fun isValidContrasena(contrasena: String): Boolean {
        return contrasena.matches(Constants.PASSWORD_REGEX.toRegex())
    }

    // Función para registrar un nuevo usuario
    fun register(nombre: String, correoElectronico: String, contrasena: String, confirmarContrasena: String) {
        _isLoading.value = true

        if (nombre.isEmpty() || correoElectronico.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
            _error.value = "Todos los campos son obligatorios"
            _isLoading.value = false
            return
        }

        if (!isValidNombre(nombre)) {
            _error.value = "El nombre debe tener al menos ${Constants.MIN_NAME_LENGTH} caracteres y solo letras"
            _isLoading.value = false
            return
        }

        if (!isValidEmail(correoElectronico)) {
            _error.value = "El correo electrónico no tiene un formato válido"
            _isLoading.value = false
            return
        }

        if (!isValidContrasena(contrasena)) {
            _error.value = "La contraseña debe tener al menos ${Constants.MIN_PASSWORD_LENGTH} caracteres alfanuméricos"
            _isLoading.value = false
            return
        }

        if (contrasena != confirmarContrasena) {
            _error.value = "Las contraseñas no coinciden"
            _isLoading.value = false
            return
        }

        // Crear el objeto Usuario
        val usuario = Usuario(
            idUsuario = 0,
            nombre = nombre,
            correoElectronico = correoElectronico,
            contrasena = contrasena,
            idTipoUsuario = Constants.USER_TYPE_REGULAR
        )

        // Registrar usuario
        viewModelScope.launch {
            try {
                val result = repository.register(usuario)

                result.onSuccess {
                    _registerSuccess.value = true
                    _isLoading.value = false
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Error al registrar usuario"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error de red: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Limpiar estados
    fun clearError() {
        _error.value = ""
    }
}