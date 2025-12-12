package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class DriverDetailsResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: DriverDetailsData?
)

data class DriverDetailsData(
    @SerializedName("user") val user: User?
)

