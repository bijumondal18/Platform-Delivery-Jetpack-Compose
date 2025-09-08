package com.platform.platformdelivery.data.models

import com.google.gson.annotations.SerializedName

data class Waypoint(
    @SerializedName("id") val id: Any?,
    @SerializedName("route_id") val routeId: Any?,
    @SerializedName("place") val place: Any?,
    @SerializedName("destination_lat") val destinationLat: Any?,
    @SerializedName("destination_lng") val destinationLng: Any?,
    @SerializedName("eta") val eta: Any?,
    @SerializedName("type") val type: Any?,
    @SerializedName("optimized_order") val optimizedOrder: Any?,
    @SerializedName("status") val status: Any?,
    @SerializedName("created_at") val createdAt: Any?,
    @SerializedName("updated_at") val updatedAt: Any?,
    @SerializedName("deleted_at") val deletedAt: Any?,
    @SerializedName("end_time") val endTime: Any?,
    @SerializedName("delivered_type") val deliveredType: Any?,
    @SerializedName("note_for_recipients") val noteForRecipients: Any?,
    @SerializedName("note_for_internal_use") val noteForInternalUse: Any?,
    @SerializedName("signature") val signature: Any?,
    @SerializedName("priority") val priority: Any?,
    @SerializedName("package_count") val packageCount: Any?,
    @SerializedName("product") val product: Any?,
    @SerializedName("external_id") val externalId: Any?,
    @SerializedName("seller_name") val sellerName: Any?,
    @SerializedName("seller_website") val sellerWebsite: Any?,
    @SerializedName("seller_order_id") val sellerOrderId: Any?,
    @SerializedName("seller_note") val sellerNote: Any?,
    @SerializedName("recipient_name") val recipientName: Any?,
    @SerializedName("recipient_phone") val recipientPhone: Any?,
    @SerializedName("recipient_email") val recipientEmail: Any?,
    @SerializedName("note_for_drivers") val noteForDrivers: Any?,
    @SerializedName("consent_given") val consentGiven: Any?,
    @SerializedName("consent_required") val consentRequired: Int?

)
