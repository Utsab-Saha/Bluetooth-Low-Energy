package com.example.ble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    fun createForegroundNotification(context: Context): Notification {
        val channel = NotificationChannel(
            Constants.FOREGROUND_CHANNEL_ID,
            "Proximity Service",
            NotificationManager.IMPORTANCE_LOW
        )

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        return NotificationCompat.Builder(context, Constants.FOREGROUND_CHANNEL_ID)
            .setContentTitle("Proximity Active")
            .setContentText("Scanning for nearby users")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
    }
}