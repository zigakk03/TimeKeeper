package com.example.timekeeper.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
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
import java.time.format.DateTimeFormatter

class CalendarFragment : Fragment() {

    // Today's date
    private val today = LocalDate.now()

    // Selected month variable - default today's date
    private var selectedMonth = LocalDate.now()

    // Selected day variable - default today's date
    private var selectedDay = LocalDate.now()

    // Date format
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-M-d")

    // Gesture detector
    private lateinit var swipeGestureDetector: GestureDetector

    // Inflated view of the calendar page
    private lateinit var layoutView: View

    // Calendar adapter
    private lateinit var customCalendarAdapter: CalendarAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        layoutView = view
        swipeGestureDetector = GestureDetector(requireContext(), SwipeGestureListener())

        // Setup of the RecyclerView
        val calendarRecyclerView: RecyclerView = view.findViewById(R.id.rvCalendar)
        calendarRecyclerView.layoutManager = object : GridLayoutManager(context, 7) {
            override fun canScrollVertically(): Boolean {
                return false // Disable vertical scrolling
            }

            override fun canScrollHorizontally(): Boolean {
                return true // Enable horizontal scrolling
            }
        }
        // On touch listener for swipe mechanic
        calendarRecyclerView.setOnTouchListener { _, event ->
            swipeGestureDetector.onTouchEvent(event)
            false
        }

        // On touch listener to select a date
        val itemTouchListener = object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, event: MotionEvent): Boolean {
                val childView = rv.findChildViewUnder(event.x, event.y)
                if (childView != null && event.action == MotionEvent.ACTION_UP) {
                    val position = rv.getChildAdapterPosition(childView)
                    if (position != RecyclerView.NO_POSITION) {
                        // Handle the tap on the element at 'position'
                        onTap(position)
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                // Not handling touch events here
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                // Not used here, but needed for the interface
            }
        }
        calendarRecyclerView.addOnItemTouchListener(itemTouchListener)

        // Updates the calendar
        updateCalendar()

        return view
    }

    fun updateCalendar() {
        // First changes the text of the year and month
        layoutView.findViewById<TextView>(R.id.txtYear).text =
            selectedMonth.format(DateTimeFormatter.ofPattern("YYYY"))
        layoutView.findViewById<TextView>(R.id.txtMonth).text =
            selectedMonth.format(DateTimeFormatter.ofPattern("MMMM"))

        // Initialization of the list of all the dates
        val days: MutableList<CalendarCell> = mutableListOf()

        // Checks if the first day in the selected month is not a monday.
        // If it isn't it ads the previous months days from the last day to the last monday to days list
        if (LocalDate.parse(
                selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-1",
                dateFormat
            ).dayOfWeek != DayOfWeek.MONDAY
        ) {
            // Previous month max days
            var maxDaysOfMonth = LocalDate.parse(
                selectedMonth.minusMonths(1).year.toString() + "-" + selectedMonth.minusMonths(1).month.value + "-1",
                dateFormat
            ).month.length(selectedMonth.isLeapYear) + 1
            // Ads the previous months days from the last day to the last monday to days list
            do {
                maxDaysOfMonth--
                days.add(
                    CalendarCell(
                        maxDaysOfMonth.toString(),
                        false,
                        false,
                        false,
                        false
                    )
                )
            } while (LocalDate.parse(
                    selectedMonth.minusMonths(1).year.toString() + "-" + selectedMonth.minusMonths(1).month.value + "-" + maxDaysOfMonth,
                    dateFormat
                ).dayOfWeek != DayOfWeek.MONDAY
            )
            // Since the days ben added in reverse order reverses the order
            days.reverse()
        }

        // Adds all the days of the selected month to the list
        val maxDaysOfMonth = LocalDate.parse(
            selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-1",
            dateFormat
        ).month.length(selectedMonth.isLeapYear)
        for (day in 1..maxDaysOfMonth) {
            days.add(
                CalendarCell(
                    day.toString(),
                    (LocalDate.parse(
                        selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-" + day,
                        dateFormat
                    ).isEqual(today)),
                    (LocalDate.parse(
                        selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-" + day,
                        dateFormat
                    ).dayOfWeek == DayOfWeek.SUNDAY),
                    true,
                    (LocalDate.parse(
                        selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-" + day,
                        dateFormat
                    ).isEqual(selectedDay))
                )
            )
        }

        // Adds the next month days to the list until the list reaches the size of 42 elements
        var counter = 1
        for (day in days.size..41) {
            days.add(
                CalendarCell(
                    counter.toString(),
                    false,
                    false,
                    false,
                    false
                )
            )
            counter++
        }

        // Finds the RecyclerView and populates it with data
        val calendarRecyclerView: RecyclerView = layoutView.findViewById(R.id.rvCalendar)
        customCalendarAdapter = CalendarAdapter(days, requireContext())
        calendarRecyclerView.adapter = customCalendarAdapter
    }


    // SwipeGestureListener for swipe functionality
    private inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (Math.abs(diffX) > Math.abs(diffY)) {
                // Swipe left or right
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight()  // Swiped right go to previous month
                    } else {
                        onSwipeLeft()  // Swiped left go to next month
                    }
                    return true
                }
            }
            return false
        }
    }

    // Go to next month
    private fun onSwipeLeft() {
        selectedMonth = selectedMonth.plusMonths(1)
        updateCalendar()
    }

    // Go to previous month
    private fun onSwipeRight() {
        selectedMonth = selectedMonth.minusMonths(1)
        updateCalendar()
    }

    // Updates witch day is selected
    private fun onTap(position: Int) {
        val item = customCalendarAdapter.getItem(position)
        if (item.isActive){
            selectedDay = LocalDate.parse(
                selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-" + item.dayString,
                dateFormat
            )
            customCalendarAdapter.updateSelectedPosition(position)
        }
    }
}