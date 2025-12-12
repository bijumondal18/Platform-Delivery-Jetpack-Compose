package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.core.network.Result
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

    suspend fun getAvailableRoutes(page: Int, perPage: Int, date: String, zipCode: String? = null): com.platform.platformdelivery.core.network.Result<RoutePathModel> {
        return try {
            val response = apiService.getAvailableRoutes(page, perPage, date, zipCode)
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

}