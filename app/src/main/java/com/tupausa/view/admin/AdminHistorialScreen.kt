package com.tupausa.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tupausa.R
import com.tupausa.model.RutinaS3
import com.tupausa.model.Usuario
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
import com.tupausa.viewModel.AdminHistorialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHistorialScreen(
    onBack: () -> Unit,
    viewModel: AdminHistorialViewModel
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val ejercicios by viewModel.ejercicios.collectAsState()
    val rutinas by viewModel.rutinasUsuarioSeleccionado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var rutinaS3Seleccionada by remember { mutableStateOf<RutinaS3?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarUsuarios()
        viewModel.cargarEjercicios()
    }

    // Detalle de Rutina Admin
    rutinaS3Seleccionada?.let { rutina ->
        AlertDialog(
            onDismissRequest = { rutinaS3Seleccionada = null },
            containerColor = Secondary,
            title = {
                Text("Detalle de Rutina (Nube)",
                    color = OnPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Fecha: ${rutina.fecha}", color = OnPrimary, fontSize = 14.sp)
                    Text("Duración: ${rutina.horaInicio} - ${rutina.horaFin}", color = Surface, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))

                    Box(modifier = Modifier.heightIn(max = 300.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(rutina.ejercicios) { ej ->
                                val nombreEjercicio = ejercicios.find { it.idEjercicio == ej.idEjercicio }?.nombreEjercicio ?: "Ejercicio #${ej.idEjercicio}"

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(OnPrimaryContainer.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)).padding(8.dp)
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Surface, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(nombreEjercicio, color = OnPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${ej.horaInicio} a ${ej.horaFin} • ${ej.tipoDeteccion ?: "N/A"}", color = PrimaryContainer, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { rutinaS3Seleccionada = null },
                    colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                ) { Text("Cerrar", color = OnPrimary) }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(
                    text = usuarioSeleccionado?.nombre ?: "Historial Global",
                    color = OnSurface
                ) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (usuarioSeleccionado != null) {
                            usuarioSeleccionado = null
                            viewModel.limpiarHistorial()
                        } else {
                            onBack()
                        }
                    }) {
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = OnPrimaryContainer
                )
            } else if (usuarioSeleccionado == null) {

                // Lista de usuarios
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(usuarios) { usuario ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                usuarioSeleccionado = usuario
                                viewModel.cargarHistorialUsuario(usuario.idUsuario)
                            },
                            colors = CardDefaults.cardColors(containerColor = Secondary)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(painter = painterResource(id = R.drawable.user), contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Unspecified)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = usuario.nombre, fontWeight = FontWeight.Bold, color = OnPrimary)
                                    Text(text = usuario.correoElectronico, fontSize = 12.sp, color = Surface)
                                }
                            }
                        }
                    }
                }
            } else {
                // Historial de usuarios
                if (rutinas.isEmpty()) {
                    Text("No hay registros en la nube para este usuario.",
                        fontSize = 18.sp, color = OnSurface, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(rutinas) { rutina ->
                            AdminRutinaItemView(
                                rutina = rutina,
                                onClick = { rutinaS3Seleccionada = rutina }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRutinaItemView(rutina: RutinaS3, onClick: () -> Unit) {
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
                color = OnSecondary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = OnSurface)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Rutina ${rutina.idRutina}",
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = "${rutina.fecha} • ${rutina.horaInicio} - ${rutina.horaFin}",
                    fontSize = 12.sp,
                    color = PrimaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${rutina.ejercicios.size} ejercicios",
                    fontWeight = FontWeight.Bold,
                    color = Tertiary,
                    fontSize = 14.sp
                )
                Text(
                    text = rutina.tipoDeteccion,
                    fontSize = 10.sp,
                    color = Surface
                )
            }
        }
    }
}