package com.example.timekeeper.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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

    @TypeConverter
    fun fromDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(value) }
    }

    @TypeConverter
    fun dateToString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(value) }
    }

    @TypeConverter
    fun timeToString(time: LocalTime?): String? {
        return time?.toString()
    }
}