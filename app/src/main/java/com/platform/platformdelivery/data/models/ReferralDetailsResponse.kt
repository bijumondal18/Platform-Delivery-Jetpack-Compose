package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class ReferralDetailsResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ReferralDetailsData?
)

data class ReferralDetailsData(
    @SerializedName("msg") val msg: String?,
    @SerializedName("data") val referralData: ReferralData?,
    @SerializedName("status") val status: Boolean?
)

data class ReferralData(
    @SerializedName("code") val code: String?,
    @SerializedName("android_url") val androidUrl: String?,
    @SerializedName("ios_url") val iosUrl: String?
)

