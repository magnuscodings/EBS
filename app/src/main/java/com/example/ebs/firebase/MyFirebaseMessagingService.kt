package com.example.ebs.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ebs.NetworkUtils
import com.example.ebs.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val authRepository = NetworkUtils.getAuthRepository()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            val billingId = remoteMessage.data["billingId"]
            val amount = remoteMessage.data["amount"]

            // Create and show notification
            showNotification(
                "New Billing Generated",
                "A new billing of $$amount has been created."
            )
        }

        // If the message contains a notification payload
        remoteMessage.notification?.let { notification ->
            showNotification(
                notification.title ?: "New Notification",
                notification.body ?: ""
            )
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "billing_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Billing Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    // Handle token refresh
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        GlobalScope.launch {
            authRepository.updateFcmToken(token)
        }
    }

}