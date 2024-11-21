package com.example.timekeeper.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.timekeeper.R
import com.example.timekeeper.database.Event
import com.example.timekeeper.database.ReminderDatabase
import com.example.timekeeper.database.RepeatType
import com.example.timekeeper.fragments.CalendarFragment
import com.example.timekeeper.fragments.CalendarFragmentDirections
import com.example.timekeeper.fragments.HomeFragmentDirections
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

        // Set btnDelete onClick todo - complicated delete
        holder.itemView.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
            val db = ReminderDatabase.getDatabase(appContext)
            val reminderDao = db.reminderDao()

            if (curEvent.repeatType != RepeatType.NONE) {

                val dialogView = LayoutInflater.from(appContext)
                    .inflate(R.layout.alert_dialog, null)
                dialogView.findViewById<TextView>(R.id.txtAlertDialogTitle).text =
                    "How do you want to delete?"

                val optionsDialog =
                    AlertDialog.Builder(appContext, R.style.AlertDialog)
                        .setView(dialogView)
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()

                val options: Array<String> = arrayOf(
                    "Delete only this event",
                    "Delete this and all future events",
                    "Delete all occurrences of this event"
                )

                // Set up the ListView
                val listView = dialogView.findViewById<ListView>(R.id.lvOptions)
                val adapter = ArrayAdapter(
                    appContext,
                    android.R.layout.simple_list_item_1,
                    options
                )
                listView.adapter = adapter

                // Handle item clicks
                listView.setOnItemClickListener { _, _, i, _ ->
                    when (i) {
                        0 -> {
                            lifecycleScope.launch {
                                val nextEvent = when (curEvent.repeatType) {
                                    RepeatType.DAILY -> calendarFragment.selectedDay.plusDays(
                                        curEvent.repeatInterval.toLong()
                                    )

                                    RepeatType.WEEKLY -> calendarFragment.selectedDay.plusWeeks(
                                        curEvent.repeatInterval.toLong()
                                    )

                                    RepeatType.MONTHLY -> calendarFragment.selectedDay.plusMonths(
                                        curEvent.repeatInterval.toLong()
                                    )

                                    RepeatType.YEARLY -> calendarFragment.selectedDay.plusYears(
                                        curEvent.repeatInterval.toLong()
                                    )

                                    else -> calendarFragment.selectedDay
                                }
                                if (curEvent.repeatType != RepeatType.NONE && (nextEvent <= curEvent.repeatEnd || curEvent.repeatEnd == null)) {
                                    reminderDao.upsertEvent(
                                        Event(
                                            0,
                                            curEvent.color,
                                            curEvent.title,
                                            curEvent.description,
                                            nextEvent,
                                            curEvent.startTime,
                                            nextEvent.plusDays(
                                                ChronoUnit.DAYS.between(
                                                    curEvent.startDate,
                                                    curEvent.endDate
                                                )
                                            ),
                                            curEvent.endTime,
                                            curEvent.repeatType,
                                            curEvent.repeatInterval,
                                            curEvent.repeatEnd
                                        )
                                    )
                                }

                                if (calendarFragment.selectedDay != curEvent.startDate) {
                                    reminderDao.upsertEvent(
                                        Event(
                                            curEvent.id,
                                            curEvent.color,
                                            curEvent.title,
                                            curEvent.description,
                                            curEvent.startDate,
                                            curEvent.startTime,
                                            curEvent.endDate,
                                            curEvent.endTime,
                                            curEvent.repeatType,
                                            curEvent.repeatInterval,
                                            calendarFragment.selectedDay.minusDays(1)
                                        )
                                    )
                                } else {
                                    reminderDao.deleteEvent(curEvent)
                                }

                                notifyDataSetChanged()
                                calendarFragment.updateCalendar()
                            }
                        }

                        1 -> {
                            lifecycleScope.launch {
                                if (calendarFragment.selectedDay != curEvent.startDate) {
                                    reminderDao.upsertEvent(
                                        Event(
                                            curEvent.id,
                                            curEvent.color,
                                            curEvent.title,
                                            curEvent.description,
                                            curEvent.startDate,
                                            curEvent.startTime,
                                            curEvent.endDate,
                                            curEvent.endTime,
                                            curEvent.repeatType,
                                            curEvent.repeatInterval,
                                            calendarFragment.selectedDay.minusDays(1)
                                        )
                                    )
                                } else {
                                    reminderDao.deleteEvent(curEvent)
                                }
                                notifyDataSetChanged()
                                calendarFragment.updateCalendar()
                            }
                        }

                        2 -> {
                            lifecycleScope.launch {
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
                    }

                    // dismiss dialog
                    optionsDialog.dismiss()
                }

                optionsDialog.show()
            }
            else {
                lifecycleScope.launch {
                    // todo - are you sure
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
        }


        // Set btnEdit onClick
        holder.itemView.findViewById<ImageButton>(R.id.btnEdit).setOnClickListener {
            // Sets the navigation arguments
            val action = CalendarFragmentDirections.navigateCalendarToEditEvent(curEvent.id, calendarFragment.selectedDay)
            // Navigates to edit reminder page
            Navigation.findNavController(holder.itemView).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return eventDataLists.size
    }

}