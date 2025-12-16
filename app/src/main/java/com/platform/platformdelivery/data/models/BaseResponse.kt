package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class BaseResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: BaseResponseData? = null
)

data class BaseResponseData(
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("status") val status: Boolean? = null
)


