package com.tupausa.database

import android.content.ContentValues
import android.content.Context
import com.tupausa.model.HistorialRegistro
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Companion object para definir constantes y nombres de tablas
    companion object {
        private const val DATABASE_NAME = "tupausa_database.db"
        private const val DATABASE_VERSION = 4

        // Definición de tablas y columnas

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
        const val COL_ONBOARDING_COMPLETADO = "onboarding_completado"
        const val COL_LIMITACIONES = "limitaciones"

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

        // Tabla Config_Notificaciones
        const val TABLE_CONFIG_NOTIF = "Config_Notificaciones"
        const val COL_ID_NOTIF = "id_notificacion"
        const val COL_TONO = "nombre_tono"

        // Tabla Historial_Ejecucion
        const val TABLE_HISTORIAL = "Historial_Ejecucion"
        const val COL_ID_REGISTRO = "id_registro"
        const val COL_FECHA_REALIZACION = "fecha_realizacion"
        const val COL_DURACION_REAL_SEG = "duracion_real_seg"
        const val COL_SE_DETECTO_MOV = "se_detecto_movimiento"
        const val COL_TIPO_DETECCION = "tipo_deteccion_usado"
        const val COL_SINCRONIZADO = "sincronizado"
        const val COL_RUTA_EVIDENCIA = "ruta_evidencia"
    }
    // Implementación de métodos de SQLiteOpenHelper
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear Tipo_usuario
        db.execSQL("CREATE TABLE $TABLE_TIPO_USUARIO ($COL_ID_TIPO_USUARIO INTEGER PRIMARY KEY AUTOINCREMENT, $COL_TIPO TEXT)")

        // Crear Usuarios
        val createUsuarios = ("CREATE TABLE $TABLE_USUARIOS ("
                + "$COL_ID_USUARIO INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_NOMBRE TEXT NOT NULL, "
                + "$COL_CORREO TEXT UNIQUE NOT NULL, "
                + "$COL_CONTRASENA TEXT NOT NULL, "
                + "$COL_FK_TIPO_USUARIO INTEGER, "
                + "$COL_ONBOARDING_COMPLETADO INTEGER DEFAULT 0, " // 0 es false, 1 es true
                + "$COL_LIMITACIONES TEXT DEFAULT '', "
                + "FOREIGN KEY($COL_FK_TIPO_USUARIO) REFERENCES $TABLE_TIPO_USUARIO($COL_ID_TIPO_USUARIO))")
        db.execSQL(createUsuarios)

        // Crear Ejercicios
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

        // Crear Historial_Ejecucion
        val createHistorial = ("CREATE TABLE $TABLE_HISTORIAL ("
                + "$COL_ID_REGISTRO INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_ID_USUARIO INTEGER, "
                + "$COL_ID_EJERCICIO INTEGER, "
                + "$COL_FECHA_REALIZACION INTEGER, "
                + "$COL_DURACION_REAL_SEG INTEGER, "
                + "$COL_SE_DETECTO_MOV INTEGER DEFAULT 0, "
                + "$COL_TIPO_DETECCION TEXT, "
                + "$COL_SINCRONIZADO INTEGER DEFAULT 0, "
                + "$COL_RUTA_EVIDENCIA TEXT, "
                + "FOREIGN KEY($COL_ID_USUARIO) REFERENCES $TABLE_USUARIOS($COL_ID_USUARIO) ON DELETE CASCADE, "
                + "FOREIGN KEY($COL_ID_EJERCICIO) REFERENCES $TABLE_EJERCICIOS($COL_ID_EJERCICIO))")
        db.execSQL(createHistorial)

        // Crear Config_Notificaciones
        val createConfig = ("CREATE TABLE $TABLE_CONFIG_NOTIF ("
                + "$COL_ID_NOTIF INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COL_ID_USUARIO INTEGER, "
                + "$COL_TONO TEXT, "
                + "FOREIGN KEY($COL_ID_USUARIO) REFERENCES $TABLE_USUARIOS($COL_ID_USUARIO) ON DELETE CASCADE)")
        db.execSQL(createConfig)

        insertarDatosIniciales(db)
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO $TABLE_TIPO_USUARIO ($COL_TIPO) VALUES ('Estudiante')")
        db.execSQL("INSERT INTO $TABLE_TIPO_USUARIO ($COL_TIPO) VALUES ('Administrador')")

        val ejercicios = listOf(
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Rotación de Cuello")
                put(COL_DESCRIPCION, "Alivia la tensión cervical con rotaciones suaves de cabeza")
                put(COL_TIPO_EJERCICIO, "CUELLO")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_cuello_rotacion")
                put(
                    COL_INSTRUCCIONES,
                    "Siéntate derecho con la espalda recta|Gira lentamente la cabeza hacia la derecha|Mantén la posición 5 segundos|Regresa al centro|Repite hacia la izquierda|Realiza 5 repeticiones completas"
                )
                put(
                    COL_BENEFICIOS,
                    "Reduce tensión cervical, mejora movilidad del cuello, previene dolores de cabeza"
                )
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Estiramiento de Muñecas")
                put(
                    COL_DESCRIPCION,
                    "Previene el síndrome del túnel carpiano y alivia tensión en manos"
                )
                put(COL_TIPO_EJERCICIO, "MUÑECAS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 45)
                put(COL_URL_IMAGEN, "gif_munecas")
                put(
                    COL_INSTRUCCIONES,
                    "Extiende el brazo frente a ti con la palma hacia arriba|Con la otra mano, tira suavemente de los dedos hacia atrás|Mantén el estiramiento 10 segundos|Haz lo mismo con la palma hacia abajo|Cambia de mano|Realiza 3 series en cada mano"
                )
                put(
                    COL_BENEFICIOS,
                    "Previene lesiones por movimientos repetitivos, alivia dolor en muñecas"
                )
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Regla 20-20-20 para Ojos")
                put(COL_DESCRIPCION, "Descansa tu vista del monitor para prevenir fatiga visual")
                put(COL_TIPO_EJERCICIO, "OJOS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 20)
                put(COL_URL_IMAGEN, "gif_ojos")
                put(
                    COL_INSTRUCCIONES,
                    "Cada 20 minutos de trabajo|Mira algo a 20 pies de distancia (6 metros)|Mantén la mirada durante 20 segundos|Parpadea varias veces|Vuelve a tu trabajo"
                )
                put(COL_BENEFICIOS, "Reduce fatiga visual, previene ojo seco, mejora enfoque")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Estiramiento de Espalda Alta")
                put(COL_DESCRIPCION, "Alivia la tensión en la parte superior de la espalda")
                put(COL_TIPO_EJERCICIO, "ESPALDA")
                put(COL_NIVEL_INTENSIDAD, "MEDIO")
                put(COL_DURACION, 90)
                put(COL_URL_IMAGEN, "gif_espalda")
                put(
                    COL_INSTRUCCIONES,
                    "De pie, entrelaza las manos frente a ti|Estira los brazos hacia adelante redondeando la espalda|Mantén 15 segundos|Lleva las manos por encima de la cabeza|Inclínate suavemente a cada lado|Repite 3 veces"
                )
                put(COL_BENEFICIOS, "Mejora postura, alivia dolor lumbar, aumenta flexibilidad")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Respiración Profunda 4-4-6")
                put(COL_DESCRIPCION, "Reduce el estrés mediante respiración controlada")
                put(COL_TIPO_EJERCICIO, "RESPIRACIÓN")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 120)
                put(COL_URL_IMAGEN, "gif_respiracion")
                put(
                    COL_INSTRUCCIONES,
                    "Siéntate cómodamente con la espalda recta|Inhala por la nariz contando hasta 4|Mantén el aire contando hasta 4|Exhala por la boca contando hasta 6|Repite 5 veces"
                )
                put(
                    COL_BENEFICIOS,
                    "Reduce ansiedad, mejora concentración, regula presión arterial"
                )
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Estiramiento de Hombros")
                put(COL_DESCRIPCION, "Libera tensión en hombros y trapecios")
                put(COL_TIPO_EJERCICIO, "HOMBROS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_hombros")
                put(
                    COL_INSTRUCCIONES,
                    "Cruza el brazo derecho sobre el pecho|Con la mano izquierda, presiona suavemente el codo derecho|Mantén 15 segundos|Haz círculos con los hombros 10 veces|Cambia de brazo"
                )
                put(COL_BENEFICIOS, "Alivia tensión en hombros, mejora movilidad")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Sentadillas de Escritorio")
                put(COL_DESCRIPCION, "Activa piernas y glúteos sin salir del espacio de trabajo")
                put(COL_TIPO_EJERCICIO, "PIERNAS")
                put(COL_NIVEL_INTENSIDAD, "MEDIO")
                put(COL_DURACION, 90)
                put(COL_URL_IMAGEN, "gif_sentadilla")
                put(
                    COL_INSTRUCCIONES,
                    "De pie, separa los pies al ancho de hombros|Baja como si fueras a sentarte|Mantén espalda recta|Sube lentamente|Realiza 10-15 repeticiones"
                )
                put(COL_BENEFICIOS, "Mejora circulación, fortalece piernas, activa metabolismo")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Torsión Espinal en Silla")
                put(COL_DESCRIPCION, "Libera la tensión de la zona lumbar y media de la espalda sin levantarte")
                put(COL_TIPO_EJERCICIO, "ESPALDA")
                put(COL_NIVEL_INTENSIDAD, "MEDIO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_espalda_torsion")
                put(
                    COL_INSTRUCCIONES,
                    "Siéntate de lado en tu silla con la espalda recta|Cruza la pierna derecha sobre la izquierda|Gira el torso hacia la derecha sujetando el respaldo de la silla|Mantén 15 segundos respirando profundo|Regresa al centro lentamente|Cambia de posición y repite hacia el lado izquierdo"
                )
                put(COL_BENEFICIOS, "Mejora movilidad de la columna, alivia dolor lumbar crónico, relaja la musculatura paravertebral")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Estiramiento de Pectorales")
                put(COL_DESCRIPCION, "Contrarresta la postura encorvada del escritorio")
                put(COL_TIPO_EJERCICIO, "HOMBROS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_pectorales")
                put(
                    COL_INSTRUCCIONES,
                    "De pie junto a una pared|Coloca el antebrazo en la pared|Gira el cuerpo alejándote del brazo|Mantén 20 segundos|Repite del otro lado"
                )
                put(COL_BENEFICIOS, "Mejora postura, abre pecho, facilita respiración")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Ejercicio de Dedos y Manos")
                put(COL_DESCRIPCION, "Previene fatiga por uso prolongado del teclado")
                put(COL_TIPO_EJERCICIO, "MUÑECAS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 45)
                put(COL_URL_IMAGEN, "gif_dedos")
                put(
                    COL_INSTRUCCIONES,
                    "Abre y cierra las manos 10 veces|Toca cada dedo con el pulgar|Haz círculos con las muñecas|Sacude las manos 10 segundos|Repite 2 veces"
                )
                put(COL_BENEFICIOS, "Previene calambres, mejora destreza, aumenta circulación")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Marcha en el Sitio")
                put(COL_DESCRIPCION, "Reactiva tu circulación sanguínea y eleva tu energía")
                put(COL_TIPO_EJERCICIO, "CARDIO SUAVE")
                put(COL_NIVEL_INTENSIDAD, "MEDIO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_marcha")
                put(
                    COL_INSTRUCCIONES,
                    "Ponte de pie con espacio suficiente|Levanta las rodillas alternadamente como si marcharas|Mueve los brazos al ritmo de las piernas|Mantén un ritmo constante|Respira fluidamente por la nariz"
                )
                put(
                    COL_BENEFICIOS,
                    "Mejora el retorno venoso, despierta el cerebro, aumenta la energía"
                )
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Rotación de Tobillos")
                put(COL_DESCRIPCION, "Mejora la circulación en los pies y previene hinchazón")
                put(COL_TIPO_EJERCICIO, "PIERNAS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_tobillos")
                put(
                    COL_INSTRUCCIONES,
                    "Siéntate y levanta un pie del suelo|Dibuja círculos grandes con la punta del pie|Haz 10 círculos hacia la derecha|Haz 10 círculos hacia la izquierda|Repite con el otro pie"
                )
                put(
                    COL_BENEFICIOS,
                    "Previene la retención de líquidos, fortalece tobillos, evita calambres"
                )
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Estiramiento Lateral de Cuello")
                put(COL_DESCRIPCION, "Libera la tensión acumulada en los laterales del cuello y trapecio superior")
                put(COL_TIPO_EJERCICIO, "CUELLO")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_cuello_lateral")
                put(
                    COL_INSTRUCCIONES,
                    "Siéntate derecho y relaja los hombros|Inclina lentamente la cabeza llevando la oreja derecha hacia el hombro derecho|Coloca tu mano derecha sobre la cabeza para dar un ligero peso (sin jalar)|Mantén el estiramiento 15 segundos|Regresa al centro y cambia de lado|Realiza 2 repeticiones por lado"
                )
                put(COL_BENEFICIOS, "Disminuye rigidez cervical, alivia dolor tensional, mejora la postura de la cabeza")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Gimnasia Visual en Cruz")
                put(COL_DESCRIPCION, "Fortalece los músculos oculares tras pasar horas enfocando un punto fijo")
                put(COL_TIPO_EJERCICIO, "OJOS")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 45)
                put(COL_URL_IMAGEN, "gif_ojos_cruz")
                put(
                    COL_INSTRUCCIONES,
                    "Siéntate con la cabeza recta y quieta (no la muevas)|Mira lo más arriba que puedas por 3 segundos|Mira hacia abajo por 3 segundos|Mira al extremo derecho por 3 segundos|Mira al extremo izquierdo por 3 segundos|Cierra los ojos fuerte, relaja y repite 3 veces"
                )
                put(COL_BENEFICIOS, "Flexibiliza músculos oculares, mejora la lubricación, reduce cefaleas tensionales")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Respiración Abdominal")
                put(COL_DESCRIPCION, "Activa tu sistema de relajación profundo para reducir el estrés del código")
                put(COL_TIPO_EJERCICIO, "RESPIRACIÓN")
                put(COL_NIVEL_INTENSIDAD, "BAJO")
                put(COL_DURACION, 120)
                put(COL_URL_IMAGEN, "gif_resp_abdominal")
                put(
                    COL_INSTRUCCIONES,
                    "Coloca una mano sobre tu pecho y otra sobre tu estómago|Inhala profundamente por la nariz sintiendo cómo se infla tu estómago|Asegúrate de que la mano del pecho casi no se mueva|Exhala lentamente por la boca hundiendo el estómago|Repite concentradamente por 2 minutos"
                )
                put(COL_BENEFICIOS, "Activa el nervio vago, oxigena el cerebro, libera tensión diafragmática")
            },
            ContentValues().apply {
                put(COL_NOMBRE_EJERCICIO, "Paso Lateral con Brazos")
                put(COL_DESCRIPCION, "Eleva tu ritmo cardíaco suavemente sin impacto ni ruido")
                put(COL_TIPO_EJERCICIO, "CARDIO SUAVE")
                put(COL_NIVEL_INTENSIDAD, "MEDIO")
                put(COL_DURACION, 60)
                put(COL_URL_IMAGEN, "gif_paso_lateral")
                put(
                    COL_INSTRUCCIONES,
                    "Ponte de pie con los pies juntos y brazos a los lados|Da un paso a la derecha mientras elevas ambos brazos sobre la cabeza|Regresa a la posición inicial bajando los brazos|Da un paso a la izquierda elevando los brazos de nuevo|Repite a un ritmo constante durante el tiempo establecido"
                )
                put(
                    COL_BENEFICIOS,
                    "Activa el sistema cardiovascular, oxigena todo el cuerpo, mejora la coordinación"
                )
            }
        )

        ejercicios.forEach { values ->
            db.insert(TABLE_EJERCICIOS, null, values)
        }
    }

    fun guardarPreferenciasUsuario(idUsuario: Int, limitaciones: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_ONBOARDING_COMPLETADO, 1) // Ya completó el cuestionario
            put(COL_LIMITACIONES, limitaciones)
        }
        db.update(TABLE_USUARIOS, values, "$COL_ID_USUARIO = ?", arrayOf(idUsuario.toString()))
        db.close()
    }

    fun obtenerLimitacionesUsuario(idUsuario: Int): List<String> {
        val db = this.readableDatabase
        var limitaciones = ""
        val cursor = db.rawQuery("SELECT $COL_LIMITACIONES FROM $TABLE_USUARIOS WHERE $COL_ID_USUARIO = ?", arrayOf(idUsuario.toString()))
        if (cursor.moveToFirst()) {
            limitaciones = cursor.getString(0) ?: ""
        }
        cursor.close()
        return if (limitaciones.isNotEmpty()) limitaciones.split(",") else emptyList()
    }

    fun obtenerTotalPausas(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_HISTORIAL WHERE $COL_ID_USUARIO = ?",
            arrayOf(userId.toString())
        )
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun obtenerTiempoTotalMinutos(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COL_DURACION_REAL_SEG) FROM $TABLE_HISTORIAL WHERE $COL_ID_USUARIO = ?",
            arrayOf(userId.toString())
        )
        var totalSegundos = 0
        if (cursor.moveToFirst()) {
            totalSegundos = cursor.getInt(0)
        }
        cursor.close()
        return totalSegundos / 60
    }

    fun insertarHistorial(
        idUsuario: Int,
        idEjercicio: Int,
        duracionSegundos: Int,
        tipoDeteccion: String = "MANUAL",
        rutaEvidencia: String? = null
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_ID_USUARIO, idUsuario)
            put(COL_ID_EJERCICIO, idEjercicio)
            put(COL_FECHA_REALIZACION, System.currentTimeMillis())
            put(COL_DURACION_REAL_SEG, duracionSegundos)
            put(COL_SE_DETECTO_MOV, 0)
            put(COL_TIPO_DETECCION, tipoDeteccion)
            put(COL_SINCRONIZADO, 0)
            put(COL_RUTA_EVIDENCIA, rutaEvidencia)
        }
        val id = db.insert(TABLE_HISTORIAL, null, values)
        db.close()
        return id
    }

    fun obtenerHistorialPorUsuario(userId: Int): List<HistorialRegistro> {
        val lista = ArrayList<HistorialRegistro>()
        val db = this.readableDatabase

        val query = """
        SELECT h.$COL_ID_REGISTRO, h.$COL_FECHA_REALIZACION, e.$COL_NOMBRE_EJERCICIO, 
               h.$COL_ID_EJERCICIO, h.$COL_DURACION_REAL_SEG, h.$COL_TIPO_DETECCION, h.$COL_RUTA_EVIDENCIA
        FROM $TABLE_HISTORIAL h
        INNER JOIN $TABLE_EJERCICIOS e ON h.$COL_ID_EJERCICIO = e.$COL_ID_EJERCICIO
        WHERE h.$COL_ID_USUARIO = ?
        ORDER BY h.$COL_FECHA_REALIZACION DESC
    """

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val item = HistorialRegistro(
                    id = cursor.getInt(0),
                    fecha = cursor.getLong(1),
                    nombreEjercicio = cursor.getString(2),
                    idEjercicio = cursor.getInt(3),
                    duracionSegundos = cursor.getInt(4),
                    tipoDeteccion = cursor.getString(5),
                    rutaEvidencia = cursor.getString(6)
                )
                lista.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun obtenerHistorialNoSincronizado(userId: Int): List<HistorialRegistro> {
        val lista = ArrayList<HistorialRegistro>()
        val db = this.readableDatabase

        val query = """
        SELECT h.$COL_ID_REGISTRO, h.$COL_FECHA_REALIZACION, e.$COL_NOMBRE_EJERCICIO, 
               h.$COL_ID_EJERCICIO, h.$COL_DURACION_REAL_SEG, h.$COL_TIPO_DETECCION, h.$COL_RUTA_EVIDENCIA
        FROM $TABLE_HISTORIAL h
        INNER JOIN $TABLE_EJERCICIOS e ON h.$COL_ID_EJERCICIO = e.$COL_ID_EJERCICIO
        WHERE h.$COL_ID_USUARIO = ? AND h.$COL_SINCRONIZADO = 0
    """

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val item = HistorialRegistro(
                    id = cursor.getInt(0),
                    fecha = cursor.getLong(1),
                    nombreEjercicio = cursor.getString(2),
                    idEjercicio = cursor.getInt(3),
                    duracionSegundos = cursor.getInt(4),
                    tipoDeteccion = cursor.getString(5),
                    rutaEvidencia = cursor.getString(6)
                )
                lista.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun marcarComoSincronizado(idRegistro: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_SINCRONIZADO, 1)
        }
        db.update(TABLE_HISTORIAL, values, "$COL_ID_REGISTRO = ?", arrayOf(idRegistro.toString()))
        db.close()
    }

    fun borrarTodoElHistorial(idUsuario: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_HISTORIAL, "$COL_ID_USUARIO = ?", arrayOf(idUsuario.toString()))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORIAL")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONFIG_NOTIF")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EJERCICIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TIPO_USUARIO")
        onCreate(db)
    }
}
