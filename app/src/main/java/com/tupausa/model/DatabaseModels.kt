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
    @SerializedName("fecha") val fecha: String,
    @SerializedName("hora_inicio") val horaInicio: String,
    @SerializedName("hora_fin") val horaFin: String,
    @SerializedName("detectado") val detectado: Int,
    @SerializedName("metodo_deteccion") val metodoDeteccion: String?,
    @SerializedName("duracion") val duracion: Int
) : Serializable

data class HistorialS3(
    @SerializedName("id_registro") val idRegistro: Int,
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("id_ejercicio") val idEjercicio: Int,
    @SerializedName("fecha_realizacion") val fechaRealizacion: String?,
    @SerializedName("hora_inicio") val horaInicio: String?,
    @SerializedName("hora_fin") val horaFin: String?,
    @SerializedName("tipo_deteccion_usado") val tipoDeteccion: String?
)