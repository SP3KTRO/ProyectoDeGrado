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
import com.tupausa.utils.rememberDrawableId
import com.tupausa.TuPausaApplication
import com.tupausa.alarm.AlarmActivity
import com.tupausa.alarm.AlarmScheduler
import com.tupausa.model.data.Alarma
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.Primary
import com.tupausa.ui.theme.Secondary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSecondary
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.OnSurfaceVariant
import com.tupausa.ui.theme.Surface
import com.tupausa.ui.theme.Tertiary
import com.tupausa.view.user.AlarmaFormDialog
import com.tupausa.viewModel.AlarmasViewModel
import com.tupausa.viewModel.AlarmasViewModelFactory
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEjercicioDetalleScreen(
    ejercicio: Ejercicio,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Configuración de AlarmScheduler
    val app = context.applicationContext as TuPausaApplication
    val scheduler = remember { AlarmScheduler(context) }

    val viewModel: AlarmasViewModel = viewModel(
        factory = AlarmasViewModelFactory(
            app.alarmaRepository,
            scheduler,
            app.ejercicioRepository
        )
    )

    // Estados
    var showAlarmaDialog by remember { mutableStateOf(false) }
    val listaEjercicios by viewModel.ejercicios.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Configuración de Coil para cargar Gif
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
                    titleContentColor = OnSurface,
                    navigationIconContentColor = OnSurface
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
            // Header con GIF
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
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
                                tint = OnSurface
                            )
                            Text(
                                text = "Vista previa no disponible",
                                color = OnSurface
                            )
                        }
                    }
                }
            }

            // Contenido del Ejercicio
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Secondary
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Titulo
                    Text(
                        text = ejercicio.nombreEjercicio,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary
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
                    HorizontalDivider()
                    // Descripción
                    SectionTitle("Descripción")
                    Text(
                        text = ejercicio.descripcion,
                        fontSize = 16.sp,
                        color = OnPrimary
                    )
                    HorizontalDivider()
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
                                containerColor = Surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = OnSurface
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = ejercicio.beneficios,
                                    fontSize = 16.sp,
                                    color = OnSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Programar alarma
                FilledTonalButton(
                    onClick = { showAlarmaDialog = true },
                    modifier = Modifier.height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Programar Alarma",
                        tint = OnSurface,
                    )
                }

                // Comenzar ejercicio
                Button(
                    onClick = {
                        val intent = Intent(context, AlarmActivity::class.java).apply {
                            putExtra("IS_MANUAL", true)
                            putIntegerArrayListExtra("ALARM_IDS_RUTINA", arrayListOf(ejercicio.idEjercicio))
                            putExtra("ALARM_NOMBRE", ejercicio.nombreEjercicio)
                            putExtra("ALARM_TIPO", ejercicio.tipoEjercicio)
                            putExtra("ALARM_DURACION", ejercicio.duracionSegundos)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null,
                        tint = OnPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comenzar Ejercicio",
                        fontSize = 18.sp,
                        color = OnPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Dialog de Alarma
        if (showAlarmaDialog) {
            val preAlarma = Alarma(
                idsEjercicios = listOf(ejercicio.idEjercicio),
                hora = 8,
                minuto = 0,
                diasRepeticion = emptyList(),
                etiqueta = ejercicio.nombreEjercicio,
                tipoEjercicio = ejercicio.tipoEjercicio,
                tonoAlarma = "Predeterminado",
                activa = true
            )

            AlarmaFormDialog(
                context = context,
                alarmaAEditar = preAlarma,
                listaEjercicios = listaEjercicios,
                onDismiss = { showAlarmaDialog = false },
                onConfirm = { hora, min, dias, etiqueta, tipo, tono, idsEjercicios ->
                    // Guardar la nueva alarma
                    val nuevaAlarma = Alarma(
                        idsEjercicios = idsEjercicios,
                        hora = hora,
                        minuto = min,
                        diasRepeticion = dias,
                        etiqueta = etiqueta,
                        tipoEjercicio = tipo,
                        tonoAlarma = tono,
                        activa = true
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

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Tertiary
    )
}

@Composable
fun InfoBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = OnSecondary
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
                tint = OnSurface
            )
            Text(
                text = text,
                fontSize = 12.sp,
                color = OnSurface,
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
            color = Tertiary
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numero.toString(),
                    color = OnSurface,
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
            color = OnPrimary
        )
    }
}