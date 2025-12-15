package com.platform.platformdelivery.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.platform.platformdelivery.R
import com.platform.platformdelivery.app.MainActivity

class PlatformFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Save token locally
        val tokenManager = com.platform.platformdelivery.data.local.TokenManager(this)
        tokenManager.saveFcmToken(token)
        
        // Send token to backend if user is logged in
        if (tokenManager.isLoggedIn()) {
            sendTokenToServer(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            
            // If notification payload exists, it will be handled by system when app is in background
            // When app is in foreground, we handle it manually
            if (remoteMessage.notification != null) {
                // Notification payload exists - show notification
                remoteMessage.notification?.let {
                    Log.d(TAG, "Message Notification Body: ${it.body}")
                    showNotification(
                        title = it.title ?: "New Route Assigned",
                        body = it.body ?: "",
                        data = remoteMessage.data
                    )
                }
            } else {
                // Data-only message - handle it manually
                handleDataMessage(remoteMessage.data)
            }
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "New Route Assigned"
        val body = data["body"] ?: data["message"] ?: "You have been assigned a new route"
        val routeId = data["route_id"] ?: data["notifiable_id"]
        
        showNotification(title, body, data)
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val routeId = data["route_id"] ?: data["notifiable_id"] ?: ""
        
        // Create notification channel for Android O and above
        createNotificationChannel()

        // Create intent for when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (routeId.isNotEmpty()) {
                putExtra("route_id", routeId)
                putExtra("from_notification", true)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_delivery_truck)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToServer(token: String) {
        // Send token to backend using FcmTokenManager
        com.platform.platformdelivery.core.utils.FcmTokenManager.registerFcmToken(this)
    }

    companion object {
        private const val TAG = "FirebaseMsgService"
        const val CHANNEL_ID = "route_notifications"
        const val CHANNEL_NAME = "Route Notifications"
        const val CHANNEL_DESCRIPTION = "Notifications for new route assignments and updates"
    }
}
