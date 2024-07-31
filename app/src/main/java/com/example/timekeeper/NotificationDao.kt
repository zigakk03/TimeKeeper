package com.example.timekeeper

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp

@Dao
interface NotificationDao {

    @Upsert
    suspend fun upsertNotification(notification: Notification)

    @Delete
    suspend fun deleteNotification(notification: Notification)

    @Query("SELECT * FROM notification WHERE dateTime <= :currentTime ORDER BY dateTime ASC")
    fun getRelevantNotifications(currentTime: Timestamp? = Timestamp(System.currentTimeMillis())): Flow<List<Notification>>
}