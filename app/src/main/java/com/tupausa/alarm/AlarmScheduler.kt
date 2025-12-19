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
        if (!alarma.activa) return // Si está apagada, no hacemos nada}

        // 1. Crear el Intent: "Cuando suene, llama a AlarmReceiver"
        // NOTA: Saldrá en ROJO 'AlarmReceiver' hasta que lo creemos en el siguiente paso.
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarma.id)
            putExtra("ALARM_NOMBRE", alarma.etiqueta)
            // --- NUEVOS DATOS ---
            putExtra("ALARM_TIPO", alarma.tipoEjercicio)
            putExtra("ALARM_DURACION", alarma.duracionSegundos)
        }
        // 2. Crear el PendingIntent (Es como un ticket que le damos a Android)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarma.id, // ID Único para que no se sobrescriban entre ellas
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Calcular cuándo debe sonar (La parte matemática)
        val tiempoDisparo = calcularProximoDisparo(alarma)

        // 4. Agendar la alarma
        // Usamos setAlarmClock para que sea precisa y salga el iconito en la barra de estado
        if (tiempoDisparo != null) {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(
                tiempoDisparo,
                pendingIntent // Esto lanza la app si tocan el icono de alarma (opcional)
            )

            // Verificamos permisos en Android 12+ (API 31)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                    Log.d("TuPausa", "Alarma programada para: $tiempoDisparo (ID: ${alarma.id})")
                } else {
                    Log.e("TuPausa", "No tienes permiso para alarmas exactas")
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
            alarma.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("TuPausa", "Alarma cancelada: ${alarma.id}")
    }

    // --- LÓGICA DE CALENDARIO ---
    // Esta función busca cuándo es la próxima vez que debe sonar
    private fun calcularProximoDisparo(alarma: Alarma): Long? {
        val ahora = Calendar.getInstance()
        val calendarAlarma = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarma.hora)
            set(Calendar.MINUTE, alarma.minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si la alarma es para hoy pero ya pasó la hora, o si hoy no es un día activo:
        // Buscamos el siguiente día válido.

        // Si no tiene días repetidos, es una alarma única
        if (alarma.diasRepeticion.isEmpty()) {
            if (calendarAlarma.before(ahora)) {
                calendarAlarma.add(Calendar.DAY_OF_YEAR, 1) // Mañana a esta hora
            }
            return calendarAlarma.timeInMillis
        }

        // Si TIENE días repetidos (ej: Lunes, Miércoles)
        // Buscamos el próximo día válido en los próximos 7 días
        for (i in 0..7) {
            val diaSemanaHoy = calendarAlarma.get(Calendar.DAY_OF_WEEK) // 1=Domingo, 2=Lunes...

            // Si el día actual del calendario está en la lista Y (es futuro O es hoy pero más tarde)
            if (alarma.diasRepeticion.contains(diaSemanaHoy)) {
                if (i == 0 && calendarAlarma.after(ahora)) {
                    return calendarAlarma.timeInMillis // Es hoy más tarde
                } else if (i > 0) {
                    return calendarAlarma.timeInMillis // Es otro día
                }
            }
            // Si no coincide, sumamos 1 día y probamos de nuevo
            calendarAlarma.add(Calendar.DAY_OF_YEAR, 1)
        }
        return null
    }
}