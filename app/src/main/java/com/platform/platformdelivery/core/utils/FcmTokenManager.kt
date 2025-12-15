package com.platform.platformdelivery.core.utils

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.data.repositories.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FcmTokenManager {
    private const val TAG = "FcmTokenManager"
    
    /**
     * Request FCM token and send it to backend
     */
    fun registerFcmToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "FCM Registration Token: $token")
            
            // Save token locally
            val tokenManager = TokenManager(context)
            tokenManager.saveFcmToken(token)
            
            // Send token to backend if user is logged in
            if (tokenManager.isLoggedIn()) {
                sendTokenToBackend(context, token)
            }
        }
    }
    
    /**
     * Send FCM token to backend via update profile API
     */
    private fun sendTokenToBackend(context: Context, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profileRepository = ProfileRepository()
                val result = profileRepository.updateProfile(fcmToken = token)
                if (result is com.platform.platformdelivery.core.network.Result.Success) {
                    Log.d(TAG, "FCM token sent to backend successfully")
                } else {
                    Log.e(TAG, "Failed to send FCM token to backend: ${(result as? com.platform.platformdelivery.core.network.Result.Error)?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception sending FCM token to backend", e)
            }
        }
    }
}
