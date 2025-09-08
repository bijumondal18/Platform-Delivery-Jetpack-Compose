package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class Route(
    @SerializedName("id") val id: Int? = 0,
    @SerializedName("name") val name: String? = "",
    @SerializedName("details") val details: String? = "",
    @SerializedName("start_date") val startDate: String? = "",
    @SerializedName("start_time") val startTime: String? = "",
    @SerializedName("origin_place") val originPlace: String? = "",
    @SerializedName("origin_lat") val originLat: String? = "",
    @SerializedName("origin_lng") val originLng: String? = "",
    @SerializedName("destination_place") val destinationPlace: String? = "",
    @SerializedName("destination_lat") val destinationLat: String? = "",
    @SerializedName("destination_lng") val destinationLng: String? = "",
    @SerializedName("center_lat") val centerLat: String? = "",
    @SerializedName("center_lng") val centerLng: String? = "",
    @SerializedName("created_by") val createdBy: Int? = 0,
    @SerializedName("driver_price") val driverPrice: Any? = null,
    @SerializedName("distance") val distance: String? = "",
    @SerializedName("estimated_total_time") val estimatedTotalTime: String? = "",
    @SerializedName("estimated_end_time") val estimatedEndTime: String? = "",
    @SerializedName("price") val price: Any? = null,
    @SerializedName("accepted_by") val acceptedBy: Int? = 0,
    @SerializedName("status") val status: String? = "",
    @SerializedName("created_at") val createdAt: String? = "",
    @SerializedName("updated_at") val updatedAt: String? = "",
    @SerializedName("trip_end_time") val tripEndTime: Any?,
    @SerializedName("trip_start_time") val tripStartTime: Any?,
    @SerializedName("trip_total_time") val tripTotalTime: Any?,
    @SerializedName("final_price") val finalPrice: Any?,
    @SerializedName("final_driver_price") val finalDriverPrice: Any?,
    @SerializedName("trip_total_distance") val tripTotalDistance: Any?,
    @SerializedName("circuit_trip") val circuitTrip: Any?,
    @SerializedName("chat_room_id") val chatRoomId: Any?,
    @SerializedName("waypoints") val waypoints: List<Waypoint>?,
    @SerializedName("client") val client: User?
)
