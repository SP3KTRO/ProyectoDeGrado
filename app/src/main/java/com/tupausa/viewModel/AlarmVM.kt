package com.tupausa.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tupausa.alarm.AlarmScheduler
import com.tupausa.model.data.Alarma
import com.tupausa.model.Ejercicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.tupausa.repository.AlarmaRepository
import com.tupausa.repository.EjercicioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmasViewModel(
    private val repository: AlarmaRepository,
    private val scheduler: AlarmScheduler,
    private val ejercicioRepository: EjercicioRepository
) : ViewModel() {

    // 1. Lista de Alarmas (Room)
    val alarmas: StateFlow<List<Alarma>> = repository.todasLasAlarmas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Lista de Ejercicios Reales (SQLite)
    private val _ejerciciosReales = MutableStateFlow<List<Ejercicio>>(emptyList())
    val ejerciciosReales: StateFlow<List<Ejercicio>> = _ejerciciosReales.asStateFlow()

    init {
        cargarEjercicios()
    }

    private fun cargarEjercicios() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lista = ejercicioRepository.getAllEjercicios()
                _ejerciciosReales.value = lista
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Guardar nueva alarma (o editar existente)
    fun guardarAlarma(alarma: Alarma) {
        viewModelScope.launch {
            // Si es una edición, cancelamos la programación previa para evitar conflictos
            if (alarma.id != 0) {
                scheduler.cancelar(alarma)
            }
            
            val id = repository.insertar(alarma)
            val alarmaGuardada = alarma.copy(id = id.toInt())
            
            if (alarmaGuardada.activa) {
                scheduler.programar(alarmaGuardada)
            }
        }
    }

    fun toggleAlarma(alarma: Alarma) {
        val nuevaAlarma = alarma.copy(activa = !alarma.activa)
        viewModelScope.launch {
            repository.actualizar(nuevaAlarma)
            if (nuevaAlarma.activa) {
                scheduler.programar(nuevaAlarma)
            } else {
                scheduler.cancelar(nuevaAlarma)
            }
        }
    }

    fun eliminarAlarma(alarma: Alarma) {
        viewModelScope.launch {
            scheduler.cancelar(alarma)
            repository.eliminar(alarma)
        }
    }
}

class AlarmasViewModelFactory(
    private val repository: AlarmaRepository,
    private val scheduler: AlarmScheduler,
    private val ejercicioRepository: EjercicioRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmasViewModel(repository, scheduler, ejercicioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
