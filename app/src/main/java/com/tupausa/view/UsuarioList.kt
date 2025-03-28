package com.tupausa.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.tupausa.viewModel.TuPausaViewModel
import com.tupausa.model.Usuario

@Composable
fun UsuarioList(viewModel: TuPausaViewModel) {
    val usuarios by viewModel.usuarios.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUsuariosFromApi()
    }

    if (!error.isNullOrEmpty()) {
        ErrorSnackbar(error = error!!, onRetry = { viewModel.fetchUsuariosFromApi() })
    }

    if (isLoading) {
        LoadingIndicator()
    } else if (usuarios.isEmpty()) {
        /*// Mostrar la pantalla de bienvenida cuando no hay usuarios
        ScreenWelcome(onNavigateToUsuarioList)*/
    } else {
        UsuarioListContent(usuarios = usuarios)
    }
}

// Indicador de carga
@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// Lista vacía
@Composable
fun EmptyListMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "No hay usuarios disponibles", style = MaterialTheme.typography.bodyMedium)
    }
}

// Contenido de la lista de usuarios
@Composable
fun UsuarioListContent(usuarios: List<Usuario>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(usuarios) { usuario ->
            UsuarioItem(usuario = usuario)
        }
    }
}

// Ítem de usuario
@Composable
fun UsuarioItem(usuario: Usuario) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${usuario.nombre}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Correo: ${usuario.correoElectronico}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Contraseña: ${usuario.contrasena}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Tipo de Usuario: ${usuario.idTipoUsuario}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "ID de Usuario: ${usuario.idUsuario}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// Snackbar de error
@Composable
fun ErrorSnackbar(error: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                Button(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        ) {
            Text(text = error)
        }
    }
}
