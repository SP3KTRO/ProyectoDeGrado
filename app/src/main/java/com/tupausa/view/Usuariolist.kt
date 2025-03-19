package com.tupausa.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tupausa.model.Usuario
import com.tupausa.ui.theme.TuPausaTheme
import com.tupausa.viewModel.TuPausaViewModel

// Actividad principal que maneja la pantalla de la lista de usuarios
class UsuarioListActivity : ComponentActivity() {

    // Obtén una instancia del ViewModel
    private val viewModel: TuPausaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configura la UI con Jetpack Compose
        setContent {
            TuPausaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pasa el ViewModel al Composable
                    UsuarioList(viewModel = viewModel)
                }
            }
        }
    }
}

// Composable que muestra la lista de usuarios
@Composable
fun UsuarioList(viewModel: TuPausaViewModel) {
    // Observa los usuarios desde el ViewModel usando observeAsState
    val usuarios by viewModel.usuarios.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    // Llama a la función para obtener los usuarios desde la API
    LaunchedEffect(Unit) {
        viewModel.fetchUsuariosFromApi()
    }

    // Muestra un Snackbar si hay un error
    if (!error.isNullOrEmpty()) {
        ErrorSnackbar(error = error!!, onRetry = { viewModel.fetchUsuariosFromApi() })
    }

    // Muestra un indicador de carga si los datos se están cargando
    if (isLoading) {
        LoadingIndicator()
    } else if (usuarios.isEmpty()) {
        // Muestra un mensaje si la lista de usuarios está vacía
        EmptyListMessage()
    } else {
        // Muestra la lista de usuarios
        UsuarioListContent(usuarios = usuarios)
    }
}

// Composable que muestra un indicador de carga
@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// Composable que muestra un mensaje si la lista está vacía
@Composable
fun EmptyListMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No hay usuarios disponibles",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Composable que muestra la lista de usuarios
@Composable
fun UsuarioListContent(usuarios: List<Usuario>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(usuarios) { usuario ->
            UsuarioItem(usuario = usuario)
        }
    }
}

// Composable que muestra un ítem de usuario en la lista
@Composable
fun UsuarioItem(usuario: Usuario) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nombre: ${usuario.nombre}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Correo: ${usuario.correoElectronico}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Contraseña: ${usuario.contrasena}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tipo de Usuario: ${usuario.idTipoUsuario}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ID de Usuario: ${usuario.idUsuario}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Composable que muestra un Snackbar de error
@Composable
fun ErrorSnackbar(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
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
