package com.platform.platformdelivery.presentation.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.platform.platformdelivery.R
import com.platform.platformdelivery.data.models.Waypoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.net.URL
import org.json.JSONObject


@Composable
fun RouteMapBox(
    latitude: Double,
    longitude: Double,
    routeId: String?,
    originLat: String?,
    originLng: String?,
    destinationLat: Double?,
    destinationLng: Double?,
    waypoints: List<Waypoint>?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun parse(value: Any?): Double? =
        when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }

    val origin = remember(originLat, originLng) {
        parse(originLat)?.let { lat ->
            parse(originLng)?.let { lng ->
                LatLng(lat, lng)
            }
        }
    }

    val destination = remember(destinationLat, destinationLng) {
        if (destinationLat != null && destinationLng != null)
            LatLng(destinationLat, destinationLng)
        else null
    }

    data class WaypointLocation(val location: LatLng, val place: String?)

    val waypointLocations = remember(waypoints) {
        waypoints?.mapNotNull {
            val lat = parse(it.destinationLat)
            val lng = parse(it.destinationLng)
            if (lat != null && lng != null)
                WaypointLocation(LatLng(lat, lng), it.place?.toString())
            else null
        } ?: emptyList()
    }

    val allLocations = remember(origin, waypointLocations, destination) {
        buildList {
            origin?.let { add(it) }
            waypointLocations.forEach { add(it.location) }
            destination?.let { add(it) }
            if (isEmpty()) add(LatLng(latitude, longitude))
        }
    }

    val cameraState = rememberCameraPositionState()
    var cameraInitialized by remember(routeId) { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val mapStyle = remember(isDark) {
        if (isDark) {
            runCatching {
                MapStyleOptions(
                    context.resources.openRawResource(R.raw.map_style_dark)
                        .bufferedReader().use { it.readText() }
                )
            }.getOrNull()
        } else null
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = false,
            mapToolbarEnabled = false
        )
    }

    val properties = remember(mapStyle) {
        MapProperties(
            mapStyleOptions = mapStyle,
            isMyLocationEnabled = false
        )
    }

    var routePolyline by remember(routeId) { mutableStateOf<List<LatLng>>(emptyList()) }
    val polylineColor = MaterialTheme.colorScheme.primary

    var mapRef by remember { mutableStateOf<GoogleMap?>(null) }
    var routePolylineRef by remember { mutableStateOf<com.google.android.gms.maps.model.Polyline?>(null) }



    /** Load Directions */
//    LaunchedEffect(origin, destination, waypointLocations) {
//        if (origin != null && destination != null) {
//            routePolyline = getDrivingRouteWithWaypoints(
//                origin,
//                waypointLocations.map { it.location },
//                destination,
//                context
//            )
//        }
//    }

    LaunchedEffect(routePolyline) {
        val map = mapRef ?: return@LaunchedEffect
        if (routePolyline.size < 2) return@LaunchedEffect

        if (routePolylineRef == null) {
            routePolylineRef = map.addPolyline(
                PolylineOptions()
                    .addAll(routePolyline)
                    .color(polylineColor.toArgb())
                    .width(12f)
                    .jointType(JointType.ROUND)
                    .startCap(RoundCap())
                    .endCap(RoundCap())
            )
        } else {
            routePolylineRef?.points = routePolyline
        }
    }

    var originIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var destinationIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var waypointIcons by remember { mutableStateOf<List<BitmapDescriptor>>(emptyList()) }


    Box(modifier = modifier.fillMaxWidth()) {

        GoogleMap(
            modifier = Modifier
                .matchParentSize()
                .nestedScroll(rememberNestedScrollInteropConnection()),
            cameraPositionState = cameraState,
            uiSettings = uiSettings,
            properties = properties,
            onMapLoaded = {
                if (!cameraInitialized) {

                    // SAFE: Maps SDK is ready
                    originIcon = createNumberedMarkerIcon(context, "O", Color.parseColor("#2196F3"))
                    destinationIcon = createNumberedMarkerIcon(context, "D", Color.parseColor("#2196F3"))

                    waypointIcons = waypointLocations.mapIndexed { index, _ ->
                        createNumberedMarkerIcon(
                            context,
                            String.format("%02d", index + 1),
                            Color.parseColor("#2196F3")
                        )
                    }

                    coroutineScope.launch {
                        val bounds = LatLngBounds.builder().apply {
                            allLocations.forEach { include(it) }
                        }.build()

                        cameraState.animate(
                            CameraUpdateFactory.newLatLngBounds(bounds, 100),
                            700
                        )
                        cameraInitialized = true
                    }
                }
            }
        ) {

            // ✅ MapEffect MUST be here
            MapEffect(
                routePolyline,
                origin,
                waypointLocations,
                destination,
                originIcon,
                destinationIcon,
                waypointIcons
            ) { map ->
                mapRef = map

//                map.clear()

                // Polyline
                if (routePolyline.size >= 2) {
                    map.addPolyline(
                        PolylineOptions()
                            .addAll(routePolyline)
                            .color(polylineColor.toArgb())
                            .width(12f)
                            .jointType(JointType.ROUND)
                            .startCap(RoundCap())
                            .endCap(RoundCap())
                    )
                }

                // Origin
                origin?.let {
                    originIcon?.let { icon ->
                        map.addMarker(
                            MarkerOptions()
                                .position(it)
                                .icon(icon)
                                .anchor(0.5f, 1f)
                                .title("Origin")
                        )
                    }
                }

                // Waypoints
                waypointLocations.forEachIndexed { index, wp ->
                    if (index < waypointIcons.size) {
                        map.addMarker(
                            MarkerOptions()
                                .position(wp.location)
                                .icon(waypointIcons[index])
                                .anchor(0.5f, 1f)
                                .title("Stop ${index + 1}")
                                .snippet(wp.place ?: "")
                        )
                    }
                }

                // Destination
                destination?.let {
                    destinationIcon?.let { icon ->
                        map.addMarker(
                            MarkerOptions()
                                .position(it)
                                .icon(icon)
                                .anchor(0.5f, 1f)
                                .title("Destination")
                        )
                    }
                }
            }
        }


    }
}


