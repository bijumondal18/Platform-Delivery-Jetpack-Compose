package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.DriverDetailsResponse
import com.platform.platformdelivery.data.remote.RetrofitClient

class ProfileRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getDriverDetails(): Result<DriverDetailsResponse> {
        return try {
            val response = apiService.getDriverDetails()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch driver details"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }
}

