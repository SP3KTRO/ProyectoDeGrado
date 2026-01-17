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
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
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
enum class TipoReto { SHAKE, TAP, FLIP, LONG_PRESS, DOUBLE_TAP, PROXIMITY, DRAW_CIRCLE, MULTI_TAP }

class AlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null

    //VARIABLES PARA SENSORES Y CAMARA
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var flipDetector: FlipDetector
    private lateinit var proximityDetector: ProximityDetector

    private var circleDetector: CircleDetector? = null
    private lateinit var cameraHelper: CameraHelper
    private var rutaFotoEvidencia: String = ""

    // ESTADO DE LA RUTINA
    private var listaEjerciciosRutina by mutableStateOf<List<Ejercicio>>(emptyList())
    private var indiceEjercicioActual by mutableIntStateOf(0)

    // ESTADO DEL RETO ACTUAL
    private var retoActual by mutableStateOf(TipoReto.SHAKE)
    private var metaRepeticiones by mutableIntStateOf(5)
    private var repeticionesActuales by mutableIntStateOf(0)


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

        // 1. CONFIGURACIÓN DE PANTALLA
        configurarPantallaInvasiva()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(
                    this@AlarmActivity,
                    "Completa la rutina para salir",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        setContent {
            // Este efecto carga la lista de ejercicios una sola vez
            LaunchedEffect(intent) {
                // 2. RECUPERACIÓN DE DATOS
                val idsRutina = intent.getIntegerArrayListExtra("ALARM_IDS_RUTINA") ?: arrayListOf()
                val isManual = intent.getBooleanExtra("IS_MANUAL", false)

                Log.d("TuPausa_Debug", "Cargando rutina: $idsRutina")

                val repository = (application as TuPausaApplication).ejercicioRepository
                val todos = repository.getAllEjercicios()

                listaEjerciciosRutina = if (idsRutina.isNotEmpty()) {
                    idsRutina.mapNotNull { id -> todos.find { it.idEjercicio == id } }
                } else {
                    Log.e("TuPausa_Debug", "La lista de IDs llegó VACÍA, usando fallback aleatorio")
                    listOf(todos.random())
                }

                if (!isManual) {
                    indiceEjercicioActual = 0 // Reiniciar al principio si es nueva alarma
                    inicializarCicloEjercicio()
                    inicializarCamara()
                    iniciarSonido()
                }
            }

            // Bloqueo del botón atrás en Compose
            BackHandler(enabled = true) {
                Toast.makeText(this, "Debes completar la rutina para salir", Toast.LENGTH_SHORT)
                    .show()
            }

            com.tupausa.ui.theme.TuPausaTheme(dynamicColor = false) {
                // Obtenemos el ejercicio actual de la lista
                val ejercicioActual = listaEjerciciosRutina.getOrNull(indiceEjercicioActual)

                ejercicioActual?.let { ejercicio ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                            // DETECTOR MULTI-TOUCH (3 DEDOS)
                            .pointerInput(retoActual) {
                                if (retoActual == TipoReto.MULTI_TAP) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            if (event.changes.size >= 3 && event.changes.any { it.changedToDown() }) {
                                                verificarProgreso(TipoReto.MULTI_TAP)
                                            }
                                        }
                                    }
                                }
                            }
                            .pointerInput(retoActual) {
                                if (retoActual in listOf(
                                        TipoReto.TAP,
                                        TipoReto.DOUBLE_TAP,
                                        TipoReto.LONG_PRESS
                                    )
                                ) {
                                    detectTapGestures(
                                        onTap = {
                                            if (retoActual == TipoReto.TAP) verificarProgreso(
                                                TipoReto.TAP
                                            )
                                        },
                                        onDoubleTap = {
                                            if (retoActual == TipoReto.DOUBLE_TAP) verificarProgreso(
                                                TipoReto.DOUBLE_TAP
                                            )
                                        },
                                        onLongPress = {
                                            if (retoActual == TipoReto.LONG_PRESS) verificarProgreso(
                                                TipoReto.LONG_PRESS
                                            )
                                        }
                                    )
                                }
                            }
                            .pointerInput(retoActual) {
                                if (retoActual == TipoReto.DRAW_CIRCLE) {
                                    detectDragGestures(
                                        onDragStart = { circleDetector?.reset() },
                                        onDragEnd = {
                                            if (circleDetector?.isCircleDetected() == true) {
                                                verificarProgreso(TipoReto.DRAW_CIRCLE)
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            circleDetector?.addPoint(change.position)
                                        }
                                    )
                                }
                            }
                    ) {
                        AlarmScreen(
                            ejercicio = ejercicio,
                            infoReto = if (!intent.getBooleanExtra("IS_MANUAL", false))
                                "Reto: ${obtenerTextoReto(retoActual)} ($repeticionesActuales/$metaRepeticiones)" else "",
                            isManual = intent.getBooleanExtra("IS_MANUAL", false),
                            onDismiss = { terminarRutinaCompleta(true) },
                            onPosponer = { posponerAlarma() }
                            //numActual = indiceEjercicioActual + 1,
                            //total = listaEjerciciosRutina.size
                        )
                    }
                } ?: run {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ArenaPrimary)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Actualiza el intent de la actividad para que el LaunchedEffect lo detecte
    }
    private fun inicializarCicloEjercicio() {
        repeticionesActuales = 0
        elegirRetoAleatorio()
        reiniciarSensoresParaNuevoReto()
    }

    private fun reiniciarSensoresParaNuevoReto() {
        if (::shakeDetector.isInitialized) shakeDetector.stop()
        if (::flipDetector.isInitialized) flipDetector.stop()
        if (::proximityDetector.isInitialized) proximityDetector.stop()
        inicializarSensores()
    }

    private fun verificarProgreso(tipoEjecutado: TipoReto) {
        if (tipoEjecutado == retoActual) {
            repeticionesActuales++
            if (repeticionesActuales >= metaRepeticiones) {
                avanzarSiguienteEjercicio()
            }
        }
    }

    private fun avanzarSiguienteEjercicio() {
        if (indiceEjercicioActual < listaEjerciciosRutina.size - 1) {
            indiceEjercicioActual++
            Toast.makeText(this, "¡Buen trabajo! Siguiente ejercicio...", Toast.LENGTH_SHORT).show()
            inicializarCicloEjercicio()
        } else {
            terminarRutinaCompleta(false)
        }
    }

    private fun terminarRutinaCompleta(isManual: Boolean) {
        detenerSonido()
        val app = application as TuPausaApplication
        val userId = PreferencesManager(this).getUserId()

        if (userId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                // Registramos cada ejercicio de la rutina en el historial
                listaEjerciciosRutina.forEach { ej ->
                    app.historialRepository.insertarHistorial(
                        idUsuario = userId,
                        idEjercicio = ej.idEjercicio,
                        duracion = ej.duracionSegundos,
                        tipo = if (isManual) "MANUAL" else "RUTINA_SENSOR",
                        rutaEvidencia = rutaFotoEvidencia
                    )
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AlarmActivity, "¡Rutina Completada!", Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    // BLOQUEO DEL BOTÓN ATRÁS
    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Toast.makeText(this, "Debes completar el reto para salir", Toast.LENGTH_SHORT).show()
    }

    private fun elegirRetoAleatorio() {
        val valores = TipoReto.entries
        retoActual = valores[Random.nextInt(valores.size)]

        metaRepeticiones = when (retoActual) {
            TipoReto.SHAKE -> 5  // Sacudir 5 veces
            TipoReto.TAP -> 8    // Tocar 8 veces
            TipoReto.FLIP -> 1   // Voltear 1 vez
            TipoReto.LONG_PRESS -> 4  // Mantener presionado 3 veces
            TipoReto.DOUBLE_TAP -> 5  // Hacer doble click 5 veces
            TipoReto.PROXIMITY -> 4    // Pasar la mano 3 veces
            TipoReto.MULTI_TAP -> 3    // Tocar con varios dedos 3 veces
            TipoReto.DRAW_CIRCLE -> 2  // Dibujar 2 círculos
        }
    }

    private fun obtenerTextoReto(reto: TipoReto): String = when (reto) {
        TipoReto.SHAKE -> "¡Sacude el celular! 📳"
        TipoReto.TAP -> "¡Toca la pantalla! 👆"
        TipoReto.FLIP -> "¡Voltea el celular! 🔄"
        TipoReto.LONG_PRESS -> "¡Mantén presionado 1 seg! 👆"
        TipoReto.DOUBLE_TAP -> "¡Haz Doble-Toque rápido! 👆"
        TipoReto.PROXIMITY -> "¡Pasa la mano sobre la cámara frontal! 👋"
        TipoReto.MULTI_TAP -> "¡Toca con 3 dedos a la vez! 🖐️"
        TipoReto.DRAW_CIRCLE -> "¡Dibuja un círculo en pantalla! ⭕"
    }

    private fun inicializarSensores() {
        shakeDetector = ShakeDetector(this)
        flipDetector = FlipDetector(this)
        circleDetector = CircleDetector()
        proximityDetector = ProximityDetector(this)

        when (retoActual) {
            TipoReto.SHAKE -> {
                shakeDetector.setOnShakeListener(object : ShakeDetector.OnShakeListener {
                    override fun onShake(count: Int) {
                        verificarProgreso(TipoReto.SHAKE)
                    }
                })
                shakeDetector.start()
            }

            TipoReto.FLIP -> flipDetector.start { verificarProgreso(TipoReto.FLIP) }
            TipoReto.PROXIMITY -> {
                if (proximityDetector.isAvailable()) {
                    proximityDetector.start { verificarProgreso(TipoReto.PROXIMITY) }
                } else {
                    retoActual = TipoReto.TAP
                }
            }

            else -> {}
        }
    }

    private fun configurarPantallaInvasiva() {
        if (SDK_INT >= Build.VERSION_CODES.O_MR1) {
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
    }

    private fun inicializarCamara() {
        cameraHelper =
            CameraHelper(this, this, { file -> rutaFotoEvidencia = file.absolutePath }, {})
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraHelper.startCamera()
            window.decorView.postDelayed({ cameraHelper.takePhoto() }, 3000)
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
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
        val idsRutina = intent.getIntegerArrayListExtra("ALARM_IDS_RUTINA")

        val intentReceiver = Intent(context, AlarmReceiver::class.java).apply {
            putIntegerArrayListExtra("ALARM_IDS_RUTINA", idsRutina)
            putExtra("ALARM_RECORD_ID", intent.getIntExtra("ALARM_RECORD_ID", 0))
            putExtra("ALARM_NOMBRE", intent.getStringExtra("ALARM_NOMBRE"))
            putExtra("ALARM_DURACION", intent.getIntExtra("ALARM_DURACION", 60))
            putExtra("ALARM_TIPO", intent.getStringExtra("ALARM_TIPO"))
        }

        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context, System.currentTimeMillis().toInt(), intentReceiver,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 5 * 60 * 1000
        alarmManager?.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

        Toast.makeText(this, "Pospuesto 5 minutos", Toast.LENGTH_SHORT).show()
        finish()
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

    override fun onDestroy() {
        super.onDestroy()
        detenerSonido()
        if (::shakeDetector.isInitialized) shakeDetector.stop()
        if (::flipDetector.isInitialized) flipDetector.stop()
        if (::proximityDetector.isInitialized) proximityDetector.stop()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AlarmScreen(
        ejercicio: Ejercicio?,
        infoReto: String,
        isManual: Boolean,
        onDismiss: () -> Unit,
        onPosponer: () -> Unit,
    ) {
        val context = LocalContext.current
        if (ejercicio == null) return

        val imageLoader = remember {
            ImageLoader.Builder(context)
                .components {
                    if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
                }
                .build()
        }

        var timeLeft by remember(ejercicio.idEjercicio) { mutableFloatStateOf(ejercicio.duracionSegundos.toFloat()) }

        LaunchedEffect(ejercicio.idEjercicio) {
            while (timeLeft > 0) {
                delay(100L)
                timeLeft -= 0.1f
            }
        }

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
                        progress = { transformProgress(timeLeft / ejercicio.duracionSegundos) },
                        modifier = Modifier.fillMaxWidth().height(6.dp), // Reducido ligeramente
                        color = ArenaOnPrimaryContainer,
                        trackColor = MaterialTheme.colorScheme.onSurface
                    )
                },
                bottomBar = {
                    Button(
                        onClick = if (isManual) onDismiss else onPosponer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp) // Padding reducido
                            .height(50.dp), // Botón un poco más compacto
                        colors = ButtonDefaults.buttonColors(containerColor = if (isManual) ArenaPrimary else ArenaOnPrimaryContainer)
                    ) {
                        Icon(
                            if (isManual) Icons.Default.Stop else Icons.Default.Snooze,
                            contentDescription = null,
                            tint = ArenaPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isManual) "TERMINAR" else "POSPONER 5 MIN", // Texto abreviado
                            color = ArenaPrimaryContainer,
                            fontSize = 16.sp, // Fuente ajustada
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            ) { padding ->
                // Usamos Column sin Scroll global
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(top = 16.dp, bottom = 8.dp), // Márgenes controlados
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- SECCIÓN 1: HEADER (Título, Tiempo, Reto) ---
                    Text(
                        text = ejercicio.nombreEjercicio,
                        style = MaterialTheme.typography.titleLarge, // Estilo más compacto
                        fontSize = 22.sp,
                        color = ArenaOnSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1, // Evitar que ocupe 2 lineas
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${timeLeft.toInt()} s",
                            fontSize = 36.sp, // Reducido de 48 a 36
                            fontWeight = FontWeight.Bold,
                            color = ArenaPrimary
                        )

                        if (infoReto.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = infoReto,
                                fontSize = 14.sp, // Mucho más pequeño (antes 20)
                                fontWeight = FontWeight.Bold,
                                color = ArenaOnSurface,
                                modifier = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.8f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- SECCIÓN 2: GIF (Flexible) ---
                    val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f) // Un poco más angosto
                            .weight(0.4f), // Ocupará el 40% del espacio disponible verticalmente
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (drawableId != 0) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(drawableId)
                                        .crossfade(true).build(),
                                    imageLoader = imageLoader,
                                    contentDescription = "Guía",
                                    contentScale = ContentScale.Fit, // Fit asegura que se vea todo, pero...
                                    // Si quieres eliminar bordes a toda costa, usa ContentScale.Crop,
                                    // pero podrías cortar partes del ejercicio.
                                    modifier = Modifier.fillMaxSize().padding(4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- SECCIÓN 3: INSTRUCCIONES (Flexible) ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .weight(0.6f) // Ocupará el resto del espacio (60%)
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    ) {
                        // Usamos LazyColumn AQUÍ adentro por si el texto es muy largo
                        // pero la pantalla principal NO se mueve.
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            item {
                                SectionTitle("Cómo realizarlo", fontSize = 16.sp)
                                Text(
                                    text = ejercicio.descripcion,
                                    fontSize = 13.sp, // Texto compacto
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            itemsIndexed(ejercicio.getInstruccionesList()) { index, instruccion ->
                                InstruccionItemCompacto( // Crear versión compacta
                                    numero = index + 1,
                                    texto = instruccion
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Helper para títulos más pequeños
    @Composable
    fun SectionTitle(text: String, fontSize: TextUnit = 18.sp) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp),
            color = ArenaPrimary
        )
    }

    // Item de instrucción optimizado para espacio
    @Composable
    fun InstruccionItemCompacto(numero: Int, texto: String) {
        Row(
            modifier = Modifier.padding(vertical = 2.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$numero.",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ArenaPrimary,
                modifier = Modifier.width(20.dp)
            )
            Text(
                text = texto,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    /*@OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AlarmScreen(
        ejercicio: Ejercicio?,
        infoReto: String,
        isManual: Boolean,
        onDismiss: () -> Unit,
        onPosponer: () -> Unit,
        numActual: Int,
        total: Int
    ) {
        val context = LocalContext.current
        if (ejercicio == null) return

        val imageLoader = remember {
            ImageLoader.Builder(context)
                .components {
                    if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(
                        GifDecoder.Factory()
                    )
                }
                .build()
        }

        var timeLeft by remember(ejercicio.idEjercicio) { mutableFloatStateOf(ejercicio.duracionSegundos.toFloat()) }
        LaunchedEffect(ejercicio.idEjercicio) {
            while (timeLeft > 0) {
                delay(100L); timeLeft -= 0.1f
            }
        }

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
                    Column {
                        LinearProgressIndicator(
                            progress = { transformProgress(timeLeft / ejercicio.duracionSegundos) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = ArenaOnPrimaryContainer,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "Ejercicio $numActual de $total",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            textAlign = TextAlign.Center,
                            color = ArenaPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                bottomBar = {
                    Button(
                        onClick = if (isManual) onDismiss else onPosponer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isManual) ArenaPrimary else ArenaOnPrimaryContainer)
                    ) {
                        Icon(
                            if (isManual) Icons.Default.Stop else Icons.Default.Snooze,
                            null,
                            tint = ArenaPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isManual) "TERMINAR" else "POSPONER 5 MIN",
                            color = ArenaPrimaryContainer,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = ejercicio.nombreEjercicio,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = ArenaOnSurface
                    )
                    Text(
                        text = "${timeLeft.toInt()} s",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = ArenaPrimary
                    )

                    if (infoReto.isNotEmpty()) {
                        Surface(
                            color = Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = infoReto,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = Color.Black
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.4f
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)
                            if (drawableId != 0) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(drawableId)
                                        .crossfade(true).build(),
                                    imageLoader = imageLoader,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.9f
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Cómo realizarlo:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ArenaPrimary
                            )
                            Text(
                                text = ejercicio.descripcion,
                                fontSize = 13.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AlarmScreen(
        ejercicio: Ejercicio?,
        infoReto: String,
        isManual: Boolean,
        onDismiss: () -> Unit,
        onPosponer: () -> Unit,
    ) {
        val context = LocalContext.current
        if (ejercicio == null) return

        val imageLoader = remember {
            ImageLoader.Builder(context)
                .components {
                    if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(
                        GifDecoder.Factory()
                    )
                }
                .build()
        }

        var timeLeft by remember(ejercicio.idEjercicio) { mutableFloatStateOf(ejercicio.duracionSegundos.toFloat()) }

        LaunchedEffect(ejercicio.idEjercicio) {
            while (timeLeft > 0) {
                delay(100L)
                timeLeft -= 0.1f
            }
        }

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
                        progress = { transformProgress(timeLeft / ejercicio.duracionSegundos) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = ArenaOnPrimaryContainer,
                        trackColor = MaterialTheme.colorScheme.onSurface
                    )
                },
                bottomBar = {
                    Button(
                        onClick = if (isManual) onDismiss else onPosponer,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isManual) ArenaPrimary else ArenaOnPrimaryContainer)
                    ) {
                        Icon(
                            if (isManual) Icons.Default.Stop else Icons.Default.Snooze,
                            contentDescription = null,
                            tint = ArenaPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isManual) "TERMINAR EJERCICIO" else "POSPONER 5 MIN",
                            color = ArenaPrimaryContainer,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))
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
                            modifier = Modifier.padding(vertical = 8.dp).background(
                                Color.White.copy(alpha = 0.8f),
                                RoundedCornerShape(8.dp)
                            ).padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.9f).height(280.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (drawableId != 0) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(drawableId)
                                        .crossfade(true).build(),
                                    imageLoader = imageLoader,
                                    contentDescription = "Guía",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(0.95f).padding(bottom = 24.dp),
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
                            ejercicio.getInstruccionesList().forEachIndexed { index, instruccion ->
                                InstruccionItem(
                                    numero = index + 1,
                                    texto = instruccion
                                )
                            }
                        }
                    }
                }
            }
        }
    }*/
}

fun transformProgress(value: Float): Float { return value.coerceIn(0f, 1f) }

