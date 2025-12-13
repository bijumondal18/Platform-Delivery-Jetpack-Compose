package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class StateListResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: StateListData?
)

data class StateListData(
    @SerializedName("states") val states: List<State>?,
    @SerializedName("status") val status: Boolean?
)

data class State(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("slug") val slug: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

