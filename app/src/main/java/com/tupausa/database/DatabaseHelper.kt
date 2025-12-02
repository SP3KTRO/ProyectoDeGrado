package com.tupausa.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.tupausa.utils.Constants

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    Constants.DATABASE_NAME,
    null,
    Constants.DATABASE_VERSION
) {

    companion object {
        private const val TAG = "DatabaseHelper"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "Creando base de datos...")

        try {
            // ==========================================
            // TABLA USUARIOS (caché local)
            // ==========================================
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Usuarios (
                    id_usuario INTEGER PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    correo_electronico TEXT UNIQUE NOT NULL,
                    contrasena TEXT NOT NULL,
                    id_tipo_usuario INTEGER DEFAULT 1
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Usuarios creada.")

            // ==========================================
            // TABLA EJERCICIOS
            // ==========================================
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Ejercicios (
                    id_ejercicio INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    descripcion TEXT NOT NULL,
                    tipo TEXT NOT NULL,
                    nivel_intensidad TEXT NOT NULL,
                    duracion_segundos INTEGER NOT NULL,
                    gif_resource TEXT,
                    instrucciones TEXT NOT NULL,
                    beneficios TEXT
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Ejercicios creada.")

            // ==========================================
            // TABLA ALARMAS
            // ==========================================
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Alarmas (
                    id_alarma INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_usuario INTEGER NOT NULL,
                    hora TEXT NOT NULL,
                    dias_semana TEXT NOT NULL,
                    activa INTEGER NOT NULL DEFAULT 1,
                    tipo_ejercicio TEXT,
                    mensaje_personalizado TEXT
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Alarmas creada.")

            // ==========================================
            // TABLA HISTORIAL_PAUSAS
            // ==========================================
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Historial_pausas (
                    id_historial INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_usuario INTEGER NOT NULL,
                    id_ejercicio INTEGER NOT NULL,
                    fecha TEXT NOT NULL,
                    hora_inicio TEXT NOT NULL,
                    hora_fin TEXT,
                    completado INTEGER NOT NULL DEFAULT 0,
                    duracion_real_segundos INTEGER
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Historial_pausas creada.")

            // INSERTAR 10 EJERCICIOS PRECARGADOS
            insertEjerciciosIniciales(db)

            Log.d(TAG, "Base de datos creada correctamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear la base de datos: ${e.message}", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Actualizando BD de versión $oldVersion a $newVersion")

        db?.execSQL("DROP TABLE IF EXISTS Historial_pausas")
        db?.execSQL("DROP TABLE IF EXISTS Alarmas")
        db?.execSQL("DROP TABLE IF EXISTS Ejercicios")
        db?.execSQL("DROP TABLE IF EXISTS Usuarios")

        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        db?.execSQL("PRAGMA foreign_keys=ON")
        Log.d(TAG, "Base de datos abierta.")
    }

    // ==========================================
    // INSERTAR 10 EJERCICIOS PRECARGADOS
    // ==========================================
    private fun insertEjerciciosIniciales(db: SQLiteDatabase?) {
        val ejercicios = listOf(
            ContentValues().apply {
                put("nombre", "Rotación de Cuello")
                put("descripcion", "Alivia la tensión cervical con rotaciones suaves de cabeza")
                put("tipo", "CUELLO")
                put("nivel_intensidad", "BAJO")
                put("duracion_segundos", 60)
                put("gif_resource", "gif_cuello_rotacion")
                put("instrucciones", "Siéntate derecho con la espalda recta|Gira lentamente la cabeza hacia la derecha|Mantén la posición 5 segundos|Regresa al centro|Repite hacia la izquierda|Realiza 5 repeticiones completas")
                put("beneficios", "Reduce tensión cervical, mejora movilidad del cuello, previene dolores de cabeza")
            },
            ContentValues().apply {
                put("nombre", "Estiramiento de Muñecas")
                put("descripcion", "Previene el síndrome del túnel carpiano y alivia tensión en manos")
                put("tipo", "MUNECAS")
                put("nivel_intensidad", "BAJO")
                put("duracion_segundos", 45)
                put("gif_resource", "gif_munecas")
                put("instrucciones", "Extiende el brazo frente a ti con la palma hacia arriba|Con la otra mano, tira suavemente de los dedos hacia atrás|Mantén el estiramiento 10 segundos|Cambia de mano|Realiza 3 series en cada mano")
                put("beneficios", "Previene lesiones por movimientos repetitivos, alivia dolor en muñecas")
            },
            ContentValues().apply {
                put("nombre", "Regla 20-20-20 para Ojos")
                put("descripcion", "Descansa tu vista del monitor para prevenir fatiga visual")
                put("tipo", "OJOS")
                put("nivel_intensidad", "BAJO")
                put("duracion_segundos", 20)
                put("gif_resource", "gif_ojos")
                put("instrucciones", "Cada 20 minutos de trabajo|Mira algo a 20 pies de distancia (6 metros)|Mantén la mirada durante 20 segundos|Parpadea varias veces|Vuelve a tu trabajo")
                put("beneficios", "Reduce fatiga visual, previene ojo seco, mejora enfoque")
            },
            ContentValues().apply {
                put("nombre", "Estiramiento de Espalda Alta")
                put("descripcion", "Alivia la tensión en la parte superior de la espalda")
                put("tipo", "ESPALDA")
                put("nivel_intensidad", "MEDIO")
                put("duracion_segundos", 90)
                put("gif_resource", "gif_espalda")
                put("instrucciones", "De pie, entrelaza las manos frente a ti|Estira los brazos hacia adelante redondeando la espalda|Mantén 15 segundos|Lleva las manos por encima de la cabeza|Inclínate suavemente a cada lado|Repite 3 veces")
                put("beneficios", "Mejora postura, alivia dolor lumbar, aumenta flexibilidad")
            },
            ContentValues().apply {
                put("nombre", "Respiración Profunda 4-4-6")
                put("descripcion", "Reduce el estrés mediante respiración controlada")
                put("tipo", "RESPIRACION")
                put("nivel_intensidad", "BAJO")
                put("duracion_segundos", 120)
                put("gif_resource", "gif_respiracion")
                put("instrucciones", "Siéntate cómodamente con la espalda recta|Inhala por la nariz contando hasta 4|Mantén el aire contando hasta 4|Exhala por la boca contando hasta 6|Repite 5 veces")
                put("beneficios", "Reduce ansiedad, mejora concentración, regula presión arterial")
            },
            ContentValues().apply {
                put("nombre", "Estiramiento de Hombros")
                put("descripcion", "Libera tensión en hombros y trapecios")
                put("tipo", "HOMBROS")
                put("nivel_intensidad", "BAJO")
                put("duracion_segundos", 60)
                put("gif_resource", "gif_hombros")
                put("instrucciones", "Cruza el brazo derecho sobre el pecho|Con la mano izquierda, presiona suavemente el codo derecho|Mantén 15 segundos|Cambia de brazo|Haz círculos con los hombros 10 veces")
                put("beneficios", "Alivia tensión en hombros, mejora movilidad")
            },
            ContentValues().apply {
                put("nombre", "Sentadillas de Escritorio")
                put("descripcion", "Activa piernas y glúteos sin salir del espacio de trabajo")
                put("tipo", "PIERNAS")
                put("nivel_intensidad", "MEDIO")
                put("duracion_segundos", 90)
                put("gif_resource", "gif_piernas")
                put("instrucciones", "De pie, separa los pies al ancho de hombros|Baja como si fueras a sentarte|Mantén espalda recta|Sube lentamente|Realiza 10-15 repeticiones")
                put("beneficios", "Mejora circulación, fortalece piernas, activa metabolismo")
            },
            ContentValues().apply {
                put("nombre", "Estiramiento Cat-Cow")
                put("descripcion", "Moviliza toda la columna vertebral")
                put("tipo", "ESPALDA")
                put("nivel_intensidad", "MEDIO")
                put("duracion_segundos", 75)
                put("gif_resource", "gif_cat_cow")
                put("instrucciones", "Apoya manos y rodillas en el suelo|Inhala arqueando la espalda|Exhala redondeando la espalda|Alterna entre ambas posiciones|Realiza 10 repeticiones")
                put("beneficios", "Mejora flexibilidad espinal, alivia tensión lumbar")
            },
            ContentValues().apply {
                put("nombre", "Estiramiento de Pectorales")
                put("descripcion", "Contrarresta la postura encorvada del escritorio")
                put("tipo", "ESTIRAMIENTO_GENERAL")
                put("nivel_intensidad", "BAJO")
                put("duracion_segundos", 60)
                put("gif_resource", "gif_pectorales")
                put("instrucciones", "De pie junto a una pared|Coloca el antebrazo en la pared|Gira el cuerpo alejándote del brazo|Mantén 20 segundos|Repite del otro lado")
                put("beneficios", "Mejora postura, abre pecho, facilita respiración")
            },
            ContentValues().apply {
                put("nombre", "Ejercicio de Dedos y Manos")
                put("descripcion", "Previene fatiga por uso prolongado del teclado")
                put("tipo", "MUÑECAS")
                put("nivel_intensidad", "BAJO")
                put("duracion_segundos", 45)
                put("gif_resource", "gif_dedos")
                put("instrucciones", "Abre y cierra las manos 10 veces|Toca cada dedo con el pulgar|Haz círculos con las muñecas|Sacude las manos 10 segundos|Repite 2 veces")
                put("beneficios", "Previene calambres, mejora destreza, aumenta circulación")
            }
        )

        ejercicios.forEach { values ->
            db?.insert("Ejercicios", null, values)
        }
        Log.d(TAG, "${ejercicios.size} ejercicios precargados insertados.")
    }
}