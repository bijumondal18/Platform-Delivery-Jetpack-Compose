package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class RouteDetailsResponse(
    @SerializedName("status") val status:  String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val routeDetailsData: RouteDetailsData? = null
)

data class RouteDetailsData(
    @SerializedName("data") val routeData: Route? = null, //for route details
    @SerializedName("status") val status: Boolean? = null
)
