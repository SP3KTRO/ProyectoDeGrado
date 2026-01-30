package com.tupausa.database

import com.tupausa.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Usuarios
    @GET("usuarios")
    suspend fun getUsuarios(): Response<List<Usuario>>
    @POST("usuarios")
    suspend fun createUsuario(@Body usuario: Usuario): Response<Map<String, String>>
    @PUT("usuarios")
    suspend fun updateUsuario(@Body usuario: Usuario): Response<Map<String, String>>
    @HTTP(method = "DELETE", path = "usuarios", hasBody = true)
    suspend fun deleteUsuario(@Body body: Map<String, Int>): Response<Map<String, String>>

    // Ejercicios
    @GET("ejercicios")
    suspend fun getEjercicios(): Response<List<Ejercicio>>
    @POST("ejercicios")
    suspend fun createEjercicio(@Body ejercicio: Ejercicio): Response<Map<String, String>>
    @PUT("ejercicios")
    suspend fun updateEjercicio(@Body ejercicio: Ejercicio): Response<Map<String, String>>
    @HTTP(method = "DELETE", path = "ejercicios", hasBody = true)
    suspend fun deleteEjercicio(@Body body: Map<String, Int>): Response<Map<String, String>>

    // Historial
    @GET("historial")
    suspend fun getHistorial(@Query("id_usuario") idUsuario: Int): Response<List<HistorialS3>>
    @POST("historial")
    suspend fun registrarPausa(@Body historial: HistorialEjecucion): Response<Map<String, String>>
}