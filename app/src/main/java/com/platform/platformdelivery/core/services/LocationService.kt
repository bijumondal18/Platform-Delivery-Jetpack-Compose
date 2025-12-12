package com.platform.platformdelivery.core.services


import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.platform.platformdelivery.core.utils.LocationUtils
import com.platform.platformdelivery.data.repositories.RouteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val routeRepository = RouteRepository()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "location_channel"
        const val NOTIFICATION_ID = 123
        const val TAG = "LocationService"
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // update every 5s
            5000
        )
            .setMinUpdateIntervalMillis(3000)
            .setWaitForAccurateLocation(true)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location: Location in result.locations) {
                    // Format coordinates to 4 decimal places for logging
                    val formattedLat = LocationUtils.formatCoordinate(location.latitude)
                    val formattedLng = LocationUtils.formatCoordinate(location.longitude)
                    
                    Log.d(TAG, "📍 Location update: $formattedLat, $formattedLng")
                    
                    // Send location update to server (repository will format to 4 decimal places)
//                    serviceScope.launch {
//                        try {
//                            val updateResult = routeRepository.updateCurrentLocation(
//                                location.latitude,
//                                location.longitude
//                            )
//                            when (updateResult) {
//                                is com.platform.platformdelivery.core.network.Result.Success -> {
//                                    Log.d(TAG, "✅ Location updated successfully")
//                                }
//                                is com.platform.platformdelivery.core.network.Result.Error -> {
//                                    Log.e(TAG, "❌ Failed to update location: ${updateResult.message}")
//                                }
//                                else -> Unit
//                            }
//                        } catch (e: Exception) {
//                            Log.e(TAG, "Exception updating location: ${e.message}", e)
//                        }
//                    }
                }
            }
        }

        startForegroundService()
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking location")
            .setContentText("Your location is being tracked")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        } else {
            Log.w(TAG, "No location permission granted.")
            stopSelf() // 🔴 Don’t keep service alive if useless
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
