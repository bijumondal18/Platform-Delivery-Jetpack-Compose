package com.platform.platformdelivery.data.local

import android.content.Context
import com.platform.platformdelivery.data.remote.RetrofitClient

class TokenManager(context: Context) : RetrofitClient.TokenProvider {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    override fun getAccessToken(): String? = prefs.getString("access_token", null)

    override fun refreshAccessToken(): String? {
        // Ideally: make synchronous API call to refresh token
        // For now, just return the saved refresh token or new token
        return prefs.getString("access_token", null)
    }

    fun saveAccessToken(token: String) {
        prefs.edit().putString("access_token", token).apply()
    }
}