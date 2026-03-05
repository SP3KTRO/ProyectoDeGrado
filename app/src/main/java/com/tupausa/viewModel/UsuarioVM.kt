package com.tupausa.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tupausa.model.Usuario
import com.tupausa.repository.UsuarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsuarioViewModel(private val repository: UsuarioRepository) : ViewModel() {
    private val _usuarios = MutableLiveData<List<Usuario>>()
    val usuarios: LiveData<List<Usuario>> get() = _usuarios

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error
    private val _operationSuccess = MutableLiveData<String>()
    val operationSuccess: LiveData<String> get() = _operationSuccess

    private val ALERT_DURATION = 4000L

    // Get usuarios desde la Api
    fun fetchUsuariosFromApi() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllUsuariosFromApi()

                result.onSuccess { usuarios ->
                    _usuarios.value = usuarios
                    _isLoading.value = false
                }.onFailure { exception ->
                    handleTemporaryError(exception.message ?: "Error al cargar usuarios")
                }
            } catch (e: Exception) {
                handleTemporaryError("Error de red. Por favor, verifica tu conexión.")
            }
        }
    }

    // Actualizar usuario
    fun updateUsuario(id: Int, usuario: Usuario) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.updateUsuario(id, usuario)

                result.onSuccess {
                    handleTemporarySuccess("Usuario actualizado correctamente")
                    // Recargar lista de usuarios
                    fetchUsuariosFromApi()
                }.onFailure { exception ->
                    handleTemporaryError(exception.message ?: "Error al actualizar usuario")
                }
            } catch (e: Exception) {
                handleTemporaryError("Error al conectar con el servidor")
            }
        }
    }

    // Eliminar usuario
    fun deleteUsuario(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.deleteUsuario(id)

                result.onSuccess {
                    handleTemporarySuccess("Usuario eliminado correctamente")
                    fetchUsuariosFromApi()
                }.onFailure { exception ->
                    handleTemporaryError(exception.message ?: "Error al eliminar usuario")
                }
            } catch (e: Exception) {
                handleTemporaryError("Error de red inesperado")
            }
        }
    }

    // Guardar preferencias de limitaciones físicas en local
    fun guardarPreferenciasOnboarding(idUsuario: Int, limitaciones: String, onComplete: () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.guardarPreferenciasLocales(idUsuario, limitaciones)

                // Volvemos al hilo principal para actualizar la UI y navegar
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onComplete()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleTemporaryError("Error al guardar las preferencias. Intenta de nuevo.")
                }
            }
        }
    }

    // Obtener días restantes para actualizar preferencias
    fun getDiasRestantesPreferencias(): Int {
        return repository.getDiasRestantesPreferencias()
    }

    // Limpiar estados
    private fun handleTemporaryError(message: String) {
        _error.value = message
        _isLoading.value = false
        viewModelScope.launch {
            delay(ALERT_DURATION)
            clearError()
        }
    }
    private fun handleTemporarySuccess(message: String) {
        _operationSuccess.value = message
        _isLoading.value = false
        viewModelScope.launch {
            delay(ALERT_DURATION)
            clearOperationSuccess()
        }
    }
    fun clearError() {
        _error.value = ""
    }
    fun clearOperationSuccess() {
        _operationSuccess.value = ""
    }
}

class UsuarioViewModelFactory(private val repository: UsuarioRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsuarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UsuarioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}