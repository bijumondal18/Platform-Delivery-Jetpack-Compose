package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class RoutePathModel(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("headdata") val headdata: HeadData? = null
)

data class HeadData(
    @SerializedName("bodydata") val bodydata: BodyData? = null,
    @SerializedName("status") val status: Boolean? = null
)

data class BodyData(
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("RoutesData") val routesData: List<Route>? = null,
    @SerializedName("first_page_url") val firstPageUrl: String? = null,
    @SerializedName("from") val from: Int? = null,
    @SerializedName("last_page") val lastPage: Int? = null,
    @SerializedName("last_page_url") val lastPageUrl: String? = null,
    @SerializedName("links") val links: List<Links>? = null,
    @SerializedName("next_page_url") val nextPageUrl: String? = null,
    @SerializedName("path") val path: String? = null,
    @SerializedName("per_page") val perPage: Int? = null,
    @SerializedName("prev_page_url") val prevPageUrl: String? = null,
    @SerializedName("to") val to: Int? = null,
    @SerializedName("total") val total: Int? = null
)

data class Links(
    @SerializedName("url") val url: String? = null,
    @SerializedName("label") val label: String? = null,
    @SerializedName("active") val active: Boolean? = null
)
