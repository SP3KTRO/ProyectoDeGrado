package com.tupausa.repository

import com.tupausa.database.ApiService
import com.tupausa.database.DatabaseHelper
import com.tupausa.model.Ejercicio
import com.tupausa.model.HistorialRegistro
import com.tupausa.model.HistorialS3
import com.tupausa.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistorialRepository(
    private val dbHelper: DatabaseHelper,
    private val apiService: ApiService
) {
    // Historial Usuario
    suspend fun insertarHistorial(idUsuario: Int, idEjercicio: Int, duracion: Int, tipo: String = "MANUAL", rutaEvidencia: String? = null) = withContext(Dispatchers.IO) {
        dbHelper.insertarHistorial(idUsuario, idEjercicio, duracion, tipo, rutaEvidencia)
    }
    suspend fun obtenerHistorial(idUsuario: Int): List<HistorialRegistro> = withContext(Dispatchers.IO) {
        dbHelper.obtenerHistorialPorUsuario(idUsuario)
    }
    suspend fun obtenerTotalPausas(idUsuario: Int): Int = withContext(Dispatchers.IO) {
        dbHelper.obtenerTotalPausas(idUsuario)
    }
    suspend fun obtenerTiempoTotalMinutos(idUsuario: Int): Int = withContext(Dispatchers.IO) {
        dbHelper.obtenerTiempoTotalMinutos(idUsuario)
    }
    suspend fun borrarHistorial(idUsuario: Int) = withContext(Dispatchers.IO) {
        dbHelper.borrarTodoElHistorial(idUsuario)
    }

    // Historial Admin
    suspend fun obtenerUsuariosRemotos(): List<Usuario> = withContext(Dispatchers.IO) {
        val response = apiService.getUsuarios()
        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
    suspend fun obtenerHistorialRemoto(idUsuario: Int): List<HistorialS3> = withContext(Dispatchers.IO) {
        val response = apiService.getHistorial(idUsuario)
        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
    suspend fun obtenerEjerciciosRemotos(): List<Ejercicio> = withContext(Dispatchers.IO) {
        val response = apiService.getEjercicios()
        if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
}
