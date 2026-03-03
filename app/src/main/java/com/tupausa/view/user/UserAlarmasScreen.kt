package com.tupausa.view.user

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tupausa.TuPausaApplication
import com.tupausa.alarm.AlarmScheduler
import com.tupausa.model.data.Alarma
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSecondary
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.OnSurfaceVariant
import com.tupausa.ui.theme.Primary
import com.tupausa.ui.theme.PrimaryContainer
import com.tupausa.ui.theme.Secondary
import com.tupausa.ui.theme.Surface
import com.tupausa.ui.theme.Tertiary
import com.tupausa.utils.PreferencesManager
import com.tupausa.viewModel.AlarmasViewModel
import com.tupausa.viewModel.AlarmasViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAlarmasScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TuPausaApplication
    val scheduler = remember { AlarmScheduler(context) }

    val viewModel: AlarmasViewModel = viewModel(
        factory = AlarmasViewModelFactory(
            app.alarmaRepository,
            scheduler,
            app.ejercicioRepository
        )
    )

    val preferencesManager = remember { PreferencesManager(context) }
    val limitaciones = remember {
        val limStr = preferencesManager.getLimitaciones()
        if (limStr.isNotEmpty()) limStr.split(",") else emptyList()
    }
    LaunchedEffect(Unit) {
        viewModel.cargarEjercicios(limitaciones)
    }

    // DATOS
    val alarmas by viewModel.alarmas.collectAsState()
    val listaEjercicios by viewModel.ejercicios.collectAsState()

    // ESTADOS DE UI
    var showCreateDialog by remember { mutableStateOf(false) }
    var alarmaAEditar: Alarma? by remember { mutableStateOf(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // PERMISOS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> if (!isGranted) showPermissionDialog = true }
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!context.getSystemService(android.app.AlarmManager::class.java).canScheduleExactAlarms()) {
                showPermissionDialog = true
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Mis Rutinas") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = OnPrimaryContainer
            ) {
                Icon(Icons.Default.Add, "Crear Alarma", tint = OnPrimary)
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (alarmas.isEmpty()) {
                // Estado Vacío
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⏰", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tienes alarmas programadas.",
                        fontSize = 20.sp,
                        color = OnSurface
                    )
                }
            } else {
                // Lista de Alarmas
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(alarmas) { alarma ->
                        Box(modifier = Modifier.clickable {
                            viewModel.prepararEdicion(alarma)
                            alarmaAEditar = alarma
                        }) {
                            AlarmaCard(
                                alarma = alarma,
                                onToggle = { viewModel.toggleAlarma(alarma) },
                                onDelete = { viewModel.eliminarAlarma(alarma) }
                            )
                        }
                    }
                }
            }
        }

        if (showCreateDialog || alarmaAEditar != null) {
            AlarmaFormDialog(
                context = context,
                alarmaAEditar = alarmaAEditar,
                listaEjercicios = listaEjercicios,
                listaArlarmas = alarmas,
                onDismiss = {
                    showCreateDialog = false
                    alarmaAEditar = null
                },
                onConfirm = { hora, min, dias, etiqueta, tipo, tono, idsEjercicios ->
                    val idFinal = alarmaAEditar?.id ?: 0
                    val nuevaAlarma = Alarma(
                        id = idFinal,
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
                    showCreateDialog = false
                    alarmaAEditar = null
                }
            )
        }

        // Diálogo de Permisos
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                containerColor = Secondary,
                title = { Text("Permisos Requeridos" , color = OnPrimary) },
                text = { Text("Para que las alarmas funcionen correctamente, necesitas otorgar permisos en la configuración.", color = OnPrimary) },
                confirmButton = {
                    Button(onClick = {
                        showPermissionDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                        colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                    ) { Text("Ir a Configuración", color = OnPrimary) }
                },
                dismissButton = { TextButton(onClick = { showPermissionDialog = false })
                { Text("Cancelar", color = OnPrimary) } }
            )
        }
    }
}

@Composable
fun AlarmaCard(
    alarma: Alarma,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Secondary
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Hora
                Text(
                    text = String.format("%02d:%02d", alarma.hora, alarma.minuto),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarma.activa) OnPrimary else Color.Gray
                )
                // Nombre y tipo
                Text(
                    text = alarma.etiqueta,
                    fontSize = 16.sp,
                    color = if (alarma.activa) Surface else Color.Gray
                )
                // Días
                Text(
                    text = if (alarma.diasRepeticion.isEmpty()) "Una vez" else "Días programados",
                    fontSize = 12.sp,
                    color = Tertiary
                )
                // Información de la Rutina
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (alarma.idsEjercicios.size < 4) Color.Red else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rutina: ${alarma.idsEjercicios.size} ejercicios",
                        fontSize = 12.sp,
                        color = if (alarma.idsEjercicios.size < 4) Primary else PrimaryContainer
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Switch Activar/Desactivar
                Switch(
                    checked = alarma.activa,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnPrimary,
                        checkedTrackColor = Primary,
                        uncheckedThumbColor = Color.Gray
                    )
                )
                // Botón eliminar
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = PrimaryContainer)
                }
            }
        }
    }
}