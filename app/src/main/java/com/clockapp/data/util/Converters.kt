package com.clockapp.data.util

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromSet(value: Set<Int>?): String? {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toSet(value: String?): Set<Int>? {
        return if (value.isNullOrEmpty()) {
            emptySet()
        } else {
            value.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }
    }
}