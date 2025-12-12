package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: NotificationDataWrapper? = null
)

data class NotificationDataWrapper(
    @SerializedName("data") val data: NotificationData? = null,
    @SerializedName("status") val status: Boolean? = null
)

data class NotificationData(
    @SerializedName("notifications") val notifications: List<Notification>? = null,
    @SerializedName("total_page") val totalPage: Int? = null
)

data class Notification(
    @SerializedName("id") val id: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("notifiable_type") val notifiableType: String? = null,
    @SerializedName("notifiable_id") val notifiableId: Int? = null,
    @SerializedName("data") val data: NotificationDataContent? = null,
    @SerializedName("read_at") val readAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class NotificationDataContent(
    @SerializedName("data") val data: NotificationInnerData? = null
)

data class NotificationInnerData(
    @SerializedName("route_data") val routeData: Any? = null,
    @SerializedName("notification") val notification: String? = null,
    @SerializedName("notification_title") val notificationTitle: String? = null
)

