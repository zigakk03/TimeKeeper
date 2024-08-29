package com.example.timekeeper.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import java.time.LocalDateTime

@Dao
interface ReminderDao {

    // Insert or update a reminder, returns the id of the inserted reminder
    @Upsert
    suspend fun upsertReminder(reminder: Reminder): Long

    // Delete a reminder
    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    // Get all reminders that are older than today's date, newest first
    @Query("SELECT * FROM reminder WHERE dateTime <= :currentTime ORDER BY dateTime DESC")
    suspend fun getRelevantReminders(currentTime: LocalDateTime? = LocalDateTime.now()): MutableList<Reminder>

    // Gets a reminder with the given id
    @Query("SELECT * FROM reminder WHERE id=:id")
    suspend fun getReminder(id: Int): Reminder
}