package com.tupausa.repository

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.tupausa.database.ApiService
import com.tupausa.database.DatabaseHelper
import com.tupausa.model.Usuario
import com.tupausa.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(
    private val dbHelper: DatabaseHelper,
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) {

    companion object {
        private const val TAG = "UsuarioRepository"
    }

    // ==========================================
    // LOGOUT
    // ==========================================
    fun logout() {
        preferencesManager.clearUserSession()
        Log.d(TAG, "Sesión Cerrada.")
    }
    // ==========================================
    // LOGIN - Validar con API
    // ==========================================

    // ==========================================
    // LOGIN
    // ==========================================
    suspend fun login(email: String, password: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsuarios()

            if (response.isSuccessful) {
                val usuarios = response.body() ?: emptyList()
                val usuario = usuarios.find {
                    it.correoElectronico == email && it.contrasena == password
                }

                if (usuario != null) {
                    saveUsuarioLocal(usuario) // SQLite
                    preferencesManager.saveUserSession(usuario) // <--- 2. GUARDAR SESIÓN EN PREFS
                    Result.success(usuario)
                } else {
                    Result.failure(Exception("Correo o Contraseña incorrectos"))
                }
            } else {
                Result.failure(Exception("Error al conectar: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login: ${e.message}")
            Result.failure(e)
        }
    }

    // ==========================================
    // REGISTRO
    // ==========================================

    suspend fun register(usuario: Usuario): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createUsuario(usuario)

            if (response.isSuccessful) {
                saveUsuarioLocal(usuario) //SQLite
                preferencesManager.saveUserSession(usuario)
                Result.success(usuario)
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

    // ==========================================
    // ADMIN: Obtener todos los usuarios de la API
    // ==========================================

    suspend fun getAllUsuariosFromApi(): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsuarios()

            if (response.isSuccessful) {
                val usuarios = response.body() ?: emptyList()
                Result.success(usuarios)
            } else {
                Result.failure(Exception("Error: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo usuarios: ${e.message}")
            Result.failure(e)
        }
    }

    // ==========================================
    // ADMIN: Actualizar usuario (PUT)
    // ==========================================

    suspend fun updateUsuario(id: Int, usuario: Usuario): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Actualizando usuario con ID: $id")
            val response = apiService.updateUsuario(usuario)

            if (response.isSuccessful) {
                updateUsuarioLocal(usuario)
                Log.d(TAG, "Usuario actualizado exitosamente: ${usuario.nombre}")
                Result.success(usuario)
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Usuario no encontrado"
                    400 -> "Datos inválidos"
                    409 -> "El correo ya está en uso por otro usuario"
                    else -> "Error del servidor: ${response.message()}"
                }
                Log.e(TAG, "Error en respuesta: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando usuario: ${e.message}")
            Result.failure(e)
        }
    }

    // ==========================================
    // ADMIN: Eliminar usuario (DELETE)
    // ==========================================

    suspend fun deleteUsuario(id: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Eliminando usuario con ID: $id")
            val response = apiService.deleteUsuario(mapOf("id_usuario" to id))

            if (response.isSuccessful) {
                deleteUsuarioLocal(id)
                Log.d(TAG, "Usuario eliminado exitosamente")
                Result.success(true)
            } else {
                val errorMsg = when (response.code()) {
                    404 -> "Usuario no encontrado"
                    403 -> "No tienes permisos para eliminar este usuario"
                    else -> "Error del servidor: ${response.message()}"
                }
                Log.e(TAG, "Error en respuesta: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando usuario: ${e.message}")
            Result.failure(e)
        }
    }

    // ==========================================
    // OBTENER USUARIOS DE LA BD LOCAL
    // ==========================================

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

    // ==========================================
    // GUARDAR USUARIO LOCAL (CACHÉ)
    // ==========================================

    fun saveUsuarioLocal(usuario: Usuario) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            if (usuario.idUsuario > 0) {
                put("id_usuario", usuario.idUsuario)
            }
            put("nombre", usuario.nombre)
            put("correo_electronico", usuario.correoElectronico)
            put("contrasena", usuario.contrasena)
            put("id_tipo_usuario", usuario.idTipoUsuario)
        }
        db.insertWithOnConflict("Usuarios", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    // ==========================================
    // ACTUALIZAR USUARIO LOCAL
    // ==========================================

    private fun updateUsuarioLocal(usuario: Usuario) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nombre", usuario.nombre)
            put("correo_electronico", usuario.correoElectronico)
            put("contrasena", usuario.contrasena)
            put("id_tipo_usuario", usuario.idTipoUsuario)
        }

        val rowsUpdated = db.update(
            "Usuarios",
            values,
            "id_usuario = ?",
            arrayOf(usuario.idUsuario.toString())
        )

        if (rowsUpdated > 0) {
            Log.d(TAG, "Usuario actualizado en BD local: ${usuario.nombre}")
        }
    }

    // ==========================================
    // ELIMINAR USUARIO LOCAL
    // ==========================================

    private fun deleteUsuarioLocal(id: Int) {
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete(
            "Usuarios",
            "id_usuario = ?",
            arrayOf(id.toString())
        )

        if (rowsDeleted > 0) {
            Log.d(TAG, "Usuario eliminado de BD local con ID: $id")
        }
    }

    // ==========================================
    // HELPER: Cursor a Usuario
    // ==========================================

    private fun cursorToUsuario(cursor: Cursor): Usuario {
        return Usuario(
            idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario")),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
            correoElectronico = cursor.getString(cursor.getColumnIndexOrThrow("correo_electronico")),
            contrasena = cursor.getString(cursor.getColumnIndexOrThrow("contrasena")),
            idTipoUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("id_tipo_usuario"))
        )
    }
}