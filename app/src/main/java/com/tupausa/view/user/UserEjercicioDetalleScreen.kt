package com.tupausa.view.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tupausa.model.Ejercicio
import android.os.Build.VERSION.SDK_INT
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.tupausa.ui.theme.ArenaOnPrimaryContainer
import com.tupausa.utils.rememberDrawableId
import com.tupausa.TuPausaApplication
import com.tupausa.alarm.AlarmActivity
import com.tupausa.alarm.AlarmScheduler
import com.tupausa.model.data.Alarma
import com.tupausa.view.user.AlarmaFormDialog
import com.tupausa.viewModel.AlarmasViewModel
import com.tupausa.viewModel.AlarmasViewModelFactory
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEjercicioDetalleScreen(
    ejercicio: Ejercicio,
    onBack: () -> Unit
    // Ya no necesitamos pasar callbacks vacíos porque implementaremos la lógica aquí dentro
) {
    val context = LocalContext.current

    // 1. CONFIGURACIÓN DEL VIEWMODEL Y SCHEDULER (Necesarios para guardar la alarma)
    val app = context.applicationContext as TuPausaApplication
    val scheduler = remember { AlarmScheduler(context) }

    val viewModel: AlarmasViewModel = viewModel(
        factory = AlarmasViewModelFactory(
            app.alarmaRepository,
            scheduler,
            app.ejercicioRepository
        )
    )

    // 2. ESTADOS
    // Controla si se muestra el diálogo de programar alarma
    var showAlarmaDialog by remember { mutableStateOf(false) }
    // Obtenemos la lista de ejercicios para que el Dialog funcione correctamente
    val listaEjercicios by viewModel.ejerciciosReales.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 3. CONFIGURACIÓN DE COIL (GIFs)
    val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)
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

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Ejercicio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = ArenaOnPrimaryContainer,
                    navigationIconContentColor = ArenaOnPrimaryContainer
                )
            )
        }
    ) { padding ->
        // Contenedor principal con Scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HEADER CON GIF ANIMADO
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                            contentDescription = ejercicio.nombreEjercicio,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Vista previa no disponible")
                        }
                    }
                }
            }

            // CONTENEDOR DE INFORMACIÓN
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Título
                    Text(
                        text = ejercicio.nombreEjercicio,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InfoBadge(
                            icon = Icons.Default.Category,
                            text = ejercicio.getTipoDisplayName()
                        )
                        InfoBadge(
                            icon = Icons.Default.Timer,
                            text = "${ejercicio.duracionSegundos}s"
                        )
                        InfoBadge(
                            icon = Icons.Default.TrendingUp,
                            text = ejercicio.getNivelDisplayName()
                        )
                    }

                    Divider()

                    // Descripción
                    SectionTitle("Descripción")
                    Text(
                        text = ejercicio.descripcion,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Divider()

                    // Instrucciones
                    SectionTitle("Cómo realizarlo")
                    val instrucciones = ejercicio.getInstruccionesList()
                    instrucciones.forEachIndexed { index, instruccion ->
                        InstruccionItem(
                            numero = index + 1,
                            texto = instruccion
                        )
                    }

                    // Beneficios
                    if (!ejercicio.beneficios.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = ejercicio.beneficios,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // --- ZONA DE BOTONES ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // BOTÓN 1: PROGRAMAR ALARMA ⏰
                FilledTonalButton(
                    onClick = { showAlarmaDialog = true }, // Abrir Dialog
                    modifier = Modifier.height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Programar Alarma"
                    )
                }

                // BOTÓN 2: COMENZAR EJERCICIO ▶️
                Button(
                    onClick = {
                        val intent = Intent(context, AlarmActivity::class.java).apply {
                            putExtra("IS_MANUAL", true)
                            putExtra("ALARM_NOMBRE", ejercicio.nombreEjercicio)
                            putExtra("ALARM_TIPO", ejercicio.tipoEjercicio)
                            putExtra("ALARM_DURACION", ejercicio.duracionSegundos)

                            // --- AGREGA ESTA LÍNEA ---
                            putExtra("ALARM_ID", ejercicio.idEjercicio)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comenzar Ejercicio", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- LÓGICA DEL DIÁLOGO FLOTANTE ---
        if (showAlarmaDialog) {
            // Creamos una alarma "ficticia" con los datos del ejercicio actual
            // Esto sirve para pre-llenar el formulario
            val preAlarma = Alarma(
                hora = 8,
                minuto = 0,
                diasRepeticion = emptyList(),
                etiqueta = ejercicio.nombreEjercicio, // Nombre del ejercicio actual
                tipoEjercicio = ejercicio.tipoEjercicio, // Tipo del ejercicio actual
                activa = true
            )

            AlarmaFormDialog(
                alarmaAEditar = preAlarma, // Pasamos la alarma ficticia para editar
                listaEjercicios = listaEjercicios, // Lista completa por si quiere cambiar
                onDismiss = { showAlarmaDialog = false },
                onConfirm = { hora, min, dias, etiqueta, tipo, tono ->
                    // Guardamos la nueva alarma
                    val nuevaAlarma = Alarma(
                        hora = hora,
                        minuto = min,
                        diasRepeticion = dias,
                        etiqueta = etiqueta,
                        tipoEjercicio = tipo,
                        activa = true
                        // tono = tono (Si ya lo implementaste en el modelo)
                    )
                    viewModel.guardarAlarma(nuevaAlarma)
                    showAlarmaDialog = false

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "¡Alarma programada con éxito!",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }
    }
}

// ... TUS COMPONENTES AUXILIARES (SectionTitle, InfoBadge, InstruccionItem) SIGUEN IGUAL ...
@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun InfoBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = text,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InstruccionItem(
    numero: Int,
    texto: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numero.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = texto,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}