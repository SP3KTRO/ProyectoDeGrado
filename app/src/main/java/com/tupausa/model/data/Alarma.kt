package com.tupausa.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_alarmas")
data class Alarma(
    @PrimaryKey(autoGenerate = true)
    // ID local en el celular
    val id: Int = 0,
    val idsEjercicios: List<Int>,
    val hora: Int,
    val minuto: Int,
    val diasRepeticion: List<Int>,
    val activa: Boolean = true,
    val etiqueta: String = "Pausa Activa",
    val duracionSegundos: Int = 60,
    val tipoEjercicio: String = "ALEATORIO",
    val tonoAlarma: String = "Predeterminado",
    // ID del servidor para sincronizar después
    val idRemoto: Int? = null
)
