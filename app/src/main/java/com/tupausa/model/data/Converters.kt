package com.tupausa.model.data

import androidx.room.TypeConverter
class Converters {
    @TypeConverter
    fun fromListIntToString(intList: List<Int>?): String? {
        return intList?.joinToString(separator = ",")
    }
    @TypeConverter
    fun fromStringToListInt(data: String?): List<Int>? {
        return if (data.isNullOrEmpty()) {
            emptyList()
        } else {
            data.split(",").map { it.toInt() }
        }
    }
}