package com.comprartir.mobile.core.database

import androidx.room.TypeConverter
import java.time.Instant

class DatabaseTypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun fromStringList(value: List<String>?): String = 
        value?.takeIf { it.isNotEmpty() }?.joinToString(separator = ";") ?: ""

    @TypeConverter
    fun toStringList(value: String?): List<String> = value
        ?.takeIf { it.isNotBlank() }
        ?.split(";")
        ?: emptyList()
}
