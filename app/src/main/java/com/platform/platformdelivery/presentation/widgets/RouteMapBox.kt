package com.platform.platformdelivery.presentation.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.platform.platformdelivery.R
import com.platform.platformdelivery.data.models.Waypoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

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

    fun parse(value: Any?): Double? = when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        else -> null
    }

    val origin = remember(originLat, originLng) {
        val lat = parse(originLat)
        val lng = parse(originLng)
        if (lat != null && lng != null) LatLng(lat, lng) else null
    }

    val destination = remember(destinationLat, destinationLng) {
        if (destinationLat != null && destinationLng != null) LatLng(destinationLat, destinationLng) else null
    }

    data class WaypointLocation(val location: LatLng, val place: String?)

    val waypointLocations = remember(waypoints) {
        waypoints?.mapNotNull { wp ->
            val lat = parse(wp.destinationLat)
            val lng = parse(wp.destinationLng)
            if (lat != null && lng != null) WaypointLocation(LatLng(lat, lng), wp.place?.toString()) else null
        } ?: emptyList()
    }

    // For initial camera fit (markers)
    val allLocations = remember(origin, waypointLocations, destination, latitude, longitude) {
        buildList {
            origin?.let { add(it) }
            waypointLocations.forEach { add(it.location) }
            destination?.let { add(it) }
            if (isEmpty()) add(LatLng(latitude, longitude))
        }
    }

    val cameraState = rememberCameraPositionState()

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

    // Route polyline state
    var routePolyline by remember(routeId) { mutableStateOf<List<LatLng>>(emptyList()) }
    val polylineColor = MaterialTheme.colorScheme.primary

    // Marker icons
    var originIcon by remember(routeId) { mutableStateOf<BitmapDescriptor?>(null) }
    var destinationIcon by remember(routeId) { mutableStateOf<BitmapDescriptor?>(null) }
    var waypointIcons by remember(routeId) { mutableStateOf<List<BitmapDescriptor>>(emptyList()) }

    // Create icons once per route + waypoint count
    LaunchedEffect(routeId, waypointLocations.size) {
        originIcon = createNumberedMarkerIcon("O", AndroidColor.parseColor("#2196F3"))
        destinationIcon = createNumberedMarkerIcon("D", AndroidColor.parseColor("#2196F3"))
        waypointIcons = List(waypointLocations.size) { index ->
            createNumberedMarkerIcon(String.format("%02d", index + 1), AndroidColor.parseColor("#2196F3"))
        }
    }

    /** Load Directions */
    LaunchedEffect(origin, destination, waypointLocations) {
        if (origin != null && destination != null) {
            routePolyline = getDrivingRouteWithWaypoints(
                origin = origin,
                waypoints = waypointLocations.map { it.location },
                destination = destination,
                apiKey = context.getString(R.string.google_maps_key)
            )

            android.util.Log.d("RouteMapBox", "routePolyline.size=${routePolyline.size}")
            if (routePolyline.isNotEmpty()) {
                android.util.Log.d("RouteMapBox", "first=${routePolyline.first()} last=${routePolyline.last()}")
            }
        } else {
            routePolyline = emptyList()
            android.util.Log.d("RouteMapBox", "Skipping directions: origin=$origin destination=$destination")
        }
    }

    // Fit camera to markers when map loads (initial)
    var didFitMarkersOnce by remember(routeId) { mutableStateOf(false) }

    // Fit camera to route when polyline arrives/updates (important!)
    LaunchedEffect(routePolyline) {
        if (routePolyline.size >= 2) {
            val bounds = LatLngBounds.builder().apply {
                routePolyline.forEach { include(it) }
            }.build()

            // If map isn't ready yet, animate can fail; wrap safely
            runCatching {
                cameraState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 100),
                    700
                )
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        GoogleMap(
            modifier = Modifier
                .matchParentSize()
                .nestedScroll(rememberNestedScrollInteropConnection()),
            cameraPositionState = cameraState,
            uiSettings = uiSettings,
            properties = properties,
            onMapLoaded = {
                if (!didFitMarkersOnce) {
                    didFitMarkersOnce = true
                    coroutineScope.launch {
                        val bounds = LatLngBounds.builder().apply {
                            allLocations.forEach { include(it) }
                        }.build()

                        runCatching {
                            cameraState.animate(
                                CameraUpdateFactory.newLatLngBounds(bounds, 100),
                                700
                            )
                        }
                    }
                }
            }
        ) {
            // ✅ Draw polyline with Compose API (not MapEffect)
            if (routePolyline.size >= 2) {
                Polyline(
                    points = routePolyline,
                    color = polylineColor,
                    width = 12f,
                    jointType = JointType.ROUND,
                    startCap = RoundCap(),
                    endCap = RoundCap()
                )
            }

            // ✅ Markers as Compose API
            origin?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Origin",
                    icon = originIcon,
                    anchor = Offset(0.5f, 1f)
                )
            }

            waypointLocations.forEachIndexed { index, wp ->
                Marker(
                    state = MarkerState(position = wp.location),
                    title = "Stop ${index + 1}",
                    snippet = wp.place ?: "",
                    icon = waypointIcons.getOrNull(index),
                    anchor = Offset(0.5f, 1f)
                )
            }

            destination?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Destination",
                    icon = destinationIcon,
                    anchor = Offset(0.5f, 1f)
                )
            }
        }
    }
}

