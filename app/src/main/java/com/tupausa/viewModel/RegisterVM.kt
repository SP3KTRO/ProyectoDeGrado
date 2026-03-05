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

    // Lista de dominios de confianza
    private val dominiosPermitidos = setOf(
        "gmail.com",
        "hotmail.com",
        "outlook.com",
        "yahoo.com",
        "yahoo.es",
        "live.com",
        "icloud.com",
        "msn.com",
        "udistrital.edu.co"
    )

    // Validar campos
    private fun isValidNombre(nombre: String): Boolean {
        return nombre.matches(Constants.NAME_REGEX.toRegex())
    }

    private fun isValidEmail(email: String): Boolean {
        // Verificamos que tenga el formato básico
        if (!email.matches(Constants.EMAIL_REGEX.toRegex())) {
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        // Extraemos el dominio
        val partes = email.split("@")
        if (partes.size != 2) return false

        val dominio = partes[1].lowercase()

        val dominiosMalEscritos = setOf(
            "gamil.com", "gmail.con", "gmai.com", "gmal.com", "gmail.coom",
            "hotmial.com", "hotmai.com", "homail.com", "hotmail.con",
            "outlok.com", "outlook.con", "outloo.com",
            "yaho.com", "yahoo.con", "yahoo.com.coo"
        )
        if (dominiosMalEscritos.contains(dominio)) return false

        val correosBasura = setOf(
            "yopmail.com", "10minutemail.com", "mailinator.com",
            "guerrillamail.com", "tempmail.com", "temp-mail.org", "throwawaymail.com"
        )
        if (correosBasura.contains(dominio)) return false

        val esDominioInstitucional = dominio.endsWith(".edu.co") ||
                dominio.endsWith(".edu") ||
                dominio.endsWith(".gov.co") ||
                dominio.endsWith(".org.co") ||
                dominio.endsWith(".mil.co")

        // Verificamos si está en nuestra lista blanca o es institucional
        return dominiosPermitidos.contains(dominio) || esDominioInstitucional
    }

    private fun isValidContrasena(contrasena: String): Boolean {
        return contrasena.matches(Constants.PASSWORD_REGEX.toRegex())
    }

    // Función para registrar un nuevo usuario
    fun register(nombre: String, correoElectronico: String, contrasena: String, confirmarContrasena: String) {
        if (nombre.isEmpty() || correoElectronico.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
            _error.value = "Todos los campos son obligatorios"
            return
        }

        if (!isValidNombre(nombre)) {
            _error.value = "El nombre debe tener al menos ${Constants.MIN_NAME_LENGTH} caracteres y solo letras"
            return
        }

        if (!isValidEmail(correoElectronico)) {
            _error.value = "Por favor, ingresa un email válido (ej: Gmail, Outlook, etc.)"
            return
        }

        if (!isValidContrasena(contrasena)) {
            _error.value = "La contraseña debe tener al menos ${Constants.MIN_PASSWORD_LENGTH} caracteres alfanuméricos"
            return
        }

        if (contrasena != confirmarContrasena) {
            _error.value = "Las contraseñas no coinciden"
            return
        }

        _isLoading.value = true

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
