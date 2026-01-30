package com.tupausa.view.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.tupausa.model.HistorialS3
import com.tupausa.model.Usuario
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSecondary
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.PrimaryContainer
import com.tupausa.ui.theme.Secondary
import com.tupausa.ui.theme.Surface
import com.tupausa.viewModel.AdminHistorialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHistorialScreen(
    onBack: () -> Unit,
    viewModel: AdminHistorialViewModel
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val ejercicios by viewModel.ejercicios.collectAsState()
    val historial by viewModel.historialUsuarioSeleccionado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Estado para controlar el usuario seleccionado
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarUsuarios()
        viewModel.cargarEjercicios()
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
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = OnPrimaryContainer
                )
            } else if (usuarioSeleccionado == null) {
                // LISTA DE USUARIOS
                LazyColumn(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(usuarios) { usuario ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    usuarioSeleccionado = usuario
                                    viewModel.cargarHistorialUsuario(usuario.idUsuario)
                                },
                            colors = CardDefaults.cardColors(containerColor = Secondary)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically)
                            {
                                Icon(painter = painterResource(id = R.drawable.user),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = usuario.nombre,
                                        fontWeight = FontWeight.Bold,
                                        color = OnPrimary
                                    )
                                    Text(
                                        text = usuario.correoElectronico,
                                        fontSize = 12.sp,
                                        color = Surface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Detalle del historial del usuario
                if (historial.isEmpty()) {
                    Text("No hay registros para este usuario.",
                        fontSize = 18.sp,
                        color = OnSurface,
                        modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(historial) { registro ->
                            val ejercicioEncontrado = ejercicios.find { it.idEjercicio == registro.idEjercicio }
                            val nombreAMostrar = ejercicioEncontrado?.nombreEjercicio ?: "Ejercicio #${registro.idEjercicio}"

                            AdminHistorialItemView(registro, nombreAMostrar)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminHistorialItemView(item: HistorialS3, nombreEjercicio: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Secondary)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = MaterialTheme.shapes.small,
                color = OnSecondary, modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.History,
                        contentDescription = null, tint = OnSurface
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombreEjercicio,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )
                Text(
                    text = "Fecha: ${item.fechaRealizacion ?: "N/A"}",
                    fontSize = 12.sp,
                    color = PrimaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = item.tipoDeteccion ?: "N/A", fontSize = 10.sp,
                    color = Surface
                )
                Text(text = "${item.horaInicio} - ${item.horaFin}",
                    fontSize = 10.sp, color = PrimaryContainer
                )
            }
        }
    }
}