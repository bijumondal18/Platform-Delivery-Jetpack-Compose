package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class RoutePathModel(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: RoutePathDataWrapper? = null
)

// Wrapper to handle both structures: 
// 1. data.headdata.bodydata.routesData (for available routes)
// 2. data.data.data (for accepted routes)
data class RoutePathDataWrapper(
    // For accepted routes: data.data structure
    @SerializedName("data") val nestedData: RoutePathNestedData? = null,
    // For available routes: data.headdata structure  
    @SerializedName("headdata") val headdata: HeadData? = null,
    @SerializedName("status") val status: Boolean? = null
)

// Handle nested data structure for accepted routes (data.data.data)
data class RoutePathNestedData(
    @SerializedName("current_page") val currentPage: Int? = null,
    @SerializedName("data") val routesData: List<Route>? = null,
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
