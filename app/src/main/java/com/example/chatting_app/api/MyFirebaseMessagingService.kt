package com.example.chatting_app.api

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.chatting_app.MainActivity
import com.example.chatting_app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val channelId = "teemo"

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val inte = Intent(this, MainActivity::class.java)
        inte.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(manager)

        val inte1 = PendingIntent.getActivity(this, 0, inte, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setAutoCancel(true)
            .setContentIntent(inte1)
            .build()

        manager.notify(Random.nextInt(), notification)
    }

    private fun createNotificationChannel(manager : NotificationManager){
        val channel = NotificationChannel(channelId,"teemochat", NotificationManager.IMPORTANCE_HIGH)

        channel.description = "New Chat"
        channel.enableLights(true)

        manager.createNotificationChannel(channel)
    }
}