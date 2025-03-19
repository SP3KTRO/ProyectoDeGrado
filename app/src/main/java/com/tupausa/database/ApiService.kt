package com.tupausa.database

import com.tupausa.model.TipoUsuario
import com.tupausa.model.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("usuarios")
    fun getUsuarios(): Call<List<List<Any>>>

    @POST("usuarios")
    fun createUsuario(@Body usuario: Usuario): Call<Usuario>

    @PUT("usuarios/{id}")
    fun updateUsuario(@Path("id") id: Int, @Body usuario: Usuario): Call<Usuario>

    @DELETE("usuarios/{id}")
    fun deleteUsuario(@Path("id") id: Int): Call<Void>

    @GET("tipo_usuario")
    fun getTiposUsuario(): Call<List<List<Any>>>

    @POST("tipo_usuario")
    fun createTipoUsuario(@Body tipoUsuario: TipoUsuario): Call<TipoUsuario>

    @PUT("tipo_usuario/{id}")
    fun updateTipoUsuario(@Path("id") id: Int, @Body tipoUsuario: TipoUsuario): Call<TipoUsuario>

    @DELETE("tipo_usuario/{id}")
    fun deleteTipoUsuario(@Path("id") id: Int): Call<Void>


}