package com.tupausa

import android.app.Application
import com.tupausa.database.DatabaseHelper
import com.tupausa.database.RetrofitClient
import com.tupausa.repository.UsuarioRepository
import com.tupausa.repository.EjercicioRepository
import com.tupausa.utils.PreferencesManager

class TuPausaApplication : Application() {

    private val database by lazy { DatabaseHelper(this) }
    val preferencesManager by lazy { PreferencesManager(this) }


    val usuarioRepository by lazy {
        UsuarioRepository(
            database,
            RetrofitClient.instance,
            preferencesManager
        )
    }

    val ejercicioRepository by lazy {
        EjercicioRepository(database)
    }

    override fun onCreate() {
        super.onCreate()
    }
}