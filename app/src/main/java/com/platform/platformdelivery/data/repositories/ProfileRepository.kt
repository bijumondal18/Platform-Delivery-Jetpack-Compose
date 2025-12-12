package com.platform.platformdelivery.data.repositories

import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.data.models.DriverDetailsResponse
import com.platform.platformdelivery.data.remote.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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

    suspend fun updateProfile(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        street: String? = null,
        city: String? = null,
        state: String? = null,
        zip: String? = null,
        baseLocation: String? = null,
        baseLocationLat: String? = null,
        baseLocationLng: String? = null,
        profilePicFile: File? = null
    ): Result<DriverDetailsResponse> {
        return try {
            // Convert strings to RequestBody
            val nameBody = name?.toRequestBody("text/plain".toMediaTypeOrNull())
            val emailBody = email?.toRequestBody("text/plain".toMediaTypeOrNull())
            val phoneBody = phone?.toRequestBody("text/plain".toMediaTypeOrNull())
            val streetBody = street?.toRequestBody("text/plain".toMediaTypeOrNull())
            val cityBody = city?.toRequestBody("text/plain".toMediaTypeOrNull())
            val stateBody = state?.toRequestBody("text/plain".toMediaTypeOrNull())
            val zipBody = zip?.toRequestBody("text/plain".toMediaTypeOrNull())
            val baseLocationBody = baseLocation?.toRequestBody("text/plain".toMediaTypeOrNull())
            val baseLocationLatBody = baseLocationLat?.toRequestBody("text/plain".toMediaTypeOrNull())
            val baseLocationLngBody = baseLocationLng?.toRequestBody("text/plain".toMediaTypeOrNull())

            // Handle profile picture upload
            var profilePicPart: MultipartBody.Part? = null
            profilePicFile?.let { file ->
                if (file.exists()) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    profilePicPart = MultipartBody.Part.createFormData("profile_pic", file.name, requestFile)
                }
            }

            val response = apiService.updateProfile(
                name = nameBody,
                email = emailBody,
                phone = phoneBody,
                street = streetBody,
                city = cityBody,
                state = stateBody,
                zip = zipBody,
                baseLocation = baseLocationBody,
                baseLocationLat = baseLocationLatBody,
                baseLocationLng = baseLocationLngBody,
                profilePic = profilePicPart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to update profile"
                Result.Error(errorMsg)
            }
        } catch (e: Exception) {
            Result.Error("Exception occurred: ${e.message}", e)
        }
    }
}

