package com.tupausa.model.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Alarma::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // ¡Importante! Aquí registramos tu traductor de Listas
abstract class AppDatabase : RoomDatabase() {

    // 1. Aquí exponemos los DAOs (nuestras herramientas)
    abstract fun alarmaDao(): AlarmaDao

    // 2. Patrón Singleton (Para que solo exista UNA base de datos en toda la app)
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tupausa_database" // Nombre del archivo físico en el celular
                )
                    // Esto permite que si cambias la base de datos, borre la vieja y empiece de cero
                    // (Útil en desarrollo para evitar crashes por cambios de columnas)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}