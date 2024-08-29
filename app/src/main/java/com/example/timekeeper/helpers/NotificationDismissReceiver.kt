package com.example.timekeeper.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timekeeper.adapters.NotificationAdapter
import com.example.timekeeper.database.ReminderDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Receiver to delete a reminder
class NotificationDismissReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Gets extra information
        val reminderId = intent.getIntExtra("reminder_id", -1)

        // Checks if the is extra information
        if (reminderId != -1) {
            // Database setup
            val db = ReminderDatabase.getDatabase(context)
            val reminderDao = db.reminderDao()
            GlobalScope.launch {
                // Gets the reminder
                val reminder = reminderDao.getReminder(reminderId)
                // Deletes the reminder
                reminderDao.deleteReminder(reminder)
            }
            // Checks if there is a mainReminderAdapter defined
            if (NotificationAdapter.mainReminderAdapter != null){
                // Removes and updates the reminder from the mainReminderAdapter
                NotificationAdapter.mainReminderAdapter!!.removeAndUpdateList(reminderId)
            }
        }
    }
}