package com.tupausa.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tupausa.database.RetrofitClient
import com.tupausa.model.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData para manejar errores
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // LiveData para indicar si el login fue exitoso
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    // Método para actualizar el mensaje de error
    fun setError(message: String) {
        _error.value = message
    }

    // Método para iniciar sesión
    fun login(correoElectronico: String, contrasena: String) {
        _isLoading.value = true
        RetrofitClient.instance.getUsuarios().enqueue(object : Callback<List<List<Any>>> {
            override fun onResponse(call: Call<List<List<Any>>>, response: Response<List<List<Any>>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val usuarios = response.body()?.mapNotNull { subarray ->
                        try {
                            Usuario(
                                idUsuario = (subarray[0] as Double).toInt(),
                                nombre = subarray[1] as String,
                                correoElectronico = subarray[2] as String,
                                contrasena = subarray[3] as String,
                                idTipoUsuario = (subarray[4] as Double).toInt()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    val usuario = usuarios.find { it.correoElectronico == correoElectronico && it.contrasena == contrasena }
                    if (usuario != null) {
                        _loginSuccess.value = true
                    } else {
                        setError("Credenciales incorrectas")
                    }
                } else {
                    setError("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<List<Any>>>, t: Throwable) {
                _isLoading.value = false
                setError("Error de red: ${t.message}")
            }
        })
    }

    // Método para limpiar el estado de error
    fun clearError() {
        _error.value = ""
    }
}