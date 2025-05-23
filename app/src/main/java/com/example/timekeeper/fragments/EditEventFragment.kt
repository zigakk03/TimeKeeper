package com.example.timekeeper.fragments

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.timekeeper.R
import com.example.timekeeper.database.Event
import com.example.timekeeper.database.ReminderDatabase
import com.example.timekeeper.database.RepeatType
import com.example.timekeeper.helpers.ReminderOptionsDialog
import com.example.timekeeper.helpers.RepeatDialog
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar


class editEventFragment : Fragment() {

    private lateinit var view: View

    private var startDate: LocalDate = LocalDate.now()
    private var startTime: LocalTime = LocalTime.now()
    private var endDate: LocalDate = LocalDate.now()
    private var endTime: LocalTime = LocalTime.now()
    private var endEdit: Boolean = false

    private var repeatPeriod: String = ""
    private var interval: Int = 1
    private var endRepeatDate: LocalDate? = null

    lateinit var selectedEvent: Event

    private var selectedNotificationOptions: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_event, container, false)

        // Variable of the colorButton background color
        var colorButton = ContextCompat.getColor(requireContext(), R.color.accent)

        val args: editEventFragmentArgs by navArgs()
        lifecycleScope.launch {
            // Database setup
            val db = ReminderDatabase.getDatabase(requireContext())
            val eventDao = db.reminderDao()
            // Gets a reminder based on the given id
            selectedEvent = eventDao.getEvent(args.eventId)

            // Sets the colorButton to color of the gotten reminder
            colorButton = Color.parseColor(selectedEvent.color)
            // Fills the views with the gotten reminder
            view.findViewById<ImageButton>(R.id.btnColorPicker).setBackgroundColor(colorButton)
            view.findViewById<EditText>(R.id.iTxtTitle).setText(selectedEvent.title)
            view.findViewById<EditText>(R.id.iTxtDescription).setText(selectedEvent.description)
            startDate = selectedEvent.startDate
            endDate = selectedEvent.endDate
            startTime = selectedEvent.startTime?: LocalTime.now()
            endTime = selectedEvent.endTime?: LocalTime.now()

            if (selectedEvent.startTime != null) {
                view.findViewById<Switch>(R.id.swIncludesTime).isChecked = true
                view.findViewById<TextView>(R.id.txtStartDate).text = (startTime.format(
                    DateTimeFormatter.ofPattern("HH:mm")) + "    " + startDate.format(
                    DateTimeFormatter.ofPattern("d. M. yyyy")))

                view.findViewById<TextView>(R.id.txtEndDate).text = (endTime.format(
                    DateTimeFormatter.ofPattern("HH:mm")) + "    " + endDate.format(
                    DateTimeFormatter.ofPattern("d. M. yyyy")))
            }
            else {
                // Sets the start and end dates to set date
                view.findViewById<TextView>(R.id.txtStartDate).setText(startDate.format(
                    DateTimeFormatter.ofPattern("d. M. yyyy")))
                view.findViewById<TextView>(R.id.txtEndDate).setText(endDate.format(
                    DateTimeFormatter.ofPattern("d. M. yyyy")))
            }

            view.findViewById<TextView>(R.id.txtEventReminderTimeText).setText(selectedEvent.notificationOptions)
            selectedNotificationOptions = selectedEvent.notificationOptions?: ""

            repeatPeriod = when(selectedEvent.repeatType) {
                RepeatType.DAILY -> "day"
                RepeatType.WEEKLY -> "week"
                RepeatType.MONTHLY -> "month"
                RepeatType.YEARLY -> "year"
                else -> ""
            }
            interval = selectedEvent.repeatInterval
            endRepeatDate = selectedEvent.repeatEnd

            if (repeatPeriod == ""){
                view.findViewById<TextView>(R.id.txtRepeatText).text = ""
            }
            else if (interval > 1) {
                if (endRepeatDate == null) {
                    view.findViewById<TextView>(R.id.txtRepeatText).text =
                        "Every " + interval + " " + repeatPeriod + "s"
                }
                else {
                    view.findViewById<TextView>(R.id.txtRepeatText).text =
                        "Every " + interval + " " + repeatPeriod + "s | " + endRepeatDate!!.format(DateTimeFormatter.ofPattern("d. M. yyyy"))
                }
            }
            else {
                if (endRepeatDate == null) {
                    view.findViewById<TextView>(R.id.txtRepeatText).text =
                        "Every $repeatPeriod"
                }
                else {
                    view.findViewById<TextView>(R.id.txtRepeatText).text =
                        "Every $repeatPeriod | "+ endRepeatDate!!.format(DateTimeFormatter.ofPattern("d. M. yyyy"))
                }
            }
        }

        // Set btnColorPicker onClick
        view.findViewById<ImageButton>(R.id.btnColorPicker).setOnClickListener {
            // Open ColorPickerDialogBuilder
            ColorPickerDialogBuilder
                .with(context, R.style.ColorPickerDialog)
                .setTitle("Choose Color")
                .initialColor(colorButton)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(8)
                .lightnessSliderOnly()
                // Sets the background color of the colorButton
                .setPositiveButton(
                    "OK"
                ) { dialog, selectedColor, allColors ->
                    colorButton = selectedColor
                    view.findViewById<ImageButton>(R.id.btnColorPicker).setBackgroundColor(selectedColor)
                }
                .setNegativeButton(
                    "CANCEL"
                ) { dialog, which -> }
                .build()
                .show()
        }

        view.findViewById<ImageButton>(R.id.btnSave).setOnClickListener {
            // Database setup
            val db = ReminderDatabase.getDatabase(requireContext())
            val reminderDao = db.reminderDao()
                // Check if iTxtTitle is empty
                if (!view.findViewById<EditText>(R.id.iTxtTitle).text.isNullOrEmpty()) {
                    // Values of an event
                    val titleTxt = view.findViewById<EditText>(R.id.iTxtTitle).text.toString()
                    val descriptionTxt = view.findViewById<EditText>(R.id.iTxtDescription).text.toString()
                    val notificationColor = '#'+colorButton.toHexString()
                    val repeatType = when(repeatPeriod) {
                        "day" -> RepeatType.DAILY
                        "week" -> RepeatType.WEEKLY
                        "month" -> RepeatType.MONTHLY
                        "year" -> RepeatType.YEARLY
                        else -> RepeatType.NONE
                    }

                    if (selectedEvent.repeatType != RepeatType.NONE) {

                        val dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)
                        dialogView.findViewById<TextView>(R.id.txtAlertDialogTitle).text =
                            "How do you want to update?"

                        val optionsDialog =
                            AlertDialog.Builder(requireContext(), R.style.AlertDialog)
                                .setView(dialogView)
                                .setNegativeButton("Cancel") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()

                        val options: Array<String> = arrayOf(
                            "Update only this event",
                            "Update this and all future events",
                            "Update all occurrences of this event"
                        )

                        // Set up the ListView
                        val listView = dialogView.findViewById<ListView>(R.id.lvOptions)
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            options
                        )
                        listView.adapter = adapter

                        // Handle item clicks
                        listView.setOnItemClickListener { _, _, i, _ ->
                            when (i) {
                                0 -> {
                                    lifecycleScope.launch {
                                        val nextEvent = when (selectedEvent.repeatType) {
                                            RepeatType.DAILY -> args.selectedDay.plusDays(
                                                selectedEvent.repeatInterval.toLong()
                                            )

                                            RepeatType.WEEKLY -> args.selectedDay.plusWeeks(
                                                selectedEvent.repeatInterval.toLong()
                                            )

                                            RepeatType.MONTHLY -> args.selectedDay.plusMonths(
                                                selectedEvent.repeatInterval.toLong()
                                            )

                                            RepeatType.YEARLY -> args.selectedDay.plusYears(
                                                selectedEvent.repeatInterval.toLong()
                                            )

                                            else -> args.selectedDay
                                        }
                                        if (selectedEvent.repeatType != RepeatType.NONE && (nextEvent <= selectedEvent.repeatEnd || selectedEvent.repeatEnd == null)) {
                                            reminderDao.upsertEvent(
                                                Event(
                                                    0,
                                                    selectedEvent.color,
                                                    selectedEvent.title,
                                                    selectedEvent.description,
                                                    nextEvent,
                                                    selectedEvent.startTime,
                                                    nextEvent.plusDays(
                                                        ChronoUnit.DAYS.between(
                                                            selectedEvent.startDate,
                                                            selectedEvent.endDate
                                                        )
                                                    ),
                                                    selectedEvent.endTime,
                                                    selectedEvent.repeatType,
                                                    selectedEvent.repeatInterval,
                                                    selectedEvent.repeatEnd,
                                                    selectedEvent.notificationOptions
                                                )
                                            )
                                        }

                                        if (args.selectedDay != selectedEvent.startDate) {
                                            reminderDao.upsertEvent(
                                                Event(
                                                    selectedEvent.id,
                                                    selectedEvent.color,
                                                    selectedEvent.title,
                                                    selectedEvent.description,
                                                    selectedEvent.startDate,
                                                    selectedEvent.startTime,
                                                    selectedEvent.endDate,
                                                    selectedEvent.endTime,
                                                    selectedEvent.repeatType,
                                                    selectedEvent.repeatInterval,
                                                    args.selectedDay.minusDays(1),
                                                    selectedEvent.notificationOptions
                                                )
                                            )
                                        } else {
                                            reminderDao.deleteEvent(selectedEvent)
                                        }

                                        if (view.findViewById<Switch>(R.id.swIncludesTime).isChecked) {
                                            reminderDao.upsertEvent(
                                                Event(
                                                    0,
                                                    notificationColor,
                                                    titleTxt,
                                                    descriptionTxt,
                                                    args.selectedDay,
                                                    startTime,
                                                    args.selectedDay.plusDays(
                                                        ChronoUnit.DAYS.between(
                                                            startDate,
                                                            endDate
                                                        )
                                                    ),
                                                    endTime,
                                                    RepeatType.NONE,
                                                    1,
                                                    null,
                                                    selectedNotificationOptions
                                                )
                                            )
                                        } else {
                                            reminderDao.upsertEvent(
                                                Event(
                                                    0,
                                                    notificationColor,
                                                    titleTxt,
                                                    descriptionTxt,
                                                    args.selectedDay,
                                                    null,
                                                    args.selectedDay.plusDays(
                                                        ChronoUnit.DAYS.between(
                                                            startDate,
                                                            endDate
                                                        )
                                                    ),
                                                    null,
                                                    RepeatType.NONE,
                                                    1,
                                                    null,
                                                    selectedNotificationOptions
                                                )
                                            )
                                        }
                                    }
                                }

                                1 -> {
                                    lifecycleScope.launch {
                                        if (args.selectedDay != selectedEvent.startDate) {
                                            reminderDao.upsertEvent(
                                                Event(
                                                    selectedEvent.id,
                                                    selectedEvent.color,
                                                    selectedEvent.title,
                                                    selectedEvent.description,
                                                    selectedEvent.startDate,
                                                    selectedEvent.startTime,
                                                    selectedEvent.endDate,
                                                    selectedEvent.endTime,
                                                    selectedEvent.repeatType,
                                                    selectedEvent.repeatInterval,
                                                    args.selectedDay.minusDays(1),
                                                    selectedEvent.notificationOptions
                                                )
                                            )
                                        } else {
                                            reminderDao.deleteEvent(selectedEvent)
                                        }

                                        if (view.findViewById<Switch>(R.id.swIncludesTime).isChecked) {
                                            reminderDao.upsertEvent(
                                                Event(
                                                    0,
                                                    notificationColor,
                                                    titleTxt,
                                                    descriptionTxt,
                                                    args.selectedDay,
                                                    startTime,
                                                    args.selectedDay.plusDays(
                                                        ChronoUnit.DAYS.between(
                                                            startDate,
                                                            endDate
                                                        )
                                                    ),
                                                    endTime,
                                                    repeatType,
                                                    interval,
                                                    endRepeatDate,
                                                    selectedNotificationOptions
                                                )
                                            )
                                        } else {
                                            reminderDao.upsertEvent(
                                                Event(
                                                    0,
                                                    notificationColor,
                                                    titleTxt,
                                                    descriptionTxt,
                                                    args.selectedDay,
                                                    null,
                                                    args.selectedDay.plusDays(
                                                        ChronoUnit.DAYS.between(
                                                            startDate,
                                                            endDate
                                                        )
                                                    ),
                                                    null,
                                                    repeatType,
                                                    interval,
                                                    endRepeatDate,
                                                    selectedNotificationOptions
                                                )
                                            )
                                        }
                                    }
                                }

                                2 -> {
                                    lifecycleScope.launch {
                                        if (view.findViewById<Switch>(R.id.swIncludesTime).isChecked) {
                                            reminderDao.upsertEvent(
                                                Event(
                                                    args.eventId,
                                                    notificationColor,
                                                    titleTxt,
                                                    descriptionTxt,
                                                    startDate,
                                                    startTime,
                                                    endDate,
                                                    endTime,
                                                    repeatType,
                                                    interval,
                                                    endRepeatDate,
                                                    selectedNotificationOptions
                                                )
                                            )
                                        } else {
                                            reminderDao.upsertEvent(
                                                Event(
                                                    args.eventId,
                                                    notificationColor,
                                                    titleTxt,
                                                    descriptionTxt,
                                                    startDate,
                                                    null,
                                                    endDate,
                                                    null,
                                                    repeatType,
                                                    interval,
                                                    endRepeatDate,
                                                    selectedNotificationOptions
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Navigates to the calender page
                            Navigation.findNavController(view)
                                .navigate(R.id.navigate_editEvent_to_calendar)

                            // dismiss dialog
                            optionsDialog.dismiss()
                        }

                        optionsDialog.show()
                    }
                    else {
                        lifecycleScope.launch {
                            if (view.findViewById<Switch>(R.id.swIncludesTime).isChecked) {
                                reminderDao.upsertEvent(
                                    Event(
                                        args.eventId,
                                        notificationColor,
                                        titleTxt,
                                        descriptionTxt,
                                        startDate,
                                        startTime,
                                        endDate,
                                        endTime,
                                        repeatType,
                                        interval,
                                        endRepeatDate,
                                        selectedNotificationOptions
                                    )
                                )
                            } else {
                                reminderDao.upsertEvent(
                                    Event(
                                        args.eventId,
                                        notificationColor,
                                        titleTxt,
                                        descriptionTxt,
                                        startDate,
                                        null,
                                        endDate,
                                        null,
                                        repeatType,
                                        interval,
                                        endRepeatDate,
                                        selectedNotificationOptions
                                    )
                                )
                            }
                        }

                        // Navigates to the calender page
                        Navigation.findNavController(view)
                            .navigate(R.id.navigate_editEvent_to_calendar)
                    }

                }
                // Title animation for when title is empty
                else {
                    // Find the iTxtTitle view
                    val iTxtTitle = view.findViewById<EditText>(R.id.iTxtTitle)

                    // Sets a new animation
                    val colorAnim = ObjectAnimator.ofArgb(
                        iTxtTitle,
                        "hintTextColor",
                        ContextCompat.getColor(requireContext(), R.color.black),
                        ContextCompat.getColor(requireContext(), R.color.alert)
                    ).apply {
                        duration = 500
                        repeatMode = ObjectAnimator.REVERSE
                        repeatCount = 3
                        setEvaluator(ArgbEvaluator())
                    }

                    // Starts the animation
                    colorAnim.start()

                    // Sets the color of the text to black after 2s
                    Handler(Looper.getMainLooper()).postDelayed({
                        iTxtTitle.setHintTextColor(
                            ContextCompat.getColor(requireContext(),
                            R.color.black
                        ))
                    }, 2000)
                }

        }

        // Start date button
        view.findViewById<ImageButton>(R.id.btnStartDate).setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext(), R.style.CustomDatePickerDialog, { _, selectedYear, selectedMonth, selectedDay ->
                // Update the TextView with the selected date
                startDate = LocalDate.parse("$selectedDay/${selectedMonth + 1}/$selectedYear", DateTimeFormatter.ofPattern("d/M/y"))
                if (endDate < startDate || !endEdit) {
                    endEdit = false
                    if (view.findViewById<Switch>(R.id.swIncludesTime).isChecked){
                        endDate = startDate
                        endTime = startTime
                        view.findViewById<TextView>(R.id.txtEndDate).text = (endTime.format(
                            DateTimeFormatter.ofPattern("HH:mm")) + "    " + endDate.format(
                            DateTimeFormatter.ofPattern("d. M. yyyy")))
                    }
                    else {
                        endDate = startDate
                        view.findViewById<TextView>(R.id.txtEndDate).setText(endDate.format(
                            DateTimeFormatter.ofPattern("d. M. yyyy")))
                    }
                }
                if (view.findViewById<Switch>(R.id.swIncludesTime).isChecked){
                    val timePickerDialog = TimePickerDialog(requireContext(), R.style.CustomTimePickerDialog, { _, selectedHour, selectedMinute ->
                        startTime = LocalTime.parse("$selectedHour/$selectedMinute", DateTimeFormatter.ofPattern("H/m"))
                        if (startDate == endDate && endTime < startTime || !endEdit){
                            endEdit = false
                            endTime = startTime
                            view.findViewById<TextView>(R.id.txtEndDate).text = (endTime.format(
                                DateTimeFormatter.ofPattern("HH:mm")) + "    " + endDate.format(
                                DateTimeFormatter.ofPattern("d. M. yyyy")))
                        }
                        view.findViewById<TextView>(R.id.txtStartDate).text = (startTime.format(
                            DateTimeFormatter.ofPattern("HH:mm")) + "    " + startDate.format(
                            DateTimeFormatter.ofPattern("d. M. yyyy")))
                    }, startTime.hour, startTime.minute, true)
                    timePickerDialog.show()
                }
                else {
                    view.findViewById<TextView>(R.id.txtStartDate).setText(startDate.format(
                        DateTimeFormatter.ofPattern("d. M. yyyy")))
                }
                if (endRepeatDate != null && startDate > endRepeatDate) {
                    endRepeatDate = null
                    interval = 0
                    repeatPeriod = ""
                    view.findViewById<TextView>(R.id.txtRepeatText).text = ""
                }
            }, startDate.year, startDate.monthValue-1, startDate.dayOfMonth)

            datePickerDialog.datePicker.firstDayOfWeek = Calendar.MONDAY

            datePickerDialog.show()
        }

        // End date button
        view.findViewById<ImageButton>(R.id.btnEndDate).setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext(), R.style.CustomDatePickerDialog, { _, selectedYear, selectedMonth, selectedDay ->
                // Update the TextView with the selected date
                endDate = LocalDate.parse("$selectedDay/${selectedMonth + 1}/$selectedYear", DateTimeFormatter.ofPattern("d/M/y"))
                if (endDate < startDate) {
                    endDate = startDate
                }
                if (view.findViewById<Switch>(R.id.swIncludesTime).isChecked){
                    val timePickerDialog = TimePickerDialog(requireContext(), R.style.CustomTimePickerDialog, { _, selectedHour, selectedMinute ->
                        endTime = LocalTime.parse("$selectedHour/$selectedMinute", DateTimeFormatter.ofPattern("H/m"))
                        if (startDate == endDate && endTime < startTime){
                            endTime = startTime
                        }
                        view.findViewById<TextView>(R.id.txtEndDate).text = (endTime.format(
                            DateTimeFormatter.ofPattern("HH:mm")) + "    " + endDate.format(
                            DateTimeFormatter.ofPattern("d. M. yyyy")))
                    }, endTime.hour, endTime.minute, true)
                    timePickerDialog.show()
                }
                else {
                    view.findViewById<TextView>(R.id.txtEndDate).setText(endDate.format(
                        DateTimeFormatter.ofPattern("d. M. yyyy")))
                }
                endEdit = true
            }, endDate.year, endDate.monthValue-1, endDate.dayOfMonth)

            datePickerDialog.datePicker.firstDayOfWeek = Calendar.MONDAY

            datePickerDialog.show()
        }

        // Sets the time switch function
        view.findViewById<Switch>(R.id.swIncludesTime).setOnCheckedChangeListener { switchView, isChecked ->
            // Displays the correct format based on the switches state
            if (isChecked) {
                view.findViewById<TextView>(R.id.txtStartDate).text = (startTime.format(
                    DateTimeFormatter.ofPattern("HH:mm")) + "    " + startDate.format(
                    DateTimeFormatter.ofPattern("d. M. yyyy")))

                view.findViewById<TextView>(R.id.txtEndDate).text = (endTime.format(
                    DateTimeFormatter.ofPattern("HH:mm")) + "    " + endDate.format(
                    DateTimeFormatter.ofPattern("d. M. yyyy")))
            }
            else {
                view.findViewById<TextView>(R.id.txtStartDate).setText(startDate.format(
                    DateTimeFormatter.ofPattern("d. M. yyyy")))

                view.findViewById<TextView>(R.id.txtEndDate).setText(endDate.format(
                    DateTimeFormatter.ofPattern("d. M. yyyy")))
            }
            selectedNotificationOptions = ""
            view.findViewById<TextView>(R.id.txtEventReminderTimeText).text = ""
        }

        // Sets the repeat button on click
        view.findViewById<ImageButton>(R.id.btnRepeat).setOnClickListener {
            val repeatDialog = RepeatDialog(startDate, repeatPeriod, interval, endRepeatDate)
            // Shows the recurrence dialog
            repeatDialog.show(childFragmentManager,"RecurrenceDialog")

            childFragmentManager.setFragmentResultListener("repeatFragmentDialogRequestCode",this) { requestKey, bundle ->
                repeatPeriod = bundle.getString("repeatPeriod") ?: ""
                interval = bundle.getInt("interval")
                val endRepeatDateText = bundle.getString("endDate")
                if (endRepeatDateText == null) {
                    endRepeatDate = null
                } else {
                    endRepeatDate = LocalDate.parse(endRepeatDateText, DateTimeFormatter.ofPattern("d. M. yyyy"))
                }


                if (repeatPeriod == ""){
                    view.findViewById<TextView>(R.id.txtRepeatText).text = ""
                    repeatPeriod = ""
                    interval = 0
                    endRepeatDate = null
                }
                else if (interval > 1) {
                    if (endRepeatDateText.equals(null)) {
                        view.findViewById<TextView>(R.id.txtRepeatText).text =
                            "Every " + interval + " " + repeatPeriod + "s"
                    }
                    else {
                        view.findViewById<TextView>(R.id.txtRepeatText).text =
                            "Every " + interval + " " + repeatPeriod + "s | " + endRepeatDateText
                    }
                }
                else {
                    if (endRepeatDateText.equals(null)) {
                        view.findViewById<TextView>(R.id.txtRepeatText).text =
                            "Every $repeatPeriod"
                    }
                    else {
                        view.findViewById<TextView>(R.id.txtRepeatText).text =
                            "Every $repeatPeriod | $endRepeatDateText"
                    }
                }
            }
        }

        view.findViewById<ImageButton>(R.id.btnEventReminderTime).setOnClickListener {
            val reminderOptionsDialog = ReminderOptionsDialog(view.findViewById<Switch>(R.id.swIncludesTime).isChecked, selectedNotificationOptions)
            // Shows the recurrence dialog
            reminderOptionsDialog.show(childFragmentManager,"NotificationDialog")

            childFragmentManager.setFragmentResultListener("notificationFragmentDialogRequestCode",this) { _, bundle ->
                selectedNotificationOptions = bundle.getString("selectedOptions") ?: ""

                view.findViewById<TextView>(R.id.txtEventReminderTimeText).text = selectedNotificationOptions
            }
        }

        return view
    }

}