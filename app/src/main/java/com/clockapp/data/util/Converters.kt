package com.clockapp.data.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {

    @TypeConverter
    fun fromSet(value: Set<Int>?): String? {
        if (value == null) return null
        val gson = Gson()
        val type = object : TypeToken<Set<Int>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toSet(value: String?): Set<Int>? {
        if (value == null) return null
        val gson = Gson()
        val type = object : TypeToken<Set<Int>>() {}.type
        return gson.fromJson(value, type)
    }
}