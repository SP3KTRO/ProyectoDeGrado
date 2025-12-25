package com.tupausa.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// MODELOS DE USUARIO

data class TipoUsuario(
    @SerializedName("id_tipo_usuario") val idTipoUsuario: Int,
    @SerializedName("tipo") val tipo: String
) : Serializable

data class Usuario(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("correo_electronico") val correoElectronico: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("id_tipo_usuario") val idTipoUsuario: Int, // Default 1 (Estudiante)
) : Serializable

// MODELOS DE NEGOCIO

data class Ejercicio(
    @SerializedName("id_ejercicio") val idEjercicio: Int,
    @SerializedName("nombre_ejercicio") val nombreEjercicio: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("tipo_ejercicio") val tipoEjercicio: String,
    @SerializedName("nivel_intensidad") val nivelIntensidad: String,
    @SerializedName("url_imagen_guia") val urlImagenGuia: String?,
    @SerializedName("duracion_segundos") val duracionSegundos: Int,
    @SerializedName("instrucciones") val instrucciones: String?,
    @SerializedName("beneficios") val beneficios: String?
) : Serializable {

    fun getInstruccionesList(): List<String> {
        return instrucciones?.split("|")?.filter { it.isNotBlank() } ?: emptyList()
    }

    fun getTipoDisplayName(): String = tipoEjercicio
    fun getNivelDisplayName(): String = nivelIntensidad
}

/*data class Alarma(
    @SerializedName("id_alarma") val idAlarma: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("hora_programada") val horaProgramada: String,
    @SerializedName("dias_activos") val diasActivos: String,
    @SerializedName("estado_alarma") val estadoAlarma: String,
    @SerializedName("duracion_estimada") val duracionEstimada: Int?,
    @SerializedName("etiqueta") val etiqueta: String?
) : Serializable*/

data class ConfigNotificacion(
    @SerializedName("id_notificacion") val idNotificacion: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("nombre_tono") val nombreTono: String?,
    @SerializedName("usuario") val usuario: Usuario?
) : Serializable

data class HistorialEjecucion(
    @SerializedName("id_registro") val idRegistro: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("id_ejercicio") val idEjercicio: Int,
    @SerializedName("fecha_realizacion") val fechaRealizacion: Int,
    @SerializedName("hora_inicio") val horaInicio: String = "",
    @SerializedName("hora_fin") val horaFin: String = "",
    @SerializedName("duracion_real_seg") val duracionRaalSeg: Int,
    @SerializedName("se_detecto_movimiento") val seDetectoMovimiento: Int,
    @SerializedName("tipo_deteccion_usado") val tipoDeteccionUsado: String?,
    @SerializedName("sincronizado") val sincronizado: Int,
    @SerializedName("ejercicio") val ejercicio: Ejercicio?,
    @SerializedName("usuario") val usuario: Usuario?
) : Serializable

/*data class EstadisticasResumen(
    @SerializedName("id_estadistica") val idEstadistica: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("total_pausas_realizadas") val totalPausas: Int,
    @SerializedName("tiempo_total_minutos") val tiempoTotalMinutos: Double,
    @SerializedName("racha_dias_consecutivos") val rachaDias: Int,
    @SerializedName("porcentaje_cumplimiento") val porcentajeCumplimiento: Double
) : Serializable*/