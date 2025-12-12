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
    val originIcon = remember(iconsReady) {
        if (iconsReady) {
            try {
                createNumberedMarkerIcon(context, "O", Color.BLUE)
            } catch (e: Exception) {
                null
            }
        } else null
    }
    val destinationIcon = remember(iconsReady) {
        if (iconsReady) {
            try {
                createNumberedMarkerIcon(context, "D", Color.parseColor("#4CAF50"))
            } catch (e: Exception) {
                null
            }
        } else null
    }
    
    // Cache waypoint icons - create icons for the number of waypoints we have
    val waypointIcons = remember(iconsReady, waypointLocations.size) {
        if (iconsReady) {
            try {
                (0 until waypointLocations.size).map { index ->
                    createNumberedMarkerIcon(context, "${index + 1}", Color.parseColor("#FF9800"))
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
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

                // Waypoint markers (numbered)
                waypointLocations.forEachIndexed { displayIndex, waypointLocation ->
                    if (displayIndex < waypointIcons.size) {
                        Marker(
                            state = MarkerState(position = waypointLocation.location),
                            title = "Stop ${displayIndex + 1}",
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
 */
fun createNumberedMarkerIcon(context: android.content.Context, text: String, color: Int): BitmapDescriptor {
    val size = 80 // Reduced size of the marker icon
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Draw circle background
    val paint = Paint().apply {
        isAntiAlias = true
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6f, paint)
    
    // Draw white border
    val borderPaint = Paint().apply {
        isAntiAlias = true
        this.color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 6f, borderPaint)
    
    // Draw text
    val textPaint = Paint().apply {
        isAntiAlias = true
        this.color = Color.WHITE
        textSize = if (text.length == 1) 32f else 26f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    
    val textY = size / 2f + (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(text, size / 2f, textY, textPaint)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}