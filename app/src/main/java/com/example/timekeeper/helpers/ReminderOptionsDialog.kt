package com.example.timekeeper.helpers

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.timekeeper.R

class ReminderOptionsDialog(hasTime: Boolean, selectedOptions: String) : DialogFragment() {

    val includesTime: Boolean = hasTime
    val selectedOptions: String = selectedOptions

    // View for accessing the component of the reminder options dialog
    private lateinit var dialogView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the layout of the repeat dialog
        dialogView = layoutInflater.inflate(R.layout.alert_dialog, null)

        dialogView.findViewById<TextView>(R.id.txtAlertDialogTitle).text = "Select event reminder"

        val redableOptions: Array<String> =
            if (includesTime) {
                arrayOf(
                    "10 minutes before",
                    "15 minutes before",
                    "20 minutes before",
                    "30 minutes before",
                    "45 minutes before",
                    "1 hour before",
                    "2 hours before",
                    "3 hours before",
                    "4 hours before",
                    "5 hours before",
                    "6 hours before",
                    "9 hours before",
                    "12 hours before",
                    "1 day before",
                    "2 days before",
                    "3 days before",
                    "5 days before",
                    "7 days before"
                )
            } else {
                arrayOf(
                    "1 day before",
                    "2 days before",
                    "3 days before",
                    "5 days before",
                    "7 days before"
                )
            }

        val options: Array<String> =
            if (includesTime) {
                arrayOf(
                    "10m",
                    "15m",
                    "20m",
                    "30m",
                    "45m",
                    "1h",
                    "2h",
                    "3h",
                    "4h",
                    "5h",
                    "6h",
                    "9h",
                    "12h",
                    "1d",
                    "2d",
                    "3d",
                    "5d",
                    "7d"
                )
            } else {
                arrayOf(
                    "1d",
                    "2d",
                    "3d",
                    "5d",
                    "7d"
                )
            }

        // Set up the ListView
        val listView = dialogView.findViewById<ListView>(R.id.lvOptions)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_multiple_choice, // Use multiple-choice layout
            redableOptions
        )
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE // Enable multiple selections

        val selectedOptionsList = selectedOptions.split(", ")
        Log.i("test", "onCreateDialog: $selectedOptionsList")
        for (optionItem in options) {
            for (selectedItem in selectedOptionsList) {
                if (optionItem == selectedItem)
                    listView.setItemChecked(options.indexOf(optionItem), true)
            }
        }

        return AlertDialog.Builder(requireContext(), R.style.RecurrenceDialog)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedItems = mutableListOf<String>()

                for (i in 0 until listView.count) {
                    if (listView.isItemChecked(i)) {
                        selectedItems.add(options[i])
                    }
                }

                var transferString: String = ""
                for (item in selectedItems){
                    transferString += item + ", "
                }

                if (transferString.length > 0){
                    transferString = transferString.dropLast(2)
                }

                val result = Bundle().apply {
                    putString("selectedOptions", transferString)
                }
                setFragmentResult("notificationFragmentDialogRequestCode", result)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }
}