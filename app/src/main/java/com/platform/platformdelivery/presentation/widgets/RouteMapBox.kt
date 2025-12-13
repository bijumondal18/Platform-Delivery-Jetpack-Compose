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
import kotlinx.coroutines.launch
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

    // Collect all locations for bounds calculation - use immutable list
    val allLocations = remember(originLocation, waypointLocations, destinationLocation) {
        buildList {
            originLocation?.let { add(it) }
            waypointLocations.forEach { add(it.location) }
            destinationLocation?.let { add(it) }
            
            // Fallback to provided latitude/longitude if no other locations
            if (isEmpty()) {
                add(LatLng(latitude, longitude))
            }
        }
    }

    // Use stable camera position state - no parameters needed
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

    // Track if icons are created (lazy initialization) - only set once
    var iconsReady by remember { mutableStateOf(false) }
    
    // Cache marker icons - create once and reuse, stable references
    val originIcon = remember(iconsReady, routeId) {
        if (iconsReady) {
            try {
                createNumberedMarkerIcon(context, "O", Color.parseColor("#2196F3"))
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    val destinationIcon = remember(iconsReady, routeId) {
        if (iconsReady) {
            try {
                createNumberedMarkerIcon(context, "D", Color.parseColor("#2196F3"))
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    // Cache waypoint icons - create once per route, stable list
    val waypointIcons = remember(iconsReady, waypointLocations.size, routeId) {
        if (iconsReady && waypointLocations.isNotEmpty()) {
            try {
                (0 until waypointLocations.size).map { index ->
                    createNumberedMarkerIcon(context, String.format("%02d", index + 1), Color.parseColor("#2196F3"))
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    // Build route points for polyline: only origin to first waypoint - use immutable list
    val routePoints = remember(originLocation, waypointLocations) {
        buildList {
            originLocation?.let { add(it) }
            waypointLocations.firstOrNull()?.let { add(it.location) }
        }
    }
    
    // State for storing decoded polyline points from Directions API - stable reference
    var actualRoutePoints by remember(routeId) { mutableStateOf<List<LatLng>>(emptyList()) }
    
    // Get polyline color from theme - read outside GoogleMap lambda
    val polylineColor = MaterialTheme.colorScheme.primary
    
    // Fetch actual road route using Directions API - only when origin/first waypoint changes
    LaunchedEffect(originLocation, waypointLocations.firstOrNull()?.location) {
        val origin = originLocation
        val firstWaypoint = waypointLocations.firstOrNull()
        
        if (origin != null && firstWaypoint != null) {
            try {
                val routePath = getDirectionsRoute(origin, firstWaypoint.location, context)
                actualRoutePoints = routePath
            } catch (e: Exception) {
                // Fallback to straight line if Directions API fails
                actualRoutePoints = listOf(origin, firstWaypoint.location)
            }
        } else {
            actualRoutePoints = emptyList()
        }
    }
    
    // Create icons after a short delay to ensure Maps is initialized
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100) // Small delay to ensure Maps SDK is initialized
        iconsReady = true
    }

    // Track if camera has been initialized to prevent repeated updates
    var isCameraInitialized by remember(routeId) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(mapHeight)
            .clip(MaterialTheme.shapes.large)
    ) {
        // GoogleMap Composable inside Box
        // Use remember to prevent recomposition during scroll - stable references
        val mapUiSettings = remember {
            MapUiSettings(
                zoomControlsEnabled = true,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true,
                tiltGesturesEnabled = false,
                rotationGesturesEnabled = true,
                mapToolbarEnabled = false,
                compassEnabled = false,
                myLocationButtonEnabled = false
            )
        }
        
        val mapProperties = remember(mapStyleOptions) {
            MapProperties(
                mapType = MapType.NORMAL,
                mapStyleOptions = mapStyleOptions,
                isMyLocationEnabled = false,
                isTrafficEnabled = false,
                isIndoorEnabled = false,
                minZoomPreference = 5f,
                maxZoomPreference = 20f
            )
        }
        
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
            properties = mapProperties,
            onMapLoaded = {
                // Only initialize camera once when map is loaded
                if (!isCameraInitialized && allLocations.isNotEmpty()) {
                    coroutineScope.launch {
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
                    }
                }
            }
        ) {
            // Draw blue polyline from origin to first waypoint on actual roads
            // Use derivedStateOf to prevent recomposition when actualRoutePoints changes during scroll
            val shouldShowPolyline = remember(actualRoutePoints.size, routePoints.size) {
                actualRoutePoints.isNotEmpty() && actualRoutePoints.size >= 2 || routePoints.size >= 2
            }
            
            val polylinePoints = remember(actualRoutePoints, routePoints) {
                if (actualRoutePoints.isNotEmpty() && actualRoutePoints.size >= 2) {
                    actualRoutePoints
                } else if (routePoints.size >= 2) {
                    routePoints
                } else {
                    emptyList()
                }
            }
            
            // Only render polyline if we have points - stable key prevents recomposition
            if (shouldShowPolyline && polylinePoints.isNotEmpty()) {
                key("polyline_${polylinePoints.size}") {
                    Polyline(
                        points = polylinePoints,
                        color = polylineColor,
                        width = 10f,
                        jointType = JointType.ROUND,
                        startCap = RoundCap(),
                        endCap = RoundCap()
                    )
                }
            }
            
            // Only show markers if icons are ready - use stable keys to prevent recomposition
            if (iconsReady) {
                // Origin marker - stable key
                originLocation?.let { location ->
                    originIcon?.let { icon ->
                        key("origin_${location.latitude}_${location.longitude}") {
                            Marker(
                                state = MarkerState(position = location),
                                title = "Origin",
                                icon = icon,
                                anchor = Offset(0.5f, 1.0f)
                            )
                        }
                    }
                }

                // Waypoint markers - stable keys prevent recomposition
                waypointLocations.forEachIndexed { displayIndex, waypointLocation ->
                    if (displayIndex < waypointIcons.size) {
                        key("waypoint_${displayIndex}_${waypointLocation.location.latitude}_${waypointLocation.location.longitude}") {
                            Marker(
                                state = MarkerState(position = waypointLocation.location),
                                title = "Stop ${String.format("%02d", displayIndex + 1)}",
                                snippet = waypointLocation.place ?: "",
                                icon = waypointIcons[displayIndex],
                                anchor = Offset(0.5f, 1.0f)
                            )
                        }
                    }
                }

                // Destination marker - stable key
                destinationLocation?.let { location ->
                    destinationIcon?.let { icon ->
                        key("destination_${location.latitude}_${location.longitude}") {
                            Marker(
                                state = MarkerState(position = location),
                                title = "Destination",
                                icon = icon,
                                anchor = Offset(0.5f, 1.0f)
                            )
                        }
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

    // Initialize camera only once when routeId changes (not on scroll)
    LaunchedEffect(routeId) {
        if (!isCameraInitialized && allLocations.isNotEmpty()) {
            // Wait for map to be ready before setting camera
            kotlinx.coroutines.delay(300)
            if (!isCameraInitialized) {
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
            }
        } else if (!isCameraInitialized) {
            // Fallback to single location
            kotlinx.coroutines.delay(300)
            if (!isCameraInitialized) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 14f),
                    durationMs = 700
                )
                isCameraInitialized = true
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