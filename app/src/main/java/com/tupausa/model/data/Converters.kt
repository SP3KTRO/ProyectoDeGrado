package com.tupausa.model.data

import androidx.room.TypeConverter

class Converters {
    // Convierte una lista de días (ej: [1, 3, 5]) a texto "1,3,5" para guardar en DB
    @TypeConverter
    fun fromListIntToString(intList: List<Int>?): String? {
        return intList?.joinToString(separator = ",")
    }

    // Convierte el texto "1,3,5" de vuelta a una lista [1, 3, 5] para usar en la app
    @TypeConverter
    fun fromStringToListInt(data: String?): List<Int>? {
        return if (data.isNullOrEmpty()) {
            emptyList()
        } else {
            data.split(",").map { it.toInt() }
        }
    }
}