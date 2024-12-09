package com.example.timekeeper.adapters

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
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.timekeeper.R
import com.example.timekeeper.database.Reminder
import com.example.timekeeper.database.ReminderDatabase
import com.example.timekeeper.fragments.CalendarFragmentDirections
import com.example.timekeeper.fragments.HomeFragmentDirections
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReminderAdapter(
    private val reminderDataLists: MutableList<Reminder>,
    private val appContext: Context,
    private val lifecycleScope: LifecycleCoroutineScope
): RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reminder, parent, false)

        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val curReminder = reminderDataLists[position]
        // If current reminder doesn't have a color, it uses the accent color
        if (!curReminder.color.isNullOrEmpty()){
            holder.itemView.findViewById<View>(R.id.vColorStrip).setBackgroundColor(Color.parseColor(curReminder.color))
        }
        // Set reminder attributes
        holder.itemView.findViewById<TextView>(R.id.tvTitle).text = curReminder.title
        holder.itemView.findViewById<TextView>(R.id.tvDescription).text = curReminder.description
        holder.itemView.findViewById<TextView>(R.id.tvDate).text = curReminder.dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        holder.itemView.findViewById<TextView>(R.id.tvTime).text = curReminder.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

        // Set btnDelete onClick
        holder.itemView.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
            lifecycleScope.launch {
                // Database setup
                val db = ReminderDatabase.getDatabase(appContext)
                val reminderDao = db.reminderDao()
                // Delete the reminder in the database
                reminderDao.deleteReminder(curReminder)

                // Remove the reminder from the reminderDataLists
                reminderDataLists.removeAt(position)
                // Notify the reminder removal
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)

                // Remove the corresponding notification
                with(NotificationManagerCompat.from(appContext)) {
                    cancel(curReminder.id)
                }
            }
        }

        // Set btnEdit onClick
        holder.itemView.findViewById<ImageButton>(R.id.btnEdit).setOnClickListener {
            if (curReminder.eventId == null) {
                // Sets the navigation arguments
                val action = HomeFragmentDirections.navigateHomeToEditReminder(curReminder.id)
                // Navigates to edit reminder page
                Navigation.findNavController(holder.itemView).navigate(action)
            }
            else {

                val dialogView = LayoutInflater.from(appContext)
                    .inflate(R.layout.alert_dialog, null)
                dialogView.findViewById<TextView>(R.id.txtAlertDialogTitle).text =
                    "What would you like to edit?"

                val optionsDialog =
                    AlertDialog.Builder(appContext, R.style.AlertDialog)
                        .setView(dialogView)
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()

                val options: Array<String> = arrayOf(
                    "Only this reminder",
                    "The entire event"
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
                            // Sets the navigation arguments
                            val action = HomeFragmentDirections.navigateHomeToEditReminder(curReminder.id)
                            // Navigates to edit reminder page
                            Navigation.findNavController(holder.itemView).navigate(action)
                        }
                        1 -> {
                            // Sets the navigation arguments
                            val action = HomeFragmentDirections.navigateHomeToEditEvent(
                                curReminder.eventId,
                                LocalDate.now()
                            )
                            // Navigates to edit reminder page
                            Navigation.findNavController(holder.itemView).navigate(action)
                        }
                    }

                    // dismiss dialog
                    optionsDialog.dismiss()
                }

                optionsDialog.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return reminderDataLists.size
    }

    // Removes and updates reminderDataLists of the given reminder id
    fun removeAndUpdateList(id: Int){
        val reminder = reminderDataLists.find { it.id == id }
        val position = reminderDataLists.indexOf(reminder)
        reminderDataLists.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}