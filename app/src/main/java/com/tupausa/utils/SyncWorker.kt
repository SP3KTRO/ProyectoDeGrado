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
            return Result.success() // Todo al día
        }

        var errores = 0
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // 2. Iterar y subir uno por uno
        pendientes.forEach { registroLocal ->
            try {
                val horaStr = sdf.format(Date(registroLocal.fecha))
                
                // 3. Mapear datos LOCALES -> Modelo API (HistorialEjecucion)
                val fechaEnSegundos = (registroLocal.fecha / 1000).toInt()

                val payload = HistorialEjecucion(
                    idRegistro = 0,
                    idUsuario = userId,
                    idEjercicio = registroLocal.idEjercicio,
                    fechaRealizacion = fechaEnSegundos,
                    duracionRaalSeg = registroLocal.duracionSegundos,
                    horaInicio = horaStr,
                    horaFin = horaStr,
                    seDetectoMovimiento = if (registroLocal.tipoDeteccion == "SENSOR") 1 else 0,
                    tipoDeteccionUsado = registroLocal.tipoDeteccion,
                    sincronizado = 1,
                    ejercicio = null,
                    usuario = null
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
