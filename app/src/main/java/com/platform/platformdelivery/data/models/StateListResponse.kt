package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class StateListResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: StateListData?
)

data class StateListData(
    @SerializedName("states") val states: List<State>?
)

data class State(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("code") val code: String?
)

