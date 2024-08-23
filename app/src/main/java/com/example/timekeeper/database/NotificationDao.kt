package com.example.timekeeper.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import java.time.LocalDateTime

@Dao
interface NotificationDao {

    @Upsert
    suspend fun upsertNotification(notification: Notification): Long

    @Delete
    suspend fun deleteNotification(notification: Notification)


    @Query("SELECT * FROM notification WHERE dateTime <= :currentTime ORDER BY dateTime DESC")
    suspend fun getRelevantNotifications(currentTime: LocalDateTime? = LocalDateTime.now()): MutableList<Notification>

    @Query("SELECT * FROM notification WHERE id=:id")
    suspend fun getNotification(id: Int): Notification
}