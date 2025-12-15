package com.platform.platformdelivery.core.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.platform.platformdelivery.data.models.Route
import com.platform.platformdelivery.data.models.RouteDetailsResponse
import kotlinx.coroutines.tasks.await

object FirestoreHelper {
    private const val TAG = "FirestoreHelper"
    private const val COLLECTION_NAME = "routes"
    private val db = FirebaseFirestore.getInstance()

    /**
     * Creates a Firestore document when a route is accepted
     * @param routeId The route ID to use as document ID
     * @param routeDetails The complete route details to save
     */
    suspend fun saveAcceptedRoute(routeId: String, routeDetails: RouteDetailsResponse?) {
        try {
            val routeData = routeDetails?.routeDetailsData?.routeData
            if (routeData == null) {
                Log.w(TAG, "Route data is null, cannot save to Firestore")
                return
            }

            // Convert route data to a map for Firestore
            val routeMap = hashMapOf<String, Any?>(
                "route_id" to routeId,
                "id" to routeData.id,
                "name" to routeData.name,
                "details" to routeData.details,
                "start_date" to routeData.startDate,
                "start_time" to routeData.startTime,
                "origin_place" to routeData.originPlace,
                "origin_lat" to routeData.originLat,
                "origin_lng" to routeData.originLng,
                "destination_place" to routeData.destinationPlace,
                "destination_lat" to routeData.destinationLat,
                "destination_lng" to routeData.destinationLng,
                "center_lat" to routeData.centerLat,
                "center_lng" to routeData.centerLng,
                "created_by" to routeData.createdBy,
                "driver_price" to routeData.driverPrice,
                "distance" to routeData.distance,
                "estimated_total_time" to routeData.estimatedTotalTime,
                "estimated_end_time" to routeData.estimatedEndTime,
                "price" to routeData.price,
                "accepted_by" to routeData.acceptedBy,
                "status" to routeData.status,
                "created_at" to routeData.createdAt,
                "updated_at" to routeData.updatedAt,
                "trip_end_time" to routeData.tripEndTime,
                "trip_start_time" to routeData.tripStartTime,
                "trip_total_time" to routeData.tripTotalTime,
                "final_price" to routeData.finalPrice,
                "final_driver_price" to routeData.finalDriverPrice,
                "trip_total_distance" to routeData.tripTotalDistance,
                "circuit_trip" to routeData.circuitTrip,
                "current_waypoint" to routeData.currentWaypoint,
                "isloaded" to routeData.isloaded,
                "chat_room_id" to routeData.chatRoomId,
                "accepted_at" to System.currentTimeMillis() // Timestamp when route was accepted
            )

            // Add waypoints if available
            if (!routeData.waypoints.isNullOrEmpty()) {
                val waypointsList = routeData.waypoints.map { waypoint ->
                    hashMapOf<String, Any?>(
                        "id" to waypoint.id,
                        "route_id" to waypoint.routeId,
                        "place" to waypoint.place,
                        "destination_lat" to waypoint.destinationLat,
                        "destination_lng" to waypoint.destinationLng,
                        "eta" to waypoint.eta,
                        "type" to waypoint.type,
                        "optimized_order" to waypoint.optimizedOrder,
                        "status" to waypoint.status,
                        "created_at" to waypoint.createdAt,
                        "updated_at" to waypoint.updatedAt,
                        "deleted_at" to waypoint.deletedAt,
                        "end_time" to waypoint.endTime,
                        "delivered_type" to waypoint.deliveredType,
                        "note_for_recipients" to waypoint.noteForRecipients,
                        "note_for_internal_use" to waypoint.noteForInternalUse,
                        "signature" to waypoint.signature,
                        "priority" to waypoint.priority,
                        "package_count" to waypoint.packageCount,
                        "product" to waypoint.product,
                        "external_id" to waypoint.externalId,
                        "seller_name" to waypoint.sellerName,
                        "seller_website" to waypoint.sellerWebsite,
                        "seller_order_id" to waypoint.sellerOrderId,
                        "seller_note" to waypoint.sellerNote,
                        "recipient_name" to waypoint.recipientName,
                        "recipient_phone" to waypoint.recipientPhone,
                        "recipient_email" to waypoint.recipientEmail,
                        "note_for_drivers" to waypoint.noteForDrivers,
                        "consent_given" to waypoint.consentGiven,
                        "consent_required" to waypoint.consentRequired
                    )
                }
                routeMap["waypoints"] = waypointsList
            }

            // Add client information if available
            routeData.client?.let { client ->
                routeMap["client"] = hashMapOf<String, Any?>(
                    "id" to client.id,
                    "name" to client.name,
                    "first_name" to client.firstName,
                    "last_name" to client.lastName,
                    "email" to client.email,
                    "phone" to client.phone,
                    "status" to client.status,
                    "driver_id" to client.driverId,
                    "profile_pic" to client.profilePic,
                    "fcm_token" to client.fcmToken,
                    "lat" to client.lat,
                    "lng" to client.lng,
                    "average_ratting" to client.averageRating,
                    "activation_count" to client.activationCount,
                    "agora_id" to client.agoraId,
                    "agora_user_created" to client.agoraUserCreated,
                    "stripe_id" to client.stripeId,
                    "stripe_charges_enabled" to client.stripeChargesEnabled,
                    "instant_payout_enabled" to client.instantPayoutEnabled,
                    "stripe_extarnal_bank_account_id" to client.stripeExternalBankAccountId,
                    "stripe_payout_interval" to client.stripePayoutInterval,
                    "stripe_payout_anchor" to client.stripePayoutAnchor,
                    "barcode_status" to client.barcodeStatus,
                    "is_map_active" to client.isMapActive,
                    "is_location_active" to client.isLocationActive,
                    "is_sig_img_active" to client.isSigImgActive,
                    "is_twilio_active" to client.isTwilioActive,
                    "ssn" to client.ssn,
                    "street" to client.street,
                    "city" to client.city,
                    "state" to client.state,
                    "base_location" to client.baseLocation,
                    "base_location_lat" to client.baseLocationLat,
                    "base_location_lng" to client.baseLocationLng,
                    "zip" to client.zip,
                    "ifsc" to client.ifsc,
                    "account_no" to client.accountNo,
                    "email_verified_at" to client.emailVerifiedAt,
                    "created_at" to client.createdAt,
                    "updated_at" to client.updatedAt
                )
            }

            // Save to Firestore with route_id as document ID
            db.collection(COLLECTION_NAME)
                .document(routeId)
                .set(routeMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Route $routeId successfully saved to Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving route $routeId to Firestore", e)
                }
                .await()

        } catch (e: Exception) {
            Log.e(TAG, "Exception saving route to Firestore", e)
        }
    }

    /**
     * Updates an existing route document in Firestore
     */
    suspend fun updateAcceptedRoute(routeId: String, updates: Map<String, Any>) {
        try {
            db.collection(COLLECTION_NAME)
                .document(routeId)
                .update(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Route $routeId successfully updated in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating route $routeId in Firestore", e)
                }
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating route in Firestore", e)
        }
    }

    /**
     * Deletes a route document from Firestore
     */
    suspend fun deleteAcceptedRoute(routeId: String) {
        try {
            db.collection(COLLECTION_NAME)
                .document(routeId)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Route $routeId successfully deleted from Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error deleting route $routeId from Firestore", e)
                }
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Exception deleting route from Firestore", e)
        }
    }
}
