package io.github.thwisse.kentinsesi.data.local.db

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        val arr = JSONArray()
        value.orEmpty().forEach { arr.put(it) }
        return arr.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val arr = JSONArray(value)
        return buildList(arr.length()) {
            for (i in 0 until arr.length()) {
                add(arr.optString(i))
            }
        }
    }
}
