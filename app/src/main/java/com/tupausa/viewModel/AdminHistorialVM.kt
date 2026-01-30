package com.tupausa.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tupausa.model.Ejercicio
import com.tupausa.model.HistorialS3
import com.tupausa.model.Usuario
import com.tupausa.repository.HistorialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminHistorialViewModel(private val repository: HistorialRepository) : ViewModel() {
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    private val _ejercicios = MutableStateFlow<List<Ejercicio>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios
    val ejercicios: StateFlow<List<Ejercicio>> = _ejercicios

    private val _historialUsuarioSeleccionado = MutableStateFlow<List<HistorialS3>>(emptyList())
    val historialUsuarioSeleccionado: StateFlow<List<HistorialS3>> = _historialUsuarioSeleccionado

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun cargarUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true
            _usuarios.value = repository.obtenerUsuariosRemotos()
            _isLoading.value = false
        }
    }

    fun cargarEjercicios() {
        viewModelScope.launch {
            _isLoading.value = true
            _ejercicios.value = repository.obtenerEjerciciosRemotos()
            _isLoading.value = false
        }
    }

    fun cargarHistorialUsuario(idUsuario: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _historialUsuarioSeleccionado.value = repository.obtenerHistorialRemoto(idUsuario)
            _isLoading.value = false
        }
    }

    fun limpiarHistorial() {
        _historialUsuarioSeleccionado.value = emptyList()
    }
}

class AdminHistorialViewModelFactory(private val repository: HistorialRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminHistorialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminHistorialViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}