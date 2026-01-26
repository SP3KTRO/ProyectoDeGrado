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
    //val rachaDias: Int
)
