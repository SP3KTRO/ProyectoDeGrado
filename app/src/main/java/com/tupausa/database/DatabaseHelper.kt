package com.tupausa.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tupausa_database.db"
        private const val DATABASE_VERSION = 2

        // ==========================================
        // DEFINICIÓN DE TABLAS Y COLUMNAS
        // ==========================================

        // Tabla Tipo_usuario
        const val TABLE_TIPO_USUARIO = "Tipo_usuario"
        const val COL_ID_TIPO_USUARIO = "id_tipo_usuario"
        const val COL_TIPO = "tipo"

        // Tabla Usuarios
        const val TABLE_USUARIOS = "Usuarios"
        const val COL_ID_USUARIO = "id_usuario"
        const val COL_NOMBRE = "nombre"
        const val COL_CORREO = "correo_electronico"
        const val COL_CONTRASENA = "contrasena"
        const val COL_FK_TIPO_USUARIO = "id_tipo_usuario"

        // Tabla Ejercicios
        const val TABLE_EJERCICIOS = "Ejercicios"
        const val COL_ID_EJERCICIO = "id_ejercicio"
        const val COL_NOMBRE_EJERCICIO = "nombre_ejercicio"
        const val COL_DESCRIPCION = "descripcion"
        const val COL_TIPO_EJERCICIO = "tipo_ejercicio"
        const val COL_NIVEL_INTENSIDAD = "nivel_intensidad"
        const val COL_DURACION = "duracion_segundos"
        const val COL_URL_IMAGEN = "url_imagen_guia"
        const val COL_INSTRUCCIONES = "instrucciones"
        const val COL_BENEFICIOS = "beneficios"

        // Tabla Alarmas
        const val TABLE_ALARMAS = "Alarmas"
        const val COL_ID_ALARMA = "id_alarma"
        const val COL_HORA_PROGRAMADA = "hora_programada"
        const val COL_DIAS_ACTIVOS = "dias_activos"
        const val COL_ESTADO_ALARMA = "estado_alarma"
        const val COL_DURACION_ESTIMADA = "duracion_estimada"
        const val COL_ETIQUETA = "etiqueta" // Faltaba esta columna

        // Tabla Config_Notificaciones
        const val TABLE_CONFIG_NOTIF = "Config_Notificaciones"
        const val COL_ID_NOTIF = "id_notificacion"
        const val COL_FRECUENCIA = "frecuencia_preferida"
        const val COL_HORARIO_PREF = "horario_preferido"
        const val COL_TONO = "nombre_tono"

        // Tabla Historial_Ejecucion
        const val TABLE_HISTORIAL = "Historial_Ejecucion"
        const val COL_ID_REGISTRO = "id_registro"
        const val COL_FECHA_REALIZACION = "fecha_realizacion"
        const val COL_HORA_INICIO = "hora_inicio"
        const val COL_HORA_FIN = "hora_fin"
        const val COL_SE_DETECTO_MOV = "se_detecto_movimiento"
        const val COL_TIPO_DETECCION = "tipo_deteccion_usado"
        const val COL_RUTA_VIDEO = "ruta_video_evidencia"

        // Tabla Estadisticas_Resumen
        const val TABLE_ESTADISTICAS = "Estadisticas_Resumen"
        const val COL_ID_ESTADISTICA = "id_estadistica"
        const val COL_TOTAL_PAUSAS = "total_pausas_realizadas"
        const val COL_TIEMPO_TOTAL = "tiempo_total_minutos"
        const val COL_PORCENTAJE = "porcentaje_cumplimiento"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Activar soporte para claves foráneas (Foreign Keys)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Crear Tipo_usuario
        db.execSQL("CREATE TABLE $TABLE_TIPO_USUARIO ($COL_ID_TIPO_USUARIO INTEGER PRIMARY KEY AUTOINCREMENT, $COL_TIPO TEXT)")

        // 2. Crear Usuarios
        // Se agrega soporte para preferencias y se mantiene FK.
        val createUsuarios = ("CREATE TABLE $TABLE_USUARIOS ("
                + "$COL_ID_USUARIO INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_NOMBRE TEXT NOT NULL, "
                + "$COL_CORREO TEXT UNIQUE NOT NULL, "
                + "$COL_CONTRASENA TEXT NOT NULL, "
                + "$COL_FK_TIPO_USUARIO INTEGER, "
                + "FOREIGN KEY($COL_FK_TIPO_USUARIO) REFERENCES $TABLE_TIPO_USUARIO($COL_ID_TIPO_USUARIO))")
        db.execSQL(createUsuarios)

        // 3. Crear Ejercicios
        val createEjercicios = ("CREATE TABLE $TABLE_EJERCICIOS ("
                + "$COL_ID_EJERCICIO INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_NOMBRE_EJERCICIO TEXT NOT NULL, "
                + "$COL_DESCRIPCION TEXT NOT NULL, "
                + "$COL_TIPO_EJERCICIO TEXT NOT NULL, "
                + "$COL_NIVEL_INTENSIDAD TEXT NOT NULL, "
                + "$COL_URL_IMAGEN TEXT, "
                + "$COL_DURACION INTEGER, "
                + "$COL_INSTRUCCIONES TEXT, "
                + "$COL_BENEFICIOS TEXT)")
        db.execSQL(createEjercicios)

        // 4. Crear Alarmas
        // Se agrega ON DELETE CASCADE para limpieza automática
        val createAlarmas = ("CREATE TABLE $TABLE_ALARMAS ("
                + "$COL_ID_ALARMA INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_ID_USUARIO INTEGER, "
                + "$COL_HORA_PROGRAMADA TEXT NOT NULL, "
                + "$COL_DIAS_ACTIVOS TEXT, "
                + "$COL_ESTADO_ALARMA TEXT DEFAULT 'ACTIVA', "
                + "$COL_ETIQUETA TEXT, "
                + "$COL_DURACION_ESTIMADA INTEGER, "
                + "FOREIGN KEY($COL_ID_USUARIO) REFERENCES $TABLE_USUARIOS($COL_ID_USUARIO) ON DELETE CASCADE)")
        db.execSQL(createAlarmas)

        // 5. Crear Config_Notificaciones
        val createConfig = ("CREATE TABLE $TABLE_CONFIG_NOTIF ("
                + "$COL_ID_NOTIF INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_ID_USUARIO INTEGER, "
                + "$COL_FRECUENCIA TEXT, "
                + "$COL_HORARIO_PREF TEXT, "
                + "$COL_TONO TEXT, "
                + "FOREIGN KEY($COL_ID_USUARIO) REFERENCES $TABLE_USUARIOS($COL_ID_USUARIO) ON DELETE CASCADE)")
        db.execSQL(createConfig)

        // 6. Crear Historial_Ejecucion
        val createHistorial = ("CREATE TABLE $TABLE_HISTORIAL ("
                + "$COL_ID_REGISTRO INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_ID_USUARIO INTEGER, "
                + "$COL_ID_EJERCICIO INTEGER, "
                + "$COL_FECHA_REALIZACION TEXT NOT NULL, "
                + "$COL_HORA_INICIO TEXT NOT NULL, "
                + "$COL_HORA_FIN TEXT NOT NULL, "
                + "$COL_SE_DETECTO_MOV INTEGER DEFAULT 0, "
                + "$COL_TIPO_DETECCION TEXT, "
                + "$COL_RUTA_VIDEO TEXT, "
                + "FOREIGN KEY($COL_ID_USUARIO) REFERENCES $TABLE_USUARIOS($COL_ID_USUARIO) ON DELETE CASCADE, "
                + "FOREIGN KEY($COL_ID_EJERCICIO) REFERENCES $TABLE_EJERCICIOS($COL_ID_EJERCICIO))")
        db.execSQL(createHistorial)

        // 7. Crear Estadisticas_Resumen
        val createStats = ("CREATE TABLE $TABLE_ESTADISTICAS ("
                + "$COL_ID_ESTADISTICA INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_ID_USUARIO INTEGER UNIQUE, "
                + "$COL_TOTAL_PAUSAS INTEGER DEFAULT 0, "
                + "$COL_TIEMPO_TOTAL REAL DEFAULT 0, "
                + "$COL_PORCENTAJE REAL DEFAULT 0, "
                + "FOREIGN KEY($COL_ID_USUARIO) REFERENCES $TABLE_USUARIOS($COL_ID_USUARIO) ON DELETE CASCADE)")
        db.execSQL(createStats)

        // INYECCIÓN DE DATOS SEMILLA
        insertarDatosIniciales(db)
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        // 1. Tipos de Usuario
        db.execSQL("INSERT INTO $TABLE_TIPO_USUARIO ($COL_TIPO) VALUES ('Estudiante')")
        db.execSQL("INSERT INTO $TABLE_TIPO_USUARIO ($COL_TIPO) VALUES ('Administrador')")

        // 2. Ejercicios Predeterminados (Tus datos intactos)
        val ejercicios = listOf(
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Rotación de Cuello")
                put(COL_DESCRIPCION, "Alivia la tensión cervical con rotaciones suaves de cabeza")
                put(COL_TIPO_EJERCICIO, "CUELLO")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_cuello_rotacion")
                put(COL_INSTRUCCIONES, "Siéntate derecho con la espalda recta|Gira lentamente la cabeza hacia la derecha|Mantén la posición 5 segundos|Regresa al centro|Repite hacia la izquierda|Realiza 5 repeticiones completas")
                put(COL_BENEFICIOS, "Reduce tensión cervical, mejora movilidad del cuello, previene dolores de cabeza")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Estiramiento de Muñecas")
                put(COL_DESCRIPCION, "Previene el síndrome del túnel carpiano y alivia tensión en manos")
                put(COL_TIPO_EJERCICIO, "MUNECAS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 45)
                put(COL_URL_IMAGEN, "gif_munecas")
                put(COL_INSTRUCCIONES, "Extiende el brazo frente a ti con la palma hacia arriba|Con la otra mano, tira suavemente de los dedos hacia atrás|Mantén el estiramiento 10 segundos|Cambia de mano|Realiza 3 series en cada mano")
                put(COL_BENEFICIOS, "Previene lesiones por movimientos repetitivos, alivia dolor en muñecas")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Regla 20-20-20 para Ojos")
                put(COL_DESCRIPCION, "Descansa tu vista del monitor para prevenir fatiga visual")
                put(COL_TIPO_EJERCICIO, "OJOS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 20)
                put(COL_URL_IMAGEN, "gif_ojos")
                put(COL_INSTRUCCIONES, "Cada 20 minutos de trabajo|Mira algo a 20 pies de distancia (6 metros)|Mantén la mirada durante 20 segundos|Parpadea varias veces|Vuelve a tu trabajo")
                put(COL_BENEFICIOS, "Reduce fatiga visual, previene ojo seco, mejora enfoque")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Estiramiento de Espalda Alta")
                put(COL_DESCRIPCION, "Alivia la tensión en la parte superior de la espalda")
                put(COL_TIPO_EJERCICIO, "ESPALDA")
                put(COL_NIVEL_INTENSIDAD, "MEDIO")
                put(COL_DURACION, 90)
                put(COL_URL_IMAGEN, "gif_espalda")
                put(COL_INSTRUCCIONES, "De pie, entrelaza las manos frente a ti|Estira los brazos hacia adelante redondeando la espalda|Mantén 15 segundos|Lleva las manos por encima de la cabeza|Inclínate suavemente a cada lado|Repite 3 veces")
                put(COL_BENEFICIOS, "Mejora postura, alivia dolor lumbar, aumenta flexibilidad")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Respiración Profunda 4-4-6")
                put(COL_DESCRIPCION, "Reduce el estrés mediante respiración controlada")
                put(COL_TIPO_EJERCICIO, "RESPIRACION")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 120)
                put(COL_URL_IMAGEN, "gif_respiracion")
                put(COL_INSTRUCCIONES, "Siéntate cómodamente con la espalda recta|Inhala por la nariz contando hasta 4|Mantén el aire contando hasta 4|Exhala por la boca contando hasta 6|Repite 5 veces")
                put(COL_BENEFICIOS, "Reduce ansiedad, mejora concentración, regula presión arterial")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Estiramiento de Hombros")
                put(COL_DESCRIPCION, "Libera tensión en hombros y trapecios")
                put(COL_TIPO_EJERCICIO, "HOMBROS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_hombros")
                put(COL_INSTRUCCIONES, "Cruza el brazo derecho sobre el pecho|Con la mano izquierda, presiona suavemente el codo derecho|Mantén 15 segundos|Cambia de brazo|Haz círculos con los hombros 10 veces")
                put(COL_BENEFICIOS, "Alivia tensión en hombros, mejora movilidad")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Sentadillas de Escritorio")
                put(COL_DESCRIPCION, "Activa piernas y glúteos sin salir del espacio de trabajo")
                put(COL_TIPO_EJERCICIO, "PIERNAS")
                put(COL_NIVEL_INTENSIDAD, "MEDIO")
                put(COL_DURACION, 90)
                put(COL_URL_IMAGEN, "gif_piernas")
                put(COL_INSTRUCCIONES, "De pie, separa los pies al ancho de hombros|Baja como si fueras a sentarte|Mantén espalda recta|Sube lentamente|Realiza 10-15 repeticiones")
                put(COL_BENEFICIOS, "Mejora circulación, fortalece piernas, activa metabolismo")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Estiramiento Cat-Cow")
                put(COL_DESCRIPCION, "Moviliza toda la columna vertebral")
                put(COL_TIPO_EJERCICIO, "ESPALDA")
                put(COL_NIVEL_INTENSIDAD, "MEDIO")
                put(COL_DURACION, 75)
                put(COL_URL_IMAGEN, "gif_cat_cow")
                put(COL_INSTRUCCIONES, "Apoya manos y rodillas en el suelo|Inhala arqueando la espalda|Exhala redondeando la espalda|Alterna entre ambas posiciones|Realiza 10 repeticiones")
                put(COL_BENEFICIOS, "Mejora flexibilidad espinal, alivia tensión lumbar")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Estiramiento de Pectorales")
                put(COL_DESCRIPCION, "Contrarresta la postura encorvada del escritorio")
                put(COL_TIPO_EJERCICIO, "ESTIRAMIENTO_GENERAL")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_pectorales")
                put(COL_INSTRUCCIONES, "De pie junto a una pared|Coloca el antebrazo en la pared|Gira el cuerpo alejándote del brazo|Mantén 20 segundos|Repite del otro lado")
                put(COL_BENEFICIOS, "Mejora postura, abre pecho, facilita respiración")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Ejercicio de Dedos y Manos")
                put(COL_DESCRIPCION, "Previene fatiga por uso prolongado del teclado")
                put(COL_TIPO_EJERCICIO, "MUÑECAS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 45)
                put(COL_URL_IMAGEN, "gif_dedos")
                put(COL_INSTRUCCIONES, "Abre y cierra las manos 10 veces|Toca cada dedo con el pulgar|Haz círculos con las muñecas|Sacude las manos 10 segundos|Repite 2 veces")
                put(COL_BENEFICIOS, "Previene calambres, mejora destreza, aumenta circulación")
            }
        )

        ejercicios.forEach { values ->
            db.insert(TABLE_EJERCICIOS, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En desarrollo: limpieza total para aplicar la nueva estructura
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ESTADISTICAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORIAL")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONFIG_NOTIF")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALARMAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EJERCICIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TIPO_USUARIO")
        onCreate(db)
    }
}