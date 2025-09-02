package com.platform.platformdelivery.app

import android.app.Application
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.data.remote.RetrofitClient

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize tokenProvider with SharedPreferences/DataStore implementation
        RetrofitClient.tokenProvider = TokenManager(this)
    }
}