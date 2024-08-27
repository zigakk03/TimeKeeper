package com.example.timekeeper.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import java.time.LocalDateTime

@Dao
interface ReminderDao {

    @Upsert
    suspend fun upsertReminder(reminder: Reminder): Long

    @Delete
    suspend fun deleteReminder(reminder: Reminder)


    @Query("SELECT * FROM reminder WHERE dateTime <= :currentTime ORDER BY dateTime DESC")
    suspend fun getRelevantReminders(currentTime: LocalDateTime? = LocalDateTime.now()): MutableList<Reminder>

    @Query("SELECT * FROM reminder WHERE id=:id")
    suspend fun getReminder(id: Int): Reminder
}