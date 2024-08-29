package com.example.timekeeper.adapters

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.timekeeper.App
import com.example.timekeeper.MainActivity
import com.example.timekeeper.R
import com.example.timekeeper.helpers.NotificationDismissReceiver

object NotificationAdapter {
    // Reference to the ReminderAdapter used on the home page
    var mainReminderAdapter: ReminderAdapter? = null

    // Creates and shows a notification
    @SuppressLint("MissingPermission")
    fun createAndShowNotification(
        context: Context,
        notificationColor: String,
        titleTxt: String,
        descriptionTxt: String,
        reminderId: Int
    ){
        // Defines tap intent
        val intent = Intent(context, MainActivity::class.java)
        // Defines pending tap intent
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Defines dismiss intent
        val deleteIntent = Intent(context, NotificationDismissReceiver::class.java).apply{
            // Extra information for deletion of the reminder
            putExtra("reminder_id", reminderId)
        }
        // Defines pending dismiss intent
        val pendingDeleteIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            deleteIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Builds the notification
        var builder = NotificationCompat.Builder(context, App.CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_notifications_24)
            .setColor(Color.parseColor(notificationColor))
            .setContentTitle(titleTxt)
            .setContentText(descriptionTxt)
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText(descriptionTxt))
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(pendingDeleteIntent)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)

        // Show or update the notification
        with(NotificationManagerCompat.from(context)) {
            notify(reminderId, builder.build())
        }
    }
}