/**
 * Creates a custom marker icon with a number or letter (pin + badge)
 */
fun createNumberedMarkerIcon(text: String, color: Int): BitmapDescriptor {
    val width = 80
    val height = 100
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val centerX = width / 2f
    val badgeRadius = 28f
    val badgeY = badgeRadius + 8f

    // Shadow
    val shadowPaint = Paint().apply {
        isAntiAlias = true
        this.color = AndroidColor.parseColor("#40000000")
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, badgeY + 2f, badgeRadius + 2f, shadowPaint)

    // Pin triangle
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

    // Badge circle
    val badgePaint = Paint().apply {
        isAntiAlias = true
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, badgeY, badgeRadius, badgePaint)

    // Border
    val borderPaint = Paint().apply {
        isAntiAlias = true
        this.color = AndroidColor.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    canvas.drawCircle(centerX, badgeY, badgeRadius, borderPaint)

    // Text
    val textPaint = Paint().apply {
        isAntiAlias = true
        this.color = AndroidColor.WHITE
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

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Fetch driving route from Directions API (origin -> waypoints -> destination).
 * IMPORTANT: doing this directly from the client is not ideal for key security, but kept as per your current approach.
 */
suspend fun getDrivingRouteWithWaypoints(
    origin: LatLng,
    waypoints: List<LatLng>,
    destination: LatLng,
    apiKey: String
): List<LatLng> = withContext(Dispatchers.IO) {
    try {
        if (apiKey.isBlank()) {
            android.util.Log.e("RouteMapBox", "Google Maps API key is empty")
            return@withContext emptyList()
        }

        val originStr = "${origin.latitude},${origin.longitude}"
        val destStr = "${destination.latitude},${destination.longitude}"

        val encodedOrigin = URLEncoder.encode(originStr, "UTF-8")
        val encodedDest = URLEncoder.encode(destStr, "UTF-8")

        val waypointsParam = if (waypoints.isNotEmpty()) {
            val raw = waypoints.joinToString("|") { "${it.latitude},${it.longitude}" }
            "&waypoints=" + URLEncoder.encode(raw, "UTF-8")
        } else ""

        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=$encodedOrigin" +
                "&destination=$encodedDest" +
                waypointsParam +
                "&mode=driving" +
                "&key=$apiKey"

        android.util.Log.d("RouteMapBox", "Fetching directions url=$url")

        val response = URL(url).readText()
        val json = JSONObject(response)

        val status = json.optString("status")
        android.util.Log.d("RouteMapBox", "Directions API status: $status")

        if (status != "OK") {
            val error = json.optString("error_message", "Unknown error")
            android.util.Log.e("RouteMapBox", "Directions API error: $status - $error")
            android.util.Log.e("RouteMapBox", "Directions raw: ${response.take(500)}")
            return@withContext emptyList()
        }

        val routes = json.getJSONArray("routes")
        if (routes.length() == 0) return@withContext emptyList()

        val route = routes.getJSONObject(0)
        val overview = route.optJSONObject("overview_polyline")
        val encoded = overview?.optString("points").orEmpty()

        if (encoded.isBlank()) {
            android.util.Log.w("RouteMapBox", "No overview_polyline points")
            return@withContext emptyList()
        }

        val decoded = decodePolyline(encoded)
        android.util.Log.d("RouteMapBox", "Decoded ${decoded.size} points from overview_polyline")
        decoded
    } catch (e: Exception) {
        android.util.Log.e("RouteMapBox", "Exception fetching driving route: ${e.message}", e)
        emptyList()
    }
}

/**
 * Correct polyline decoder (fixed precedence bug).
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
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lng += dlng

        poly.add(LatLng(lat / 1e5, lng / 1e5))
    }

    return poly
}
