package com.example.timekeeper.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val color: String?,
    val title: String,
    val description: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime
    // Todo - repeat
    // Todo - notification / reminder
)
