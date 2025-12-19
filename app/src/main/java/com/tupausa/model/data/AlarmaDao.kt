package com.tupausa.model.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmaDao {
    @Query("SELECT * FROM tabla_alarmas ORDER BY hora ASC, minuto ASC")
    fun obtenerTodas(): Flow<List<Alarma>>

    @Query("SELECT * FROM tabla_alarmas WHERE activa = 1")
    suspend fun obtenerAlarmasActivas(): List<Alarma>

    @Query("SELECT * FROM tabla_alarmas WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): Alarma?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(alarma: Alarma): Long

    @Delete
    suspend fun eliminar(alarma: Alarma)

    @Update
    suspend fun actualizar(alarma: Alarma)
}