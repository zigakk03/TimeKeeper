package com.example.timekeeper

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp
import java.time.LocalDateTime

@Dao
interface NotificationDao {

    @Upsert
    suspend fun upsertNotification(notification: Notification)

    @Delete
    suspend fun deleteNotification(notification: Notification)


    @Query("SELECT * FROM notification WHERE dateTime <= :currentTime ORDER BY dateTime DESC")
    suspend fun getRelevantNotifications(currentTime: LocalDateTime? = LocalDateTime.now()): MutableList<Notification>

    @Query("SELECT * FROM notification WHERE id=:id")
    suspend fun getNotification(id: Int): Notification
}