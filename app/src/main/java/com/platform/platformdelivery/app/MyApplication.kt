package com.platform.platformdelivery.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.data.remote.RetrofitClient
import android.util.Log

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize tokenProvider with SharedPreferences/DataStore implementation
        RetrofitClient.tokenProvider = TokenManager(this)
        
        // Request FCM token
        requestFcmToken()
    }
    
    private fun requestFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "FCM Registration Token: $token")
            
            // Save token locally
            val tokenManager = TokenManager(this)
            tokenManager.saveFcmToken(token)
            
            // Send token to backend if user is logged in
            if (tokenManager.isLoggedIn()) {
                sendFcmTokenToBackend(token)
            }
        }
    }
    
    private fun sendFcmTokenToBackend(token: String) {
        // This will be handled by ProfileViewModel when user logs in
        // or when token is refreshed
        Log.d(TAG, "FCM token should be sent to backend: $token")
    }
    
    companion object {
        private const val TAG = "MyApplication"
    }
}