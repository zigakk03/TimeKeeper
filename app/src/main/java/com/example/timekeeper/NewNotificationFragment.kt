package com.example.timekeeper

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class NewNotificationFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_new_notification, container, false)

        var colorButton = ContextCompat.getColor(requireContext(), R.color.accent)
        view.findViewById<ImageButton>(R.id.btnColorPicker).setOnClickListener {
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(colorButton)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(8)
                .lightnessSliderOnly()
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

        view.findViewById<ImageButton>(R.id.btnSave).setOnClickListener {
            //database setup
            val db = NotificationDatabase.getDatabase(requireContext())
            val notifDao = db.notificationDao()
            lifecycleScope.launch {
                if (!view.findViewById<EditText>(R.id.iTxtTitle).text.isNullOrEmpty()) {
                    notifDao.upsertNotification(Notification(
                        0,
                        '#'+colorButton.toHexString(),
                        view.findViewById<EditText>(R.id.iTxtTitle).text.toString(),
                        view.findViewById<EditText>(R.id.iTxtDescription).text.toString(),
                        LocalDateTime.now()
                        ))

                    Navigation.findNavController(view).navigate(R.id.navigate_newNotification_to_home)
                }
                else {
                    val iTxtTitle = view.findViewById<EditText>(R.id.iTxtTitle)

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

                    colorAnim.start()

                    Handler(Looper.getMainLooper()).postDelayed({
                        iTxtTitle.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    }, 2000)
                }
            }
        }

        return view
    }
}