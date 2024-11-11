package com.example.timekeeper.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.timekeeper.R
import com.example.timekeeper.database.Event
import com.example.timekeeper.database.ReminderDatabase
import com.example.timekeeper.fragments.CalendarFragment
import com.example.timekeeper.fragments.CalendarFragmentDirections
import com.example.timekeeper.fragments.HomeFragmentDirections
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class EventAdapter(
    private val eventDataLists: MutableList<Event>,
    private val appContext: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val calendarFragment: CalendarFragment
): RecyclerView.Adapter<EventAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reminder, parent, false)

        return ReminderViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val curEvent = eventDataLists[position]
        // If current reminder doesn't have a color, it uses the accent color
        if (!curEvent.color.isNullOrEmpty()){
            holder.itemView.findViewById<View>(R.id.vColorStrip).setBackgroundColor(Color.parseColor(curEvent.color))
        }
        // Set reminder attributes
        holder.itemView.findViewById<TextView>(R.id.tvTitle).text = curEvent.title
        holder.itemView.findViewById<TextView>(R.id.tvDescription).text = curEvent.description
        if (curEvent.startDate == curEvent.endDate) {
            holder.itemView.findViewById<TextView>(R.id.tvDate).text = curEvent.startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }
        else {
            holder.itemView.findViewById<TextView>(R.id.tvDate).text = curEvent.startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))  + " - " + curEvent.endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }
        if (curEvent.startTime != null && curEvent.endTime != null) {
            if (curEvent.startTime.hour == curEvent.endTime.hour && curEvent.startTime.minute == curEvent.endTime.minute) {
                holder.itemView.findViewById<TextView>(R.id.tvTime).text = curEvent.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            else {
                holder.itemView.findViewById<TextView>(R.id.tvTime).text = curEvent.startTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + curEvent.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
        } else {
            holder.itemView.findViewById<TextView>(R.id.tvTime).text = ""
        }

        // Set btnDelete onClick
        holder.itemView.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
            lifecycleScope.launch {
                // Database setup
                val db = ReminderDatabase.getDatabase(appContext)
                val reminderDao = db.reminderDao()
                // Delete the reminder in the database
                reminderDao.deleteEvent(curEvent)

                // Remove the reminder from the reminderDataLists
                eventDataLists.removeAt(position)
                // Notify the reminder removal
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)
                calendarFragment.updateCalendar()
            }
        }


        // Set btnEdit onClick
        holder.itemView.findViewById<ImageButton>(R.id.btnEdit).setOnClickListener {
            // Sets the navigation arguments
            val action = CalendarFragmentDirections.navigateCalendarToEditEvent(curEvent.id)
            // Navigates to edit reminder page
            Navigation.findNavController(holder.itemView).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return eventDataLists.size
    }

    /*
    // Removes and updates reminderDataLists of the given reminder id
    fun removeAndUpdateList(id: Int){
        val reminder = eventDataLists.find { it.id == id }
        val position = eventDataLists.indexOf(reminder)
        eventDataLists.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
     */
}