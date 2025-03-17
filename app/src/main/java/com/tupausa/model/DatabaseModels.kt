package com.tupausa.model

// Entidad TipoUsuario
data class TipoUsuario(
    val idTipoUsuario: Int,
    val tipo: String
)

// Entidad Usuario
data class Usuario(
    val idUsuario: Int,
    val nombre: String,
    val correoElectronico: String,
    val contrasena: String,
    val idTipoUsuario: Int
)

// Entidad EstadoPausa
data class EstadoPausa(
    val idEstadoPausa: Int,
    val estado: String
)

// Entidad PausaActiva
data class PausaActiva(
    val idPausa: Int,
    val idUsuario: Int,
    val fecha: String,
    val hora: String,
    val duracion: Int,
    val idEstadoPausa: Int
)

// Entidad NombreEjercicio
data class NombreEjercicio(
    val idNombreEjer: Int,
    val nombreEjercicio: String
)

// Entidad DescripcionEjercicio
data class DescripcionEjercicio(
    val idDescripcion: Int,
    val descripcion: String
)

// Entidad TipoEjercicio
data class TipoEjercicio(
    val idTipoEjercicio: Int,
    val tipo: String
)

// Entidad NivelIntensidad
data class NivelIntensidad(
    val idNivelIntensidad: Int,
    val nivel: String
)

// Entidad Ejercicio
data class Ejercicio(
    val idEjercicio: Int,
    val idNombreEjer: Int,
    val idDescripcion: Int,
    val idTipoEjercicio: Int,
    val idNivelIntensidad: Int
)

// Entidad RegistroPausa
data class RegistroPausa(
    val idRegistro: Int,
    val idPausa: Int,
    val fechaRealizacion: String,
    val horaInicio: String,
    val horaFin: String
)

// Entidad TipoDeteccion
data class TipoDeteccion(
    val idTipoDeteccion: Int,
    val tipo: String
)

// Entidad DeteccionMovimientos
data class DeteccionMovimientos(
    val idMovimiento: Int,
    val idRegistro: Int,
    val fecha: String,
    val idTipoDeteccion: Int,
    val resultadoValidacion: String?,
    val video: ByteArray?
)

// Entidad HistorialPausas
data class HistorialPausas(
    val idHistorial: Int,
    val idUsuario: Int,
    val tiempoTotal: Int,
    val idTipoDeteccion: Int
)

// Entidad EstadisticasGenerales
data class EstadisticasGenerales(
    val idEstadistica: Int,
    val idUsuario: Int,
    val frecuenciasPausas: String,
    val idTipoDeteccion: Int,
    val porcentajeCumplimiento: Double
)

// Entidad FrecuenciaNotificacion
data class FrecuenciaNotificacion(
    val idFrecuencia: Int,
    val frecuencia: String
)

// Entidad HorarioNotificacion
data class HorarioNotificacion(
    val idHorario: Int,
    val horario: String
)

// Entidad TonoNotificacion
data class TonoNotificacion(
    val idTono: Int,
    val tono: String
)

// Entidad Notificacion
data class Notificacion(
    val idNotificacion: Int,
    val idUsuario: Int,
    val idFrecuencia: Int,
    val idHorario: Int,
    val idTono: Int
)
