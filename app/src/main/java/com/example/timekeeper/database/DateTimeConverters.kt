package com.example.timekeeper.database

import androidx.room.TypeConverter
import java.time.LocalDateTime

// Type converters from LocalDateTime to String
class DateTimeConverters {
    @TypeConverter
    fun fromDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(value) }
    }

    @TypeConverter
    fun dateTimeToString(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
}