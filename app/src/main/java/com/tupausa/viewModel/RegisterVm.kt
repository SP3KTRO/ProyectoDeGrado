package com.tupausa.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tupausa.database.RetrofitClient
import com.tupausa.model.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    // Método para registrar un nuevo usuario
    fun register(nombre: String, correoElectronico: String, contrasena: String, idTipoUsuario: Int) {
        _isLoading.value = true
        val usuario = Usuario(
            idUsuario = 0, // El ID se genera automáticamente en la base de datos
            nombre = nombre,
            correoElectronico = correoElectronico,
            contrasena = contrasena,
            idTipoUsuario = idTipoUsuario
        )
        RetrofitClient.instance.createUsuario(usuario).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _registerSuccess.value = true
                } else {
                    _error.value = "Error: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error de red: ${t.message}"
            }
        })
    }

    fun clearError() {
        _error.value = ""
    }
}