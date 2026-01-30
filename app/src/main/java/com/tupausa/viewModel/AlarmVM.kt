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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmasViewModel(
    private val repository: AlarmaRepository,
    private val scheduler: AlarmScheduler,
    private val ejercicioRepository: EjercicioRepository
) : ViewModel() {

    // Lista de Alarmas - Room
    val alarmas: StateFlow<List<Alarma>> = repository.todasLasAlarmas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _ejercicios = MutableStateFlow<List<Ejercicio>>(emptyList())
    val ejercicios: StateFlow<List<Ejercicio>> = _ejercicios.asStateFlow()

    private val _ejerciciosSeleccionadosIds = MutableStateFlow<List<Int>>(emptyList())
    val ejerciciosSeleccionadosIds = _ejerciciosSeleccionadosIds.asStateFlow()

    // Valida si la rutina cumple con el mínimo de 4 ejercicios
    val puedeGuardarRutina: StateFlow<Boolean> = _ejerciciosSeleccionadosIds
        .combine(_ejercicios) { seleccionados, _ ->
            seleccionados.size >= 4
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        cargarEjercicios()
    }

    private fun cargarEjercicios() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lista = ejercicioRepository.getAllEjercicios()
                _ejercicios.value = lista
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Gestionar la selección de ejercicios para la rutina
    fun toggleSeleccionEjercicio(id: Int) {
        val listaActual = _ejerciciosSeleccionadosIds.value.toMutableList()
        if (listaActual.contains(id)) {
            listaActual.remove(id)
        } else {
            listaActual.add(id)
        }
        _ejerciciosSeleccionadosIds.value = listaActual
    }

    fun limpiarSeleccion() {
        _ejerciciosSeleccionadosIds.value = emptyList()
    }

    fun prepararEdicion(alarma: Alarma) {
        _ejerciciosSeleccionadosIds.value = alarma.idsEjercicios
    }

    // Guardar nueva alarma o editar existente
    fun guardarAlarma(alarma: Alarma) {
        viewModelScope.launch {
            // Aseguramos que la alarma lleve los ejercicios seleccionados
            if (alarma.id != 0) {
                scheduler.cancelar(alarma)
            }
            val id = repository.insertar(alarma)
            val alarmaGuardada = alarma.copy(id = id.toInt())

            if (alarmaGuardada.activa) {
                scheduler.programar(alarmaGuardada)
            }
            limpiarSeleccion()
        }
    }
    // Actualizar alarma
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
