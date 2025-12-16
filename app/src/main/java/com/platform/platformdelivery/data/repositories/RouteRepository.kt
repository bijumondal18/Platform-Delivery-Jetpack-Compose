package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.core.utils.ApiDebugUtils
import com.platform.platformdelivery.core.utils.LocationUtils
import com.platform.platformdelivery.data.models.BaseResponse
import com.platform.platformdelivery.data.models.LoginResponse
import com.platform.platformdelivery.data.models.RequestRouteDetails
import com.platform.platformdelivery.data.models.Route
import com.platform.platformdelivery.data.models.RouteDetailsResponse
import com.platform.platformdelivery.data.models.RouteHistory
import com.platform.platformdelivery.data.models.RoutePathModel
import com.platform.platformdelivery.data.remote.RetrofitClient

class RouteRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getAvailableRoutes(
        page: Int, 
        perPage: Int, 
        date: String, 
        radius: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): com.platform.platformdelivery.core.network.Result<RoutePathModel> {
        return try {
            // Format coordinates to 4 decimal places if provided
            // Only format if values are actually provided (not null)
            val formattedLat = latitude?.let { LocationUtils.formatCoordinate(it) }
            val formattedLng = longitude?.let { LocationUtils.formatCoordinate(it) }
            
            // Only pass radius if it's not null and not empty
            // Match Flutter behavior: only send parameters that have values
            val radiusParam = if (radius.isNullOrEmpty()) null else radius
            
            // Log request parameters for debugging
            val baseUrl = RetrofitClient.tokenProvider.getBaseUrl() ?: com.platform.platformdelivery.core.network.ApiConfig.baseUrl
            ApiDebugUtils.logAvailableRoutesRequest(
                baseUrl = baseUrl,
                page = page,
                perPage = perPage,
                date = date,
                radius = radiusParam,
                latitude = formattedLat?.toDoubleOrNull(),
                longitude = formattedLng?.toDoubleOrNull()
            )
            
            // Match Flutter: only send radius/lat/lng if they have actual values
            val response = apiService.getAvailableRoutes(
                page, 
                perPage, 
                date, 
                radiusParam,
                formattedLat,
                formattedLng
            )
            if (response.isSuccessful && response.body() != null) {
                com.platform.platformdelivery.core.network.Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch routes"
                com.platform.platformdelivery.core.network.Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }


    suspend fun getRouteHistory(page: Int, perPage: Int, date: String): com.platform.platformdelivery.core.network.Result<RouteHistory> {
        return try {
            val response = apiService.getRouteHistory(page, perPage, date)
            if (response.isSuccessful && response.body() != null) {
                com.platform.platformdelivery.core.network.Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch route history"
                com.platform.platformdelivery.core.network.Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun getRouteDetails(requestRouteDetails: RequestRouteDetails): com.platform.platformdelivery.core.network.Result<RouteDetailsResponse> {
        return try {
            val response = apiService.getRouteDetails(requestRouteDetails)
            if (response.isSuccessful && response.body() != null) {
                com.platform.platformdelivery.core.network.Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch route details"
                com.platform.platformdelivery.core.network.Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun getAcceptedTrips(page: Int, perPage: Int, date: String): com.platform.platformdelivery.core.network.Result<RoutePathModel> {
        return try {
            val response = apiService.getAcceptedTrips(page, perPage, date)
            if (response.isSuccessful && response.body() != null) {
                com.platform.platformdelivery.core.network.Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch accepted trips"
                com.platform.platformdelivery.core.network.Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun updateCurrentLocation(latitude: Double, longitude: Double): Result<BaseResponse> {
        return try {
            // Format coordinates to 4 decimal places
            val formattedLat = LocationUtils.formatCoordinate(latitude) ?: return Result.Error("Invalid latitude")
            val formattedLng = LocationUtils.formatCoordinate(longitude) ?: return Result.Error("Invalid longitude")
            
            val response = apiService.updateCurrentLocation(formattedLat, formattedLng)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to update location"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun acceptRoute(routeId: String): Result<BaseResponse> {
        return try {
            val response = apiService.acceptRoute(routeId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to accept route"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun cancelRoute(routeId: String, currentTime: String): Result<BaseResponse> {
        return try {
            val response = apiService.cancelRoute(routeId, currentTime)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to cancel route"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun tripStartTime(routeId: String, currentTime: String): Result<BaseResponse> {
        return try {
            val response = apiService.tripStartTime(routeId, currentTime)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to start trip"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun vehicleLoaded(routeId: String, waypointIds: String, lat: String, lng: String, datetime: String): Result<BaseResponse> {
        return try {
            val response = apiService.vehicleLoaded(routeId, waypointIds, lat, lng, datetime)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to load vehicle"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun routeDeliveryWithOptions(
        routeId: String,
        waypointId: String,
        deliveryStatus: String,
        currentTime: String,
        deliveryType: String? = null
    ): Result<BaseResponse> {
        return try {
            val response = apiService.routeDeliveryWithOptions(routeId, waypointId, deliveryStatus, currentTime, deliveryType)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to update delivery status"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

}