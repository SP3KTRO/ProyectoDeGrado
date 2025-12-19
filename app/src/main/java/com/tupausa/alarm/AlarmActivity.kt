package com.tupausa.alarm

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tupausa.R
import kotlinx.coroutines.delay

class AlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Lógica para despertar la pantalla y mostrarse sobre el bloqueo
        encenderPantalla()

        // 2. Obtener datos de la alarma
        val nombreAlarma = intent.getStringExtra("ALARM_NOMBRE") ?: "Pausa Activa"

        // 3. Iniciar Sonido y Vibración
        iniciarAlerta()

        setContent {
            // Usamos un tema oscuro forzado o tu tema normal para que resalte en la noche
            MaterialTheme {
                AlarmScreen(
                    nombreAlarma = nombreAlarma,
                    onDismiss = {
                        detenerAlerta()
                        finish() // Cierra la actividad
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerAlerta()
    }

    private fun encenderPantalla() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun iniciarAlerta() {
        try {
            // Sonido de Alarma
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            ringtone = RingtoneManager.getRingtone(applicationContext, uri).apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                play()
            }

            // Vibración
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            // Patrón de vibración: 0ms espera, 1000ms vibra, 1000ms para...
            val pattern = longArrayOf(0, 1000, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 = repetir indefinidamente
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun detenerAlerta() {
        ringtone?.stop()
        vibrator?.cancel()
    }
}

@Composable
fun AlarmScreen(
    nombreAlarma: String,
    onDismiss: () -> Unit
) {
    // Un temporizador simple para mostrar (ej: 60 segundos)
    var timeLeft by remember { mutableIntStateOf(60) }

    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    // Fondo: Usamos una imagen de fondo o un color sólido
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Fondo oscuro para contraste, o usa tu imagen de arena
    ) {
        // Imagen de Fondo (Opcional, si tienes la de welcome)
        Image(
            painter = painterResource(id = R.drawable.welcome), // O usa tu fondo de arena
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.5f), // Un poco oscura para leer texto
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "¡Es hora de tu Pausa!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre del Ejercicio / Alarma
            Text(
                text = nombreAlarma,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary, // Color Bronce/Dorado
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Círculo con Temporizador (Simulando el ejercicio)
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = timeLeft / 60f, // Progreso basado en 60s (valor Float)
                    modifier = Modifier.size(200.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
                Text(
                    text = "$timeLeft",
                    fontSize = 64.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Botón para detener
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.8f) // Rojo para llamar la atención de "Parar"
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("TERMINAR PAUSA", fontSize = 18.sp)
            }
        }
    }
}
