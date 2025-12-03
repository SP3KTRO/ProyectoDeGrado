// Ruta: com/tupausa/viewModel/UsuarioViewModel.kt
package com.tupausa.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tupausa.model.Usuario
import com.tupausa.repository.UsuarioRepository
import kotlinx.coroutines.launch

class UsuarioViewModel(private val repository: UsuarioRepository) : ViewModel() {

    // LiveData para la lista de usuarios
    private val _usuarios = MutableLiveData<List<Usuario>>()
    val usuarios: LiveData<List<Usuario>> get() = _usuarios

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData para manejar errores
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // LiveData para operaciones exitosas
    private val _operationSuccess = MutableLiveData<String>()
    val operationSuccess: LiveData<String> get() = _operationSuccess

    // ==========================================
    // OBTENER USUARIOS DESDE LA API
    // ==========================================

    fun fetchUsuariosFromApi() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllUsuariosFromApi()

                result.onSuccess { usuarios ->
                    _usuarios.value = usuarios
                    _isLoading.value = false
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Error al cargar usuarios"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error de red: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // ==========================================
    // ACTUALIZAR USUARIO
    // ==========================================

    fun updateUsuario(id: Int, usuario: Usuario) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.updateUsuario(id, usuario)

                result.onSuccess {
                    _operationSuccess.value = "Usuario actualizado correctamente"
                    _isLoading.value = false
                    // Recargar lista de usuarios
                    fetchUsuariosFromApi()
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Error al actualizar usuario"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error de red: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // ==========================================
    // ELIMINAR USUARIO
    // ==========================================

    fun deleteUsuario(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.deleteUsuario(id)

                result.onSuccess {
                    _operationSuccess.value = "Usuario eliminado correctamente"
                    _isLoading.value = false
                    // Recargar lista de usuarios
                    fetchUsuariosFromApi()
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Error al eliminar usuario"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error de red: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // ==========================================
    // LIMPIAR ESTADOS
    // ==========================================

    fun clearError() {
        _error.value = ""
    }

    fun clearOperationSuccess() {
        _operationSuccess.value = ""
    }
}