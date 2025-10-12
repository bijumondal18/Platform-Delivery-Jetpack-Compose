package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class HeadData(
    @SerializedName("data") val bodydata: BodyData? = null,
    @SerializedName("status") val status: Boolean? = null
)