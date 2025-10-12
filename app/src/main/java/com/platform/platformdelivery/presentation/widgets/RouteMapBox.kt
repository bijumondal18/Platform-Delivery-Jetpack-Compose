package com.platform.platformdelivery.presentation.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

@Composable
fun RouteMapBox(
    latitude: Double,
    longitude: Double,
    routeId: String? = null // 👈 add optional routeId for key stability
) {

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val mapHeight = screenHeight * 0.3f // 30% of device height

    val zoomLevel: Float = 14f


    // Initial camera position (e.g., some coordinates)
    val location = remember(latitude, longitude) { LatLng(latitude, longitude) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, zoomLevel)
    }

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
            uiSettings = MapUiSettings(zoomControlsEnabled = true),
            properties = MapProperties(mapType = MapType.NORMAL)
        ) {
            // Example marker
            Marker(
                state = MarkerState(position = location),
                title = "Route Location",
                snippet = "Lat: $latitude, Lng: $longitude"
            )
        }
    }

    // Optional: animate camera when coordinates change
    LaunchedEffect(latitude, longitude) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(location, zoomLevel),
            durationMs = 700
        )
    }
}