package com.example.timekeeper.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.timekeeper.adapters.ReminderAdapter
import com.example.timekeeper.R
import com.example.timekeeper.adapters.NotificationAdapter
import com.example.timekeeper.database.ReminderDatabase
import com.example.timekeeper.helpers.MidnightWorker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleDailyWork(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        lifecycleScope.launch {
            // Database setup
            val db = ReminderDatabase.getDatabase(requireContext())
            val notifDao = db.reminderDao()
            val relevantNotifications = notifDao.getRelevantReminders()

            // Notification RecyclerView setup
            val notificationRecyclerView: RecyclerView = view.findViewById(R.id.rvNotifications)
            notificationRecyclerView.layoutManager = LinearLayoutManager(context)
            val customReminderAdapter = ReminderAdapter(relevantNotifications, requireContext(), lifecycleScope)
            notificationRecyclerView.adapter = customReminderAdapter

            // Set mainReminderAdapter for reference across the app
            NotificationAdapter.mainReminderAdapter = customReminderAdapter
        }

        // Set the floating button onClick to navigate to add reminder screen
        view.findViewById<FloatingActionButton>(R.id.fBtnAddNotification).setOnClickListener {
            // Sets the navigation arguments
            val action = HomeFragmentDirections.navigateToAddReminder(null)
            // Navigates to edit reminder page
            Navigation.findNavController(view).navigate(action)
        }

        view.findViewById<FloatingActionButton>(R.id.fBtnToCalendar).setOnClickListener { Navigation.findNavController(view).navigate(
            R.id.navigate_home_to_calendar
        ) }

        return view
    }

    fun scheduleDailyWork(context: Context) {
        // Constraints for the work to run
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()

        // Create the PeriodicWorkRequest
        val workRequest = PeriodicWorkRequestBuilder<MidnightWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS) // Calculate delay
            .build()

        // Schedule the work
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "MidnightWorker", // Unique name to avoid duplicates
            ExistingPeriodicWorkPolicy.REPLACE, // Replace if it already exists
            workRequest
        )
    }

    // Function to calculate the initial delay until the next midnight
    private fun calculateInitialDelay(): Long {
        val currentTime = System.currentTimeMillis()
        val nextMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1) // Move to the next day
        }.timeInMillis
        return nextMidnight - currentTime
    }
}