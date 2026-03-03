package com.tupausa.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tupausa.model.Ejercicio
import com.tupausa.model.RutinaS3
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

    private val _rutinasUsuarioSeleccionado = MutableStateFlow<List<RutinaS3>>(emptyList())
    val rutinasUsuarioSeleccionado: StateFlow<List<RutinaS3>> = _rutinasUsuarioSeleccionado

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun cargarUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _usuarios.value = repository.obtenerUsuariosRemotos()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarEjercicios() {
        viewModelScope.launch {
            try {
                _ejercicios.value = repository.obtenerEjerciciosRemotos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cargarHistorialUsuario(idUsuario: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val historialCrudo = repository.obtenerHistorialRemoto(idUsuario)

                // Agrupar por rutina
                val rutinasAgrupadas = historialCrudo.groupBy { registro ->
                    val fecha = registro.fechaRealizacion ?: "Sin Fecha"
                    val horaBase = registro.horaInicio?.take(4) ?: "Sin Hora"
                    "${fecha}_${horaBase}"
                }.map { (clave, lista) ->
                    RutinaS3(
                        idRutina = clave,
                        fecha = lista.first().fechaRealizacion ?: "Desconocida",
                        horaInicio = lista.minByOrNull { it.horaInicio ?: "23:59" }?.horaInicio ?: "",
                        horaFin = lista.maxByOrNull { it.horaFin ?: "00:00" }?.horaFin ?: "",
                        tipoDeteccion = lista.first().tipoDeteccion ?: "N/A",
                        ejercicios = lista
                    )
                }.sortedByDescending { it.fecha + it.horaInicio }

                _rutinasUsuarioSeleccionado.value = rutinasAgrupadas
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarHistorial() {
        _rutinasUsuarioSeleccionado.value = emptyList()
    }
}

class AdminHistorialViewModelFactory(private val repository: HistorialRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminHistorialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminHistorialViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}