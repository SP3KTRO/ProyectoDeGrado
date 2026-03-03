package com.tupausa.model


data class HistorialRegistro(
    val id: Int,
    val fecha: Long,
    val nombreEjercicio: String,
    val idEjercicio: Int,
    val duracionSegundos: Int,
    val tipoDeteccion: String,
    val rutaEvidencia: String? = null
)
data class ResumenEstadistico(
    val totalPausas: Int,
    val tiempoTotalMinutos: Int
)

data class RutinaHistorial(
    val idRutina: String,
    val timestamp: Long,
    val fechaFormateada: String,
    val duracionTotalSegundos: Int,
    val tipoDeteccion: String,
    val ejercicios: List<HistorialRegistro>
)

data class DiaStat(
    val nombreDia: String,
    val minutosTotales: Int
)

data class RutinaS3(
    val idRutina: String,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val tipoDeteccion: String,
    val ejercicios: List<HistorialS3>
)