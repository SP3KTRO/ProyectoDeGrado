package com.tupausa.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tupausa.TuPausaApplication
import com.tupausa.model.DiaStat
import com.tupausa.model.HistorialRegistro
import com.tupausa.model.ResumenEstadistico
import com.tupausa.model.RutinaHistorial
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistorialViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as TuPausaApplication
    private val repository = app.historialRepository
    private val prefs = app.preferencesManager

    // Estados
    private val _rutinasList = MutableStateFlow<List<RutinaHistorial>>(emptyList())
    val rutinasList: StateFlow<List<RutinaHistorial>> = _rutinasList

    private val _resumen = MutableStateFlow(ResumenEstadistico(0, 0))
    val resumen: StateFlow<ResumenEstadistico> = _resumen

    private val _statsSemana = MutableStateFlow<List<DiaStat>>(emptyList())
    val statsSemana: StateFlow<List<DiaStat>> = _statsSemana

    private val _mensajeMotivacional = MutableStateFlow("")
    val mensajeMotivacional: StateFlow<String> = _mensajeMotivacional

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun cargarDatos() {
        val userId = prefs.getUserId()
        if (userId == -1) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Obtener datos crudos
                val listaCruda = repository.obtenerHistorial(userId)

                // Agrupar por rutina
                val formatoAgrupacion = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
                val formatoAmigable = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

                val rutinasAgrupadas = listaCruda.groupBy {
                    formatoAgrupacion.format(Date(it.fecha))
                }.map { (clave, ejercicios) ->
                    RutinaHistorial(
                        idRutina = clave,
                        timestamp = ejercicios.maxOf { it.fecha },
                        fechaFormateada = formatoAmigable.format(Date(ejercicios.maxOf { it.fecha })),
                        duracionTotalSegundos = ejercicios.sumOf { it.duracionSegundos },
                        tipoDeteccion = ejercicios.first().tipoDeteccion,
                        ejercicios = ejercicios
                    )
                }.sortedByDescending { it.timestamp } // Ordenamos de más reciente a más antiguo

                _rutinasList.value = rutinasAgrupadas

                // Resumen Histórico Global
                val total = repository.obtenerTotalPausas(userId)
                val tiempo = repository.obtenerTiempoTotalMinutos(userId)
                _resumen.value = ResumenEstadistico(total, tiempo)

                // Calcular Estadísticas de los Últimos 7 Días
                calcularEstadisticasSemanales(rutinasAgrupadas)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calcularEstadisticasSemanales(rutinas: List<RutinaHistorial>) {
        val calendar = Calendar.getInstance()
        val stats = mutableListOf<DiaStat>()
        val formatoDia = SimpleDateFormat("EEE", Locale.getDefault())
        var diasActivos = 0

        // Retrocedemos 6 días para tener los últimos 7 días terminando hoy
        for (i in 6 downTo 0) {
            val calDia = Calendar.getInstance()
            calDia.add(Calendar.DAY_OF_YEAR, -i)

            // Filtramos las rutinas
            val rutinasDelDia = rutinas.filter {
                val calRutina = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                calRutina.get(Calendar.YEAR) == calDia.get(Calendar.YEAR) &&
                        calRutina.get(Calendar.DAY_OF_YEAR) == calDia.get(Calendar.DAY_OF_YEAR)
            }

            val minutosDia = rutinasDelDia.sumOf { it.duracionTotalSegundos } / 60
            if (minutosDia > 0) diasActivos++

            stats.add(DiaStat(
                nombreDia = formatoDia.format(calDia.time).take(3).capitalize(Locale.getDefault()),
                minutosTotales = minutosDia
            ))
        }

        _statsSemana.value = stats

        // Generar Mensaje Motivacional
        _mensajeMotivacional.value = when (diasActivos) {
            0 -> "Esta semana no has registrado pausas. ¡Hoy es un excelente día para empezar!"
            in 1..2 -> "Un comienzo suave. ¡Intenta sumar un par de rutinas más esta semana!"
            in 3..5 -> "¡Gran constancia! Estás cuidando tu cuerpo de maravilla."
            else -> "¡Racha impecable! Eres un maestro de las pausas activas. Sigue así."
        }
    }

    fun borrarHistorial() {
        val userId = prefs.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            repository.borrarHistorial(userId)
            _rutinasList.value = emptyList()
            _resumen.value = ResumenEstadistico(0, 0)
            _statsSemana.value = emptyList()
            _mensajeMotivacional.value = "Historial limpio. ¡Es momento de empezar unas buenas pausas activas!"
        }
    }
}