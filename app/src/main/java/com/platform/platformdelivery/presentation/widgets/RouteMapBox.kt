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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.platform.platformdelivery.R
import com.platform.platformdelivery.data.models.Waypoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject

@Composable
fun RouteMapBox(
    latitude: Double,
    longitude: Double,
    routeId: String? = null,
    originLat: String? = null,
    originLng: String? = null,
    destinationLat: Double? = null,
    destinationLng: Double? = null,
    waypoints: List<Waypoint>? = null
) {

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val mapHeight = screenHeight * 0.3f // 30% of device height

    val context = LocalContext.current

    // Helper function to safely parse coordinates
    fun parseCoordinate(value: Any?): Double? {
        return when (value) {
            is Number -> value.toDouble()
            is String -> try { value.toDouble() } catch (e: Exception) { null }
            else -> null
        }
    }

    // Parse origin coordinates
    val originLocation = remember(originLat, originLng) {
        if (!originLat.isNullOrEmpty() && !originLng.isNullOrEmpty()) {
            val lat = parseCoordinate(originLat)
            val lng = parseCoordinate(originLng)
            if (lat != null && lng != null) {
                LatLng(lat, lng)
            } else null
        } else null
    }

    // Parse waypoint coordinates
    data class WaypointLocation(val location: LatLng, val index: Int, val place: String?)
    
    val waypointLocations = remember(waypoints) {
        waypoints?.mapIndexedNotNull { index, waypoint ->
            val lat = parseCoordinate(waypoint.destinationLat)
            val lng = parseCoordinate(waypoint.destinationLng)
            if (lat != null && lng != null) {
                WaypointLocation(
                    location = LatLng(lat, lng),
                    index = index,
                    place = waypoint.place?.toString()
                )
            } else null
        }?.sortedBy { waypoint ->
            (waypoints?.get(waypoint.index)?.optimizedOrder as? Number)?.toInt() ?: waypoint.index
        } ?: emptyList()
    }

    // Parse destination coordinates
    val destinationLocation = remember(destinationLat, destinationLng) {
        if (destinationLat != null && destinationLng != null) {
            LatLng(destinationLat, destinationLng)
        } else null
    }

    // Collect all locations for bounds calculation
    val allLocations = remember(originLocation, waypointLocations, destinationLocation) {
        mutableListOf<LatLng>().apply {
            originLocation?.let { add(it) }
            waypointLocations.forEach { add(it.location) }
            destinationLocation?.let { add(it) }
            
            // Fallback to provided latitude/longitude if no other locations
            if (isEmpty()) {
                add(LatLng(latitude, longitude))
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState()

    // Detect dark theme
    val isDarkTheme = isSystemInDarkTheme()

    // Load dark map style JSON from raw resources
    val mapStyleOptions = remember(isDarkTheme) {
        if (isDarkTheme) {
            try {
                MapStyleOptions(
                    context.resources.openRawResource(R.raw.map_style_dark)
                        .bufferedReader().use { it.readText() }
                )
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // Track if icons are created (lazy initialization)
    var iconsReady by remember { mutableStateOf(false) }
    
    // Cache marker icons - create lazily after map is ready
    // Origin and destination should show home icon or special marker
    val originIcon = remember(iconsReady) {
        if (iconsReady) {
            try {
                // Use blue circle with home icon or "O" for origin
                createNumberedMarkerIcon(context, "O", Color.parseColor("#2196F3")) // Blue color
            } catch (e: Exception) {
                null
            }
        } else null
    }
    val destinationIcon = remember(iconsReady) {
        if (iconsReady) {
            try {
                // Use blue circle with "D" or home icon for destination
                createNumberedMarkerIcon(context, "D", Color.parseColor("#2196F3")) // Blue color
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    // Cache waypoint icons - create icons for the number of waypoints we have
    // Markers should show "01", "02", etc. (with leading zeros)
    val waypointIcons = remember(iconsReady, waypointLocations.size) {
        if (iconsReady) {
            try {
                (0 until waypointLocations.size).map { index ->
                    createNumberedMarkerIcon(context, String.format("%02d", index + 1), Color.parseColor("#2196F3")) // Blue color like in image
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    // Build route points for polyline: only origin to first waypoint
    val routePoints = remember(originLocation, waypointLocations) {
        mutableListOf<LatLng>().apply {
            originLocation?.let { add(it) }
            // Only add first waypoint if it exists
            waypointLocations.firstOrNull()?.let { add(it.location) }
        }
    }
    
    // State for storing decoded polyline points from Directions API
    var actualRoutePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    
    // Fetch actual road route using Directions API
    LaunchedEffect(originLocation, waypointLocations.firstOrNull()) {
        actualRoutePoints = emptyList()
        
        val origin = originLocation
        val firstWaypoint = waypointLocations.firstOrNull()
        
        if (origin != null && firstWaypoint != null) {
            try {
                // Use Google Directions API to get actual road route
                val routePath = getDirectionsRoute(origin, firstWaypoint.location, context)
                actualRoutePoints = routePath
            } catch (e: Exception) {
                // Fallback to straight line if Directions API fails
                actualRoutePoints = listOf(origin, firstWaypoint.location)
            }
        }
    }
    
    // Create icons after a short delay to ensure Maps is initialized
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100) // Small delay to ensure Maps SDK is initialized
        iconsReady = true
    }

    // Track if camera has been initialized to prevent repeated updates
    var isCameraInitialized by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(mapHeight)
            .clip(MaterialTheme.shapes.large)
    ) {
        // GoogleMap Composable inside Box
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true,
                tiltGesturesEnabled = false,
                rotationGesturesEnabled = true,
                mapToolbarEnabled = false
            ),
            properties = MapProperties(
                mapType = MapType.NORMAL,
                mapStyleOptions = mapStyleOptions,
                isMyLocationEnabled = false,
                isTrafficEnabled = false
            )
        ) {
            // Draw blue polyline from origin to first waypoint on actual roads
            if (actualRoutePoints.isNotEmpty() && actualRoutePoints.size >= 2) {
                Polyline(
                    points = actualRoutePoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 8f
                )
            } else if (routePoints.size >= 2) {
                // Fallback to straight line if Directions API route not available
                Polyline(
                    points = routePoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 8f
                )
            }
            
            // Only show markers if icons are ready
            if (iconsReady) {
                // Origin marker
                originLocation?.let { location ->
                    originIcon?.let { icon ->
                        Marker(
                            state = MarkerState(position = location),
                            title = "Origin",
                            icon = icon
                        )
                    }
                }

                // Waypoint markers (numbered as 01, 02, 03, etc.)
                waypointLocations.forEachIndexed { displayIndex, waypointLocation ->
                    if (displayIndex < waypointIcons.size) {
                        Marker(
                            state = MarkerState(position = waypointLocation.location),
                            title = "Stop ${String.format("%02d", displayIndex + 1)}",
                            snippet = waypointLocation.place ?: "",
                            icon = waypointIcons[displayIndex]
                        )
                    }
                }

                // Destination marker
                destinationLocation?.let { location ->
                    destinationIcon?.let { icon ->
                        Marker(
                            state = MarkerState(position = location),
                            title = "Destination",
                            icon = icon
                        )
                    }
                }
            }

            // Fallback marker if no other locations (always show, doesn't need custom icon)
            if (originLocation == null && waypointLocations.isEmpty() && destinationLocation == null) {
                Marker(
                    state = MarkerState(position = LatLng(latitude, longitude)),
                    title = "Route Location",
                    snippet = "Lat: $latitude, Lng: $longitude"
                )
            }
        }
    }

    // Adjust camera to fit all markers (only once on initial load)
    LaunchedEffect(routeId) {
        if (!isCameraInitialized && allLocations.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            allLocations.forEach { location ->
                boundsBuilder.include(location)
            }
            val bounds = boundsBuilder.build()
            val padding = 100 // padding in pixels
            
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, padding),
                durationMs = 700
            )
            isCameraInitialized = true
        } else if (!isCameraInitialized) {
            // Fallback to single location
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 14f),
                durationMs = 700
            )
            isCameraInitialized = true
        }
    }
}

/**
 * Creates a custom marker icon with a number or letter
 * Matches the numbered badge style from the image (01, 02, etc.)
 */
fun createNumberedMarkerIcon(context: android.content.Context, text: String, color: Int): BitmapDescriptor {
    val size = 100 // Size of the marker icon
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Draw circle background with blue color
    val paint = Paint().apply {
        isAntiAlias = true
        this.color = color
        style = Paint.Style.FILL
    }
    val radius = size / 2f - 8f
    canvas.drawCircle(size / 2f, size / 2f, radius, paint)
    
    // Draw white border (thicker for better visibility)
    val borderPaint = Paint().apply {
        isAntiAlias = true
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    canvas.drawCircle(size / 2f, size / 2f, radius, borderPaint)
    
    // Draw text (white, bold, centered)
    val textPaint = Paint().apply {
        isAntiAlias = true
        this.color = Color.WHITE
        // Adjust text size based on length (for "01", "02" vs "O", "D")
        textSize = when {
            text.length <= 1 -> 36f
            text.length == 2 -> 28f
            else -> 24f
        }
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    
    val textY = size / 2f + (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(text, size / 2f, textY, textPaint)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Fetches actual road route from Google Directions API
 * Returns list of LatLng points following actual roads
 */
suspend fun getDirectionsRoute(
    origin: LatLng,
    destination: LatLng,
    context: android.content.Context
): List<LatLng> = withContext(Dispatchers.IO) {
    try {
        // Get API key from resources (you'll need to add this to your strings.xml or buildConfig)
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
                val legs = route.getJSONArray("legs")
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