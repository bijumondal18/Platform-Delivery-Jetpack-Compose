package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.EarningResponse
import com.platform.platformdelivery.data.models.RequestRouteDetails
import com.platform.platformdelivery.data.models.RouteDetailsResponse
import com.platform.platformdelivery.data.remote.RetrofitClient

class EarningRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getEarningDetails(): com.platform.platformdelivery.core.network.Result<EarningResponse> {
        return try {
            val response = apiService.getEarningDetails()
            if (response.isSuccessful && response.body() != null) {
                com.platform.platformdelivery.core.network.Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch earning details"
                com.platform.platformdelivery.core.network.Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }
}