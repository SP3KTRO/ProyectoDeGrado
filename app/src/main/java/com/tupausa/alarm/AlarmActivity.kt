package com.tupausa.alarm

import android.app.KeyguardManager
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.tupausa.TuPausaApplication
import com.tupausa.R
import com.tupausa.model.Ejercicio
import com.tupausa.ui.theme.ArenaOnPrimaryContainer
import com.tupausa.ui.theme.ArenaPrimary
import com.tupausa.utils.rememberDrawableId
import com.tupausa.view.user.InstruccionItem
import com.tupausa.view.user.SectionTitle
import com.tupausa.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.content.ContextCompat
import com.tupausa.ui.theme.ArenaOnSurface
import com.tupausa.ui.theme.ArenaOnSurfaceVariant
import com.tupausa.ui.theme.ArenaPrimaryContainer
import com.tupausa.utils.CameraHelper
import com.tupausa.utils.FlipDetector
import com.tupausa.utils.ShakeDetector
import com.tupausa.utils.ProximityDetector
import com.tupausa.utils.CircleDetector
import kotlin.random.Random

// Enumeración de los retos disponibles
enum class TipoReto {SHAKE, TAP, FLIP, LONG_PRESS, DOUBLE_TAP, PROXIMITY, DRAW_CIRCLE, MULTI_TAP}

class AlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null

    //VARIABLES PARA SENSORES Y CAMARA
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var flipDetector: FlipDetector
    private lateinit var proximityDetector: ProximityDetector

    private var circleDetector: CircleDetector? = null
    private lateinit var cameraHelper: CameraHelper
    private var rutaFotoEvidencia: String = ""

    // Estado del reto actual
    private var retoActual by mutableStateOf(TipoReto.SHAKE)
    private var metaRepeticiones by mutableIntStateOf(5)
    private var repeticionesActuales by mutableIntStateOf(0)
    private var ejercicioRealCargado by mutableStateOf<Ejercicio?>(null)

    // Launcher de permisos para la camara
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            iniciarCamaraYTomarFoto()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. BLINDAJE DE VENTANA (Método moderno para evitar cierre al desbloquear)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // 2. BLOQUEO DE BOTÓN ATRÁS
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(this@AlarmActivity, "Debes completar el reto para salir", Toast.LENGTH_SHORT).show()
            }
        })

        // 3. RECUPERACIÓN DE DATOS E INICIALIZACIÓN
        val isManual = intent.getBooleanExtra("IS_MANUAL", false)
        val targetId = intent.getIntExtra("ALARM_ID", -1)
        val tipoObjetivo = intent.getStringExtra("ALARM_TIPO") ?: "ALEATORIO"

        // Inicializar sensores

        if (!isManual) {
            encenderPantalla()
            elegirRetoAleatorio()
            inicializarSensores()
            inicializarCamara()
            iniciarSonido()
        } else {
            retoActual = TipoReto.TAP
            metaRepeticiones = 1
        }

        setContent {
            // Variable de estado para redesplegar la UI cuando el ejercicio cargue
            var ejercicioState by remember { mutableStateOf<Ejercicio?>(null) }
            // Cargamos el ejercicio de forma segura dentro del ciclo de vida de Compose
            LaunchedEffect(Unit) {
                val repository = (application as TuPausaApplication).ejercicioRepository
                val todos = repository.getAllEjercicios()
                val encontrado = if (targetId != -1) {
                    todos.find { it.idEjercicio == targetId }
                } else if (tipoObjetivo != "ALEATORIO") {
                    todos.filter { it.tipoEjercicio == tipoObjetivo }.randomOrNull()
                } else {
                    todos.randomOrNull()
                }
                ejercicioState = encontrado ?: todos.randomOrNull()
                ejercicioRealCargado = ejercicioState
            }

            BackHandler(enabled = true) {
                // Lo dejamos vacío para que NO haga nada, o mostramos un mensaje.
                Toast.makeText(this, "Debes completar el reto para salir", Toast.LENGTH_SHORT).show()
            }
            com.tupausa.ui.theme.TuPausaTheme(dynamicColor = false) {
                // SOLO mostramos la UI si el ejercicio ya se cargó
                ejercicioState?.let { ejercicio ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp) // ZONA SEGURA para evitar gestos de sistema
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (retoActual == TipoReto.MULTI_TAP && event.changes.size >= 3) {
                                            if (event.changes.any { it.changedToDown() }) {
                                                verificarProgreso(TipoReto.MULTI_TAP)
                                            }
                                        }
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { verificarProgreso(TipoReto.TAP) },
                                    onDoubleTap = { verificarProgreso(TipoReto.DOUBLE_TAP) },
                                    onLongPress = { verificarProgreso(TipoReto.LONG_PRESS) }
                                )
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { circleDetector?.reset() },
                                    onDragEnd = {
                                        circleDetector?.let { detector ->
                                            if (retoActual == TipoReto.DRAW_CIRCLE && detector.isCircleDetected()) {
                                                verificarProgreso(TipoReto.DRAW_CIRCLE)
                                            }
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        // 1. Protección para DRAW_CIRCLE usando verificación de nulidad
                                        if (retoActual == TipoReto.DRAW_CIRCLE) {
                                            // El operador ?. asegura que addPoint solo se llame si circleDetector NO es null
                                            circleDetector?.addPoint(change.position)
                                        }
                                    }
                                )
                            }
                    ) {
                        AlarmScreen(
                            ejercicio = ejercicio, // Usamos la variable segura del let
                            infoReto = if (!isManual) "Reto: ${obtenerTextoReto(retoActual)} ($repeticionesActuales/$metaRepeticiones)" else "",
                            isManual = isManual,
                            onDismiss = { terminarEjercicio(isManualMode = true) },
                            onPosponer = { posponerAlarma() }
                        )
                    }
                } ?: run {
                    // Pantalla de carga opcional mientras el DB responde
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // C. BLOQUEO DEL BOTÓN ATRÁS
    // -----------------------------------------------------------------------
    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // No llamamos a super.onBackPressed(), así el botón no hace nada.
        Toast.makeText(this, "Debes completar el reto para salir", Toast.LENGTH_SHORT).show()
    }

    // --- LÓGICA DE RETOS ---
