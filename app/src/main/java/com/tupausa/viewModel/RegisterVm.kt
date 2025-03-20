package com.tupausa.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tupausa.database.RetrofitClient
import com.tupausa.model.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

class RegisterViewModel : ViewModel() {

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData para manejar errores
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // LiveData para indicar si el registro fue exitoso
    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    // Método para validar el nombre
    private fun isValidNombre(nombre: String): Boolean {
        val nombreRegex = "^[a-zA-Z\\s]{5,}\$".toRegex()
        return nombre.matches(nombreRegex)
    }

    // Método para validar el correo electrónico
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        return email.matches(emailRegex.toRegex())
    }

    // Método para validar la contraseña
    private fun isValidContrasena(contrasena: String): Boolean {
        val contrasenaRegex = "^[a-zA-Z0-9]{8,}\$".toRegex()
        return contrasena.matches(contrasenaRegex)
    }

    // Método para registrar un nuevo usuario
    fun register(nombre: String, correoElectronico: String, contrasena: String, confirmarContrasena: String) {
        _isLoading.value = true

        // Validar campos vacíos
        if (nombre.isEmpty() || correoElectronico.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
            _error.value = "Error: Todos los campos son obligatorios."
            _isLoading.value = false
            return
        }

        // Validar nombre
        if (!isValidNombre(nombre)) {
            _error.value = "Error: El nombre debe tener al menos 5 caracteres y no puede contener caracteres especiales."
            _isLoading.value = false
            return
        }

        // Validar correo electrónico
        if (!isValidEmail(correoElectronico)) {
            _error.value = "Error: El correo electrónico no tiene un formato válido."
            _isLoading.value = false
            return
        }

        // Validar contraseña
        if (!isValidContrasena(contrasena)) {
            _error.value = "Error: La contraseña debe tener al menos 8 caracteres y no puede contener caracteres especiales."
            _isLoading.value = false
            return
        }

        // Validar que las contraseñas coincidan
        if (contrasena != confirmarContrasena) {
            _error.value = "Error: Las contraseñas no coinciden."
            _isLoading.value = false
            return
        }

        // Crear el objeto Usuario
        val usuario = Usuario(
            idUsuario = 0, // La base de datos generará el ID
            nombre = nombre,
            correoElectronico = correoElectronico,
            contrasena = contrasena,
            idTipoUsuario = 2 // Siempre será 2 (usuario)
        )

        // Llamar al servicio de la API
        Log.d("RegisterViewModel", "Enviando solicitud de registro...")
        RetrofitClient.instance.createUsuario(usuario).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                _isLoading.value = false
                Log.d("RegisterViewModel", "Respuesta recibida: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("RegisterViewModel", "Registro exitoso")
                    _registerSuccess.value = true
                } else {
                    // Manejar errores del servidor
                    val errorBody = response.errorBody()?.string()
                    Log.e("RegisterViewModel", "Error en la respuesta: ${errorBody ?: "Sin detalles"}")
                    val errorMessage = when (response.code()) {
                        400 -> "Error: Solicitud inválida. ${errorBody ?: "Verifica los datos ingresados."}"
                        409 -> "Error: El correo electrónico ya está registrado. ${errorBody ?: ""}"
                        else -> "Error: ${response.message()} ${errorBody ?: ""}"
                    }
                    _error.value = errorMessage
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                _isLoading.value = false
                Log.e("RegisterViewModel", "Error de red: ${t.message}")
                _error.value = "Error de red: ${t.message}"
            }
        })
    }

    // Método para limpiar el mensaje de error
    fun clearError() {
        _error.value = ""
    }
}