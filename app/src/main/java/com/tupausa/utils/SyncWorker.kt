package com.tupausa.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tupausa.database.DatabaseHelper
import com.tupausa.database.RetrofitClient
import com.tupausa.model.HistorialEjecucion
import java.text.SimpleDateFormat
import java.util.*

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val dbHelper = DatabaseHelper(context)
        val prefs = PreferencesManager(context)
        val userId = prefs.getUserId()

        // Si no hay usuario logueado, no hacemos nada
        if (userId == -1) return Result.success()

        // 1. Obtener lista de pendientes desde SQLite
        val pendientes = dbHelper.obtenerHistorialNoSincronizado(userId)

        if (pendientes.isEmpty()) {
            return Result.success()
        }

        var errores = 0

        // 2. Iterar y subir uno por uno
        pendientes.forEach { registroLocal ->
            try {
                // 1. CONVERSIÓN DE FECHAS (Timestamp -> String)
                val sdfFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                val dateObj = Date(registroLocal.fecha) // Tu fecha local es Long (milisegundos)

                val fechaStr = sdfFecha.format(dateObj)      // Ej: "2025-12-25"
                val horaInicioStr = sdfHora.format(dateObj)  // Ej: "18:30:00"

                // Calculamos hora fin sumando la duración
                val finDateObj = Date(registroLocal.fecha + (registroLocal.duracionSegundos * 1000))
                val horaFinStr = sdfHora.format(finDateObj)

                // 2. CREAR EL PAYLOAD CORREGIDO
                val payload = HistorialEjecucion(
                    idRegistro = 0,
                    idUsuario = userId,
                    idEjercicio = registroLocal.idEjercicio,
                    fecha = fechaStr,
                    horaInicio = horaInicioStr,
                    horaFin = horaFinStr,
                    detectado = if (registroLocal.tipoDeteccion == "SENSOR") 1 else 0,
                    metodoDeteccion = registroLocal.tipoDeteccion,
                    duracion = registroLocal.duracionSegundos
                )

                // 4. Llamada a Retrofit
                val response = RetrofitClient.instance.registrarPausa(payload)

                if (response.isSuccessful) {
                    // 5. Éxito: Marcamos en SQLite local como sincronizado
                    dbHelper.marcarComoSincronizado(registroLocal.id)
                    Log.d("SyncWorker", "Registro ${registroLocal.id} sincronizado.")
                } else {
                    errores++
                    Log.e("SyncWorker", "Error API ${response.code()}: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                Log.e("SyncWorker", "Error en sincronización", e)
                errores++
            }
        }

        return if (errores == 0) Result.success() else Result.retry()
    }
}
