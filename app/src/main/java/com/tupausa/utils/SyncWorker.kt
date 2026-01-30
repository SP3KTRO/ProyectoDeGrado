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

    // Función para sincronizar con la API en segundo plano
    override suspend fun doWork(): Result {
        val context = applicationContext
        val dbHelper = DatabaseHelper(context)
        val prefs = PreferencesManager(context)
        val userId = prefs.getUserId()

        // Si no hay usuario logueado, no hacemos nada
        if (userId == -1) return Result.success()

        // Obtener lista de pendientes desde SQLite
        val pendientes = dbHelper.obtenerHistorialNoSincronizado(userId)

        if (pendientes.isEmpty()) {
            return Result.success()
        }

        var errores = 0

        // Subir uno a uno
        pendientes.forEach { registroLocal ->
            try {
                // Conversor de fechas - Timestamp -> String
                val sdfFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val dateObj = Date(registroLocal.fecha)
                val fechaStr = sdfFecha.format(dateObj)

                val horaInicioStr = sdfHora.format(dateObj)
                val finDateObj = Date(registroLocal.fecha + (registroLocal.duracionSegundos * 1000))
                val horaFinStr = sdfHora.format(finDateObj)

                // Preparar datos para la API
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

                val response = RetrofitClient.instance.registrarPausa(payload)

                if (response.isSuccessful) {
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
