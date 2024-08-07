package com.example.timekeeper

import androidx.room.TypeConverter
import java.sql.Timestamp
import java.time.LocalDateTime

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