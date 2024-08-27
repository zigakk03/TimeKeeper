package com.example.timekeeper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timekeeper.adapters.ReminderAdapter
import com.example.timekeeper.R
import com.example.timekeeper.database.ReminderDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        lifecycleScope.launch {
            //database setup
            val db = ReminderDatabase.getDatabase(requireContext())
            val notifDao = db.reminderDao()
            val relevantNotifications = notifDao.getRelevantReminders()

            // Notification RecyclerView setup
            val notificationRecyclerView: RecyclerView = view.findViewById(R.id.rvNotifications)
            notificationRecyclerView.layoutManager = LinearLayoutManager(context)
            val customReminderAdapter = ReminderAdapter(relevantNotifications, requireContext(), lifecycleScope)
            notificationRecyclerView.adapter = customReminderAdapter
        }



        view.findViewById<FloatingActionButton>(R.id.fBtnAddNotification).setOnClickListener { Navigation.findNavController(view).navigate(
            R.id.navigate_to_add_reminder
        ) }

        return view
    }

}