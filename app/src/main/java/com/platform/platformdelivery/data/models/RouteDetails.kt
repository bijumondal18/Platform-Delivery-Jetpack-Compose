package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class RouteDetails(
    @SerializedName("status") val status: Any?,
    @SerializedName("message") val message: Any?,
    @SerializedName("data") val routeDetailsData: RoueDetailsData?
)

data class RoueDetailsData(
    @SerializedName("data") val routeDetails: Route?,
    @SerializedName("status") val status: Boolean?
)