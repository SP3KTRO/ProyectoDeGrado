package com.tupausa.repository

import android.database.Cursor
import android.util.Log
import com.tupausa.database.DatabaseHelper
import com.tupausa.model.Ejercicio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EjercicioRepository(private val dbHelper: DatabaseHelper) {

    companion object {
        private const val TAG = "EjercicioRepository"
    }
    // OBTENER TODOS LOS EJERCICIOS

    suspend fun getAllEjercicios(): List<Ejercicio> = withContext(Dispatchers.IO) {
        val ejercicios = mutableListOf<Ejercicio>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            "Ejercicios",
            null,
            null,
            null,
            null,
            null,
            "nombre ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                ejercicios.add(cursorToEjercicio(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        Log.d(TAG, "Ejercicios obtenidos: ${ejercicios.size}")
        return@withContext ejercicios
    }

    // OBTENER EJERCICIO POR ID

    suspend fun getEjercicioById(id: Int): Ejercicio? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "Ejercicios",
            null,
            "id_ejercicio = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        val ejercicio = if (cursor.moveToFirst()) {
            cursorToEjercicio(cursor)
        } else null

        cursor.close()
        return@withContext ejercicio
    }

    // FILTRAR POR TIPO

    suspend fun getEjerciciosByTipo(tipo: String): List<Ejercicio> = withContext(Dispatchers.IO) {
        val ejercicios = mutableListOf<Ejercicio>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            "Ejercicios",
            null,
            "tipo = ?",
            arrayOf(tipo),
            null, null,
            "nombre ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                ejercicios.add(cursorToEjercicio(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return@withContext ejercicios
    }

    // FILTRAR POR NIVEL

    suspend fun getEjerciciosByNivel(nivel: String): List<Ejercicio> = withContext(Dispatchers.IO) {
        val ejercicios = mutableListOf<Ejercicio>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            "Ejercicios",
            null,
            "nivel_intensidad = ?",
            arrayOf(nivel),
            null, null,
            "nombre ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                ejercicios.add(cursorToEjercicio(cursor))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return@withContext ejercicios
    }

    // OBTENER EJERCICIO ALEATORIO

    suspend fun getEjercicioAleatorio(): Ejercicio? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM Ejercicios ORDER BY RANDOM() LIMIT 1",
            null
        )

        val ejercicio = if (cursor.moveToFirst()) {
            cursorToEjercicio(cursor)
        } else null

        cursor.close()
        return@withContext ejercicio
    }

    // HELPER: Cursor a Ejercicio

    private fun cursorToEjercicio(cursor: Cursor): Ejercicio {
        return Ejercicio(
            idEjercicio = cursor.getInt(cursor.getColumnIndexOrThrow("id_ejercicio")),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
            tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo")),
            nivelIntensidad = cursor.getString(cursor.getColumnIndexOrThrow("nivel_intensidad")),
            duracionSegundos = cursor.getInt(cursor.getColumnIndexOrThrow("duracion_segundos")),
            gifResource = cursor.getString(cursor.getColumnIndexOrThrow("gif_resource")),
            instrucciones = cursor.getString(cursor.getColumnIndexOrThrow("instrucciones")),
            beneficios = cursor.getString(cursor.getColumnIndexOrThrow("beneficios"))
        )
    }
}