/**
 * Creates a custom marker icon with a number or letter
 * Modern pin-style marker with rounded badge
 */
fun createNumberedMarkerIcon(context: android.content.Context, text: String, color: Int): BitmapDescriptor {
    val width = 80
    val height = 100 // Pin shape: taller than wide
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val centerX = width / 2f
    val badgeRadius = 28f
    val badgeY = badgeRadius + 8f // Position badge near top
    
    // Draw pin shadow (subtle)
    val shadowPaint = Paint().apply {
        isAntiAlias = true
        this.color = Color.parseColor("#40000000")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, badgeY + 2f, badgeRadius + 2f, shadowPaint)
    
    // Draw pin point (bottom triangle)
    val pinPath = android.graphics.Path().apply {
        moveTo(centerX, height.toFloat() - 4f)
        lineTo(centerX - 12f, badgeY + badgeRadius + 8f)
        lineTo(centerX + 12f, badgeY + badgeRadius + 8f)
        close()
    }
    val pinPaint = Paint().apply {
        isAntiAlias = true
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawPath(pinPath, pinPaint)
    
    // Draw badge circle background
    val badgePaint = Paint().apply {
        isAntiAlias = true
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, badgeY, badgeRadius, badgePaint)
    
    // Draw white border around badge
    val borderPaint = Paint().apply {
        isAntiAlias = true
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    canvas.drawCircle(centerX, badgeY, badgeRadius, borderPaint)
    
    // Draw text (white, bold, centered)
    val textPaint = Paint().apply {
        isAntiAlias = true
        this.color = Color.WHITE
        // Adjust text size based on length
        textSize = when {
            text.length <= 1 -> 32f
            text.length == 2 -> 24f
            else -> 20f
        }
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    
    val textY = badgeY + (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(text, centerX, textY, textPaint)
    
    // Set anchor point to bottom center of pin
    return BitmapDescriptorFactory.fromBitmap(bitmap).apply {
        // Anchor at bottom center of pin
    }
}

/**
 * Fetches driving route from Google Directions API with waypoints
 * Route order: origin -> waypoint 1 -> waypoint 2 -> ... -> destination
 * Returns a single continuous polyline following actual roads
 */
suspend fun getDrivingRouteWithWaypoints(
    origin: LatLng,
    waypoints: List<LatLng>,
    destination: LatLng,
    context: android.content.Context
): List<LatLng> = withContext(Dispatchers.IO) {
    try {
        // Get API key from resources
        val apiKey = context.getString(R.string.google_maps_key)
        if (apiKey.isEmpty()) {
            android.util.Log.e("RouteMapBox", "Google Maps API key is empty")
            return@withContext emptyList()
        }
        
        // Build waypoints parameter - Google Directions API will route through them in order
        val waypointsParam = if (waypoints.isNotEmpty()) {
            "&waypoints=" + waypoints.joinToString("|") { "${it.latitude},${it.longitude}" }
        } else {
            ""
        }
        
        // Request driving directions with waypoints
        // URL encode the parameters properly
        val originStr = "${origin.latitude},${origin.longitude}"
        val destStr = "${destination.latitude},${destination.longitude}"
        val encodedOrigin = java.net.URLEncoder.encode(originStr, "UTF-8")
        val encodedDest = java.net.URLEncoder.encode(destStr, "UTF-8")
        
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=$encodedOrigin" +
                "&destination=$encodedDest" +
                waypointsParam +
                "&mode=driving" +
                "&key=$apiKey"
        
        android.util.Log.d("RouteMapBox", "Fetching directions. Origin: $originStr, Dest: $destStr, Waypoints: ${waypoints.size}")
        
        val response = URL(url).readText()
        val json = JSONObject(response)
        
        val status = json.getString("status")
        android.util.Log.d("RouteMapBox", "Directions API status: $status")
        
        if (status == "OK") {
            val routes = json.getJSONArray("routes")
            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                
                // Use overview_polyline - it's simpler and more reliable
                // It contains the complete route through all waypoints
                if (route.has("overview_polyline")) {
                    val overviewPolyline = route.getJSONObject("overview_polyline")
                    val encodedPolyline = overviewPolyline.getString("points")
                    val decodedPoints = decodePolyline(encodedPolyline)
                    android.util.Log.d("RouteMapBox", "Decoded ${decodedPoints.size} points from overview_polyline")
                    return@withContext decodedPoints
                } else {
                    android.util.Log.w("RouteMapBox", "No overview_polyline in route response")
                    // Fallback: extract from legs
                    val legs = route.getJSONArray("legs")
                    val allPolylinePoints = mutableListOf<LatLng>()
                    
                    for (i in 0 until legs.length()) {
                        val leg = legs.getJSONObject(i)
                        val steps = leg.getJSONArray("steps")
                        
                        for (j in 0 until steps.length()) {
                            val step = steps.getJSONObject(j)
                            val polyline = step.getJSONObject("polyline")
                            val encodedPolyline = polyline.getString("points")
                            val decodedPoints = decodePolyline(encodedPolyline)
                            
                            if (allPolylinePoints.isEmpty()) {
                                allPolylinePoints.addAll(decodedPoints)
                            } else {
                                val startIndex = if (decodedPoints.isNotEmpty() && 
                                    decodedPoints.first() == allPolylinePoints.last()) {
                                    1
                                } else {
                                    0
                                }
                                if (startIndex < decodedPoints.size) {
                                    allPolylinePoints.addAll(decodedPoints.subList(startIndex, decodedPoints.size))
                                }
                            }
                        }
                    }
                    
                    android.util.Log.d("RouteMapBox", "Extracted ${allPolylinePoints.size} points from legs")
                    return@withContext allPolylinePoints
                }
            } else {
                android.util.Log.w("RouteMapBox", "No routes in response")
                return@withContext emptyList()
            }
        } else {
            // Log error status for debugging
            val errorMessage = if (json.has("error_message")) {
                json.getString("error_message")
            } else {
                "Unknown error"
            }
            android.util.Log.e("RouteMapBox", "Directions API error: $status - $errorMessage")
            return@withContext emptyList()
        }
    } catch (e: Exception) {
        // Log exception for debugging
        android.util.Log.e("RouteMapBox", "Exception fetching driving route: ${e.message}", e)
        return@withContext emptyList()
    }
}

/**
 * Fetches actual road route from Google Directions API (legacy method for single segment)
 * Returns list of LatLng points following actual roads
 */
suspend fun getDirectionsRoute(
    origin: LatLng,
    destination: LatLng,
    context: android.content.Context
): List<LatLng> = withContext(Dispatchers.IO) {
    try {
        // Get API key from resources
        val apiKey = context.getString(R.string.google_maps_key)
            .takeIf { it.isNotEmpty() } 
            ?: return@withContext listOf(origin, destination) // Fallback if no API key
        
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=$apiKey"
        
        val response = URL(url).readText()
        val json = JSONObject(response)
        
        if (json.getString("status") == "OK") {
            val routes = json.getJSONArray("routes")
            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val overviewPolyline = route.getJSONObject("overview_polyline")
                val encodedPolyline = overviewPolyline.getString("points")
                
                // Decode the polyline string to list of LatLng
                decodePolyline(encodedPolyline)
            } else {
                listOf(origin, destination)
            }
        } else {
            listOf(origin, destination)
        }
    } catch (e: Exception) {
        // Fallback to straight line on error
        listOf(origin, destination)
    }
}

/**
 * Decodes Google Maps encoded polyline string to list of LatLng points
 */
fun decodePolyline(encoded: String): List<LatLng> {
    val poly = mutableListOf<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat
        
        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng
        
        poly.add(LatLng(lat / 1e5, lng / 1e5))
    }
    
    return poly
}