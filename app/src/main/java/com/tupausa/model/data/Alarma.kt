package com.tupausa.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_alarmas")
data class Alarma(
    @PrimaryKey(autoGenerate = true)
    // ID local en el celular
    val id: Int = 0,
    // Lista de IDs para la rutina
    val idsEjercicios: List<Int>,
    // Guardamos hora y minuto separados para programar el AlarmManager
    val hora: Int,
    val minuto: Int,

    val diasRepeticion: List<Int>,
    val activa: Boolean = true,
    val etiqueta: String = "Pausa Activa",
    val duracionSegundos: Int = 60,
    val tipoEjercicio: String = "ALEATORIO",
    // ID del servidor para sincronizar después
    val idRemoto: Int? = null
)
