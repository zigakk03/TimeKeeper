package com.example.timekeeper.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val color: String?,
    val title: String,
    val description: String,
    val startDate: LocalDate,
    val startTime: LocalTime?,
    val endDate: LocalDate,
    val endTime: LocalTime?,

    /*
    val recurrenceType: RecurrenceType? = null, // DAILY, WEEKLY, MONTHLY, YEARLY
    val recurrenceInterval: Int = 1,
    val recurrenceEnd: LocalDateTime? = null, // Recurrence end date
    */
    // Todo - notification / reminder
)

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}