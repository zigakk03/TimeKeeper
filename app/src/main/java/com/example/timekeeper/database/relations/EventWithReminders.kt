package com.example.timekeeper.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.timekeeper.database.Event
import com.example.timekeeper.database.Reminder

data class EventWithReminders (
    @Embedded
    val event: Event,
    @Relation(
        parentColumn = "id",
        entityColumn = "eventId"
    )
    val reminders: List<Reminder>
)