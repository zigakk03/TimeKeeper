package com.example.timekeeper.adapters

import android.content.Context
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.timekeeper.R
import com.example.timekeeper.helpers.CalendarCell

class CalendarAdapter(
    private val daysOfMonth: MutableList<CalendarCell>,
    private val appContext: Context,
): RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_cell, parent, false)

        view.layoutParams.height = (parent.height * (1/6.toDouble())).toInt()

        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val curDay = daysOfMonth[position]
        holder.itemView.findViewById<TextView>(R.id.txtCalendarDay).text = curDay.dayString
        if (curDay.isActive){
            if (curDay.isToday){
                holder.itemView.findViewById<TextView>(R.id.txtCalendarDay).setTextColor(
                    ContextCompat.getColor(appContext, R.color.accent)
                )
            }
            else if (curDay.isDayOff){
                holder.itemView.findViewById<TextView>(R.id.txtCalendarDay).setTextColor(
                    ContextCompat.getColor(appContext, R.color.alert)
                )
            }
        } else {
            holder.itemView.findViewById<TextView>(R.id.txtCalendarDay).setTextColor(
                ContextCompat.getColor(appContext, R.color.gray)
            )
        }
    }

    override fun getItemCount(): Int {
        return daysOfMonth.size
    }

}