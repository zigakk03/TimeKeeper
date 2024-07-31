package com.example.timekeeper

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val colour: String?,
    val title: String,
    val description: String,
    val dateTime: Timestamp
)
