package com.platform.platformdelivery.core.utils

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object GeocoderUtils {
    /**
     * Gets zip code from latitude and longitude using reverse geocoding
     * @param context Android context
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return Zip code string or null if not found
     */
    suspend fun getZipCodeFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) {
            return@withContext null
        }

        return@withContext try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use new API for Android 13+
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.postalCode
            } else {
                // Use deprecated API for older versions
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.postalCode
            }
        } catch (e: Exception) {
            null
        }
    }
}

