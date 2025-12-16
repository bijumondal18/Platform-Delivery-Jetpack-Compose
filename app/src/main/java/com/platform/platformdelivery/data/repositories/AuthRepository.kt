package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.data.models.BaseResponse
import com.platform.platformdelivery.data.models.LoginResponse
import com.platform.platformdelivery.data.remote.RetrofitClient
import com.platform.platformdelivery.core.network.Result
import okhttp3.ResponseBody
import java.io.IOException

class AuthRepository {
    private val apiService = RetrofitClient.apiService
    
    /**
     * Safely reads error body from response.
     * Handles cases where the body stream is already closed (e.g., by logging interceptor).
     */
    private fun ResponseBody?.safeString(): String? {
        return try {
            this?.string()
        } catch (e: IllegalStateException) {
            // Body stream already closed (likely by logging interceptor)
            null
        } catch (e: IOException) {
            // IO error reading body
            null
        }
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(email, password)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody().safeString() ?: "Login failed"
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
                val errorMsg = response.errorBody().safeString() ?: "Failed to send reset password email"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun verifyOtp(userId: String, otp: String): Result<BaseResponse> {
        return try {
            val response = apiService.verifyOtp(userId, otp)
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
                val errorMsg = response.errorBody().safeString() ?: "Failed to verify OTP"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun resendOtp(email: String): Result<BaseResponse> {
        return try {
            val response = apiService.resendOtp(email)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Check if the business logic indicates success (data.status == true)
                if (body.data?.status == true) {
                    Result.Success(body)
                } else {
                    // Business logic failure - extract error message
                    val errorMsg = body.data?.msg ?: body.message ?: "Failed to resend OTP"
                    Result.Error(errorMsg)
                }
            } else {
                val errorMsg = response.errorBody().safeString() ?: "Failed to resend OTP"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun resetPassword(token: String, password: String): Result<BaseResponse> {
        return try {
            val response = apiService.resetPassword(token, password)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Check if the business logic indicates success (data.status == true)
                if (body.data?.status == true) {
                    Result.Success(body)
                } else {
                    // Business logic failure - extract error message
                    val errorMsg = body.data?.msg ?: body.message ?: "Failed to reset password"
                    Result.Error(errorMsg)
                }
            } else {
                val errorMsg = response.errorBody().safeString() ?: "Failed to reset password"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

}