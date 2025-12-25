package com.tupausa.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tupausa.TuPausaApplication
import com.tupausa.model.HistorialRegistro
import com.tupausa.model.ResumenEstadistico
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TuPausaApplication
    private val repository = app.historialRepository
    private val prefs = app.preferencesManager

    private val _historialList = MutableStateFlow<List<HistorialRegistro>>(emptyList())
    val historialList: StateFlow<List<HistorialRegistro>> = _historialList

    private val _resumen = MutableStateFlow(ResumenEstadistico(0, 0, 0))
    val resumen: StateFlow<ResumenEstadistico> = _resumen

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun cargarDatos() {
        val userId = prefs.getUserId()
        if (userId == -1) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Cargar Lista
                val lista = repository.obtenerHistorial(userId)
                _historialList.value = lista

                // 2. Cargar Estadísticas
                val total = repository.obtenerTotalPausas(userId)
                val tiempo = repository.obtenerTiempoTotalMinutos(userId)

                _resumen.value = ResumenEstadistico(
                    totalPausas = total,
                    tiempoTotalMinutos = tiempo,
                    rachaDias = calcularRacha(lista)
                )
            } catch (e: Exception) {
                // Manejar error si es necesario
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calcularRacha(historial: List<HistorialRegistro>): Int {
        // Implementación básica de racha
        if (historial.isEmpty()) return 0
        // Por ahora retornamos 0 o una lógica simple, se puede expandir comparando fechas
        return 0 
    }
}
