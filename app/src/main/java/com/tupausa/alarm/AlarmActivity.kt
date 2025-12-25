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

class AlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Verificar si es inicio manual
        val isManual = intent.getBooleanExtra("IS_MANUAL", false)

        // 2. Solo encendemos pantalla si NO es manual
        if (!isManual) {
            encenderPantalla()
        }

        val nombreAlarma = intent.getStringExtra("ALARM_NOMBRE") ?: "Pausa Activa"
        val tipoEjercicio = intent.getStringExtra("ALARM_TIPO") ?: "ALEATORIO"

        // CAPTURAR DATOS PARA EL HISTORIAL
        val alarmId = intent.getIntExtra("ALARM_ID", -1) 
        val alarmDuracion = intent.getIntExtra("ALARM_DURACION", 60)

        // INSTANCIAR PREFS PARA EL ID DE USUARIO
        val prefs = PreferencesManager(this)
        val userId = prefs.getUserId()

        val app = application as TuPausaApplication
        val repository = app.ejercicioRepository
        val historialRepository = app.historialRepository

        // 3. Solo iniciamos sonido si NO es manual
        if (!isManual) {
            iniciarSonido()
        }

        setContent {
            com.tupausa.ui.theme.TuPausaTheme(
                dynamicColor = false
            ){
                AlarmScreen(
                    nombreAlarma = if(isManual) " " else nombreAlarma,
                    tipoObjetivo = tipoEjercicio,
                    repository = repository,
                    onDismiss = {
                        // --- AQUÍ GUARDAMOS EL HISTORIAL ---
                        if (userId != -1 && alarmId != -1) {
                            CoroutineScope(Dispatchers.IO).launch {
                                historialRepository.insertarHistorial(
                                    idUsuario = userId,
                                    idEjercicio = alarmId,
                                    duracion = alarmDuracion,
                                    tipo = "MANUAL"
                                )
                            }
                        }
                    // -----------------------------------

                    if (!isManual) detenerSonido()
                            finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerSonido()
    }

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
    nombreAlarma: String,
    tipoObjetivo: String,
    repository: com.tupausa.repository.EjercicioRepository,
    onDismiss: () -> Unit
) {
    var ejercicioActual by remember { mutableStateOf<Ejercicio?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val targetId = activity?.intent?.getIntExtra("ALARM_ID", -1) ?: -1

    // CONFIGURACIÓN DEL CARGADOR DE IMAGENES PARA GIFS
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val todos = repository.getAllEjercicios()
            val ejercicioEncontrado = if (targetId != -1) {
                todos.find { it.idEjercicio == targetId }
            } else if (tipoObjetivo != "ALEATORIO") {
                todos.filter { it.tipoEjercicio == tipoObjetivo }.randomOrNull()
            } else {
                todos.randomOrNull()
            }
            ejercicioActual = ejercicioEncontrado ?: todos.randomOrNull()
            isLoading = false
        }
    }

    if (isLoading || ejercicioActual == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        val ejercicio = ejercicioActual!!
        val duracionTotal = ejercicio.duracionSegundos
        var timeLeft by remember { mutableFloatStateOf(duracionTotal.toFloat()) }

        LaunchedEffect(Unit) {
            while (timeLeft > 0) {
                delay(100L)
                timeLeft -= 0.1f
            }
        }

        val progreso = timeLeft / duracionTotal

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier.fillMaxSize()
            )

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    LinearProgressIndicator(
                        progress = { transformProgress(progreso) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = ArenaOnPrimaryContainer,
                        trackColor = MaterialTheme.colorScheme.onSurface
                    )
                },
                bottomBar = {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TERMINAR EJERCICIO", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        text = nombreAlarma,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = ejercicio.nombreEjercicio,
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 24.sp,
                        color = ArenaPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Text(
                        text = "${timeLeft.toInt()} s",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.errorContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(280.dp),
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
                                    model = ImageRequest.Builder(context)
                                        .data(drawableId)
                                        .crossfade(true)
                                        .build(),
                                    imageLoader = imageLoader,
                                    contentDescription = "Guía del ejercicio",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Imagen no disponible", color = Color.Gray)
                                }
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
}

fun transformProgress(value: Float): Float {
    return value.coerceIn(0f, 1f)
}
