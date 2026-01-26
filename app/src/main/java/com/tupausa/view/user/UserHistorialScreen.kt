package com.tupausa.view.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tupausa.model.HistorialRegistro
import com.tupausa.ui.theme.*
import com.tupausa.viewModel.HistorialViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHistorialScreen(
    onBack: () -> Unit,
    viewModel: HistorialViewModel = viewModel()
) {
    val historial by viewModel.historialList.collectAsState()
    val resumen by viewModel.resumen.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Cargar datos al entrar
    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    // DIÁLOGO DE CONFIRMACIÓN
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Borrar historial?") },
            text = { Text("Esta acción eliminará todos tus registros locales de pausas activas. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrarHistorial()
                    showDeleteDialog = false
                }) {
                    Text("Borrar todo", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Mi Historial") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // BOTÓN DE BORRAR
                    if (historial.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar todo",
                                tint = OnSurface
                            )
                        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // TARJETAS DE RESUMEN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Pausas",
                    value = resumen.totalPausas.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = OnPrimary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Minutos Totales",
                    value = resumen.tiempoTotalMinutos.toString(),
                    icon = Icons.Default.AccessTime,
                    color = Surface
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Historial Reciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // LISTA DE REGISTROS
            if (historial.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no tienes actividad registrada.", color = OnSurface)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historial) { item ->
                        HistorialItemView(item)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Secondary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 12.sp, color = color)
        }
    }
}

@Composable
fun HistorialItemView(item: HistorialRegistro) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val fechaStr = dateFormat.format(Date(item.fecha))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Secondary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono lateral
            Surface(
                shape = MaterialTheme.shapes.small,
                color = OnSecondary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = OnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombreEjercicio,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )
                Text(
                    text = fechaStr,
                    fontSize = 12.sp,
                    color = PrimaryContainer
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.duracionSegundos}s",
                    fontWeight = FontWeight.Bold,
                    color = Tertiary
                )
                Text(
                    text = if(item.tipoDeteccion == "MANUAL") "Manual" else "Sensor",
                    fontSize = 10.sp,
                    color = Surface
                )
            }
        }
    }
}