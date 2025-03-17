package com.tupausa.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.tupausa.database.DatabaseHelper
import com.tupausa.model.Usuario
import com.tupausa.model.PausaActiva
import com.tupausa.model.Ejercicio

class TuPausaRepository(private val dbHelper: DatabaseHelper) {

    // Insertar un usuario
    fun insertUsuario(usuario: Usuario): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nombre", usuario.nombre)
            put("correo_electronico", usuario.correoElectronico)
            put("contrasena", usuario.contrasena)
            put("id_tipo_usuario", usuario.idTipoUsuario)
        }
        return db.insert("Usuarios", null, values)
    }

    // Obtener todos los usuarios
    fun getAllUsuarios(): List<Usuario> {
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
        return usuarios
    }

    // Insertar una pausa activa
    fun insertPausaActiva(pausaActiva: PausaActiva): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id_usuario", pausaActiva.idUsuario)
            put("fecha", pausaActiva.fecha)
            put("hora", pausaActiva.hora)
            put("duracion", pausaActiva.duracion)
            put("id_estado_pausa", pausaActiva.idEstadoPausa)
        }
        return db.insert("Pausas_activas", null, values)
    }

    // Obtener todas las pausas activas
    fun getAllPausasActivas(): List<PausaActiva> {
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
        return pausasActivas
    }

    // Insertar un ejercicio
    fun insertEjercicio(ejercicio: Ejercicio): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("id_nombre_ejer", ejercicio.idNombreEjer)
            put("id_descripcion", ejercicio.idDescripcion)
            put("id_tipo_ejercicio", ejercicio.idTipoEjercicio)
            put("id_nivel_intensidad", ejercicio.idNivelIntensidad)
        }
        return db.insert("Ejercicios", null, values)
    }

    // Obtener todos los ejercicios
    fun getAllEjercicios(): List<Ejercicio> {
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
        return ejercicios
    }
}