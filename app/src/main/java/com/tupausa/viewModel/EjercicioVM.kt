package com.tupausa.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tupausa.TuPausaApplication
import com.tupausa.model.Ejercicio
import kotlinx.coroutines.launch

class EjercicioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as TuPausaApplication).ejercicioRepository
    private val _ejercicios = MutableLiveData<List<Ejercicio>>()
    val ejercicios: LiveData<List<Ejercicio>> get() = _ejercicios
    private val _ejercicioSeleccionado = MutableLiveData<Ejercicio?>()
    val ejercicioSeleccionado: LiveData<Ejercicio?> get() = _ejercicioSeleccionado
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error
    private val _mensaje = MutableLiveData<String?>()
    val mensaje: LiveData<String?> get() = _mensaje

    // Cargar ejercicios
    fun loadEjercicios() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val ejerciciosList = repository.getAllEjercicios()
                _ejercicios.value = ejerciciosList
            } catch (e: Exception) {
                _error.value = "Error al cargar ejercicios: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadEjercicioById(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val ejercicio = repository.getEjercicioById(id)
                _ejercicioSeleccionado.value = ejercicio
            } catch (e: Exception) {
                _error.value = "Error al cargar el ejercicio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadEjerciciosByTipo(tipo: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val ejerciciosList = repository.getEjerciciosByTipo(tipo)
                _ejercicios.value = ejerciciosList
            } catch (e: Exception) {
                _error.value = "Error al filtrar ejercicios: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Obtener un ejercicio aleatorio
    fun loadEjercicioAleatorio() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val ejercicio = repository.getEjercicioAleatorio()
                _ejercicioSeleccionado.value = ejercicio
            } catch (e: Exception) {
                _error.value = "Error al obtener ejercicio aleatorio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun agregarEjercicio(ejercicio: Ejercicio) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val id = repository.insertEjercicio(ejercicio)
                if (id > -1) {
                    _mensaje.value = "Ejercicio guardado correctamente"
                    loadEjercicios() // Recargamos la lista para ver el cambio
                } else {
                    _error.value = "No se pudo guardar el ejercicio"
                }
            } catch (e: Exception) {
                _error.value = "Error al guardar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarEjercicio(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val rows = repository.deleteEjercicio(id)
                if (rows > 0) {
                    _mensaje.value = "Ejercicio eliminado"
                    loadEjercicios() // Recargamos la lista
                } else {
                    _error.value = "No se encontró el ejercicio a eliminar"
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Limpieza de estados
    fun clearError() {
        _error.value = null
    }

    fun clearMensaje() {
        _mensaje.value = null
    }
}