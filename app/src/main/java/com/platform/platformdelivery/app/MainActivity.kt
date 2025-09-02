package com.platform.platformdelivery.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.platform.platformdelivery.data.local.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }


        lifecycleScope.launch {
            // Do token check in background
            val tokenManager = TokenManager(this@MainActivity)
            val token = withContext(Dispatchers.IO) {
                tokenManager.getAccessToken()
            }

            // Decide navigation target based on token
            val startDestination = if (token.isNullOrEmpty()) {
                "login"
            } else {
                "main"
            }

            // Now hide splash and set content
            keepSplash = false
            setContent {
                PlatformDeliveryApp(startDestination = startDestination)
            }
        }
    }
}