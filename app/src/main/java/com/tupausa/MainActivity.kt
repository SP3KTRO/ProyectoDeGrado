package com.tupausa

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tupausa.model.Usuario
import com.tupausa.ui.theme.TuPausaTheme
import com.tupausa.view.UsuarioList
import com.tupausa.viewModel.TuPausaViewModel
import com.tupausa.viewModel.appNavegation.appNavegation
import com.tupausa.viewmodel.TuPausaViewModelFactory

class MainActivity : ComponentActivity() {

    private val tuPausaViewModel: TuPausaViewModel by viewModels {
        TuPausaViewModelFactory((application as TuPausaApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TuPausaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    appNavegation()
                }
            }
        }

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

