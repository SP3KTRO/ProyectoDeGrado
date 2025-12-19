package com.tupausa.database

import com.tupausa.model.*
import com.tupausa.model.data.Alarma
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // USUARIOS
    @GET("usuarios")
    suspend fun getUsuarios(): Response<List<Usuario>>

    @POST("usuarios")
    suspend fun createUsuario(@Body usuario: Usuario): Response<Map<String, String>>

    @PUT("usuarios")
    suspend fun updateUsuario(@Body usuario: Usuario): Response<Map<String, String>>

    // Usamos @HTTP para poder enviar body en un DELETE (Requerido por tu Lambda)
    @HTTP(method = "DELETE", path = "usuarios", hasBody = true)
    suspend fun deleteUsuario(@Body body: Map<String, Int>): Response<Map<String, String>>


    // EJERCICIOS

    @GET("ejercicios")
    suspend fun getEjercicios(): Response<List<Ejercicio>>

    @POST("ejercicios")
    suspend fun createEjercicio(@Body ejercicio: Ejercicio): Response<Map<String, String>>

    @PUT("ejercicios")
    suspend fun updateEjercicio(@Body ejercicio: Ejercicio): Response<Map<String, String>>

    @HTTP(method = "DELETE", path = "ejercicios", hasBody = true)
    suspend fun deleteEjercicio(@Body body: Map<String, Int>): Response<Map<String, String>>

    // ALARMAS

    @GET("alarmas")
    suspend fun getAlarmas(@Query("id_usuario") idUsuario: Int): Response<List<Alarma>>

    @POST("alarmas")
    suspend fun createAlarma(@Body alarma: Alarma): Response<Map<String, String>>

    @PUT("alarmas")
    suspend fun updateAlarma(@Body alarma: Alarma): Response<Map<String, String>>

    @HTTP(method = "DELETE", path = "alarmas", hasBody = true)
    suspend fun deleteAlarma(@Body body: Map<String, Int>): Response<Map<String, String>>


    // HISTORIAL (Registrar Pausas)

    @GET("historial")
    suspend fun getHistorial(@Query("id_usuario") idUsuario: Int): Response<List<HistorialEjecucion>>

    @POST("historial")
    suspend fun registrarPausa(@Body historial: HistorialEjecucion): Response<Map<String, String>>
}