package com.example.timekeeper.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timekeeper.adapters.NotificationAdapter
import com.example.timekeeper.database.ReminderDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationDismissReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)

        if (reminderId != -1) {

            val db = ReminderDatabase.getDatabase(context)
            val reminderDao = db.reminderDao()
            GlobalScope.launch {
                val reminder = reminderDao.getReminder(reminderId)
                reminderDao.deleteReminder(reminder)
            }
            if (NotificationAdapter.mainReminderAdapter != null){
                NotificationAdapter.mainReminderAdapter!!.removeAndUpdateList(reminderId)
            }
        }
    }
}