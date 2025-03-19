package com.tupausa.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tupausa.database.RetrofitClient
import com.tupausa.model.Usuario
import com.tupausa.repository.TuPausaRepository
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TuPausaViewModel(private val repository: TuPausaRepository) : ViewModel() {

    // LiveData para la lista de usuarios
    private val _usuarios = MutableLiveData<List<Usuario>>()
    val usuarios: LiveData<List<Usuario>> get() = _usuarios

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData para manejar errores
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Método para obtener usuarios desde la API
    fun fetchUsuariosFromApi() {
        _isLoading.value = true
        RetrofitClient.instance.getUsuarios().enqueue(object : Callback<List<List<Any>>> {
            override fun onResponse(call: Call<List<List<Any>>>, response: Response<List<List<Any>>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    // Convierte la lista de listas en una lista de usuarios
                    val usuarios = response.body()?.mapNotNull { subarray ->
                        try {
                            Usuario(
                                idUsuario = (subarray[0] as Double).toInt(), // Convierte Double a Int
                                nombre = subarray[1] as String,
                                correoElectronico = subarray[2] as String,
                                contrasena = subarray[3] as String,
                                idTipoUsuario = (subarray[4] as Double).toInt() // Convierte Double a Int
                            )
                        } catch (e: Exception) {
                            null // Si hay un error en el parsing, ignora este usuario
                        }
                    } ?: emptyList()

                    _usuarios.value = usuarios
                } else {
                    // Maneja el error de la API
                    val errorBody = response.errorBody()?.string()
                    _error.value = "Error: ${response.message()}. Detalles: $errorBody"
                }
            }

            override fun onFailure(call: Call<List<List<Any>>>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error de red: ${t.message}"
            }
        })
    }

    // Método para insertar un usuario en la base de datos local
    fun insertUsuario(usuario: Usuario) {
        viewModelScope.launch {
            repository.insertUsuarioLocal(usuario)
        }
    }

    // Método para cargar usuarios desde la base de datos local
    fun loadUsuarios() {
        viewModelScope.launch {
            val usuarios = repository.getAllUsuariosLocal()
            _usuarios.value = usuarios
        }
    }

    // Método para limpiar el error
    fun clearError() {
        _error.value = ""
    }
}