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
import com.tupausa.utils.SyncWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {

    private val app by lazy { application as TuPausaApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. PROGRAMAR LA TAREA DE FONDO (WORKER)
        // Esto iniciará el ciclo para subir datos a S3 cuando haya internet
        programarSincronizacion()

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

    // --- FUNCIÓN PARA CONFIGURAR LA SINCRONIZACIÓN ---
    private fun programarSincronizacion() {
        // Restricción: Solo ejecutar si hay conexión a Internet
        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Configurar: Ejecutar cada 1 hora (Intervalo mínimo permitido por Android es 15 min)
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(restricciones)
            .build()

        // Encolar: Usamos 'KEEP' para que si ya existe una tarea programada,
        // no la duplique ni la reinicie, simplemente mantenga la existente.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SincronizacionHistorial",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
