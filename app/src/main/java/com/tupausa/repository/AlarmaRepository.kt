package com.tupausa.repository

import kotlinx.coroutines.flow.Flow
import com.tupausa.model.data.Alarma
import com.tupausa.model.data.AlarmaDao


class AlarmaRepository(private val alarmaDao: AlarmaDao) {

    // Esta variable se actualiza sola en tiempo real gracias a Flow
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

    suspend fun obtenerPorId(id: Int): Alarma? {
        return alarmaDao.obtenerPorId(id)
    }

    suspend fun obtenerActivas(): List<Alarma> {
        return alarmaDao.obtenerAlarmasActivas()
    }
}