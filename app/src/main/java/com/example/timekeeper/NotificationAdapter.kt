package com.example.timekeeper

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class NotificationAdapter(
    private val notificationDataLists: MutableList<Notification>,
    private val appContext: Context,
    private val lifecycleScope: LifecycleCoroutineScope
): RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification, parent, false)

        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val curNotification = notificationDataLists[position]
        if (!curNotification.colour.isNullOrEmpty()){
            holder.itemView.findViewById<View>(R.id.vColorStrip).setBackgroundColor(Color.parseColor(curNotification.colour))
        }
        holder.itemView.findViewById<TextView>(R.id.tvTitle).text = curNotification.title
        holder.itemView.findViewById<TextView>(R.id.tvDescription).text = curNotification.description
        holder.itemView.findViewById<TextView>(R.id.tvDate).text = curNotification.dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        holder.itemView.findViewById<TextView>(R.id.tvTime).text = curNotification.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

        holder.itemView.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
            lifecycleScope.launch {
                //database setup
                val db = NotificationDatabase.getDatabase(appContext)
                val notifDao = db.notificationDao()
                notifDao.deleteNotification(curNotification)

                notificationDataLists.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)

            }
        }
    }

    override fun getItemCount(): Int {
        return notificationDataLists.size
    }
}