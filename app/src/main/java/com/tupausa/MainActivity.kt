package com.tupausa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.tupausa.ui.theme.TuPausaTheme
import com.tupausa.viewModel.*
import com.tupausa.viewModel.appNavigation.*


class MainActivity : ComponentActivity() {

    private val app by lazy { application as TuPausaApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TuPausaTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // CAPA 1 BACK
                    Image(
                        painter = painterResource(id = R.drawable.fondo),
                        contentDescription = "Fondo de la app",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // CAPA 2 FRONT
                    val usuarioViewModel: UsuarioViewModel by viewModels {
                        UsuarioViewModelFactory(app.usuarioRepository)
                    }
                    val loginViewModel: LoginViewModel by viewModels()
                    val ejercicioViewModel: EjercicioViewModel by viewModels()

                    AppNavigation(
                        usuarioViewModel = usuarioViewModel,
                        loginViewModel = loginViewModel,
                        ejercicioViewModel = ejercicioViewModel
                    )
                }
            }
        }
    }
}
