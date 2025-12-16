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
                val body = response.body()!!
                // Check if the business logic indicates success (data.status == true)
                if (body.data?.status == true) {
                    Result.Success(body)
                } else {
                    // Business logic failure - extract error message
                    val errorMsg = body.data?.msg ?: body.message ?: "Failed to send reset password email"
                    Result.Error(errorMsg)
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to send reset password email"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Result<BaseResponse> {
        return try {
            val response = apiService.verifyOtp(email, otp)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Check if the business logic indicates success (data.status == true)
                if (body.data?.status == true) {
                    Result.Success(body)
                } else {
                    // Business logic failure - extract error message
                    val errorMsg = body.data?.msg ?: body.message ?: "Invalid OTP"
                    Result.Error(errorMsg)
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to verify OTP"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

}