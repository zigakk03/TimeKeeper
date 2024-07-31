package com.example.timekeeper

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Notification::class],
    version = 1
)
@TypeConverters(TimestampConverters::class)
abstract class NotificationDatabase: RoomDatabase() {
    abstract val dao: NotificationDao
}