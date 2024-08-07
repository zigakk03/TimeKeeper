package com.example.timekeeper

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val colour: String?,
    val title: String,
    val description: String,
    val dateTime: LocalDateTime
)
