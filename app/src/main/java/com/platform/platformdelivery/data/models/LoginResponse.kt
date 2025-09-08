package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: LoginData?
)

data class LoginData(
    @SerializedName("msg") val msg: String?,
    @SerializedName("status") val status: Boolean?,
    @SerializedName("token") val token: String?,
    @SerializedName("user") val user: User?
)
