package com.example.timekeeper.helpers

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.timekeeper.R
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class RepeatDialog(startDate: LocalDate, repeatPeriod: String, interval: Int, endRepeatDateValue: LocalDate?): DialogFragment() {

    private var selectedOption: Int = R.id.checkBox1

    private var endRepeatDate: LocalDate? = endRepeatDateValue

    // Minimum selectable date for the date picker
    // Had to set it again, otherwise I have no access to the variable
    private var minSelectableDate: LocalDate = startDate

    // Text for updating the seeker bar text (day, week, month, year)
    private var frequencyType: String = repeatPeriod

    private var repeatInterval: Int = interval

    // View for accessing the component of the repeat dialog
    private lateinit var dialogView: View

    @SuppressLint("DialogFragmentCallbacksDetector", "InflateParams", "SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the layout of the repeat dialog
        dialogView = layoutInflater.inflate(R.layout.repeat_dialog, null)

        // Set the button and seek bar to disabled
        dialogView.findViewById<ImageButton>(R.id.btnRepeatEnd).isEnabled = false
        dialogView.findViewById<SeekBar>(R.id.sbRepeatFrequency).apply {
            isEnabled = false
            progress = repeatInterval-1
        }
        updateSekBar()

        when (frequencyType) {
            "day" -> switchSelected(R.id.checkBox2)
            "week" -> switchSelected(R.id.checkBox3)
            "month" -> switchSelected(R.id.checkBox4)
            "year" -> switchSelected(R.id.checkBox5)
            else -> switchSelected(R.id.checkBox1)
        }

        if (endRepeatDate != null){
            dialogView.findViewById<TextView>(R.id.txtRepeatEndDate).text = endRepeatDate!!.format(
                DateTimeFormatter.ofPattern("d. M. yyyy"))
        }

        // Set the on click for every option
        dialogView.findViewById<ConstraintLayout>(R.id.option1).setOnClickListener {
            switchSelected(R.id.checkBox1)
            frequencyType = ""
        }
        dialogView.findViewById<ConstraintLayout>(R.id.option2).setOnClickListener {
            switchSelected(R.id.checkBox2)
            frequencyType = "day"
            updateSekBar()
        }
        dialogView.findViewById<ConstraintLayout>(R.id.option3).setOnClickListener {
            switchSelected(R.id.checkBox3)
            frequencyType = "week"
            updateSekBar()
        }
        dialogView.findViewById<ConstraintLayout>(R.id.option4).setOnClickListener {
            switchSelected(R.id.checkBox4)
            frequencyType = "month"
            updateSekBar()
        }
        dialogView.findViewById<ConstraintLayout>(R.id.option5).setOnClickListener {
            switchSelected(R.id.checkBox5)
            frequencyType = "year"
            updateSekBar()
        }

        // Image button in click opens date picker
        dialogView.findViewById<ImageButton>(R.id.btnRepeatEnd).setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext(), R.style.CustomDatePickerDialog, { _, selectedYear, selectedMonth, selectedDay ->
                endRepeatDate = LocalDate.parse("$selectedDay/${selectedMonth + 1}/$selectedYear", DateTimeFormatter.ofPattern("d/M/y"))
                dialogView.findViewById<TextView>(R.id.txtRepeatEndDate).text = endRepeatDate!!.format(
                    DateTimeFormatter.ofPattern("d. M. yyyy"))
            },
                endRepeatDate?.year ?: LocalDate.now().year,
                (endRepeatDate?.monthValue ?: LocalDate.now().monthValue) -1,
                endRepeatDate?.dayOfMonth ?: LocalDate.now().dayOfMonth
            )

            datePickerDialog.datePicker.firstDayOfWeek = Calendar.MONDAY

            datePickerDialog.datePicker.minDate = minSelectableDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // todo - check if there is a third button option
            datePickerDialog.setOnCancelListener {
                dialogView.findViewById<TextView>(R.id.txtRepeatEndDate).setText("")
                endRepeatDate = null
            }

            datePickerDialog.show()
        }

        dialogView.findViewById<SeekBar>(R.id.sbRepeatFrequency).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            // Seek bar updates seek bar text every time its value changes
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateSekBar()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional
            }
        })


        return AlertDialog.Builder(requireContext(), R.style.RecurrenceDialog)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val result = Bundle().apply {
                    putString("repeatPeriod", frequencyType)
                    putInt("interval", repeatInterval)
                    putString("endDate", endRepeatDate?.format(
                        DateTimeFormatter.ofPattern("d. M. yyyy"))
                    )
                }
                setFragmentResult("repeatFragmentDialogRequestCode", result)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    // Function to visually switch the selected repeat period
    private fun switchSelected(newSelectedOption: Int){
        dialogView.findViewById<View>(selectedOption).setBackgroundResource(R.drawable.baseline_check_box_outline_blank_24)
        selectedOption = newSelectedOption
        dialogView.findViewById<View>(selectedOption).setBackgroundResource(R.drawable.baseline_check_box_24)
        if (newSelectedOption != R.id.checkBox1) {
            dialogView.findViewById<ConstraintLayout>(R.id.vRepeatEndDate).alpha = 1F
            dialogView.findViewById<ConstraintLayout>(R.id.vRepeatFrequency).alpha = 1F
            dialogView.findViewById<ImageButton>(R.id.btnRepeatEnd).isEnabled = true
            dialogView.findViewById<SeekBar>(R.id.sbRepeatFrequency).isEnabled = true
        }
        else {
            dialogView.findViewById<ConstraintLayout>(R.id.vRepeatEndDate).alpha = 0.5F
            dialogView.findViewById<ConstraintLayout>(R.id.vRepeatFrequency).alpha = 0.5F
            dialogView.findViewById<ImageButton>(R.id.btnRepeatEnd).isEnabled = false
            dialogView.findViewById<SeekBar>(R.id.sbRepeatFrequency).isEnabled = false
        }
    }

    // Updates the seek bar text
    @SuppressLint("SetTextI18n")
    fun updateSekBar(){
        val progress = dialogView.findViewById<SeekBar>(R.id.sbRepeatFrequency).progress
        val progressValue = (progress + 1).toString()
        if (progress > 0) {
            repeatInterval = progressValue.toInt()
            dialogView.findViewById<TextView>(R.id.txtRepeatFrequency).text = "Repeat every " + progressValue + " " + frequencyType + "s"
        }
        else {
            dialogView.findViewById<TextView>(R.id.txtRepeatFrequency).text =
                "Repeat every $frequencyType"
        }
    }
}