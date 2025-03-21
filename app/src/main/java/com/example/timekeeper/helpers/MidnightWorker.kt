package com.example.timekeeper.helpers

import NotificationWorker
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.timekeeper.adapters.NotificationAdapter
import com.example.timekeeper.database.Event
import com.example.timekeeper.database.Reminder
import com.example.timekeeper.database.ReminderDao
import com.example.timekeeper.database.ReminderDatabase
import com.example.timekeeper.database.RepeatType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MidnightWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    val workerContext = context

    override fun doWork(): Result {
        CoroutineScope(Dispatchers.IO).launch {
            // Database setup
            val db = ReminderDatabase.getDatabase(workerContext)
            val dao = db.reminderDao()

            val date = LocalDate.now()

            val relevantEvents = dao.getEventsRelevantToSelectedDate(date)

            val filteredEvents = relevantEvents.filter { event ->
                // Calculate the difference between startDate and selectedDate based on the recurrence type
                val daysBetween =
                    ChronoUnit.DAYS.between(event.startDate.atStartOfDay(), date.atStartOfDay())
                        .toDouble()
                val weeksBetween = daysBetween / 7
                val monthsBetween =
                    ChronoUnit.MONTHS.between(event.startDate.atStartOfDay(), date.atStartOfDay())
                        .toDouble()
                val yearsBetween =
                    ChronoUnit.YEARS.between(event.startDate.atStartOfDay(), date.atStartOfDay())
                        .toDouble()


                // Determine if the event should occur on the selected date based on its recurrence type and interval
                when (event.repeatType) {
                    RepeatType.DAILY -> daysBetween >= 0 && daysBetween % event.repeatInterval == 0.0
                    RepeatType.WEEKLY -> weeksBetween >= 0 && weeksBetween % event.repeatInterval == 0.0
                    RepeatType.MONTHLY -> event.startDate.dayOfMonth == date.dayOfMonth && monthsBetween % event.repeatInterval == 0.0
                    RepeatType.YEARLY -> event.startDate.dayOfMonth == date.dayOfMonth && event.startDate.month == date.month && yearsBetween % event.repeatInterval == 0.0
                    else -> event.startDate == date  // Non-recurring event
                }
            }.toMutableList()

            for (event in filteredEvents){
                if (event.startTime == null) {
                    val reminderId = dao.upsertReminder(
                        Reminder(
                            0,
                            event.color,
                            event.title,
                            event.description,
                            LocalDateTime.now(),
                            event.id
                        )
                    )

                    // Shows a new notification referring to the previously created reminder
                    NotificationAdapter.createAndShowNotification(
                        workerContext,
                        event.color ?: "#FF0FA2E6",
                        event.title,
                        event.description,
                        reminderId.toInt()
                    )
                }
                else {
                    // Set the time for the alarm
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis() // Start with the current time
                        set(Calendar.HOUR_OF_DAY, event.startTime.hour) // Set hour (24-hour format)
                        set(Calendar.MINUTE, event.startTime.minute) // Set minute
                        set(Calendar.SECOND, 0) // Optional: Set seconds to 0
                    }
                    Log.i("Test", calendar.toString())

                    //  If the time has already passed it shows the notification
                    if (calendar.timeInMillis < System.currentTimeMillis()) {
                        val reminderId = dao.upsertReminder(
                            Reminder(
                                0,
                                event.color,
                                event.title,
                                event.description,
                                LocalDateTime.now(),
                                event.id
                            )
                        )

                        // Shows a new notification referring to the previously created reminder
                        NotificationAdapter.createAndShowNotification(
                            workerContext,
                            event.color ?: "#FF0FA2E6",
                            event.title,
                            event.description,
                            reminderId.toInt()
                        )
                    }
                    else {
                        scheduleNotificationWithWorkManager(
                            workerContext,
                            event
                        )
                    }

                }
            }

            // Notification event code
            val eventsWithNotifications = dao.getEventsRelevantToSelectedDateWithNotifications(date.plusDays(7))
            Log.i("test", "$eventsWithNotifications")
            
            for (i in 7 downTo 0) {
                val filteredENotifications = eventsWithNotifications.filter { event ->
                    // Calculate the difference between startDate and selectedDate based on the recurrence type
                    val daysBetween =
                        ChronoUnit.DAYS.between(event.startDate.atStartOfDay(), date.plusDays(i.toLong()).atStartOfDay())
                            .toDouble()
                    val weeksBetween = daysBetween / 7
                    val monthsBetween =
                        ChronoUnit.MONTHS.between(event.startDate.atStartOfDay(), date.plusDays(i.toLong()).atStartOfDay())
                            .toDouble()
                    val yearsBetween =
                        ChronoUnit.YEARS.between(event.startDate.atStartOfDay(), date.plusDays(i.toLong()).atStartOfDay())
                            .toDouble()

                    // Determine if the event should occur on the selected date based on its recurrence type and interval
                    when (event.repeatType) {
                        RepeatType.DAILY -> daysBetween >= 0 && daysBetween % event.repeatInterval == 0.0
                        RepeatType.WEEKLY -> weeksBetween >= 0 && weeksBetween % event.repeatInterval == 0.0
                        RepeatType.MONTHLY -> event.startDate.dayOfMonth == date.plusDays(i.toLong()).dayOfMonth && monthsBetween % event.repeatInterval == 0.0
                        RepeatType.YEARLY -> event.startDate.dayOfMonth == date.plusDays(i.toLong()).dayOfMonth && event.startDate.month == date.plusDays(i.toLong()).month && yearsBetween % event.repeatInterval == 0.0
                        else -> event.startDate == date.plusDays(i.toLong())  // Non-recurring event
                    }
                }.toMutableList()

                for (event in filteredENotifications){
                    if (event.notificationOptions != "" && event.notificationOptions != null) {
                        val splitOptions = event.notificationOptions.split(", ")

                        for (option in splitOptions) {
                            val duration = option.dropLast(1).toInt()
                            val period = option[option.length-1]

                            when (period){
                                'm' -> {
                                    if (event.startTime != null) {
                                        if (i == 1 && event.startTime.hour == 0 && event.startTime.minute < duration) {
                                            // Set the time for the alarm
                                            val calendar = Calendar.getInstance().apply {
                                                timeInMillis = System.currentTimeMillis() // Start with the current time
                                                set(Calendar.HOUR_OF_DAY, event.startTime.minusMinutes(duration.toLong()).hour) // Set hour (24-hour format)
                                                set(Calendar.MINUTE, event.startTime.minusMinutes(duration.toLong()).minute) // Set minute
                                                set(Calendar.SECOND, 0) // Optional: Set seconds to 0
                                            }

                                            //  If the time has already passed it shows the notification
                                            if (calendar.timeInMillis < System.currentTimeMillis()) {
                                                val reminderId = dao.upsertReminder(
                                                    Reminder(
                                                        0,
                                                        event.color,
                                                        event.title + " (" + option + ")",
                                                        event.description,
                                                        LocalDateTime.now(),
                                                        event.id
                                                    )
                                                )

                                                // Shows a new notification referring to the previously created reminder
                                                NotificationAdapter.createAndShowNotification(
                                                    workerContext,
                                                    event.color ?: "#FF0FA2E6",
                                                    event.title + " (" + option + ")",
                                                    event.description,
                                                    reminderId.toInt()
                                                )
                                            }
                                            else {
                                                scheduleNotificationWithWorkManager(
                                                    workerContext,
                                                    event
                                                )
                                            }
                                        }
                                        else if (i == 0) {
                                            // Set the time for the alarm
                                            val calendar = Calendar.getInstance().apply {
                                                timeInMillis = System.currentTimeMillis() // Start with the current time
                                                set(Calendar.HOUR_OF_DAY, event.startTime.minusMinutes(duration.toLong()).hour) // Set hour (24-hour format)
                                                set(Calendar.MINUTE, event.startTime.minusMinutes(duration.toLong()).minute) // Set minute
                                                set(Calendar.SECOND, 0) // Optional: Set seconds to 0
                                            }

                                            //  If the time has already passed it shows the notification
                                            if (calendar.timeInMillis < System.currentTimeMillis()) {
                                                val reminderId = dao.upsertReminder(
                                                    Reminder(
                                                        0,
                                                        event.color,
                                                        event.title + " (" + option + ")",
                                                        event.description,
                                                        LocalDateTime.now(),
                                                        event.id
                                                    )
                                                )

                                                // Shows a new notification referring to the previously created reminder
                                                NotificationAdapter.createAndShowNotification(
                                                    workerContext,
                                                    event.color ?: "#FF0FA2E6",
                                                    event.title + " (" + option + ")",
                                                    event.description,
                                                    reminderId.toInt()
                                                )
                                            }
                                            else {
                                                scheduleNotificationWithWorkManager(
                                                    workerContext,
                                                    event
                                                )
                                            }
                                        }
                                    }
                                }
                                'h' -> {
                                    if (event.startTime != null) {
                                        if (i == 1 && event.startTime.hour < duration) {
                                            // Set the time for the alarm
                                            val calendar = Calendar.getInstance().apply {
                                                timeInMillis = System.currentTimeMillis() // Start with the current time
                                                set(Calendar.HOUR_OF_DAY, event.startTime.minusHours(duration.toLong()).hour) // Set hour (24-hour format)
                                                set(Calendar.MINUTE, event.startTime.minute) // Set minute
                                                set(Calendar.SECOND, 0) // Optional: Set seconds to 0
                                            }

                                            //  If the time has already passed it shows the notification
                                            if (calendar.timeInMillis < System.currentTimeMillis()) {
                                                val reminderId = dao.upsertReminder(
                                                    Reminder(
                                                        0,
                                                        event.color,
                                                        event.title + " (" + option + ")",
                                                        event.description,
                                                        LocalDateTime.now(),
                                                        event.id
                                                    )
                                                )

                                                // Shows a new notification referring to the previously created reminder
                                                NotificationAdapter.createAndShowNotification(
                                                    workerContext,
                                                    event.color ?: "#FF0FA2E6",
                                                    event.title + " (" + option + ")",
                                                    event.description,
                                                    reminderId.toInt()
                                                )
                                            }
                                            else {
                                                scheduleNotificationWithWorkManager(
                                                    workerContext,
                                                    event
                                                )
                                            }
                                        }
                                        else if (i == 0) {
                                            // Set the time for the alarm
                                            val calendar = Calendar.getInstance().apply {
                                                timeInMillis = System.currentTimeMillis() // Start with the current time
                                                set(Calendar.HOUR_OF_DAY, event.startTime.minusHours(duration.toLong()).hour) // Set hour (24-hour format)
                                                set(Calendar.MINUTE, event.startTime.minute) // Set minute
                                                set(Calendar.SECOND, 0) // Optional: Set seconds to 0
                                            }

                                            //  If the time has already passed it shows the notification
                                            if (calendar.timeInMillis < System.currentTimeMillis()) {
                                                val reminderId = dao.upsertReminder(
                                                    Reminder(
                                                        0,
                                                        event.color,
                                                        event.title + " (" + option + ")",
                                                        event.description,
                                                        LocalDateTime.now(),
                                                        event.id
                                                    )
                                                )

                                                // Shows a new notification referring to the previously created reminder
                                                NotificationAdapter.createAndShowNotification(
                                                    workerContext,
                                                    event.color ?: "#FF0FA2E6",
                                                    event.title + " (" + option + ")",
                                                    event.description,
                                                    reminderId.toInt()
                                                )
                                            }
                                            else {
                                                scheduleNotificationWithWorkManager(
                                                    workerContext,
                                                    event
                                                )
                                            }
                                        }
                                    }
                                }
                                'd' -> {
                                    if (duration == i){
                                        if (event.startTime == null) {
                                            val reminderId = dao.upsertReminder(
                                                Reminder(
                                                    0,
                                                    event.color,
                                                    event.title + " (" + option + ")",
                                                    event.description,
                                                    LocalDateTime.now(),
                                                    event.id
                                                )
                                            )

                                            // Shows a new notification referring to the previously created reminder
                                            NotificationAdapter.createAndShowNotification(
                                                workerContext,
                                                event.color ?: "#FF0FA2E6",
                                                event.title + " (" + option + ")",
                                                event.description,
                                                reminderId.toInt()
                                            )
                                        }
                                        else {
                                            // Set the time for the alarm
                                            val calendar = Calendar.getInstance().apply {
                                                timeInMillis = System.currentTimeMillis() // Start with the current time
                                                set(Calendar.HOUR_OF_DAY, event.startTime.hour) // Set hour (24-hour format)
                                                set(Calendar.MINUTE, event.startTime.minute) // Set minute
                                                set(Calendar.SECOND, 0) // Optional: Set seconds to 0
                                            }

                                            //  If the time has already passed it shows the notification
                                            if (calendar.timeInMillis < System.currentTimeMillis()) {
                                                val reminderId = dao.upsertReminder(
                                                    Reminder(
                                                        0,
                                                        event.color,
                                                        event.title + " (" + option + ")",
                                                        event.description,
                                                        LocalDateTime.now(),
                                                        event.id
                                                    )
                                                )

                                                // Shows a new notification referring to the previously created reminder
                                                NotificationAdapter.createAndShowNotification(
                                                    workerContext,
                                                    event.color ?: "#FF0FA2E6",
                                                    event.title + " (" + option + ")",
                                                    event.description,
                                                    reminderId.toInt()
                                                )
                                            }
                                            else {
                                                scheduleNotificationWithWorkManager(
                                                    workerContext,
                                                    event
                                                )
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        return Result.success()
    }

    private fun scheduleNotificationWithWorkManager(context: Context, event: Event) {
        // Calculate the time difference for the notification
        val notificationDelay = ChronoUnit.MILLIS.between(
            LocalDateTime.now(),
            event.startTime!!.atDate(LocalDate.now())
        )

        // Create data to pass to the worker
        val data = Data.Builder()
            .putString("color", event.color)
            .putString("title", event.title)
            .putString("description", event.description)
            .putInt("event_id", event.id)
            .build()

        // Schedule a OneTimeWorkRequest for the notification
        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(notificationDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(notificationRequest)
    }
}