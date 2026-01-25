package com.tupausa.alarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.os.PowerManager
import com.tupausa.R

class AlarmReceiver : BroadcastReceiver() {

    @SuppressLint("SuspiciousIndentation")
    override fun onReceive(context: Context, intent: Intent) {

        // WAKELOCK Despierta el CPU inmediatamente
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TuPausa:AlarmWakeLockTag"
        )
        // Adquirimos el lock por 10 segundos para dar tiempo a la Activity de cargar
        wakeLock.acquire(10 * 1000L)

        // Recuperamos los IDs por separado
        val idsRutina = intent.getIntegerArrayListExtra("ALARM_IDS_RUTINA") ?: arrayListOf()
        val alarmRecordId = intent.getIntExtra("ALARM_RECORD_ID", 0)
        val alarmName = intent.getStringExtra("ALARM_NOMBRE")
        val alarmTipo = intent.getStringExtra("ALARM_TIPO") ?: "ALEATORIO"
        val alarmDuracion = intent.getIntExtra("ALARM_DURACION", 60)
        val alarmToneName = intent.getStringExtra("ALARM_TONO") ?: "Predeterminado"

        // Preparamos el Intent para la Actividad
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION

            // Le pasamos a la Activity el ID DEL EJERCICIO bajo la clave "ALARM_ID"
            putIntegerArrayListExtra("ALARM_IDS_RUTINA", idsRutina)
            putExtra("ALARM_RECORD_ID", alarmRecordId)
            putExtra("ALARM_NOMBRE", alarmName)
            putExtra("ALARM_TIPO", alarmTipo)
            putExtra("ALARM_DURACION", alarmDuracion)
            putExtra("ALARM_TONO", alarmToneName)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmRecordId, // Usamos ID de registro para el PendingIntent
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // Crear Canal de Notificación
        val channelId = "alarm_channel_tupausa"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarmas TuPausa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de pantalla completa para alarmas"

                enableVibration(true)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Notificación
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo_white)
            .setContentTitle("¡Es hora de tu Pausa!")
            .setContentText("¿Aún no has hecho tu pausa activa? Comienza ahora: $alarmName")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent) // Para click manual

            notificationManager.notify(alarmRecordId, builder.build())

            // OPCIONAL Intenta lanzar la actividad directamente si el dispositivo está desbloqueado
            try {
            context.startActivity(fullScreenIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
    }
}