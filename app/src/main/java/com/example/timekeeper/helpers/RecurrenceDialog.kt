package com.example.timekeeper.helpers

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
import com.example.timekeeper.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class RecurrenceDialog: DialogFragment() {

    var selectedOption: Int = R.id.checkBox1

    private var endRepeatDate: LocalDate = LocalDate.now()

    lateinit var dialogView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogView = layoutInflater.inflate(R.layout.recurrence_dialog, null)

        dialogView.findViewById<ConstraintLayout>(R.id.option1).setOnClickListener {
            switchSelected(R.id.checkBox1)
        }
        dialogView.findViewById<ConstraintLayout>(R.id.option2).setOnClickListener {
            switchSelected(R.id.checkBox2)
        }
        dialogView.findViewById<ConstraintLayout>(R.id.option3).setOnClickListener {
            switchSelected(R.id.checkBox3)
        }
        dialogView.findViewById<ConstraintLayout>(R.id.option4).setOnClickListener {
            switchSelected(R.id.checkBox4)
        }
        dialogView.findViewById<ConstraintLayout>(R.id.option5).setOnClickListener {
            switchSelected(R.id.checkBox5)
        }

        dialogView.findViewById<ImageButton>(R.id.btnRepeatEnd).setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext(), R.style.CustomDatePickerDialog, { _, selectedYear, selectedMonth, selectedDay ->
                endRepeatDate = LocalDate.parse("$selectedDay/${selectedMonth + 1}/$selectedYear", DateTimeFormatter.ofPattern("d/M/y"))
                dialogView.findViewById<TextView>(R.id.txtRepeatEndDate).setText(endRepeatDate.format(
                    DateTimeFormatter.ofPattern("d. M. yyyy")))
            }, endRepeatDate.year, endRepeatDate.monthValue-1, endRepeatDate.dayOfMonth)

            datePickerDialog.datePicker.firstDayOfWeek = Calendar.MONDAY

            datePickerDialog.show()
        }

        dialogView.findViewById<SeekBar>(R.id.sbRepeatFrequency).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Display the current value of the SeekBar, e.g., in a TextView or as a Toast
                val progressValue = progress.toString()
                // Example: Display in a TextView
                dialogView.findViewById<TextView>(R.id.txtRepeatFrequency).text = "Repeat every " + progressValue
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Implement if you need to handle the event when the user starts dragging
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Implement if you need to handle the event when the user stops dragging
            }
        })

        return AlertDialog.Builder(requireContext(), R.style.RecurrenceDialog)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ -> }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
    }

    fun switchSelected(newSelectedOption: Int){
        dialogView.findViewById<View>(selectedOption).setBackgroundResource(R.drawable.baseline_check_box_outline_blank_24)
        selectedOption = newSelectedOption
        dialogView.findViewById<View>(selectedOption).setBackgroundResource(R.drawable.baseline_check_box_24)
        if (newSelectedOption != R.id.checkBox1) {
            dialogView.findViewById<ConstraintLayout>(R.id.vRepeatEndDate).alpha = 1F
            dialogView.findViewById<ConstraintLayout>(R.id.vRepeatFrequency).alpha = 1F
            dialogView.findViewById<ImageButton>(R.id.btnRepeatEnd).isClickable = true
            dialogView.findViewById<SeekBar>(R.id.sbRepeatFrequency).isEnabled = true
        }
        else {
            dialogView.findViewById<ConstraintLayout>(R.id.vRepeatEndDate).alpha = 0.5F
            dialogView.findViewById<ConstraintLayout>(R.id.vRepeatFrequency).alpha = 0.5F
            dialogView.findViewById<ImageButton>(R.id.btnRepeatEnd).isClickable = false
            dialogView.findViewById<SeekBar>(R.id.sbRepeatFrequency).isEnabled = false
        }
    }
}