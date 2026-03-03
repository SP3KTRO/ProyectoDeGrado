package com.tupausa.view.user

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tupausa.model.HistorialRegistro
import com.tupausa.model.RutinaHistorial
import com.tupausa.model.DiaStat
import com.tupausa.ui.theme.*
import com.tupausa.viewModel.HistorialViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHistorialScreen(
    onBack: () -> Unit,
    viewModel: HistorialViewModel = viewModel()
) {
    val rutinas by viewModel.rutinasList.collectAsState()
    val resumen by viewModel.resumen.collectAsState()
    val statsSemana by viewModel.statsSemana.collectAsState()
    val mensajeMotivacional by viewModel.mensajeMotivacional.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var rutinaSeleccionada by remember { mutableStateOf<RutinaHistorial?>(null) }

    // Cargar datos al entrar
    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    // Dialog de confirmación para borrar historial
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Secondary,
            title = { Text("¿Borrar historial?", color = OnPrimary) },
            text = { Text("Esta acción eliminará todos tus registros locales. No se puede deshacer.", color = OnPrimary) },
            confirmButton = {
                TextButton(onClick = { viewModel.borrarHistorial(); showDeleteDialog = false }) {
                    Text("Borrar todo", color = Tertiary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = OnPrimary) }
            }
        )
    }

    // Detalle de Rutina
    rutinaSeleccionada?.let { rutina ->
        AlertDialog(
            onDismissRequest = { rutinaSeleccionada = null },
            containerColor = Secondary,
            title = {
                Text("Detalle de la Rutina", color = OnPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {

                    Text(rutina.fechaFormateada, color = PrimaryContainer, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))

                    Box(modifier = Modifier.heightIn(max = 300.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                            items(rutina.ejercicios) { ej ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(OnPrimaryContainer.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)).padding(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Tertiary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(ej.nombreEjercicio, color = OnPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${ej.duracionSegundos} segundos", color = Surface, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { rutinaSeleccionada = null },
                    colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                ) { Text("Cerrar", color = OnPrimary) }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Mi Progreso") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                },
                actions = {
                    if (rutinas.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Borrar todo", tint = OnSurfaceVariant)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = OnSurface, navigationIconContentColor = OnSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            // Dashboard con scroll
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Cards de resumen global
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Rutinas Completadas",
                            value = rutinas.size.toString(),
                            icon = Icons.Default.EmojiEvents, color = Tertiary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Minutos Totales",
                            value = resumen.tiempoTotalMinutos.toString(),
                            icon = Icons.Default.Timer, color = OnPrimary
                        )
                    }
                }

                // Gráfica semanal
                if (statsSemana.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Secondary)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Últimos 7 Días",
                                    fontWeight = FontWeight.Bold,
                                    color = OnPrimary,
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .padding(bottom = 16.dp)
                                )
                                GraficaSemanal(statsSemana)
                            }
                        }
                    }
                }

                // Mensaje motivacional
                if (mensajeMotivacional.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OnSurface)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(mensajeMotivacional, color = OnSurface, fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                }

                // Lista de rutinas
                item {
                    Text("Historial Detallado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OnSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (rutinas.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Aún no tienes actividad registrada.", fontSize = 18.sp, color = OnSurface)
                        }
                    }
                } else {
                    items(rutinas) { rutina ->
                        RutinaItemView(rutina = rutina, onClick = { rutinaSeleccionada = rutina })
                    }
                }
            }
        }
    }
}

@Composable
fun GraficaSemanal(stats: List<DiaStat>) {
    val maxMinutos = stats.maxOf { it.minutosTotales }.coerceAtLeast(1)
    val alturaMaximaGrafica = 120.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(alturaMaximaGrafica + 40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        stats.forEach { stat ->
            val heightPercent by animateFloatAsState(
                targetValue = stat.minutosTotales.toFloat() / maxMinutos.toFloat(),
                animationSpec = tween(durationMillis = 1000), label = "grafica_anim"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (stat.minutosTotales > 0) {
                            Text(
                                text = stat.minutosTotales.toString(),
                                fontSize = 10.sp,
                                color = OnPrimary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(alturaMaximaGrafica * heightPercent)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(if (stat.minutosTotales > 0) Tertiary else PrimaryContainer.copy(alpha = 0.2f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stat.nombreDia,
                    fontSize = 12.sp,
                    color = OnPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Secondary)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 14.sp, color = color, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RutinaItemView(rutina: RutinaHistorial, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OnPrimary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = OnSurface)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Rutina Completada", fontWeight = FontWeight.Bold, color = OnPrimary, fontSize = 16.sp)
                Text(text = rutina.fechaFormateada, fontSize = 12.sp, color = PrimaryContainer)
                Text(text = "${rutina.ejercicios.size} ejercicios realizados", fontSize = 12.sp, color = Surface, modifier = Modifier.padding(top = 4.dp))
            }

            Column(horizontalAlignment = Alignment.End) {
                val minutos = rutina.duracionTotalSegundos / 60
                val segundos = rutina.duracionTotalSegundos % 60
                val tiempoTexto = if (minutos > 0) "${minutos}m ${segundos}s" else "${segundos}s"

                Text(text = tiempoTexto, fontWeight = FontWeight.Bold, color = Tertiary, fontSize = 14.sp)
            }
        }
    }
}