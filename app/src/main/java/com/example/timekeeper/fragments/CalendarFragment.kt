package com.example.timekeeper.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timekeeper.R
import com.example.timekeeper.adapters.CalendarAdapter
import com.example.timekeeper.adapters.EventAdapter
import com.example.timekeeper.database.Event
import com.example.timekeeper.database.ReminderDatabase
import com.example.timekeeper.database.RepeatType
import com.example.timekeeper.helpers.CalendarCell
import com.example.timekeeper.helpers.EventDataTransfer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong

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

        view.findViewById<FloatingActionButton>(R.id.fBtnAddEvent).setOnClickListener {
            Navigation.findNavController(view).navigate(
                R.id.navigate_calendar_to_newReminder
            )
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        updateMonthYearText()
        // Updates the calendar
        Handler(Looper.getMainLooper()).postDelayed({
            updateCalendar()
        }, 500)

        Thread {
            updateViewableEvents()
        }.start()
    }


    // ---------- calender logic and population ----------


    fun updateCalendar() {
        // Update UI components immediately with the basics
        updateMonthYearText()

        CoroutineScope(Dispatchers.Main).launch {
            val days =
                withContext(Dispatchers.Default) { loadCalendarCells() } // Load calendar cells in the background
            setupCalendarAdapter(days) // Populate RecyclerView after days are loaded
            loadEvents(days)
        }
    }

    private fun updateMonthYearText() {
        // Changes the text of the year and month
        layoutView.findViewById<TextView>(R.id.txtYear).text =
            selectedMonth.format(DateTimeFormatter.ofPattern("YYYY"))
        layoutView.findViewById<TextView>(R.id.txtMonth).text =
            selectedMonth.format(DateTimeFormatter.ofPattern("MMMM"))
    }

    private fun loadCalendarCells(): MutableList<CalendarCell> {
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
            val maxDaysOfMonth = LocalDate.parse(
                selectedMonth.minusMonths(1).year.toString() + "-" + selectedMonth.minusMonths(1).month.value + "-1",
                dateFormat
            ).month.length(selectedMonth.isLeapYear) + 1

            var date = LocalDate.parse(
                selectedMonth.minusMonths(1).year.toString() + "-" + selectedMonth.minusMonths(1).month.value + "-" + (maxDaysOfMonth - 1),
                dateFormat
            )

            // Ads the previous months days from the last day to the last monday to days list
            while (date.dayOfWeek != DayOfWeek.MONDAY) {
                days.add(
                    CalendarCell(
                        date.dayOfMonth.toString(),
                        mutableListOf(),
                        false,
                        false,
                        false,
                        false
                    )
                )

                date = date.minusDays(1)
            }

            if (date.dayOfWeek == DayOfWeek.MONDAY) {
                days.add(
                    CalendarCell(
                        date.dayOfMonth.toString(),
                        mutableListOf(),
                        false,
                        false,
                        false,
                        false
                    )
                )
            } else {
                days.add(
                    CalendarCell(
                        (date.dayOfMonth - 1).toString(),
                        mutableListOf(),
                        false,
                        false,
                        false,
                        false
                    )
                )
            }


            // Since the days ben added in reverse order reverses the order
            days.reverse()
        }

        var activeMonthDay = LocalDate.parse(
            selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-1",
            dateFormat
        )

        // Adds all the days of the selected month to the list
        val maxDaysOfMonth = activeMonthDay.month.length(selectedMonth.isLeapYear)
        for (day in 1..maxDaysOfMonth) {

            days.add(
                CalendarCell(
                    day.toString(),
                    mutableListOf(),
                    (activeMonthDay.isEqual(today)),
                    (activeMonthDay.dayOfWeek == DayOfWeek.SUNDAY),
                    true,
                    (activeMonthDay.isEqual(selectedDay))
                )
            )

            activeMonthDay = activeMonthDay.plusDays(1)
        }

        // Adds the next month days to the list until the list reaches the size of 42 elements
        var counter = 1
        for (day in days.size..41) {
            days.add(
                CalendarCell(
                    counter.toString(),
                    mutableListOf(),
                    false,
                    false,
                    false,
                    false
                )
            )
            counter++
        }

        return days
    }

    private suspend fun loadEvents(days: MutableList<CalendarCell>) {
        var date: LocalDate
        if (days[0].dayString != "1") {
            date = LocalDate.parse(
                selectedMonth.year.toString() + "-" + selectedMonth.minusMonths(1).month.value + "-" + days[0].dayString,
                dateFormat
            )
        } else {
            date = LocalDate.parse(
                selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-" + days[0].dayString,
                dateFormat
            )
        }

        // Database setup
        val db = ReminderDatabase.getDatabase(requireContext())
        val dao = db.reminderDao()

        val eventsTransfer = List(42) { mutableListOf<EventDataTransfer>() }

        for (i in 0..<eventsTransfer.size) {
            val relevantEvents = dao.getEventsRelevantToSelectedDate(date)
            val filteredEvents = filterEvents(relevantEvents, date)

            for (event in filteredEvents.take(3)) {
                eventsTransfer[i].add(
                    EventDataTransfer(
                        event.title,
                        event.color
                    )
                )
            }

            date = date.plusDays(1)
        }

        customCalendarAdapter.updateEvents(eventsTransfer)

    }

    private fun setupCalendarAdapter(days: MutableList<CalendarCell>) {
        // Finds the RecyclerView and populates it with data
        val calendarRecyclerView: RecyclerView = layoutView.findViewById(R.id.rvCalendar)
        customCalendarAdapter = CalendarAdapter(days, requireContext())
        calendarRecyclerView.adapter = customCalendarAdapter
    }


    // ---------- tap and gesture ----------


    private var fling: Boolean = false

    // SwipeGestureListener for swipe functionality
    private inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {

        private val swipeThreshold = 100
        private val swipeVelocityThreshold = 100

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
                if (Math.abs(diffX) > swipeThreshold && Math.abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        onSwipeRight()  // Swiped right go to previous month
                    } else {
                        onSwipeLeft()  // Swiped left go to next month
                    }
                    fling = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        fling = false
                    }, 500)
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
        if (!fling) {
            val item = customCalendarAdapter.getItem(position)
            if (item.isActive) {
                selectedDay = LocalDate.parse(
                    selectedMonth.year.toString() + "-" + selectedMonth.month.value + "-" + item.dayString,
                    dateFormat
                )
                customCalendarAdapter.updateSelectedPosition(position)
                updateViewableEvents()
            }
        }
    }


    // ---------- viewable events ----------


    private fun updateViewableEvents() {
        Thread {
            lifecycleScope.launch {
                // Database setup
                val db = ReminderDatabase.getDatabase(requireContext())
                val dao = db.reminderDao()
                val relevantEvents = dao.getEventsRelevantToSelectedDate(selectedDay)
                val filterdEvents = filterEvents(relevantEvents, selectedDay)

                // Notification RecyclerView setup
                val notificationRecyclerView: RecyclerView = layoutView.findViewById(R.id.rvEvents)
                notificationRecyclerView.layoutManager = LinearLayoutManager(context)
                val customReminderAdapter =
                    EventAdapter(filterdEvents, requireContext(), lifecycleScope, this@CalendarFragment)
                notificationRecyclerView.adapter = customReminderAdapter
            }
        }.start()
    }

    private fun filterEvents(events: MutableList<Event>, day: LocalDate): MutableList<Event> {
        return events.filter { event ->
            // Calculate the difference between startDate and selectedDate based on the recurrence type
            val daysBetween = ChronoUnit.DAYS.between(event.startDate.atStartOfDay(), day.atStartOfDay())
                .toDouble()
            val weeksBetween = daysBetween / 7
            val monthsBetween = ChronoUnit.MONTHS.between(event.startDate.atStartOfDay(), day.atStartOfDay()).toDouble()
            val yearsBetween = ChronoUnit.YEARS.between(event.startDate.atStartOfDay(), day.atStartOfDay()).toDouble()


            // Determine if the event should occur on the selected date based on its recurrence type and interval
            when (event.repeatType) {
                RepeatType.DAILY -> validRepeat(daysBetween, event.repeatInterval)
                RepeatType.WEEKLY -> validRepeat(weeksBetween, event.repeatInterval)
                RepeatType.MONTHLY -> event.startDate.dayOfMonth == day.dayOfMonth && monthsBetween % event.repeatInterval == 0.0
                RepeatType.YEARLY -> event.startDate.dayOfMonth == day.dayOfMonth && event.startDate.month == day.month && yearsBetween % event.repeatInterval == 0.0
                else -> event.startDate == day  // Non-recurring event
            }
        }.toMutableList()
    }

    private fun validRepeat(value: Double, interval: Int): Boolean {
        return value >= 0 && value % interval == 0.0  // Check if value is divisible by the interval
    }
}