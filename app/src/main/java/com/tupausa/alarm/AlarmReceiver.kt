package com.tupausa.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tupausa.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // 1. Recuperamos los IDs por separado
        // Este es el ID del ejercicio (ej: 4 - Cuello)
        val exerciseId = intent.getIntExtra("ALARM_ID", -1)

        // Este es el ID de la alarma programada (ej: 102)
        val alarmRecordId = intent.getIntExtra("ALARM_RECORD_ID", 0)

        val alarmName = intent.getStringExtra("ALARM_NOMBRE")
        val alarmTipo = intent.getStringExtra("ALARM_TIPO") ?: "ALEATORIO"
        val alarmDuracion = intent.getIntExtra("ALARM_DURACION", 60)

        // 2. Preparamos el Intent para la Actividad
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION

            // Le pasamos a la Activity el ID DEL EJERCICIO bajo la clave "ALARM_ID"
            // (Porque tu Activity espera recibir el ID del ejercicio en esa clave)
            putExtra("ALARM_ID", exerciseId)

            putExtra("ALARM_NOMBRE", alarmName)
            putExtra("ALARM_TIPO", alarmTipo)
            putExtra("ALARM_DURACION", alarmDuracion)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmRecordId, // Usamos ID de registro para unicidad
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Crear Canal (Igual que antes)
        val channelId = "alarm_channel_tupausa"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarmas TuPausa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de pantalla completa"

                enableVibration(true)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 4. Notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo_white) // Asegúrate que este icono exista
            .setContentTitle("¡Es hora de tu Pausa!")
            .setContentText(alarmName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        // 5. IMPORTANTE: Usamos alarmRecordId para notificar
        // Si usáramos exerciseId, dos alarmas diferentes con el mismo ejercicio se cancelarían mutuamente.
        notificationManager.notify(alarmRecordId, notification)
    }
}