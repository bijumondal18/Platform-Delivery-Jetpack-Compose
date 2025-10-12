package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.LoginResponse
import com.platform.platformdelivery.data.models.Route
import com.platform.platformdelivery.data.models.RouteHistory
import com.platform.platformdelivery.data.models.RoutePathModel
import com.platform.platformdelivery.data.remote.RetrofitClient

class RouteRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getAvailableRoutes(page: Int, perPage: Int, date: String): com.platform.platformdelivery.core.network.Result<RoutePathModel> {
        return try {
            val response = apiService.getAvailableRoutes(page, perPage, date)
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

}