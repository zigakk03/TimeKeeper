package com.example.timekeeper.adapters

import android.content.Context
import android.icu.text.Transliterator.Position
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
    private val appContext: Context
): RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var selectedItemPosition: Int = 0

    class CalendarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_cell, parent, false)

        // Multiplies the height of each element by 1/6 to make 6 rows
        view.layoutParams.height = (parent.height * (1/6.toDouble())).toInt()

        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val curDay = daysOfMonth[position]
        // Sets the day number
        holder.itemView.findViewById<TextView>(R.id.txtCalendarDay).text = curDay.dayString
        // Checks how to color the day number
        if (curDay.isActive){
            // Checks if the day is selected
            if (curDay.isSelected) {
                holder.itemView.findViewById<View>(R.id.vSelectedCircle).visibility = View.VISIBLE
                selectedItemPosition = holder.adapterPosition
            }
            else {
                holder.itemView.findViewById<View>(R.id.vSelectedCircle).visibility = View.INVISIBLE
            }

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

    // Returns single item at given position
    fun getItem(position: Int): CalendarCell {
        return daysOfMonth[position]
    }

    fun updateSelectedPosition(newPosition: Int) {
        daysOfMonth[selectedItemPosition].isSelected = false
        notifyItemChanged(selectedItemPosition, daysOfMonth[selectedItemPosition])
        selectedItemPosition = newPosition
        daysOfMonth[selectedItemPosition].isSelected = true
        notifyItemChanged(selectedItemPosition, daysOfMonth[selectedItemPosition])
    }
}