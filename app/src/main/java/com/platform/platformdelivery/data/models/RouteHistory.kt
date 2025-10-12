package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class RouteHistory(
    @SerializedName("status") val status:  String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val headdata: HeadData? = null
)