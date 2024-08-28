package com.example.timekeeper.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
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
import com.example.timekeeper.database.Reminder
import com.example.timekeeper.database.ReminderDatabase
import com.example.timekeeper.fragments.HomeFragmentDirections
import kotlinx.coroutines.launch
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
        if (!curReminder.color.isNullOrEmpty()){
            holder.itemView.findViewById<View>(R.id.vColorStrip).setBackgroundColor(Color.parseColor(curReminder.color))
        }
        holder.itemView.findViewById<TextView>(R.id.tvTitle).text = curReminder.title
        holder.itemView.findViewById<TextView>(R.id.tvDescription).text = curReminder.description
        holder.itemView.findViewById<TextView>(R.id.tvDate).text = curReminder.dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        holder.itemView.findViewById<TextView>(R.id.tvTime).text = curReminder.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

        holder.itemView.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
            lifecycleScope.launch {
                //database setup
                val db = ReminderDatabase.getDatabase(appContext)
                val reminderDao = db.reminderDao()
                reminderDao.deleteReminder(curReminder)

                reminderDataLists.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)

                with(NotificationManagerCompat.from(appContext)) {
                    // notificationId is a unique int for each notification that you must define.
                    cancel(curReminder.id)
                }
            }
        }

        holder.itemView.findViewById<ImageButton>(R.id.btnEdit).setOnClickListener {
            val action = HomeFragmentDirections.navigateHomeToEditReminder(curReminder.id)

            Navigation.findNavController(holder.itemView).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return reminderDataLists.size
    }

    fun removeAndUpdateList(id: Int){
        val reminder = reminderDataLists.find { it.id == id }
        val position = reminderDataLists.indexOf(reminder)
        reminderDataLists.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}