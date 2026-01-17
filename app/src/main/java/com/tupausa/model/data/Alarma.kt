package com.tupausa.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_alarmas")
data class Alarma(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // ID local en el celular

    val idsEjercicios: List<Int>, // Ahora guardamos una lista de IDs para la rutina
    val idEjercicio: Int? = null, // ID del ejercicio actual

    // Guardamos hora y minuto separados para programar fácil el AlarmManager
    val hora: Int,
    val minuto: Int,

    // Tu formato de servidor (ej: "Lunes,Martes") lo convertiremos a lista después
    // O usamos el Converter que creamos para guardar la lista de días
    val diasRepeticion: List<Int>,

    val activa: Boolean = true,
    val etiqueta: String = "Pausa Activa",
    val duracionSegundos: Int = 60,
    val tipoEjercicio: String = "ALEATORIO",

    // Opcional: ID del servidor para sincronizar después
    val idRemoto: Int? = null
)
