package com.example.timekeeper.fragments

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.timekeeper.R
import com.example.timekeeper.adapters.NotificationAdapter
import com.example.timekeeper.database.Reminder
import com.example.timekeeper.database.ReminderDatabase
import com.example.timekeeper.helpers.RepeatDialog
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class NewReminderFragment : Fragment() {

    private lateinit var view: View

    private var startDate: LocalDate = LocalDate.now()
    private var startTime: LocalTime = LocalTime.now()
    private var endDate: LocalDate = LocalDate.now()
    private var endTime: LocalTime = LocalTime.now()

    private var repeatPeriod: String? = ""
    private var interval: Int = 0
    private var endRepeatDate: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    @OptIn(ExperimentalStdlibApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_new_reminder_event, container, false)

        view.findViewById<TextView>(R.id.txtStartDate).setText(startDate.format(
            DateTimeFormatter.ofPattern("d. M. yyyy")))
        view.findViewById<TextView>(R.id.txtEndDate).setText(endDate.format(
            DateTimeFormatter.ofPattern("d. M. yyyy")))
        // Disables the buttons
        switchSelection(false)

        // Variable of the colorButton background color
        var colorButton = ContextCompat.getColor(requireContext(), R.color.accent)
        // Set btnColorPicker onClick
        view.findViewById<ImageButton>(R.id.btnColorPicker).setOnClickListener {
            // Open ColorPickerDialogBuilder
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(colorButton)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(8)
                .lightnessSliderOnly()
                // Sets the background color of the colorButton
                .setPositiveButton(
                    "ok"
                ) { dialog, selectedColor, allColors ->
                    colorButton = selectedColor
                    view.findViewById<ImageButton>(R.id.btnColorPicker).setBackgroundColor(selectedColor)
                }
                .setNegativeButton(
                    "cancel"
                ) { dialog, which -> }
                .build()
                .show()
        }

        // Set btnSave onClick
        view.findViewById<ImageButton>(R.id.btnSave).setOnClickListener {
            // Database setup
            val db = ReminderDatabase.getDatabase(requireContext())
            val notificationDao = db.reminderDao()

            lifecycleScope.launch {
                // Check if iTxtTitle is empty
                if (!view.findViewById<EditText>(R.id.iTxtTitle).text.isNullOrEmpty()) {
                    // Values of a reminder
                    val titleTxt = view.findViewById<EditText>(R.id.iTxtTitle).text.toString()
                    val descriptionTxt = view.findViewById<EditText>(R.id.iTxtDescription).text.toString()
                    val notificationColor = '#'+colorButton.toHexString()

                    // Inserts a new reminder and gets its id
                    val reminderId = notificationDao.upsertReminder(
                        Reminder(
                        0,
                        notificationColor,
                        titleTxt,
                        descriptionTxt,
                        LocalDateTime.now(),
                            null
                        )
                    )

                    // Shows a new notification referring to the previously created reminder
                    NotificationAdapter.createAndShowNotification(
                        requireContext(),
                        notificationColor,
                        titleTxt,
                        descriptionTxt,
                        reminderId.toInt()
                    )

                    // Navigates back to the home page
                    Navigation.findNavController(view).navigate(R.id.navigate_newReminder_to_home)
                }
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
                        iTxtTitle.setHintTextColor(ContextCompat.getColor(requireContext(),
                            R.color.black
                        ))
                    }, 2000)
                }
            }
        }

        // Sets the selection switch function
        view.findViewById<Switch>(R.id.swSelection).setOnCheckedChangeListener { switchView, isChecked ->
            switchSelection(isChecked)
        }

        // Start date button
        view.findViewById<ImageButton>(R.id.btnStartDate).setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext(), R.style.CustomDatePickerDialog, { _, selectedYear, selectedMonth, selectedDay ->
                // Update the TextView with the selected date
                startDate = LocalDate.parse("$selectedDay/${selectedMonth + 1}/$selectedYear", DateTimeFormatter.ofPattern("d/M/y"))
                if (endDate < startDate) {
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
                        if (startDate == endDate && endTime < startTime){
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
        }

        // todo - clear repeat if end is bigger than start date
        // Sets the repeat button on click
        view.findViewById<ImageButton>(R.id.btnRepeat).setOnClickListener {
            val repeatDialog = RepeatDialog(startDate)
            // Shows the recurrence dialog
            repeatDialog.show(childFragmentManager,"RecurrenceDialog")

            childFragmentManager.setFragmentResultListener("repeatFragmentDialogRequestCode",this) { requestKey, bundle ->
                repeatPeriod = bundle.getString("repeatPeriod")
                interval = bundle.getInt("interval")
                endRepeatDate = bundle.getString("endDate")
                if (repeatPeriod == ""){
                    view.findViewById<TextView>(R.id.txtRepeatText).text = ""
                }
                else if (interval > 1) {
                    if (endRepeatDate.equals(null)) {
                        view.findViewById<TextView>(R.id.txtRepeatText).text =
                            "Every " + interval + " " + repeatPeriod + "s"
                    }
                    else {
                        view.findViewById<TextView>(R.id.txtRepeatText).text =
                            "Every " + interval + " " + repeatPeriod + "s | " + endRepeatDate
                    }
                }
                else {
                    if (endRepeatDate.equals(null)) {
                        view.findViewById<TextView>(R.id.txtRepeatText).text =
                            "Every $repeatPeriod"
                    }
                    else {
                        view.findViewById<TextView>(R.id.txtRepeatText).text =
                            "Every $repeatPeriod | $endRepeatDate"
                    }
                }
            }
        }

        return view
    }

    // Function switches the opacity and if the buttons are enabled
    private fun switchSelection(isChecked: Boolean){
        if (isChecked){
            view.findViewById<View>(R.id.vContainer1).alpha = 1.0F
            view.findViewById<View>(R.id.vContainer2).alpha = 1.0F
            view.findViewById<View>(R.id.vLine1).alpha = 1.0F
            view.findViewById<View>(R.id.vLine2).alpha = 1.0F
            view.findViewById<View>(R.id.vLine3).alpha = 1.0F
            view.findViewById<View>(R.id.vClockIcon).alpha = 1.0F
            view.findViewById<TextView>(R.id.txtStart).alpha = 1.0F
            view.findViewById<TextView>(R.id.txtEnd).alpha = 1.0F
            view.findViewById<TextView>(R.id.txtStartDate).alpha = 1.0F
            view.findViewById<TextView>(R.id.txtEndDate).alpha = 1.0F
            view.findViewById<TextView>(R.id.txtRepeat).alpha = 1.0F
            view.findViewById<TextView>(R.id.txtRepeatText).alpha = 1.0F
            view.findViewById<TextView>(R.id.txtEventReminderTime).alpha = 1.0F
            view.findViewById<TextView>(R.id.txtEventReminderTimeText).alpha = 1.0F
            view.findViewById<ImageButton>(R.id.btnStartDate).apply {
                alpha = 1.0F
                isEnabled = true
            }
            view.findViewById<ImageButton>(R.id.btnEndDate).apply {
                alpha = 1.0F
                isEnabled = true
            }
            view.findViewById<ImageButton>(R.id.btnRepeat).apply {
                alpha = 1.0F
                isEnabled = true
            }
            view.findViewById<ImageButton>(R.id.btnEventReminderTime).apply {
                alpha = 1.0F
                isEnabled = true
            }
            view.findViewById<Switch>(R.id.swIncludesTime).apply {
                alpha = 1.0F
                isEnabled = true
            }

            view.findViewById<TextView>(R.id.txtTitle).setText(R.string.add_page_title2)
        }
        else {
            view.findViewById<View>(R.id.vContainer1).alpha = 0.5F
            view.findViewById<View>(R.id.vContainer2).alpha = 0.5F
            view.findViewById<View>(R.id.vLine1).alpha = 0.5F
            view.findViewById<View>(R.id.vLine2).alpha = 0.5F
            view.findViewById<View>(R.id.vLine3).alpha = 0.5F
            view.findViewById<View>(R.id.vClockIcon).alpha = 0.5F
            view.findViewById<TextView>(R.id.txtStart).alpha = 0.5F
            view.findViewById<TextView>(R.id.txtEnd).alpha = 0.5F
            view.findViewById<TextView>(R.id.txtStartDate).alpha = 0.5F
            view.findViewById<TextView>(R.id.txtEndDate).alpha = 0.5F
            view.findViewById<TextView>(R.id.txtRepeat).alpha = 0.5F
            view.findViewById<TextView>(R.id.txtRepeatText).alpha = 0.5F
            view.findViewById<TextView>(R.id.txtEventReminderTime).alpha = 0.5F
            view.findViewById<TextView>(R.id.txtEventReminderTimeText).alpha = 0.5F
            view.findViewById<ImageButton>(R.id.btnStartDate).apply {
                alpha = 0.5F
                isEnabled = false
            }
            view.findViewById<ImageButton>(R.id.btnEndDate).apply {
                alpha = 0.5F
                isEnabled = false
            }
            view.findViewById<ImageButton>(R.id.btnRepeat).apply {
                alpha = 0.5F
                isEnabled = false
            }
            view.findViewById<ImageButton>(R.id.btnEventReminderTime).apply {
                alpha = 0.5F
                isEnabled = false
            }
            view.findViewById<Switch>(R.id.swIncludesTime).apply {
                alpha = 0.5F
                isEnabled = false
            }

            view.findViewById<TextView>(R.id.txtTitle).setText(R.string.add_page_title1)
        }
    }
}