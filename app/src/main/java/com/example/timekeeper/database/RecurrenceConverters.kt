package com.example.timekeeper.database

import androidx.room.TypeConverter

class RecurrenceConverters {
    @TypeConverter
    fun fromRecurrence(recurrence: RepeatType): String {
        return recurrence.name
    }

    @TypeConverter
    fun toRecurrence(recurrence: String): RepeatType {
        return RepeatType.valueOf(recurrence)
    }
}