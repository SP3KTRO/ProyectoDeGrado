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
        // 1. Recuperar datos que venían desde el Scheduler
        val alarmId = intent.getIntExtra("ALARM_ID", 0)
        val alarmName = intent.getStringExtra("ALARM_NOMBRE")
        // Recibir
        val alarmTipo = intent.getStringExtra("ALARM_TIPO") ?: "ALEATORIO"
        val alarmDuracion = intent.getIntExtra("ALARM_DURACION", 60)

        // 2. Preparar la Pantalla que va a saltar (AlarmActivity)
        // NOTA: Saldrá en ROJO hasta el próximo paso, ignóralo por ahora.
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_NOMBRE", alarmName)
            // Reenviar a la Activity
            putExtra("ALARM_TIPO", alarmTipo)
            putExtra("ALARM_DURACION", alarmDuracion)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Crear el Canal de Notificaciones (Obligatorio desde Android 8)
        val channelId = "alarm_channel_tupausa"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarmas TuPausa",
                NotificationManager.IMPORTANCE_HIGH // ¡PRIORIDAD MÁXIMA!
            ).apply {
                description = "Notificaciones de pantalla completa para las alarmas"
                enableVibration(true)
                // El sonido lo manejaremos en la Activity para poder apagarlo con un botón
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 4. Construir la Notificación "Disruptiva"
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo_white) // Asegúrate de usar un icono blanco/transparente válido
            .setContentTitle("¡Es hora de tu Pausa!")
            .setContentText(alarmName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // ESTA ES LA MAGIA: .setFullScreenIntent hace que la pantalla se prenda
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        // 5. ¡Fuego!
        notificationManager.notify(alarmId, notification)
    }
}