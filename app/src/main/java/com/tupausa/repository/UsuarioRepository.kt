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

    // Función para cerrar sesión
    fun logout() {
        preferencesManager.clearUserSession()
        Log.d(TAG, "Sesión Cerrada.")
    }

    // Login
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
                    preferencesManager.saveUserSession(usuario) // Guardar sesión en SharedPreferences

                    // Al hacer login, intentamos recuperar si ya había hecho el onboarding en local
                    recuperarEstadoOnboardingLocal(usuario.idUsuario)

                    Result.success(usuario)
                } else {
                    Result.failure(Exception("Correo o Contraseña incorrectos"))
                }
            } else {
                Result.failure(Exception("No se pudo conectar con el servidor. Inténtalo más tarde."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            val errorPersonalizado = if (e is java.net.UnknownHostException) {
                "No hay conexión a internet. Revisa tu red."
            } else {
                "Ocurrió un error inesperado. Inténtalo de nuevo."
            }
            Result.failure(Exception(errorPersonalizado))
        }
    }

    // Registro
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
                    else -> "No se pudo conectar con el servidor. Inténtalo más tarde."
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            val errorPersonalizado = if (e is java.net.UnknownHostException) {
                "No hay conexión a internet. Revisa tu red."
            } else {
                "Ocurrió un error inesperado. Inténtalo de nuevo."
            }
            Result.failure(Exception(errorPersonalizado))
        }
    }

    // Admin

    // Obtener todos los usuarios de la API
    suspend fun getAllUsuariosFromApi(): Result<List<Usuario>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsuarios()

            if (response.isSuccessful) {
                val usuarios = response.body() ?: emptyList()
                Result.success(usuarios)
            } else {
                Result.failure(Exception("No se pudo conectar con el servidor. Inténtalo más tarde."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            val errorPersonalizado = if (e is java.net.UnknownHostException) {
                "No hay conexión a internet. Revisa tu red."
            } else {
                "Ocurrió un error inesperado. Inténtalo de nuevo."
            }
            Result.failure(Exception(errorPersonalizado))
        }
    }

    // Actualizar usuario PUT
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
                    else -> "No se pudo conectar con el servidor. Inténtalo más tarde."
                }
                Log.e(TAG, "Error en respuesta: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            val errorPersonalizado = if (e is java.net.UnknownHostException) {
                "No hay conexión a internet. Revisa tu red."
            } else {
                "Ocurrió un error inesperado. Inténtalo de nuevo."
            }
            Result.failure(Exception(errorPersonalizado))
        }
    }

    // Eliminar usuario DELETE
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
                    else -> "No se pudo conectar con el servidor. Inténtalo más tarde."
                }
                Log.e(TAG, "Error en respuesta: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            val errorPersonalizado = if (e is java.net.UnknownHostException) {
                "No hay conexión a internet. Revisa tu red."
            } else {
                "Ocurrió un error inesperado. Inténtalo de nuevo."
            }
            Result.failure(Exception(errorPersonalizado))
        }
    }

    // Guardar preferencias del Onboarding
    suspend fun guardarPreferenciasLocales(idUsuario: Int, limitaciones: String) = withContext(Dispatchers.IO) {
        try {
            // Guardar en SQLite usando el DatabaseHelper
            dbHelper.guardarPreferenciasUsuario(idUsuario, limitaciones)

            // Actualizar la sesión actual en SharedPreferences
            preferencesManager.saveOnboardingPreferences(limitaciones)

            Log.d(TAG, "Preferencias de onboarding guardadas localmente")
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando preferencias locales: ${e.message}")
            throw e
        }
    }

    // Metodo auxiliar para cuando el usuario hace login en un dispositivo nuevo o limpia caché
    private fun recuperarEstadoOnboardingLocal(idUsuario: Int) {
        try {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery(
                "SELECT ${DatabaseHelper.COL_ONBOARDING_COMPLETADO}," +
                        "${DatabaseHelper.COL_LIMITACIONES} FROM ${DatabaseHelper.TABLE_USUARIOS} WHERE ${DatabaseHelper.COL_ID_USUARIO} = ?",
                arrayOf(idUsuario.toString())
            )

            if (cursor.moveToFirst()) {
                val completado = cursor.getInt(0) == 1
                val limitaciones = cursor.getString(1) ?: ""

                if (completado) {
                    preferencesManager.saveOnboardingPreferences(limitaciones)
                }
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error recuperando estado de onboarding: ${e.message}")
        }
    }

    // Guardar usuario en BD local - caché
    fun saveUsuarioLocal(usuario: Usuario) {
        val db = dbHelper.writableDatabase

        var onboardingGuardado = 0
        var limitacionesGuardadas = ""

        try {
            val cursor = db.rawQuery(
                "SELECT onboarding_completado, limitaciones FROM ${DatabaseHelper.TABLE_USUARIOS} WHERE ${DatabaseHelper.COL_ID_USUARIO} = ?",
                arrayOf(usuario.idUsuario.toString())
            )
            if (cursor.moveToFirst()) {
                onboardingGuardado = cursor.getInt(0)
                limitacionesGuardadas = cursor.getString(1) ?: ""
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error rescatando datos locales antes de actualizar: ${e.message}")
        }

        val values = ContentValues().apply {
            if (usuario.idUsuario > 0) {
                put("id_usuario", usuario.idUsuario)
            }
            put("nombre", usuario.nombre)
            put("correo_electronico", usuario.correoElectronico)
            put("contrasena", usuario.contrasena)
            put("id_tipo_usuario", usuario.idTipoUsuario)

            // Inyectar los datos locales que no vienen de AWS
            put("onboarding_completado", onboardingGuardado)
            put("limitaciones", limitacionesGuardadas)
        }
        db.insertWithOnConflict("Usuarios", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    // Actualizar usuario local
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

    // Elimimar usuario local
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

    // Obtener usuario local
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