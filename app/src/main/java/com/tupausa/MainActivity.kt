package com.tupausa

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tupausa.model.Usuario
import com.tupausa.ui.theme.TuPausaTheme
import com.tupausa.viewModel.TuPausaViewModel
import com.tupausa.viewmodel.TuPausaViewModelFactory

class MainActivity : ComponentActivity() {

    // Obtén una instancia del ViewModel usando el Factory
    private val tuPausaViewModel: TuPausaViewModel by viewModels {
        TuPausaViewModelFactory((application as TuPausaApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configura la UI con Jetpack Compose
        setContent {
            TuPausaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = tuPausaViewModel)
                }
            }
        }
    }
}

// Navegación de la aplicación
@Composable
fun AppNavigation(viewModel: TuPausaViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.WELCOME
    ) {
        // Pantalla de Bienvenida
        composable(AppRoutes.WELCOME) {
            ScreenWelcome(
                onNavigateToUsuarioList = {
                    navController.navigate(AppRoutes.USUARIOS_LIST)
                }
            )
        }

        // Pantalla de Lista de Usuarios
        composable(AppRoutes.USUARIOS_LIST) {
            val usuarios by viewModel.usuarios.observeAsState(emptyList())
            val isLoading by viewModel.isLoading.observeAsState(false)
            val error by viewModel.error.observeAsState()

            // Cargar usuarios desde la API al entrar en esta pantalla
            LaunchedEffect(Unit) {
                viewModel.fetchUsuariosFromApi()
            }

            UsuarioList(
                usuarios = usuarios,
                isLoading = isLoading,
                error = error,
                onRetry = { viewModel.fetchUsuariosFromApi() }
            )
        }
    }
}

// Pantalla de Bienvenida
@Composable
fun ScreenWelcome(onNavigateToUsuarioList: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido a TuPausa",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToUsuarioList) {
                Text("Ver Lista de Usuarios")
            }
        }
    }
}

// Pantalla de Lista de Usuarios
@Composable
fun UsuarioList(
    usuarios: List<Usuario>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    // Muestra un Snackbar si hay un error
    if (!error.isNullOrEmpty()) {
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

    // Muestra un indicador de carga si los datos se están cargando
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Muestra la lista de usuarios
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
}

// Ítem de Usuario
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

// Rutas de la aplicación
object AppRoutes {
    const val WELCOME = "welcome"
    const val USUARIOS_LIST = "usuarios_list"
}