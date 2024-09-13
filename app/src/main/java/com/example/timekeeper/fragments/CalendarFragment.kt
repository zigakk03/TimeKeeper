package com.example.timekeeper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timekeeper.R
import com.example.timekeeper.adapters.CalendarAdapter
import com.example.timekeeper.helpers.CalendarCell
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Todo - coment
// Todo - swipe functionality
// Todo - select date
class CalendarFragment : Fragment() {

    val today = LocalDate.now()
    var selectedMonth = LocalDate.now()
    val dateFormat = DateTimeFormatter.ofPattern("yyyy-M-d")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        updateCalendar(view)

        return view
    }

    fun updateCalendar (view: View) {

        view.findViewById<TextView>(R.id.txtYear).text = selectedMonth.format(DateTimeFormatter.ofPattern("YYYY"))
        view.findViewById<TextView>(R.id.txtMonth).text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM"))

        val days: MutableList<CalendarCell> = mutableListOf()

        if (LocalDate.parse(selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-1", dateFormat).dayOfWeek != DayOfWeek.MONDAY){
            var maxDaysOfMonth = LocalDate.parse(selectedMonth.year.toString() + "-" + (selectedMonth.month.value - 1) + "-1", dateFormat).month.maxLength()
            do {
                days.add(CalendarCell(
                    maxDaysOfMonth.toString(),
                    false,
                    false,
                    false))
                maxDaysOfMonth--
            } while (LocalDate.parse(selectedMonth.year.toString() + "-" + (selectedMonth.month.value - 1) + "-" + maxDaysOfMonth, dateFormat).dayOfWeek != DayOfWeek.MONDAY)
            days.add(CalendarCell(maxDaysOfMonth.toString(), false, false, false))
            days.reverse()
        }

        val maxDaysOfMonth = LocalDate.parse(selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-1", dateFormat).month.maxLength()
        for (day in 1..maxDaysOfMonth){
            days.add(CalendarCell(
                day.toString(),
                (LocalDate.parse(selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-" + day, dateFormat).isEqual(today)),
                (LocalDate.parse(selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-" + day, dateFormat).dayOfWeek == DayOfWeek.SUNDAY),
                true
            ))
        }

        var counter = 1
        for (day in days.size..41){
            days.add(CalendarCell(
                counter.toString(),
                false,
                false,
                false
            ))
            counter++
        }

        val calendarRecyclerView: RecyclerView = view.findViewById(R.id.rvCalendar)
        calendarRecyclerView.layoutManager = object: GridLayoutManager(context, 7) {
            override fun canScrollVertically(): Boolean {
                return false // Disable vertical scrolling
            }

            override fun canScrollHorizontally(): Boolean {
                return true // Enable horizontal scrolling
            }
        }
        val customCalendarAdapter = CalendarAdapter(days, requireContext())
        calendarRecyclerView.adapter = customCalendarAdapter
    }
}