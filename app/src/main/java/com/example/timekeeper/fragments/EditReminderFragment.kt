package com.example.timekeeper.fragments

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.example.timekeeper.R
import com.example.timekeeper.adapters.NotificationAdapter
import com.example.timekeeper.database.Reminder
import com.example.timekeeper.database.ReminderDatabase
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalStdlibApi::class)
class EditReminderFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_notification, container, false)

        // Variable of the colorButton background color
        var colorButton = ContextCompat.getColor(requireContext(), R.color.accent)
        // Gets the navigation arguments
        val args: EditReminderFragmentArgs by navArgs()
        lifecycleScope.launch {
            // Database setup
            val db = ReminderDatabase.getDatabase(requireContext())
            val notifDao = db.reminderDao()
            // Gets a reminder based on the given id
            val selectedNotification = notifDao.getReminder(args.notificationId)

            // Sets the colorButton to color of the gotten reminder
            colorButton = Color.parseColor(selectedNotification.color)
            // Fills the views with the gotten reminder
            view.findViewById<ImageButton>(R.id.btnColorPicker).setBackgroundColor(colorButton)
            view.findViewById<EditText>(R.id.iTxtTitle).setText(selectedNotification.title)
            view.findViewById<EditText>(R.id.iTxtDescription).setText(selectedNotification.description)
        }

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
            val reminderDao = db.reminderDao()
            lifecycleScope.launch {
                // Check if iTxtTitle is empty
                if (!view.findViewById<EditText>(R.id.iTxtTitle).text.isNullOrEmpty()) {
                    // Values of a reminder
                    val titleTxt = view.findViewById<EditText>(R.id.iTxtTitle).text.toString()
                    val descriptionTxt = view.findViewById<EditText>(R.id.iTxtDescription).text.toString()
                    val notificationColor = '#'+colorButton.toHexString()

                    // Updates the reminder
                    reminderDao.upsertReminder(
                        Reminder(
                        args.notificationId,
                        notificationColor,
                        titleTxt,
                        descriptionTxt,
                        LocalDateTime.now(),
                            null
                    )
                    )

                    // Updates the notification
                    NotificationAdapter.createAndShowNotification(
                        requireContext(),
                        notificationColor,
                        titleTxt,
                        descriptionTxt,
                        args.notificationId
                    )

                    // Navigates to the home page
                    Navigation.findNavController(view).navigate(R.id.navigate_editReminder_to_home)
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

        return view
    }
}