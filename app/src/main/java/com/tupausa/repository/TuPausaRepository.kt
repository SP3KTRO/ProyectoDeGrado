package com.tupausa.repository

import android.content.ContentValues
import android.util.Log
import com.tupausa.database.DatabaseHelper
import com.tupausa.database.RetrofitClient
import com.tupausa.model.Ejercicio
import com.tupausa.model.PausaActiva
import com.tupausa.model.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TuPausaRepository(private val dbHelper: DatabaseHelper) {

    private val apiService = RetrofitClient.instance
    private val TAG = "TuPausaRepository" // Etiqueta para los logs

    // Insertar un usuario localmente
    fun insertUsuarioLocal(usuario: Usuario): Long {
        val db = dbHelper.writableDatabase

        // Verificar si el usuario ya existe
        val cursor = db.query(
            "Usuarios",
            arrayOf("id_usuario"),
            "id_usuario = ?",
            arrayOf(usuario.idUsuario.toString()),
            null, null, null
        )

        return if (cursor.count > 0) {
            // El usuario ya existe, no se inserta
            Log.d(TAG, "Usuario con ID ${usuario.idUsuario} ya existe en la base de datos local.")
            cursor.close()
            -1
        } else {
            // Insertar el usuario
            val values = ContentValues().apply {
                put("id_usuario", usuario.idUsuario)
                put("nombre", usuario.nombre)
                put("correo_electronico", usuario.correoElectronico)
                put("contrasena", usuario.contrasena)
                put("id_tipo_usuario", usuario.idTipoUsuario)
            }
            cursor.close()
            val result = db.insert("Usuarios", null, values)
            if (result != -1L) {
                Log.d(TAG, "Usuario con ID ${usuario.idUsuario} guardado en la base de datos local.")
            } else {
                Log.e(TAG, "Error al guardar el usuario con ID ${usuario.idUsuario} en la base de datos local.")
            }
            result
        }
    }

    // Obtener todos los usuarios desde la API y guardarlos en la base de datos local
    fun getAllUsuariosFromApi(callback: (List<Usuario>) -> Unit) {
        Log.d(TAG, "Iniciando llamada a la API para obtener usuarios...")
        apiService.getUsuarios().enqueue(object : Callback<List<List<Any>>> {
            override fun onResponse(call: Call<List<List<Any>>>, response: Response<List<List<Any>>>) {
                if (response.isSuccessful) {
                    val usuarios = response.body()?.mapNotNull { subarray ->
                        try {
                            Usuario(
                                idUsuario = (subarray[0] as Double).toInt(), // Convierte Double a Int
                                nombre = subarray[1] as String,
                                correoElectronico = subarray[2] as String,
                                contrasena = subarray[3] as String,
                                idTipoUsuario = (subarray[4] as Double).toInt() // Convierte Double a Int
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al convertir subarray a Usuario: ${e.message}")
                            null // Si hay un error en el parsing, ignora este usuario
                        }
                    } ?: emptyList()

                    Log.d(TAG, "Llamada a la API exitosa. Se obtuvieron ${usuarios.size} usuarios.")

                    // Guardar los usuarios en la base de datos local
                    usuarios.forEach { usuario ->
                        val result = insertUsuarioLocal(usuario)
                        if (result == -1L) {
                            Log.w(TAG, "El usuario con ID ${usuario.idUsuario} no se guardó en la base de datos local (posible duplicado).")
                        }
                    }

                    // Devolver los usuarios a través del callback
                    callback(usuarios)
                } else {
                    Log.e(TAG, "Error en la respuesta de la API: ${response.code()} - ${response.message()}")
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<List<List<Any>>>, t: Throwable) {
                Log.e(TAG, "Error en la llamada a la API: ${t.message}")
                callback(emptyList())
            }
        })
    }

    // Obtener todos los usuarios localmente
    fun getAllUsuariosLocal(): List<Usuario> {
        Log.d(TAG, "Obteniendo usuarios desde la base de datos local...")
        val db = dbHelper.readableDatabase
        val cursor = db.query("Usuarios", null, null, null, null, null, null)
        val usuarios = mutableListOf<Usuario>()
        with(cursor) {
            while (moveToNext()) {
                val idUsuario = getInt(getColumnIndexOrThrow("id_usuario"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val correoElectronico = getString(getColumnIndexOrThrow("correo_electronico"))
                val contrasena = getString(getColumnIndexOrThrow("contrasena"))
                val idTipoUsuario = getInt(getColumnIndexOrThrow("id_tipo_usuario"))
                usuarios.add(Usuario(idUsuario, nombre, correoElectronico, contrasena, idTipoUsuario))
            }
        }
        cursor.close()
        Log.d(TAG, "Se obtuvieron ${usuarios.size} usuarios desde la base de datos local.")
        return usuarios
    }

    // Insertar una pausa activa localmente
    fun insertPausaActivaLocal(pausaActiva: PausaActiva): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id_usuario", pausaActiva.idUsuario)
            put("fecha", pausaActiva.fecha)
            put("hora", pausaActiva.hora)
            put("duracion", pausaActiva.duracion)
            put("id_estado_pausa", pausaActiva.idEstadoPausa)
        }
        val result = db.insert("Pausas_activas", null, values)
        if (result != -1L) {
            Log.d(TAG, "Pausa activa con ID ${pausaActiva.idPausa} guardada en la base de datos local.")
        } else {
            Log.e(TAG, "Error al guardar la pausa activa con ID ${pausaActiva.idPausa} en la base de datos local.")
        }
        return result
    }

    // Obtener todas las pausas activas localmente
    fun getAllPausasActivasLocal(): List<PausaActiva> {
        Log.d(TAG, "Obteniendo pausas activas desde la base de datos local...")
        val db = dbHelper.readableDatabase
        val cursor = db.query("Pausas_activas", null, null, null, null, null, null)
        val pausasActivas = mutableListOf<PausaActiva>()
        with(cursor) {
            while (moveToNext()) {
                val idPausa = getInt(getColumnIndexOrThrow("id_pausa"))
                val idUsuario = getInt(getColumnIndexOrThrow("id_usuario"))
                val fecha = getString(getColumnIndexOrThrow("fecha"))
                val hora = getString(getColumnIndexOrThrow("hora"))
                val duracion = getInt(getColumnIndexOrThrow("duracion"))
                val idEstadoPausa = getInt(getColumnIndexOrThrow("id_estado_pausa"))
                pausasActivas.add(PausaActiva(idPausa, idUsuario, fecha, hora, duracion, idEstadoPausa))
            }
        }
        cursor.close()
        Log.d(TAG, "Se obtuvieron ${pausasActivas.size} pausas activas desde la base de datos local.")
        return pausasActivas
    }

    // Insertar un ejercicio localmente
    fun insertEjercicioLocal(ejercicio: Ejercicio): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id_nombre_ejer", ejercicio.idNombreEjer)
            put("id_descripcion", ejercicio.idDescripcion)
            put("id_tipo_ejercicio", ejercicio.idTipoEjercicio)
            put("id_nivel_intensidad", ejercicio.idNivelIntensidad)
        }
        val result = db.insert("Ejercicios", null, values)
        if (result != -1L) {
            Log.d(TAG, "Ejercicio con ID ${ejercicio.idEjercicio} guardado en la base de datos local.")
        } else {
            Log.e(TAG, "Error al guardar el ejercicio con ID ${ejercicio.idEjercicio} en la base de datos local.")
        }
        return result
    }

    // Obtener todos los ejercicios localmente
    fun getAllEjerciciosLocal(): List<Ejercicio> {
        Log.d(TAG, "Obteniendo ejercicios desde la base de datos local...")
        val db = dbHelper.readableDatabase
        val cursor = db.query("Ejercicios", null, null, null, null, null, null)
        val ejercicios = mutableListOf<Ejercicio>()
        with(cursor) {
            while (moveToNext()) {
                val idEjercicio = getInt(getColumnIndexOrThrow("id_ejercicio"))
                val idNombreEjer = getInt(getColumnIndexOrThrow("id_nombre_ejer"))
                val idDescripcion = getInt(getColumnIndexOrThrow("id_descripcion"))
                val idTipoEjercicio = getInt(getColumnIndexOrThrow("id_tipo_ejercicio"))
                val idNivelIntensidad = getInt(getColumnIndexOrThrow("id_nivel_intensidad"))
                ejercicios.add(Ejercicio(idEjercicio, idNombreEjer, idDescripcion, idTipoEjercicio, idNivelIntensidad))
            }
        }
        cursor.close()
        Log.d(TAG, "Se obtuvieron ${ejercicios.size} ejercicios desde la base de datos local.")
        return ejercicios
    }
}
