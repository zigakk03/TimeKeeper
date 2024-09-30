package com.example.timekeeper.fragments

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class NewReminderFragment : Fragment() {

    private lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_new_notification, container, false)

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

        view.findViewById<Switch>(R.id.swSelection).setOnCheckedChangeListener { switchView, isChecked ->
            switchSelection(isChecked)
        }

        return view
    }

    fun switchSelection(isChecked: Boolean){
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
                isClickable = true
            }
            view.findViewById<ImageButton>(R.id.btnEndDate).apply {
                alpha = 1.0F
                isClickable = true
            }
            view.findViewById<ImageButton>(R.id.btnRepeat).apply {
                alpha = 1.0F
                isClickable = true
            }
            view.findViewById<ImageButton>(R.id.btnEventReminderTime).apply {
                alpha = 1.0F
                isClickable = true
            }
            view.findViewById<Switch>(R.id.swIncludesTime).apply {
                alpha = 1.0F
                isClickable = true
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
                isClickable = false
            }
            view.findViewById<ImageButton>(R.id.btnEndDate).apply {
                alpha = 0.5F
                isClickable = false
            }
            view.findViewById<ImageButton>(R.id.btnRepeat).apply {
                alpha = 0.5F
                isClickable = false
            }
            view.findViewById<ImageButton>(R.id.btnEventReminderTime).apply {
                alpha = 0.5F
                isClickable = false
            }
            view.findViewById<Switch>(R.id.swIncludesTime).apply {
                alpha = 0.5F
                isClickable = false
            }

            view.findViewById<TextView>(R.id.txtTitle).setText(R.string.add_page_title1)
        }
    }
}