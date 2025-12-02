package com.tupausa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tupausa.ui.theme.TuPausaTheme
import com.tupausa.viewModel.EjercicioViewModel
import com.tupausa.viewModel.LoginViewModel
import com.tupausa.viewModel.UsuarioViewModel
import com.tupausa.viewModel.appNavigation.AppNavigation
import com.tupausa.viewmodel.UsuarioViewModelFactory

class MainActivity : ComponentActivity() {

    private val app by lazy { application as TuPausaApplication }

    private val tupausaViewModel: UsuarioViewModel by viewModels {
        UsuarioViewModelFactory(app.usuarioRepository)
    }

    private val loginViewModel: LoginViewModel by viewModels()
    private val ejercicioViewModel: EjercicioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TuPausaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        tupausaViewModel = tupausaViewModel,
                        loginViewModel = loginViewModel,
                        ejercicioViewModel = ejercicioViewModel
                    )
                }
            }
        }
    }
}