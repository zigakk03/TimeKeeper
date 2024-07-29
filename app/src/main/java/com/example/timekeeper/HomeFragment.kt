package com.example.timekeeper

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Notification RecyclerView setup
        val notificationRecyclerView: RecyclerView = view.findViewById(R.id.rvNotifications)
        notificationRecyclerView.layoutManager = LinearLayoutManager(context)
        val customNotificationAdapter = NotificationAdapter()
        notificationRecyclerView.adapter = customNotificationAdapter

        view.findViewById<FloatingActionButton>(R.id.fBtnAddNotification).setOnClickListener { Navigation.findNavController(view).navigate(R.id.navigate_to_add_notification) }

        return view
    }

}