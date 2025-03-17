package com.tupausa.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tupausa.TuPausaApplication
import com.tupausa.model.Usuario
import androidx.compose.runtime.livedata.observeAsState
import com.tupausa.ui.theme.TuPausaTheme
import com.tupausa.viewModel.TuPausaViewModel
import com.tupausa.viewmodel.TuPausaViewModelFactory

class MainActivity : ComponentActivity() {

    private val tuPausaViewModel: TuPausaViewModel by viewModels {
        TuPausaViewModelFactory((application as TuPausaApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insertar un usuario de prueba
        val usuario = Usuario(
            idUsuario = 0,
            nombre = "Juan Pérez",
            correoElectronico = "juan@example.com",
            contrasena = "password123",
            idTipoUsuario = 1
        )
        tuPausaViewModel.insertUsuario(usuario)

        // Cargar usuarios
        tuPausaViewModel.loadUsuarios()

        // Usar Jetpack Compose para la UI
        setContent {
            TuPausaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val usuarios by tuPausaViewModel.usuarios.observeAsState(emptyList())
                    UsuarioList(usuarios = usuarios)
                }
            }
        }
    }
}

