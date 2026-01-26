package com.tupausa

import android.app.Application
import com.tupausa.database.ApiService
import com.tupausa.database.DatabaseHelper
import com.tupausa.database.RetrofitClient
import com.tupausa.repository.UsuarioRepository
import com.tupausa.repository.EjercicioRepository
import com.tupausa.utils.PreferencesManager
import com.tupausa.repository.AlarmaRepository
import com.tupausa.repository.HistorialRepository
import com.tupausa.model.data.AppDatabase

class TuPausaApplication : Application() {

    // Base de datos SQLITE
    private val database by lazy { DatabaseHelper(this) }

    //Base de Datos (Room)
    val roomDatabase by lazy { AppDatabase.getDatabase(this) }

    val preferencesManager by lazy { PreferencesManager(this) }

    //Repositorios
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

    //Repositorio de Alarmas
    val alarmaRepository by lazy {
        AlarmaRepository(roomDatabase.alarmaDao())
    }

    //Repositorio de Historial
    val historialRepository by lazy {
        HistorialRepository(
            database,
            RetrofitClient.instance
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}
