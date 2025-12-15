package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.data.models.BaseResponse
import com.platform.platformdelivery.data.models.LoginResponse
import com.platform.platformdelivery.data.remote.RetrofitClient
import com.platform.platformdelivery.core.network.Result

class AuthRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(email, password)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Login failed"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun forgotPassword(email: String): Result<BaseResponse> {
        return try {
            val response = apiService.forgotPassword(email)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to send password reset email"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

}