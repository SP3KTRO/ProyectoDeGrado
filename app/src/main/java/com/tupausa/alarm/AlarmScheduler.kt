package com.tupausa.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.tupausa.model.data.Alarma
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun programar(alarma: Alarma) {
        if (!alarma.activa) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            // --- CORRECCIÓN CRÍTICA AQUÍ ---

            // 1. Enviamos el ID de la ALARMA con un nombre específico (para la notificación)
            putExtra("ALARM_RECORD_ID", alarma.id)

            // 2. Enviamos el ID del EJERCICIO bajo el nombre "ALARM_ID"
            // Esto es lo que la AlarmActivity espera para buscar el ejercicio correcto.
            // NOTA: Asegúrate de que tu objeto 'alarma' tenga la propiedad 'idEjercicio'.
            // Si es una alarma aleatoria, puedes enviar -1.
            putExtra("ALARM_ID", alarma.idEjercicio)

            putExtra("ALARM_NOMBRE", alarma.etiqueta)
            putExtra("ALARM_TIPO", alarma.tipoEjercicio)
            putExtra("ALARM_DURACION", alarma.duracionSegundos)
        }

        // El PendingIntent SÍ debe usar el ID de la ALARMA para ser único en el sistema
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarma.id, // ID Único de la alarma
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tiempoDisparo = calcularProximoDisparo(alarma)

        if (tiempoDisparo != null) {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(
                tiempoDisparo,
                pendingIntent
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    android.util.Log.d("TuPausa", "Alarma programada: $tiempoDisparo")
                } else {
                    android.util.Log.e("TuPausa", "Sin permiso alarmas exactas")
                }
            } else {
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        }
    }

    fun cancelar(alarma: Alarma) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarma.id, // Usamos el ID de la alarma para encontrar la correcta a cancelar
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calcularProximoDisparo(alarma: Alarma): Long? {
        val ahora = java.util.Calendar.getInstance()
        val calendarAlarma = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, alarma.hora)
            set(java.util.Calendar.MINUTE, alarma.minuto)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        if (alarma.diasRepeticion.isEmpty()) {
            if (calendarAlarma.before(ahora)) {
                calendarAlarma.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            return calendarAlarma.timeInMillis
        }

        for (i in 0..7) {
            val diaSemanaHoy = calendarAlarma.get(java.util.Calendar.DAY_OF_WEEK)
            if (alarma.diasRepeticion.contains(diaSemanaHoy)) {
                if (i == 0 && calendarAlarma.after(ahora)) {
                    return calendarAlarma.timeInMillis
                } else if (i > 0) {
                    return calendarAlarma.timeInMillis
                }
            }
            calendarAlarma.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return null
    }
}