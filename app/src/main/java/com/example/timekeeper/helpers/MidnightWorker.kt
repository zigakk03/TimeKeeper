package com.example.timekeeper.helpers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.timekeeper.adapters.NotificationAdapter
import com.example.timekeeper.database.Reminder
import com.example.timekeeper.database.ReminderDatabase
import com.example.timekeeper.database.RepeatType
import com.example.timekeeper.fragments.CalendarFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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

            // todo - notification shows later (if it has the time set)
        }

        return Result.success()
    }
}