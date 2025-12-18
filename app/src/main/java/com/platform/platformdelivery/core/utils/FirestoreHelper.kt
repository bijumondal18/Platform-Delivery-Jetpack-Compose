package com.platform.platformdelivery.core.utils

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.platform.platformdelivery.data.models.Route
import com.platform.platformdelivery.data.models.RouteDetailsResponse
import com.platform.platformdelivery.data.models.RouteDetailsData
import com.platform.platformdelivery.data.models.User
import com.platform.platformdelivery.data.models.Waypoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    /**
     * Checks if a route document exists in Firestore
     */
    suspend fun routeDocumentExists(routeId: String): Boolean {
        return try {
            val document = db.collection(COLLECTION_NAME)
                .document(routeId)
                .get()
                .await()
            document.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Exception checking if route document exists", e)
            false
        }
    }

    /**
     * Creates a Firestore document from a Route object (used when clicking on route item)
     * This creates a basic document that can be updated later with full details
     */
    suspend fun createRouteDocumentFromRoute(routeId: String, route: Route) {
        try {
            val routeMap = hashMapOf<String, Any?>(
                "route_id" to routeId,
                "id" to route.id,
                "name" to route.name,
                "details" to route.details,
                "start_date" to route.startDate,
                "start_time" to route.startTime,
                "origin_place" to route.originPlace,
                "origin_lat" to route.originLat,
                "origin_lng" to route.originLng,
                "destination_place" to route.destinationPlace,
                "destination_lat" to route.destinationLat,
                "destination_lng" to route.destinationLng,
                "center_lat" to route.centerLat,
                "center_lng" to route.centerLng,
                "created_by" to route.createdBy,
                "driver_price" to route.driverPrice,
                "distance" to route.distance,
                "estimated_total_time" to route.estimatedTotalTime,
                "estimated_end_time" to route.estimatedEndTime,
                "price" to route.price,
                "accepted_by" to route.acceptedBy,
                "status" to route.status,
                "created_at" to route.createdAt,
                "updated_at" to route.updatedAt,
                "trip_end_time" to route.tripEndTime,
                "trip_start_time" to route.tripStartTime,
                "trip_total_time" to route.tripTotalTime,
                "final_price" to route.finalPrice,
                "final_driver_price" to route.finalDriverPrice,
                "trip_total_distance" to route.tripTotalDistance,
                "circuit_trip" to route.circuitTrip,
                "current_waypoint" to route.currentWaypoint,
                "isloaded" to route.isloaded,
                "chat_room_id" to route.chatRoomId,
                "created_at_firestore" to System.currentTimeMillis()
            )

            // Add waypoints if available
            if (!route.waypoints.isNullOrEmpty()) {
                val waypointsList = route.waypoints.map { waypoint ->
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
            route.client?.let { client ->
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
                    Log.d(TAG, "Route $routeId successfully created in Firestore from Route object")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error creating route $routeId in Firestore", e)
                }
                .await()

        } catch (e: Exception) {
            Log.e(TAG, "Exception creating route document from Route object", e)
        }
    }

    /**
     * Converts a Firestore DocumentSnapshot to a Route object
     */
    private fun documentToRoute(snapshot: DocumentSnapshot): Route? {
        return try {
            val data = snapshot.data ?: return null
            
            // Parse waypoints
            val waypointsList = (data["waypoints"] as? List<Map<String, Any>>)?.mapNotNull { waypointMap ->
                try {
                    Waypoint(
                        id = waypointMap["id"],
                        routeId = waypointMap["route_id"],
                        place = waypointMap["place"],
                        destinationLat = waypointMap["destination_lat"],
                        destinationLng = waypointMap["destination_lng"],
                        eta = waypointMap["eta"],
                        type = waypointMap["type"],
                        optimizedOrder = waypointMap["optimized_order"],
                        status = waypointMap["status"],
                        createdAt = waypointMap["created_at"],
                        updatedAt = waypointMap["updated_at"],
                        deletedAt = waypointMap["deleted_at"],
                        endTime = waypointMap["end_time"],
                        deliveredType = waypointMap["delivered_type"],
                        noteForRecipients = waypointMap["note_for_recipients"],
                        noteForInternalUse = waypointMap["note_for_internal_use"],
                        signature = waypointMap["signature"],
                        priority = waypointMap["priority"],
                        packageCount = waypointMap["package_count"],
                        product = waypointMap["product"],
                        externalId = waypointMap["external_id"],
                        sellerName = waypointMap["seller_name"],
                        sellerWebsite = waypointMap["seller_website"],
                        sellerOrderId = waypointMap["seller_order_id"],
                        sellerNote = waypointMap["seller_note"],
                        recipientName = waypointMap["recipient_name"],
                        recipientPhone = waypointMap["recipient_phone"],
                        recipientEmail = waypointMap["recipient_email"],
                        noteForDrivers = waypointMap["note_for_drivers"],
                        consentGiven = waypointMap["consent_given"],
                        consentRequired = (waypointMap["consent_required"] as? Number)?.toInt()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing waypoint", e)
                    null
                }
            }

            // Parse client
            val clientMap = data["client"] as? Map<String, Any>
            val client = clientMap?.let {
                try {
                    User(
                        id = (it["id"] as? Number)?.toInt() ?: 0,
                        name = it["name"] as? String,
                        firstName = it["first_name"] as? String,
                        lastName = it["last_name"] as? String,
                        email = it["email"] as? String,
                        status = it["status"] as? String,
                        phone = it["phone"] as? String,
                        driverId = it["driver_id"] as? String,
                        profilePic = it["profile_pic"] as? String,
                        fcmToken = it["fcm_token"] as? String,
                        lat = it["lat"] as? String,
                        lng = it["lng"] as? String,
                        averageRating = (it["average_ratting"] as? Number)?.toInt(),
                        activationCount = (it["activation_count"] as? Number)?.toInt(),
                        agoraId = it["agora_id"] as? String,
                        agoraUserCreated = (it["agora_user_created"] as? Number)?.toInt(),
                        stripeId = it["stripe_id"] as? String,
                        stripeChargesEnabled = (it["stripe_charges_enabled"] as? Number)?.toInt(),
                        instantPayoutEnabled = (it["instant_payout_enabled"] as? Number)?.toInt(),
                        stripeExternalBankAccountId = it["stripe_extarnal_bank_account_id"] as? String,
                        stripePayoutInterval = it["stripe_payout_interval"] as? String,
                        stripePayoutAnchor = it["stripe_payout_anchor"] as? String,
                        barcodeStatus = it["barcode_status"] as? String,
                        isMapActive = it["is_map_active"] as? String,
                        isLocationActive = it["is_location_active"] as? String,
                        isSigImgActive = it["is_sig_img_active"] as? String,
                        isTwilioActive = it["is_twilio_active"] as? String,
                        ssn = it["ssn"] as? String,
                        street = it["street"] as? String,
                        city = it["city"] as? String,
                        state = it["state"] as? String,
                        baseLocation = it["base_location"] as? String,
                        baseLocationLat = it["base_location_lat"] as? String,
                        baseLocationLng = it["base_location_lng"] as? String,
                        zip = it["zip"] as? String,
                        ifsc = it["ifsc"] as? String,
                        accountNo = it["account_no"] as? String,
                        emailVerifiedAt = it["email_verified_at"],
                        createdAt = it["created_at"],
                        updatedAt = it["updated_at"]
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing client", e)
                    null
                }
            }

            Route(
                id = (data["id"] as? Number)?.toInt(),
                name = data["name"] as? String,
                details = data["details"] as? String,
                startDate = data["start_date"] as? String,
                startTime = data["start_time"] as? String,
                originPlace = data["origin_place"] as? String,
                originLat = data["origin_lat"] as? String,
                originLng = data["origin_lng"] as? String,
                destinationPlace = data["destination_place"] as? String,
                destinationLat = (data["destination_lat"] as? Number)?.toDouble(),
                destinationLng = (data["destination_lng"] as? Number)?.toDouble(),
                centerLat = data["center_lat"] as? String,
                centerLng = data["center_lng"] as? String,
                createdBy = (data["created_by"] as? Number)?.toInt(),
                driverPrice = data["driver_price"],
                distance = data["distance"] as? String,
                estimatedTotalTime = data["estimated_total_time"] as? String,
                estimatedEndTime = data["estimated_end_time"] as? String,
                price = data["price"],
                acceptedBy = (data["accepted_by"] as? Number)?.toInt(),
                status = data["status"] as? String,
                createdAt = data["created_at"] as? String,
                updatedAt = data["updated_at"] as? String,
                tripEndTime = data["trip_end_time"],
                tripStartTime = data["trip_start_time"],
                tripTotalTime = data["trip_total_time"],
                finalPrice = data["final_price"],
                finalDriverPrice = data["final_driver_price"],
                tripTotalDistance = data["trip_total_distance"],
                circuitTrip = data["circuit_trip"],
                currentWaypoint = data["current_waypoint"],
                isloaded = (data["isloaded"] as? Number)?.toInt(),
                chatRoomId = data["chat_room_id"],
                waypoints = waypointsList,
                client = client
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception converting document to Route", e)
            null
        }
    }

    /**
     * Streams route details from Firestore in real-time
     * Returns a Flow that emits RouteDetailsResponse whenever the document changes
     */
    fun streamRouteDetails(routeId: String): Flow<RouteDetailsResponse?> = callbackFlow {
        val listenerRegistration = db.collection(COLLECTION_NAME)
            .document(routeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to route $routeId in Firestore", error)
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val routeData = documentToRoute(snapshot)
                        if (routeData != null) {
                            val routeDetailsResponse = RouteDetailsResponse(
                                status = "success",
                                message = null,
                                routeDetailsData = RouteDetailsData(
                                    routeData = routeData,
                                    status = true
                                )
                            )
                            trySend(routeDetailsResponse)
                        } else {
                            Log.w(TAG, "Route document $routeId exists but could not be parsed")
                            trySend(null)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception parsing route document from Firestore", e)
                        trySend(null)
                    }
                } else {
                    Log.d(TAG, "Route document $routeId does not exist in Firestore")
                    trySend(null)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }
}
