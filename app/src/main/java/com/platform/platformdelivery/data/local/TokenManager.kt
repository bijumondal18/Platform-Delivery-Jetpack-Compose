package com.platform.platformdelivery.data.local

import android.content.Context
import com.platform.platformdelivery.data.remote.RetrofitClient

class TokenManager(context: Context) : RetrofitClient.TokenProvider {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
    }

    override fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    override fun refreshAccessToken(): String? {
        // In real-world: call refresh endpoint synchronously
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Int? = prefs.getInt(KEY_USER_ID, 0)

    fun clear() {
        prefs.edit().clear().apply()
    }
}