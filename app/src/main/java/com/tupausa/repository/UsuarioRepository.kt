package com.tupausa.repository

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.tupausa.database.ApiService
import com.tupausa.database.DatabaseHelper
import com.tupausa.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(
    private val dbHelper: DatabaseHelper,
    private val apiService: ApiService
) {

    companion object {
        private const val TAG = "TuPausaRepository"
    }

    // LOGIN - Validar con API

    suspend fun login(email: String, password: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsuarios().execute()

            if (response.isSuccessful) {
                val usuarios = response.body()?.mapNotNull { subarray ->
                    try {
                        Usuario(
                            idUsuario = (subarray[0] as Double).toInt(),
                            nombre = subarray[1] as String,
                            correoElectronico = subarray[2] as String,
                            contrasena = subarray[3] as String,
                            idTipoUsuario = (subarray[4] as Double).toInt()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing usuario: ${e.message}")
                        null
                    }
                } ?: emptyList()

                val usuario = usuarios.find {
                    it.correoElectronico == email && it.contrasena == password
                }

                if (usuario != null) {
                    // Guardar usuario en BD local para caché
                    saveUsuarioLocal(usuario)
                    Result.success(usuario)
                } else {
                    Result.failure(Exception("Credenciales incorrectas"))
                }
            } else {
                Result.failure(Exception("Error del servidor: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login: ${e.message}")
            Result.failure(e)
        }
    }

    // REGISTRO - Crear usuario en API

    suspend fun register(usuario: Usuario): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createUsuario(usuario).execute()

            if (response.isSuccessful) {
                val nuevoUsuario = response.body()
                if (nuevoUsuario != null) {
                    saveUsuarioLocal(nuevoUsuario)
                    Result.success(nuevoUsuario)
                } else {
                    Result.failure(Exception("Error al crear usuario"))
                }
            } else {
                val errorMsg = when (response.code()) {
                    409 -> "El correo ya está registrado"
                    400 -> "Datos inválidos"
                    else -> "Error del servidor: ${response.message()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en registro: ${e.message}")
            Result.failure(e)
        }
    }

    // ADMIN: Obtener todos los usuarios de la API

    suspend fun getAllUsuariosFromApi(): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsuarios().execute()

            if (response.isSuccessful) {
                val usuarios = response.body()?.mapNotNull { subarray ->
                    try {
                        Usuario(
                            idUsuario = (subarray[0] as Double).toInt(),
                            nombre = subarray[1] as String,
                            correoElectronico = subarray[2] as String,
                            contrasena = subarray[3] as String,
                            idTipoUsuario = (subarray[4] as Double).toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                Result.success(usuarios)
            } else {
                Result.failure(Exception("Error: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuarios: ${e.message}")
            Result.failure(e)
        }
    }

    // OBTENER USUARIOS DE LA BD LOCAL

    suspend fun getAllUsuariosLocal(): List<Usuario> = withContext(Dispatchers.IO) {
        val usuarios = mutableListOf<Usuario>()
        val db = dbHelper.readableDatabase
        val cursor = db.query("Usuarios", null, null, null, null, null, "nombre ASC")
        if (cursor.moveToFirst()) {
            do {
                usuarios.add(cursorToUsuario(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        Log.d(TAG, "Usuarios locales obtenidos: ${usuarios.size}")
        return@withContext usuarios
    }

    private fun cursorToUsuario(cursor: Cursor): Usuario {
        return Usuario(
            idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario")),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
            correoElectronico = cursor.getString(cursor.getColumnIndexOrThrow("correo_electronico")),
            contrasena = cursor.getString(cursor.getColumnIndexOrThrow("contrasena")),
            idTipoUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("id_tipo_usuario"))
        )
    }

    // GUARDAR USUARIO LOCAL (CACHÉ)

     fun saveUsuarioLocal(usuario: Usuario) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id_usuario", usuario.idUsuario)
            put("nombre", usuario.nombre)
            put("correo_electronico", usuario.correoElectronico)
            put("contrasena", usuario.contrasena)
            put("id_tipo_usuario", usuario.idTipoUsuario)
        }

        db.insertWithOnConflict(
            "Usuarios",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        Log.d(TAG, "Usuario guardado localmente: ${usuario.nombre}")
    }
}