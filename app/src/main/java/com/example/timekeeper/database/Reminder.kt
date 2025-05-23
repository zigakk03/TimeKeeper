package com.example.timekeeper.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val color: String?,
    val title: String,
    val description: String,
    val dateTime: LocalDateTime,
    val eventId: Int?
    )
