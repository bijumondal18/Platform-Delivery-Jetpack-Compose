package com.platform.platformdelivery.data.local

import android.content.Context
import com.platform.platformdelivery.data.remote.RetrofitClient
import androidx.core.content.edit

class TokenManager(context: Context) : RetrofitClient.TokenProvider {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_ONLINE = "is_online"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROFILE_PIC = "profile_pic"
        private const val KEY_LOGGED_ON = "is_logged_in"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_PUSH_NOTIFICATION = "push_notification"
        private const val KEY_EMAIL_NOTIFICATION = "email_notification"
        private const val KEY_SMS_NOTIFICATION = "sms_notification"
    }

    override fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    override fun refreshAccessToken(): String? {
        // In real-world: call refresh endpoint synchronously
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun saveAccessToken(token: String) {
        prefs.edit { putString(KEY_ACCESS_TOKEN, token) }
    }

    fun saveRefreshToken(token: String) {
        prefs.edit { putString(KEY_REFRESH_TOKEN, token) }
    }

    fun setIsLoggedIn(isLoggedIn: Boolean) {
        prefs.edit { putBoolean(KEY_LOGGED_ON, isLoggedIn) }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_ON, false)


    fun saveUserId(userId: Int) {
        prefs.edit { putInt(KEY_USER_ID, userId) }
    }

    fun getUserId(): Int? = prefs.getInt(KEY_USER_ID, 0)

    fun saveOnlineStatus(isOnline: Boolean) {
        prefs.edit { putBoolean(KEY_IS_ONLINE, isOnline) }
    }

    fun isOnline(): Boolean = prefs.getBoolean(KEY_IS_ONLINE, false)


    fun getName(): String? = prefs.getString(KEY_NAME, null)

    fun saveName(name: String) {
        prefs.edit { putString(KEY_NAME, name) }
    }

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun saveEmail(email: String) {
        prefs.edit { putString(KEY_EMAIL, email) }
    }

    fun getProfilePic(): String? = prefs.getString(KEY_PROFILE_PIC, null)

    fun saveProfilePic(profilePic: String) {
        prefs.edit { putString(KEY_PROFILE_PIC, profilePic) }
    }

    fun saveBaseUrl(baseUrl: String) {
        prefs.edit { putString(KEY_BASE_URL, baseUrl) }
    }

    override fun getBaseUrl(): String? = prefs.getString(KEY_BASE_URL, null)

    fun isDarkTheme(): Boolean? {
        return if (prefs.contains(KEY_DARK_THEME)) {
            prefs.getBoolean(KEY_DARK_THEME, false)
        } else {
            null // No preference set yet
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        prefs.edit { putBoolean(KEY_DARK_THEME, isDark) }
    }

    // Notification preferences
    fun isPushNotificationEnabled(): Boolean = prefs.getBoolean(KEY_PUSH_NOTIFICATION, true)

    fun setPushNotificationEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_PUSH_NOTIFICATION, enabled) }
    }

    fun isEmailNotificationEnabled(): Boolean = prefs.getBoolean(KEY_EMAIL_NOTIFICATION, true)

    fun setEmailNotificationEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_EMAIL_NOTIFICATION, enabled) }
    }

    fun isSmsNotificationEnabled(): Boolean = prefs.getBoolean(KEY_SMS_NOTIFICATION, true)

    fun setSmsNotificationEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_SMS_NOTIFICATION, enabled) }
    }

    fun clear() {
        prefs.edit { clear() }
    }
}