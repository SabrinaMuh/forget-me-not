package com.appdev.forgetmenot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi

class MyNotificationPublisher: BroadcastReceiver() {
    val NOTIFICATION_ID: String = "notification-id"
    val TITLE: String = "title"
    val TEXT: String = "text"
    var notificationManager: NotificationManager? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = intent.getStringExtra(TITLE)
        val text = intent.getStringExtra(TEXT)

        val notification = createNotification(context, title, text)

        val importance = NotificationManager.IMPORTANCE_HIGH
        //NOTE TO ME: Check this everytime before you use the app
        //NOTE TO ME: Don't change it without to change it also by the notification
        val notificationChannel = NotificationChannel("forget-me-not", "Forget-Me-Not_Alarm", importance)
        if (notificationManager!=null)
            notificationManager!!.createNotificationChannel(notificationChannel)

        //notificationid needs to be unique by notificationmanager
        val id: Int = intent.getIntExtra(NOTIFICATION_ID, 0)
        if (notificationManager!=null)
            notificationManager!!.notify(id, notification)
    }

    //create notification
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification(context: Context, title: String?, text: String?): Notification {
        //open app after you clicked on the notification
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        return Notification.Builder(context, "forget-me-not")
            .setTicker(title)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.notification_icon)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setLights(Color.BLUE, 3000, 3000)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }
}