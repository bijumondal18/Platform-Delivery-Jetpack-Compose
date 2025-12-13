package com.platform.platformdelivery.core.utils

import android.util.Log

object ApiDebugUtils {
    private const val TAG = "ApiDebug"
    
    fun logAvailableRoutesRequest(
        baseUrl: String,
        page: Int,
        perPage: Int,
        date: String,
        radius: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        Log.d(TAG, "=== Available Routes API Request ===")
        Log.d(TAG, "Base URL: $baseUrl")
        Log.d(TAG, "Page: $page")
        Log.d(TAG, "Per Page: $perPage")
        Log.d(TAG, "Date: $date")
        Log.d(TAG, "Radius: ${radius ?: "null"}")
        Log.d(TAG, "Latitude: ${latitude ?: "null"}")
        Log.d(TAG, "Longitude: ${longitude ?: "null"}")
        Log.d(TAG, "Full URL: ${baseUrl}available-routes?page=$page&perpage=$perPage&date=$date${if (radius != null) "&radius=$radius" else ""}${if (latitude != null) "&lat=$latitude" else ""}${if (longitude != null) "&lng=$longitude" else ""}")
        Log.d(TAG, "=====================================")
    }
    
    fun logApiVersionRequest(baseUrl: String) {
        Log.d(TAG, "=== API Version Request ===")
        Log.d(TAG, "API Version URL: $baseUrl")
        Log.d(TAG, "===========================")
    }
    
    fun logApiVersionResponse(baseUrl: String?) {
        Log.d(TAG, "=== API Version Response ===")
        Log.d(TAG, "Received Base URL: ${baseUrl ?: "null"}")
        Log.d(TAG, "============================")
    }
}

