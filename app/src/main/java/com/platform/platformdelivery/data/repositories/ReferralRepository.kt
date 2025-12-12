package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.ReferralDetailsResponse
import com.platform.platformdelivery.data.remote.RetrofitClient

class ReferralRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getReferralDetails(): Result<ReferralDetailsResponse> {
        return try {
            val response = apiService.getReferralDetails()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch referral details"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }
}

