package com.platform.platformdelivery.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.platform.platformdelivery.core.services.LocationService
import com.platform.platformdelivery.core.utils.PermissionUtils
import com.platform.platformdelivery.data.local.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }


        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
                val backgroundGranted =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
                    } else true

                val notificationGranted =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
                    } else true

                if (fineGranted || coarseGranted || backgroundGranted) {
                    // ✅ Permissions granted → now safe to start location service
                    startLocationService()
                } else {
                    // ❌ User denied → show a message/snackbar
                }
            }


        lifecycleScope.launch {
            // Do token check in background
            val tokenManager = TokenManager(this@MainActivity)
            val isLoggedIn = withContext(Dispatchers.IO) {
                tokenManager.isLoggedIn()
            }

            // Decide navigation target based on token
            val startDestination = if (!isLoggedIn) {
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

    fun requestOrStartLocationService() {
        if (PermissionUtils.hasLocationPermissions(this)) {
            // ✅ Already granted → directly start
            startLocationService()
        } else {
            // 🚨 Ask permissions
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun startLocationService() {
        val intent = Intent(applicationContext, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    fun stopLocationService() {
        val intent = Intent(applicationContext, LocationService::class.java)
        stopService(intent)
    }

}