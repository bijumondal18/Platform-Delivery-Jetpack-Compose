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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.platform.platformdelivery.core.services.LocationService
import com.platform.platformdelivery.core.utils.PermissionUtils
import com.platform.platformdelivery.data.local.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var onPermissionStateChanged: ((Boolean, Boolean) -> Unit)? = null

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
                val locationGranted = fineGranted || coarseGranted

                val notificationGranted =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
                    } else {
                        true // Not required for older Android versions
                    }

                // Notify state change
                onPermissionStateChanged?.invoke(locationGranted, notificationGranted)

                // If both permissions granted, start location service and proceed
                if (locationGranted && notificationGranted) {
                    startLocationService()
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
                val hasLocationPermission = remember { mutableStateOf(PermissionUtils.hasLocationPermissions(this@MainActivity)) }
                val hasNotificationPermission = remember { mutableStateOf(PermissionUtils.hasNotificationPermission(this@MainActivity)) }

                // Set callback to update state when permissions change
                onPermissionStateChanged = { location, notification ->
                    hasLocationPermission.value = location
                    hasNotificationPermission.value = notification
                }

                PlatformDeliveryApp(
                    startDestination = startDestination,
                    hasLocationPermission = hasLocationPermission.value,
                    hasNotificationPermission = hasNotificationPermission.value,
                    onRequestPermissions = {
                        requestPermissions(hasLocationPermission.value, hasNotificationPermission.value)
                    },
                    onCheckPermissions = {
                        hasLocationPermission.value = PermissionUtils.hasLocationPermissions(this@MainActivity)
                        hasNotificationPermission.value = PermissionUtils.hasNotificationPermission(this@MainActivity)
                    }
                )
            }
        }
    }

    private fun requestPermissions(hasLocation: Boolean, hasNotification: Boolean) {
        val permissionsToRequest = mutableListOf<String>()
        
        // Add location permissions
        if (!hasLocation) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        // Add notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotification) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
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
        if (PermissionUtils.hasLocationPermissions(this)) {
            val intent = Intent(applicationContext, LocationService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }
    }

    fun stopLocationService() {
        val intent = Intent(applicationContext, LocationService::class.java)
        stopService(intent)
    }

    override fun onResume() {
        super.onResume()
        // Trigger permission check when app resumes
        onPermissionStateChanged?.invoke(
            PermissionUtils.hasLocationPermissions(this),
            PermissionUtils.hasNotificationPermission(this)
        )
    }
}
