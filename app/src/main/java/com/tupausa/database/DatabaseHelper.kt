package com.tupausa.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tupausa_database.db"
        private const val DATABASE_VERSION = 1
        private const val TAG = "DatabaseHelper" // Etiqueta para los logs
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "Creando base de datos...")

        try {
            // Crear tabla Tipo_usuario
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Tipo_usuario (
                    id_tipo_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo TEXT NOT NULL
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Tipo_usuario creada correctamente.")

            // Crear tabla Usuarios
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Usuarios (
                    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    correo_electronico TEXT UNIQUE NOT NULL,
                    contrasena TEXT NOT NULL,
                    id_tipo_usuario INTEGER,
                    FOREIGN KEY (id_tipo_usuario) REFERENCES Tipo_usuario(id_tipo_usuario)
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Usuarios creada correctamente.")

            // Crear tabla Estado_pausa
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Estado_pausa (
                    id_estado_pausa INTEGER PRIMARY KEY AUTOINCREMENT,
                    estado TEXT NOT NULL
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Estado_pausa creada correctamente.")

            // Crear tabla Pausas_activas
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Pausas_activas (
                    id_pausa INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_usuario INTEGER,
                    fecha TEXT NOT NULL,
                    hora TEXT NOT NULL,
                    duracion INTEGER NOT NULL,
                    id_estado_pausa INTEGER,
                    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario),
                    FOREIGN KEY (id_estado_pausa) REFERENCES Estado_pausa(id_estado_pausa)
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Pausas_activas creada correctamente.")

            // Crear tabla Nombre_ejercicio
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Nombre_ejercicio (
                    id_nombre_ejer INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre_ejercicio TEXT NOT NULL
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Nombre_ejercicio creada correctamente.")

            // Crear tabla Descripcion_ejercicio
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Descripcion_ejercicio (
                    id_descripcion INTEGER PRIMARY KEY AUTOINCREMENT,
                    descripcion TEXT NOT NULL
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Descripcion_ejercicio creada correctamente.")

            // Crear tabla Tipo_ejercicio
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Tipo_ejercicio (
                    id_tipo_ejercicio INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo TEXT NOT NULL
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Tipo_ejercicio creada correctamente.")

            // Crear tabla Nivel_intensidad
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Nivel_intensidad (
                    id_nivel_intensidad INTEGER PRIMARY KEY AUTOINCREMENT,
                    nivel TEXT NOT NULL
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Nivel_intensidad creada correctamente.")

            // Crear tabla Ejercicios
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Ejercicios (
                    id_ejercicio INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_nombre_ejer INTEGER,
                    id_descripcion INTEGER,
                    id_tipo_ejercicio INTEGER,
                    id_nivel_intensidad INTEGER,
                    FOREIGN KEY (id_nombre_ejer) REFERENCES Nombre_ejercicio(id_nombre_ejer),
                    FOREIGN KEY (id_descripcion) REFERENCES Descripcion_ejercicio(id_descripcion),
                    FOREIGN KEY (id_tipo_ejercicio) REFERENCES Tipo_ejercicio(id_tipo_ejercicio),
                    FOREIGN KEY (id_nivel_intensidad) REFERENCES Nivel_intensidad(id_nivel_intensidad)
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Ejercicios creada correctamente.")

            // Crear tabla Registro_pausa
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Registro_pausa (
                    id_registro INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_pausa INTEGER,
                    fecha_realizacion TEXT NOT NULL,
                    hora_inicio TEXT NOT NULL,
                    hora_fin TEXT NOT NULL,
                    FOREIGN KEY (id_pausa) REFERENCES Pausas_activas(id_pausa)
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Registro_pausa creada correctamente.")

            // Crear tabla Tipo_deteccion
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Tipo_deteccion (
                    id_tipo_deteccion INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo TEXT NOT NULL
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Tipo_deteccion creada correctamente.")

            // Crear tabla Deteccion_movimientos
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Deteccion_movimientos (
                    id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_registro INTEGER,
                    fecha TEXT NOT NULL,
                    id_tipo_deteccion INTEGER,
                    resultado_validacion TEXT,
                    video BLOB,
                    FOREIGN KEY (id_registro) REFERENCES Registro_pausa(id_registro),
                    FOREIGN KEY (id_tipo_deteccion) REFERENCES Tipo_deteccion(id_tipo_deteccion)
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Deteccion_movimientos creada correctamente.")

            // Crear tabla Historial_pausas
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Historial_pausas (
                    id_historial INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_usuario INTEGER,
                    tiempo_total INTEGER NOT NULL,
                    id_tipo_deteccion INTEGER,
                    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario),
                    FOREIGN KEY (id_tipo_deteccion) REFERENCES Tipo_deteccion(id_tipo_deteccion)
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Historial_pausas creada correctamente.")

            // Crear tabla Estadisticas_generales
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Estadisticas_generales (
                    id_estadistica INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_usuario INTEGER,
                    frecuencias_pausas TEXT NOT NULL,
                    id_tipo_deteccion INTEGER,
                    porcentaje_cumplimiento REAL NOT NULL,
                    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario),
                    FOREIGN KEY (id_tipo_deteccion) REFERENCES Tipo_deteccion(id_tipo_deteccion)
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Estadisticas_generales creada correctamente.")

            // Crear tabla Frecuencia_notificacion
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Frecuencia_notificacion (
                    id_frecuencia INTEGER PRIMARY KEY AUTOINCREMENT,
                    frecuencia TEXT NOT NULL
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Frecuencia_notificacion creada correctamente.")

            // Crear tabla Horario_notificacion
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Horario_notificacion (
                    id_horario INTEGER PRIMARY KEY AUTOINCREMENT,
                    horario TEXT NOT NULL
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Horario_notificacion creada correctamente.")

            // Crear tabla Tono_notificacion
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Tono_notificacion (
                    id_tono INTEGER PRIMARY KEY AUTOINCREMENT,
                    tono TEXT NOT NULL
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Tono_notificacion creada correctamente.")

            // Crear tabla Notificaciones
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS Notificaciones (
                    id_notificacion INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_usuario INTEGER,
                    id_frecuencia INTEGER,
                    id_horario INTEGER,
                    id_tono INTEGER,
                    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario),
                    FOREIGN KEY (id_frecuencia) REFERENCES Frecuencia_notificacion(id_frecuencia),
                    FOREIGN KEY (id_horario) REFERENCES Horario_notificacion(id_horario),
                    FOREIGN KEY (id_tono) REFERENCES Tono_notificacion(id_tono)
                )
            """.trimIndent())
            Log.d(TAG, "Tabla Notificaciones creada correctamente.")

            Log.d(TAG, "Base de datos creada correctamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear la base de datos: ${e.message}")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Actualizando base de datos de la versión $oldVersion a $newVersion...")

        try {
            // Eliminar y recrear las tablas si la versión cambia
            db?.execSQL("DROP TABLE IF EXISTS Tipo_usuario")
            db?.execSQL("DROP TABLE IF EXISTS Usuarios")
            db?.execSQL("DROP TABLE IF EXISTS Estado_pausa")
            db?.execSQL("DROP TABLE IF EXISTS Pausas_activas")
            db?.execSQL("DROP TABLE IF EXISTS Nombre_ejercicio")
            db?.execSQL("DROP TABLE IF EXISTS Descripcion_ejercicio")
            db?.execSQL("DROP TABLE IF EXISTS Tipo_ejercicio")
            db?.execSQL("DROP TABLE IF EXISTS Nivel_intensidad")
            db?.execSQL("DROP TABLE IF EXISTS Ejercicios")
            db?.execSQL("DROP TABLE IF EXISTS Registro_pausa")
            db?.execSQL("DROP TABLE IF EXISTS Tipo_deteccion")
            db?.execSQL("DROP TABLE IF EXISTS Deteccion_movimientos")
            db?.execSQL("DROP TABLE IF EXISTS Historial_pausas")
            db?.execSQL("DROP TABLE IF EXISTS Estadisticas_generales")
            db?.execSQL("DROP TABLE IF EXISTS Frecuencia_notificacion")
            db?.execSQL("DROP TABLE IF EXISTS Horario_notificacion")
            db?.execSQL("DROP TABLE IF EXISTS Tono_notificacion")
            db?.execSQL("DROP TABLE IF EXISTS Notificaciones")
            Log.d(TAG, "Tablas eliminadas correctamente.")

            onCreate(db)
            Log.d(TAG, "Base de datos actualizada correctamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar la base de datos: ${e.message}")
        }
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        Log.d(TAG, "Base de datos abierta correctamente.")
    }
}