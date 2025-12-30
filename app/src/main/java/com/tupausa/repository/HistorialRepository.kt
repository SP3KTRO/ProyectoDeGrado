package com.tupausa.repository

import com.tupausa.database.DatabaseHelper
import com.tupausa.model.HistorialRegistro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistorialRepository(private val dbHelper: DatabaseHelper) {

    // 1. Guardar
    suspend fun insertarHistorial(idUsuario: Int, idEjercicio: Int, duracion: Int, tipo: String = "MANUAL", rutaEvidencia: String? = null) = withContext(Dispatchers.IO) {
        dbHelper.insertarHistorial(idUsuario, idEjercicio, duracion, tipo, rutaEvidencia)
    }

    // 2. Leer Lista
    suspend fun obtenerHistorial(idUsuario: Int): List<HistorialRegistro> = withContext(Dispatchers.IO) {
        dbHelper.obtenerHistorialPorUsuario(idUsuario)
    }

    // 3. Leer Estadísticas
    suspend fun obtenerTotalPausas(idUsuario: Int): Int = withContext(Dispatchers.IO) {
        dbHelper.obtenerTotalPausas(idUsuario)
    }

    suspend fun obtenerTiempoTotalMinutos(idUsuario: Int): Int = withContext(Dispatchers.IO) {
        dbHelper.obtenerTiempoTotalMinutos(idUsuario)
    }
}