// Esta función maneja tanto el fin por sensores como el fin manual
    private fun terminarEjercicio(isManualMode: Boolean = false) {
        detenerSonido()

        // Mensaje diferente según modo
        if (!isManualMode) {
            Toast.makeText(this, "¡Reto Completado!", Toast.LENGTH_SHORT).show()
        }

        val prefs = PreferencesManager(this)
        val userId = prefs.getUserId()
        val app = application as TuPausaApplication

        // ARREGLO #1 (Continuación): Usamos los datos del 'ejercicioRealCargado'
        val ejercicioFinal = ejercicioRealCargado

        if (userId != -1 && ejercicioFinal != null) {
            CoroutineScope(Dispatchers.IO).launch {
                app.historialRepository.insertarHistorial(
                    idUsuario = userId,
                    idEjercicio = ejercicioFinal.idEjercicio, // ID Real
                    duracion = ejercicioFinal.duracionSegundos, // Duración Real (20s, 45s, etc)
                    tipo = if (isManualMode) "MANUAL" else "SENSOR",
                    rutaEvidencia = rutaFotoEvidencia
                )
            }
        }

        finish() // Cerramos la actividad
    }

    private fun verificarProgreso(tipoEjecutado: TipoReto) {
        val isManual = intent.getBooleanExtra("IS_MANUAL", false)
        if (isManual) return // En manual no validamos retos

        if (tipoEjecutado == retoActual) {
            repeticionesActuales++
            if (repeticionesActuales >= metaRepeticiones) {
                terminarEjercicio(isManualMode = false)
            }
        }
    }


    private fun elegirRetoAleatorio() {
        val valores = TipoReto.values()
        retoActual = valores[Random.nextInt(valores.size)]

        metaRepeticiones = when(retoActual) {
            TipoReto.SHAKE -> 5  // Sacudir 5 veces
            TipoReto.TAP -> 8    // Tocar 8 veces
            TipoReto.FLIP -> 1   // Voltear 1 vez
            TipoReto.LONG_PRESS -> 4  // Mantener presionado 3 veces
            TipoReto.DOUBLE_TAP -> 5  // Hacer doble click 5 veces
            TipoReto.PROXIMITY -> 4    // Pasar la mano 3 veces
            TipoReto.MULTI_TAP -> 3    // Tocar con varios dedos 3 veces
            TipoReto.DRAW_CIRCLE -> 2  // Dibujar 2 círculos
        }
        repeticionesActuales = 0
    }

    private fun obtenerTextoReto(reto: TipoReto): String {
        return when(reto) {
            TipoReto.SHAKE -> "¡Sacude el celular! 📳"
            TipoReto.TAP -> "¡Toca la pantalla! 👆"
            TipoReto.FLIP -> "¡Voltea el celular! 🔄"
            TipoReto.LONG_PRESS -> "¡Mantén presionado 1 seg! 👆"
            TipoReto.DOUBLE_TAP -> "¡Haz Doble-Toque rápido! 👆"
            TipoReto.PROXIMITY -> "¡Pasa la mano sobre la cámara frontal! 👋"
            TipoReto.MULTI_TAP -> "¡Toca con 3 dedos a la vez! 🖐️"
            TipoReto.DRAW_CIRCLE -> "¡Dibuja un círculo en pantalla! ⭕"
        }
    }

    private fun inicializarSensores() {
        shakeDetector = ShakeDetector(this)
        flipDetector = FlipDetector(this)
        circleDetector = CircleDetector()
        proximityDetector = ProximityDetector(this)

        if (retoActual == TipoReto.SHAKE) {
            shakeDetector.setOnShakeListener(object : ShakeDetector.OnShakeListener {
                override fun onShake(count: Int) {
                    verificarProgreso(TipoReto.SHAKE)
                }
            })
            shakeDetector.start()
        }

        if (retoActual == TipoReto.FLIP) {
            flipDetector.start {
                verificarProgreso(TipoReto.FLIP)
            }
        }
        if (retoActual == TipoReto.PROXIMITY) {
            if (proximityDetector.isAvailable()) {
                proximityDetector.start {
                    verificarProgreso(TipoReto.PROXIMITY)
                }
            } else {
                // Si no hay sensor, fallback a un reto simple
                retoActual = TipoReto.TAP
            }
        }
    }

    private fun inicializarCamara() {
        cameraHelper = CameraHelper(
            context = this,
            owner = this,
            onPhotoTaken = { file ->
                rutaFotoEvidencia = file.absolutePath
                android.util.Log.d("AlarmActivity", "Evidencia guardada: ${file.absolutePath}")
            },
            onError = { e -> e.printStackTrace() }
        )

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarCamaraYTomarFoto()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun iniciarCamaraYTomarFoto() {
        cameraHelper.startCamera()
        // Tomamos la foto a los 2 segundos de abrir la app
        window.decorView.postDelayed({ cameraHelper.takePhoto() }, 2000)
    }

    private fun posponerAlarma() {
        detenerSonido()
        val context = applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager

        // Recuperamos datos para reprogramar
        val alarmId = intent.getIntExtra("ALARM_ID", -1) // ID Ejercicio
        val alarmRecordId = intent.getIntExtra("ALARM_RECORD_ID", 0) // ID Registro Alarma
        val alarmNombre = intent.getStringExtra("ALARM_NOMBRE")
        val alarmDuracion = intent.getIntExtra("ALARM_DURACION", 60)
        val alarmTipo = intent.getStringExtra("ALARM_TIPO")

        val intentReceiver = android.content.Intent(context, com.tupausa.alarm.AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_RECORD_ID", alarmRecordId)
            putExtra("ALARM_NOMBRE", alarmNombre)
            putExtra("ALARM_DURACION", alarmDuracion)
            putExtra("ALARM_TIPO", alarmTipo)
        }

        val uniqueId = System.currentTimeMillis().toInt()
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context, uniqueId, intentReceiver,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 5 * 60 * 1000 // 5 minutos

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager?.canScheduleExactAlarms() == true) {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager?.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }

        Toast.makeText(this, "Pospuesto 5 minutos", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerSonido()
        if (::shakeDetector.isInitialized) shakeDetector.stop()
        if (::flipDetector.isInitialized) flipDetector.stop()
        if (::proximityDetector.isInitialized) proximityDetector.stop()
    }

    // Funciones de Sonido y Pantalla
    private fun encenderPantalla() {
        if (SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
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

    private fun iniciarSonido() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            ringtone = RingtoneManager.getRingtone(applicationContext, uri).apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun detenerSonido() {
        ringtone?.stop()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    ejercicio: Ejercicio?,
    infoReto: String,
    isManual: Boolean,
    onDismiss: () -> Unit,
    onPosponer: () -> Unit
) {
    val context = LocalContext.current

    // Si aún no carga el ejercicio, mostramos Loading
    if (ejercicio == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ArenaPrimary)
        }
        return
    }

    // CONFIGURACIÓN DEL CARGADOR DE IMAGENES PARA GIFS
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
            }
            .build()
    }

    val duracionTotal = ejercicio.duracionSegundos
    var timeLeft by remember { mutableFloatStateOf(duracionTotal.toFloat()) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(100L)
            timeLeft -= 0.1f
        }
    }

    val progreso = timeLeft / duracionTotal

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LinearProgressIndicator(
                    progress = { transformProgress(progreso) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = ArenaOnPrimaryContainer,
                    trackColor = MaterialTheme.colorScheme.onSurface
                )
            },
            bottomBar = {
                Button(
                    onClick = if (isManual) onDismiss else onPosponer, // AQUÍ SE ACTIVA EL BOTÓN MANUAL
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(isManual) ArenaPrimary else ArenaOnPrimaryContainer
                    )
                ) {
                    Icon(if(isManual) Icons.Default.Stop else Icons.Default.Snooze, contentDescription = null,
                        tint = ArenaPrimaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if(isManual) "TERMINAR EJERCICIO" else "POSPONER 5 MIN",
                        color = ArenaPrimaryContainer,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if(isManual) "Modo Manual" else "¡Es hora de tu Pausa!",
                    style = MaterialTheme.typography.titleMedium,
                    color = ArenaOnSurfaceVariant
                )
                Text(
                    text = ejercicio.nombreEjercicio,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    color = ArenaOnSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "${timeLeft.toInt()} s",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArenaPrimary
                )

                if (infoReto.isNotEmpty()) {
                    Text(
                        text = infoReto,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArenaOnSurface,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                    Spacer(modifier = Modifier.height(16.dp))

                val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(280.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (drawableId != 0) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(drawableId).crossfade(true).build(),
                                imageLoader = imageLoader,
                                contentDescription = "Guía",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Imagen no disponible", color = Color.Gray)
                        }
                    }
                }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionTitle("Cómo realizarlo")
                            Text(
                                text = ejercicio.descripcion,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val instrucciones = ejercicio.getInstruccionesList()
                            instrucciones.forEachIndexed { index, instruccion ->
                                InstruccionItem(
                                    numero = index + 1,
                                    texto = instruccion
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Intensidad: ${ejercicio.nivelIntensidad}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }


fun transformProgress(value: Float): Float { return value.coerceIn(0f, 1f)
}
