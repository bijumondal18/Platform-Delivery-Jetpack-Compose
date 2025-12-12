package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.NotificationResponse
import com.platform.platformdelivery.data.remote.RetrofitClient

class NotificationRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun getAllNotifications(page: Int, perPage: Int): com.platform.platformdelivery.core.network.Result<NotificationResponse> {
        return try {
            val response = apiService.getAllNotifications(page, perPage)
            if (response.isSuccessful && response.body() != null) {
                com.platform.platformdelivery.core.network.Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch notifications"
                com.platform.platformdelivery.core.network.Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }

    suspend fun getUnreadNotifications(page: Int, perPage: Int): com.platform.platformdelivery.core.network.Result<NotificationResponse> {
        return try {
            val response = apiService.getUnreadNotifications(page, perPage)
            if (response.isSuccessful && response.body() != null) {
                com.platform.platformdelivery.core.network.Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch unread notifications"
                com.platform.platformdelivery.core.network.Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }
}

