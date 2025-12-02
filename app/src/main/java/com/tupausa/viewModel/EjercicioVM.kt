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

    // LiveData para la lista de ejercicios
    private val _ejercicios = MutableLiveData<List<Ejercicio>>()
    val ejercicios: LiveData<List<Ejercicio>> get() = _ejercicios

    // LiveData para un ejercicio específico
    private val _ejercicioSeleccionado = MutableLiveData<Ejercicio?>()
    val ejercicioSeleccionado: LiveData<Ejercicio?> get() = _ejercicioSeleccionado

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData para manejar errores
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // CARGAR TODOS LOS EJERCICIOS
    fun loadEjercicios() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val ejerciciosList = repository.getAllEjercicios()
                _ejercicios.value = ejerciciosList
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Error al cargar ejercicios: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // CARGAR EJERCICIO POR ID
    fun loadEjercicioById(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val ejercicio = repository.getEjercicioById(id)
                _ejercicioSeleccionado.value = ejercicio
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Error al cargar ejercicio: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // FILTRAR POR TIPO
    fun loadEjerciciosByTipo(tipo: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val ejerciciosList = repository.getEjerciciosByTipo(tipo)
                _ejercicios.value = ejerciciosList
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Error al filtrar ejercicios: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // OBTENER EJERCICIO ALEATORIO
    fun loadEjercicioAleatorio() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val ejercicio = repository.getEjercicioAleatorio()
                _ejercicioSeleccionado.value = ejercicio
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Error al obtener ejercicio: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // Método para limpiar el error
    fun clearError() {
        _error.value = ""
    }
}