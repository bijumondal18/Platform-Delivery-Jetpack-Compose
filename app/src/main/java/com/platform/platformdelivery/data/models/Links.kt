package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class Links(
    @SerializedName("url") val url: String? = null,
    @SerializedName("label") val label: String? = null,
    @SerializedName("active") val active: Boolean? = null
)