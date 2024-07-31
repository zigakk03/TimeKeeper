package com.example.timekeeper

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(
    private val notificationDataTempLists: MutableList<NotificationDataTemp> = mutableListOf(
        NotificationDataTemp(null,"test", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("#E6DD0F","test1", "this\nis\na\nsimple\ntest", "12:12", "12.01.23"),
        NotificationDataTemp(null,"test2", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris condimentum felis at est mollis, ac venenatis turpis malesuada. Praesent suscipit vehicula nisi ac dapibus. Curabitur convallis risus sed pretium venenatis. Maecenas erat leo, interdum sit amet congue vel, tincidunt ac erat. Aliquam id eros vestibulum massa pellentesque efficitur non non mauris. Donec tincidunt pellentesque iaculis. Suspendisse leo enim, mattis vel ipsum non, porttitor consectetur ex. Vestibulum vestibulum tortor vel aliquet malesuada. Ut mi quam, consequat at augue quis, auctor sodales justo. Nam a varius nunc, et rutrum diam. Praesent nunc ligula, tincidunt at varius ac, aliquet ac massa.", "12:12", "12.01.23"),
        NotificationDataTemp("","test3", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("","test3", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("#E6DD0F","test3", "this is a colour test", "12:12", "12.01.23"),
        NotificationDataTemp("","test3", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("","test3", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp(null,"test3", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("","test3", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("","test3", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("#E6DD0F","test3", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("#2DE60F","test3 tetetzetetezteeghfhffghfhfnklmlkkcsz hfjk hfhjd zfuthig gfbkn hgjk gkjbkg hkhg", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("#2DE60F","test3 jsalkdjaj\nisajdsa\njane", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("#2DE60F","test3", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp(null,"test3", "this is a simple test", "12:12", "12.01.23"),
        NotificationDataTemp("","test3", "this is a simple test", "12:12", "12.01.23"),
        )
): RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification, parent, false)

        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val curNotification = notificationDataTempLists[position]
        if (!curNotification.colour.isNullOrEmpty()){
            holder.itemView.findViewById<View>(R.id.vColorStrip).setBackgroundColor(Color.parseColor(curNotification.colour))
        }
        holder.itemView.findViewById<TextView>(R.id.tvTitle).text = curNotification.title
        holder.itemView.findViewById<TextView>(R.id.tvDescription).text = curNotification.description
        holder.itemView.findViewById<TextView>(R.id.tvDate).text = curNotification.date
        holder.itemView.findViewById<TextView>(R.id.tvTime).text = curNotification.time
    }

    override fun getItemCount(): Int {
        return notificationDataTempLists.size
    }
}