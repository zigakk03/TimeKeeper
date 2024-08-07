package com.example.timekeeper

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.room.Room
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class NewNotificationFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_notification, container, false)

        var colorButton = Color.parseColor("#FF0FA2E6")
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
            val appContext = requireContext().applicationContext
            val db = Room.databaseBuilder(
                appContext,
                NotificationDatabase::class.java, "notifications"
            ).build()
            val notifDao = db.notificationDao()
            lifecycleScope.launch {
                // TODO: Saving custom notification
                notifDao.upsertNotification(Notification(0, "#FF0FA2E6", "test", "test d", LocalDateTime.now() ))
            }

            Navigation.findNavController(view).navigate(R.id.navigate_newNotification_to_home)
        }

        return view
    }
}