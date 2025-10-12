package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class EarningResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val earning: Earning? = null
)

data class Earning(
    @SerializedName("earnings") val earnings: String? = null,
    @SerializedName("total_distance") val totalDistance: String? = null,
    @SerializedName("total_time") val totalTime: Any? = null,
    @SerializedName("total_withdrawn") val totalWithdrawn: String? = null,
    @SerializedName("current_due") val currentDue: String? = null,
    @SerializedName("next_payout_date") val nextPayoutDate: String? = null,
    @SerializedName("last_payout_date") val lastPayoutDate: String? = null,
    @SerializedName("last_payout") val lastPayout: String? = null,
    @SerializedName("status") val status: Boolean? = null
)
