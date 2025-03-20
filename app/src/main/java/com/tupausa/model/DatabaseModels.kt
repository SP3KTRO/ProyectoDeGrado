package com.tupausa.model

import com.google.gson.annotations.SerializedName

// Entidad TipoUsuario
data class TipoUsuario(
    val idTipoUsuario: Int,
    val tipo: String
)

// Entidad Usuario
data class Usuario(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("correo_electronico") val correoElectronico: String, // Mapea a "correo_electronico"
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("id_tipo_usuario") val idTipoUsuario: Int
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
) {
    // Sobrescribir equals()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeteccionMovimientos) return false

        // Comparar todas las propiedades
        if (idMovimiento != other.idMovimiento) return false
        if (idRegistro != other.idRegistro) return false
        if (fecha != other.fecha) return false
        if (idTipoDeteccion != other.idTipoDeteccion) return false
        if (resultadoValidacion != other.resultadoValidacion) return false

        // Comparar el contenido del ByteArray
        if (video != null) {
            if (other.video == null) return false
            if (!video.contentEquals(other.video)) return false
        } else if (other.video != null) return false

        return true
    }

    // Sobrescribir hashCode()
    override fun hashCode(): Int {
        var result = idMovimiento
        result = 31 * result + idRegistro
        result = 31 * result + fecha.hashCode()
        result = 31 * result + idTipoDeteccion
        result = 31 * result + (resultadoValidacion?.hashCode() ?: 0)
        result = 31 * result + (video?.contentHashCode() ?: 0)
        return result
    }
}

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
