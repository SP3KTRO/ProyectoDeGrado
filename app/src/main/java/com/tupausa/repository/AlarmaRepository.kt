package com.tupausa.repository

import kotlinx.coroutines.flow.Flow
import com.tupausa.model.data.Alarma
import com.tupausa.model.data.AlarmaDao

class AlarmaRepository(private val alarmaDao: AlarmaDao) {

    // Operaciones Crud
    val todasLasAlarmas: Flow<List<Alarma>> = alarmaDao.obtenerTodas()
    suspend fun insertar(alarma: Alarma): Long {
        return alarmaDao.insertar(alarma)
    }
    suspend fun actualizar(alarma: Alarma) {
        alarmaDao.actualizar(alarma)
    }
    suspend fun eliminar(alarma: Alarma) {
        alarmaDao.eliminar(alarma)
    }
}