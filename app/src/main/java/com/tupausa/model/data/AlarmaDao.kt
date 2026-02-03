package com.tupausa.model.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Interfaz de acceso a datos (DAO)
@Dao
interface AlarmaDao {
    @Query("SELECT * FROM tabla_alarmas ORDER BY hora ASC, minuto ASC")
    fun obtenerTodas(): Flow<List<Alarma>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(alarma: Alarma): Long

    @Delete
    suspend fun eliminar(alarma: Alarma)

    @Update
    suspend fun actualizar(alarma: Alarma)
}