package com.appdev.forgetmenot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class MyNotificationPublisher: BroadcastReceiver() {
    val NOTIFICATION_ID: String = "notification-id"
    val NOTIFICATION: String = "notification"
    var notificationManager: NotificationManager? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)

        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel("test", "Test_Alarm", importance)
        if (notificationManager!=null)
            notificationManager!!.createNotificationChannel(notificationChannel)

        val id: Int = intent.getIntExtra(NOTIFICATION_ID, 0)
        if (notificationManager!=null)
            notificationManager!!.notify(id, notification)
    }
}