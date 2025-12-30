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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tupausa.TuPausaApplication
import com.tupausa.alarm.AlarmScheduler
import com.tupausa.model.data.Alarma
import com.tupausa.model.Ejercicio
import com.tupausa.ui.theme.ArenaOnPrimaryContainer
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

    // --- DATOS ---
    val alarmas by viewModel.alarmas.collectAsState()
    val listaEjercicios by viewModel.ejerciciosReales.collectAsState()

    // --- ESTADOS DE UI ---
    var showCreateDialog by remember { mutableStateOf(false) }
    var alarmaAEditar: Alarma? by remember { mutableStateOf(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // --- PERMISOS
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

    // --- UI PRINCIPAL ---
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Mis Alarmas") },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }, // ESTO PONE LA VARIABLE EN TRUE
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Crear Alarma", tint = Color.White)
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (alarmas.isEmpty()) {
                // Estado Vacío (Igual que antes)
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⏰", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tienes alarmas",
                        fontSize = 20.sp,
                        color = ArenaOnPrimaryContainer
                    )
                }
            } else {
                // Lista de Alarmas
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(alarmas) { alarma ->
                        Box(modifier = Modifier.clickable { alarmaAEditar = alarma }) {
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
                alarmaAEditar = alarmaAEditar,
                listaEjercicios = listaEjercicios, // Le pasamos la lista al dialog
                onDismiss = {
                    showCreateDialog = false
                    alarmaAEditar = null
                },
                onConfirm = { hora, min, dias, etiqueta, tipo, tono, idEjercicio ->
                    // Preservamos el ID si es edición, si no es 0
                    val idFinal = alarmaAEditar?.id ?: 0
                    val nuevaAlarma = Alarma(
                        id = idFinal,
                        idEjercicio = idEjercicio,
                        hora = hora,
                        minuto = min,
                        diasRepeticion = dias,
                        etiqueta = etiqueta,
                        tipoEjercicio = tipo,
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
                title = { Text("Permisos Requeridos") },
                text = { Text("Para que las alarmas funcionen correctamente, necesitas otorgar permisos en la configuración.") },
                confirmButton = {
                    Button(onClick = {
                        showPermissionDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) { Text("Ir a Configuración") }
                },
                dismissButton = { TextButton(onClick = { showPermissionDialog = false }) { Text("Cancelar") } }
            )
        }
    }
}