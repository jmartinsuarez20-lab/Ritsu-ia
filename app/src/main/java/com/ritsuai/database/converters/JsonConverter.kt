package com.ritsuai.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonConverter {
    @TypeConverter
    fun <T> fromString(value: String, classOfT: Class<T>): T? {
        return try {
            Gson().fromJson(value, classOfT)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun <T> fromObject(obj: T): String {
        return Gson().toJson(obj)
    }
}