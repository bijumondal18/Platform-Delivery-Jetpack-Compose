package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class ApiVersionResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: ApiVersionData?
)

data class ApiVersionData(
    @SerializedName("base_url")
    val baseUrl: String?
)


