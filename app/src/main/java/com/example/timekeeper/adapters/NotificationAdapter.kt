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

object NotificationAdapter {
    @SuppressLint("MissingPermission")
    fun createAndShowNotification(
        context: Context,
        notificationColor: String,
        titleTxt: String,
        descriptionTxt: String,
        reminderId: Int
    ){
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // TODO - investigate why it isn't deleting
        val deleteIntent = Intent(context, NotificationDismissReceiver::class.java).apply{
            putExtra("reminder_id", reminderId)
        }
        val pendingDeleteIntent = PendingIntent.getActivity(
            context,
            2,
            deleteIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

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


        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define.
            notify(reminderId, builder.build())
        }
    }
}