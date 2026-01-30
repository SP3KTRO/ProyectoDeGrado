package com.tupausa.repository

import android.content.ContentValues
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

    // Obetener los ejercicios
    suspend fun getAllEjercicios(): List<Ejercicio> = withContext(Dispatchers.IO) {
        val ejercicios = mutableListOf<Ejercicio>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_EJERCICIOS,
            null, null, null, null, null,
            "${DatabaseHelper.COL_NOMBRE_EJERCICIO} ASC"
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

    suspend fun getEjercicioById(id: Int): Ejercicio? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_EJERCICIOS,
            null,
            "${DatabaseHelper.COL_ID_EJERCICIO} = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        val ejercicio = if (cursor.moveToFirst()) cursorToEjercicio(cursor) else null
        cursor.close()
        return@withContext ejercicio
    }

    suspend fun getEjerciciosByTipo(tipo: String): List<Ejercicio> = withContext(Dispatchers.IO) {
        val ejercicios = mutableListOf<Ejercicio>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_EJERCICIOS,
            null,
            "${DatabaseHelper.COL_TIPO_EJERCICIO} = ?",
            arrayOf(tipo),
            null, null,
            "${DatabaseHelper.COL_NOMBRE_EJERCICIO} ASC"
        )
        if (cursor.moveToFirst()) {
            do {
                ejercicios.add(cursorToEjercicio(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return@withContext ejercicios
    }

    suspend fun getEjercicioAleatorio(): Ejercicio? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_EJERCICIOS} ORDER BY RANDOM() LIMIT 1", null)
        val ejercicio = if (cursor.moveToFirst()) cursorToEjercicio(cursor) else null
        cursor.close()
        return@withContext ejercicio
    }

    // POST / PUT / DELETE
    suspend fun insertEjercicio(ejercicio: Ejercicio): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_NOMBRE_EJERCICIO, ejercicio.nombreEjercicio)
            put(DatabaseHelper.COL_DESCRIPCION, ejercicio.descripcion)
            put(DatabaseHelper.COL_TIPO_EJERCICIO, ejercicio.tipoEjercicio)
            put(DatabaseHelper.COL_NIVEL_INTENSIDAD, ejercicio.nivelIntensidad)
            put(DatabaseHelper.COL_URL_IMAGEN, ejercicio.urlImagenGuia)
            put(DatabaseHelper.COL_DURACION, ejercicio.duracionSegundos)
            put(DatabaseHelper.COL_INSTRUCCIONES, ejercicio.instrucciones)
            put(DatabaseHelper.COL_BENEFICIOS, ejercicio.beneficios)
        }
        val id = db.insert(DatabaseHelper.TABLE_EJERCICIOS, null, values)
        db.close()
        return@withContext id
    }

    suspend fun updateEjercicio(ejercicio: Ejercicio): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_NOMBRE_EJERCICIO, ejercicio.nombreEjercicio)
            put(DatabaseHelper.COL_DESCRIPCION, ejercicio.descripcion)
            put(DatabaseHelper.COL_TIPO_EJERCICIO, ejercicio.tipoEjercicio)
            put(DatabaseHelper.COL_NIVEL_INTENSIDAD, ejercicio.nivelIntensidad)
            put(DatabaseHelper.COL_URL_IMAGEN, ejercicio.urlImagenGuia)
            put(DatabaseHelper.COL_DURACION, ejercicio.duracionSegundos)
            put(DatabaseHelper.COL_INSTRUCCIONES, ejercicio.instrucciones)
            put(DatabaseHelper.COL_BENEFICIOS, ejercicio.beneficios)
        }
        val rows = db.update(
            DatabaseHelper.TABLE_EJERCICIOS,
            values,
            "${DatabaseHelper.COL_ID_EJERCICIO} = ?",
            arrayOf(ejercicio.idEjercicio.toString())
        )
        db.close()
        return@withContext rows
    }

    suspend fun deleteEjercicio(id: Int): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val rows = db.delete(
            DatabaseHelper.TABLE_EJERCICIOS,
            "${DatabaseHelper.COL_ID_EJERCICIO} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return@withContext rows
    }

    // Helper para mapeo de columnas nuevas
    private fun cursorToEjercicio(cursor: Cursor): Ejercicio {
        return Ejercicio(
            idEjercicio = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID_EJERCICIO)),
            nombreEjercicio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOMBRE_EJERCICIO)),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPCION)),
            tipoEjercicio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TIPO_EJERCICIO)),
            nivelIntensidad = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NIVEL_INTENSIDAD)),
            urlImagenGuia = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_URL_IMAGEN)),
            duracionSegundos = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DURACION)),
            instrucciones = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INSTRUCCIONES)),
            beneficios = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BENEFICIOS))
        )
    }
}