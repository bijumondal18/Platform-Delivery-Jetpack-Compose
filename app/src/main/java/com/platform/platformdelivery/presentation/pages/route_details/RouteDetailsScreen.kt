package com.platform.platformdelivery.presentation.pages.route_details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.R
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.core.theme.SuccessGreen
import com.platform.platformdelivery.data.models.RequestRouteDetails
import com.platform.platformdelivery.data.models.Waypoint
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import com.platform.platformdelivery.presentation.widgets.RouteMapBox
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailsScreen(
    modifier: Modifier = Modifier,
    routesViewModel: RoutesViewModel = viewModel(),
    routeId: String?,
    onTitleChange: (String) -> Unit = {},
    navController: NavController? = null
) {

    val routeDetails by routesViewModel.routeDetails.collectAsState()
    val isLoading by routesViewModel.isRouteDetailsLoading.collectAsState()
    val error by routesViewModel.routeDetailsError.collectAsState()

    // Get route title for TopAppBar
    val routeTitle = remember(routeDetails) {
        routeDetails?.routeDetailsData?.routeData?.id?.let { id ->
            "Route #$id"
        } ?: "Route Details"
    }

    LaunchedEffect(routeDetails) {
        routeDetails?.routeDetailsData?.routeData?.id?.let { id ->
            onTitleChange("Route #$id")
        }
    }

    LaunchedEffect(routeId) {
        if (!routeId.isNullOrEmpty()) {
            routesViewModel.getRouteDetails(RequestRouteDetails(routeId = routeId))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Text(
                        routeTitle,
                        style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController?.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            // Optimize scrolling performance
            userScrollEnabled = true
        ) {


            when {
                isLoading -> {
                    item {
                        Text(
                            "Loading routes...",
                            style = AppTypography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                else -> {
                    if (error != null) {
                        item {
                            Text(
                                "$error",
                                style = AppTypography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    } else if (routeDetails != null) {
                        val route = routeDetails!!.routeDetailsData?.routeData

                        // Map at the top - use stable key and remember to prevent recomposition
                        item(key = "routeMap_$routeId") {
                            val route = routeDetails!!.routeDetailsData?.routeData

                            // Remember map parameters to prevent unnecessary recomposition
                            val mapParams: MapParams =
                                remember(routeId, route?.originLat, route?.originLng) {
                                    MapParams(
                                        latitude = route?.destinationLat ?: 0.0,
                                        longitude = route?.destinationLng ?: 0.0,
                                        originLat = route?.originLat,
                                        originLng = route?.originLng,
                                        destinationLat = route?.destinationLat,
                                        destinationLng = route?.destinationLng,
                                        waypoints = route?.waypoints
                                    )
                                }

                            RouteMapBox(
                                latitude = mapParams.latitude,
                                longitude = mapParams.longitude,
                                routeId = routeId,
                                originLat = mapParams.originLat,
                                originLng = mapParams.originLng,
                                destinationLat = mapParams.destinationLat,
                                destinationLng = mapParams.destinationLng,
                                waypoints = mapParams.waypoints
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Route Summary Section (like in the image)
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Date row
                                route?.startDate?.let { startDate ->
                                    Text(
                                        text = formatRouteDate(startDate),
                                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                } ?: route?.startTime?.let { startTime ->
                                    // Fallback to current date if startDate not available
                                    val currentDate = java.time.LocalDate.now()
                                    val formatter = java.time.format.DateTimeFormatter.ofPattern(
                                        "EEE MMM dd",
                                        java.util.Locale.ENGLISH
                                    )
                                    Text(
                                        text = currentDate.format(formatter),
                                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                // Summary row: "X stops • X km • Xh Xm"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val totalStops = (1 + (route?.waypoints?.size
                                            ?: 0) + if (route?.destinationPlace.isNullOrEmpty()) 0 else 1)
                                        route?.distance?.let { distance ->
                                            route?.estimatedTotalTime?.let { totalTime ->
                                                Text(
                                                    text = "$totalStops stops • $distance • $totalTime",
                                                    style = AppTypography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        }
                                    }

                                }

                                // Optimization info (if available)
                                // Note: This would come from API if available
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }

                        // Route Stops List (numbered like in image)
                        item {
                            val routeStatus = route?.status ?: ""
                            // Check for both "completed" and "compleated" (API typo)
                            val isCompleted = routeStatus.equals("completed", ignoreCase = true) ||
                                    routeStatus.equals("compleated", ignoreCase = true)
                            RouteStopsList(
                                originPlace = route?.originPlace ?: "",
                                destinationPlace = route?.destinationPlace ?: "",
                                waypoints = route?.waypoints,
                                routeStartTime = route?.startTime,
                                originLat = route?.originLat,
                                originLng = route?.originLng,
                                routeStatus = routeStatus,
                                routeId = route?.id?.toString() ?: "",
                                routesViewModel = routesViewModel,
                                shouldShowActionButtons = !isCompleted,
                                onRouteAccepted = {
                                    // Refresh route details after accepting
                                    routeId?.let { id ->
                                        routesViewModel.getRouteDetails(RequestRouteDetails(routeId = id))
                                    }
                                }
                            )
                        }

                    } else {
                        item {
                            Text(
                                "Something went wrong...",
                                style = AppTypography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }

                }

            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteStopsList(
    originPlace: String,
    destinationPlace: String,
    waypoints: List<Waypoint>?,
    routeStartTime: String? = null,
    originLat: String? = null,
    originLng: String? = null,
    routeStatus: String = "",
    routeId: String = "",
    routesViewModel: RoutesViewModel,
    shouldShowActionButtons: Boolean = true,
    onRouteAccepted: () -> Unit = {}
) {
    var showMapBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isAcceptingRoute by routesViewModel.isAcceptingRoute.collectAsState()
    val isStartingTrip by routesViewModel.isStartingTrip.collectAsState()
    val tripStartResult by routesViewModel.tripStartResult.collectAsState()
    val routeDetails by routesViewModel.routeDetails.collectAsState()
    val isRouteAvailable = routeStatus.equals("available", ignoreCase = true)
    
    // Get route data for status and isloaded check
    val routeData = routeDetails?.routeDetailsData?.routeData
    val routeStatusFromDetails = routeData?.status ?: routeStatus
    val isloaded = routeData?.isloaded ?: 0
    
    // Check if already checked in based on route details (trip_start_time exists)
    val isCheckedIn = routeData?.tripStartTime != null
    
    // If status == "ongoing" and isloaded == 0, disable check-in and enable load vehicle
    val isOngoingAndNotLoaded = routeStatusFromDetails.equals("ongoing", ignoreCase = true) && isloaded == 0
    
    // Handle trip start result
    LaunchedEffect(tripStartResult) {
        when (tripStartResult) {
            is com.platform.platformdelivery.core.network.Result.Success -> {
                // Refresh route details to get updated trip_start_time
                if (routeId.isNotEmpty()) {
                    routesViewModel.getRouteDetails(RequestRouteDetails(routeId = routeId))
                }
            }
            is com.platform.platformdelivery.core.network.Result.Error -> {
                // Handle error - could show a snackbar
                android.util.Log.e("RouteDetailsScreen", "Failed to start trip: ${(tripStartResult as Result.Error).message}")
            }
            else -> Unit
        }
    }
    // Sort waypoints once and memoize
    val sortedWaypoints = remember(waypoints) {
        waypoints?.sortedBy { waypoint ->
            (waypoint.optimizedOrder as? Number)?.toInt() ?: 0
        } ?: emptyList()
    }

    val hasWaypoints = sortedWaypoints.isNotEmpty()
    val hasDestination = destinationPlace.isNotEmpty()

    // Build complete list of stops with notes
    data class StopInfo(val place: String, val stopNumber: Int, val note: String?)

    val allStops = remember(originPlace, sortedWaypoints, destinationPlace) {
        buildList {
            add(StopInfo(originPlace, 0, null)) // Origin is stop 0, no note
            sortedWaypoints.forEachIndexed { index, waypoint ->
                val note = waypoint.noteForDrivers?.toString()?.takeIf { it.isNotEmpty() }
                    ?: waypoint.noteForInternalUse?.toString()?.takeIf { it.isNotEmpty() }
                add(StopInfo(waypoint.place?.toString() ?: "", index + 1, note))
            }
            if (hasDestination) {
                add(
                    StopInfo(
                        destinationPlace,
                        sortedWaypoints.size + 1,
                        null
                    )
                ) // Destination, no note
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        allStops.forEachIndexed { index, stopInfo ->
            val isFirst = index == 0 // Origin
            val isLast = index == allStops.size - 1
            val isWaypoint = !isFirst && !isLast

            RouteStopItem(
                place = stopInfo.place,
                stopNumber = stopInfo.stopNumber,
                scheduledTime = calculateScheduledTime(routeStartTime, index),
                note = stopInfo.note,
                isFirst = isFirst,
                isLast = isLast,
                isWaypoint = isWaypoint,
                isCheckedIn = isCheckedIn,
                isRouteAvailable = isRouteAvailable,
                routeId = routeId,
                isAcceptingRoute = isAcceptingRoute,
                shouldShowActionButtons = shouldShowActionButtons,
                isStartingTrip = isStartingTrip,
                isOngoingAndNotLoaded = isOngoingAndNotLoaded,
                onNavigateClick = { showMapBottomSheet = true },
                onCheckInClick = {
                    if (routeId.isNotEmpty()) {
                        routesViewModel.tripStartTime(routeId) {
                            // Success handled in LaunchedEffect
                        }
                    }
                },
                onLoadVehicleClick = { /* Load vehicle action */ },
                onAcceptRouteClick = {
                    if (routeId.isNotEmpty()) {
                        routesViewModel.acceptRoute(routeId) {
                            onRouteAccepted()
                        }
                    }
                }
            )

            // Add divider after origin and waypoints (not after destination)
            if (!isLast) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(start = 60.dp) // Align with content
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Map selection bottom sheet
    if (showMapBottomSheet) {
        MapSelectionBottomSheet(
            onDismiss = { showMapBottomSheet = false },
            onMapSelected = { mapPackage ->
                val lat = waypoints?.firstOrNull()?.let {
                    parseCoordinate(it.destinationLat)?.toDouble()
                }
                val lng = waypoints?.firstOrNull()?.let {
                    parseCoordinate(it.destinationLng)?.toDouble()
                }
                if (lat != null && lng != null) {
                    openMap(context, mapPackage, lat, lng)
                }
                showMapBottomSheet = false
            }
        )
    }
}

// Data class to hold map parameters for stable recomposition
private data class MapParams(
    val latitude: Double,
    val longitude: Double,
    val originLat: String?,
    val originLng: String?,
    val destinationLat: Double?,
    val destinationLng: Double?,
    val waypoints: List<Waypoint>?
)

// Helper function to parse coordinates
fun parseCoordinate(value: Any?): Double? {
    return when (value) {
        is Number -> value.toDouble()
        is String -> try {
            value.toDouble()
        } catch (e: Exception) {
            null
        }

        else -> null
    }
}

// Helper function to format date like "Mon Dec 17"
@RequiresApi(Build.VERSION_CODES.O)
fun formatRouteDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val inputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter =
            java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd", java.util.Locale.ENGLISH)
        val date = java.time.LocalDate.parse(dateString, inputFormatter)
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateString
    }
}

// Helper function to calculate scheduled time for each stop
fun calculateScheduledTime(startTime: String?, stopIndex: Int): String {
    if (startTime.isNullOrEmpty()) return ""
    return try {
        // Parse start time (assuming format like "7:00" or "07:00")
        val timeParts = startTime.split(":")
        val startHour = timeParts[0].toIntOrNull() ?: 0
        val startMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        // Add 23 minutes per stop (as shown in image: 7:00, 7:23, 7:46, 8:09)
        val minutesToAdd = stopIndex * 23
        val totalMinutes = startHour * 60 + startMinute + minutesToAdd
        val finalHour = (totalMinutes / 60) % 24
        val finalMinute = totalMinutes % 60

        String.format("%d:%02d", finalHour, finalMinute)
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun RouteStopItem(
    place: String,
    stopNumber: Int,
    scheduledTime: String = "",
    note: String? = null,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isWaypoint: Boolean = false,
    isCheckedIn: Boolean = false,
    isRouteAvailable: Boolean = false,
    routeId: String = "",
    isAcceptingRoute: Boolean = false,
    isStartingTrip: Boolean = false,
    shouldShowActionButtons: Boolean = true,
    isOngoingAndNotLoaded: Boolean = false,
    onNavigateClick: () -> Unit = {},
    onCheckInClick: () -> Unit = {},
    onLoadVehicleClick: () -> Unit = {},
    onAcceptRouteClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon/Number badge with vertical line
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Origin and Destination: Home icon, Waypoints: Number badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isFirst || isLast) {
                    // Home icon for origin and destination
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = if (isFirst) "Origin" else "Destination",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // Number badge for waypoints (starting from 01)
                    Text(
                        text = String.format("%02d", stopNumber),
                        style = AppTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Vertical blue line connecting stops
            if (!isLast) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        }

        // Stop details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Title: Warehouse for origin/destination, Drop-Off for waypoints
            val title = if (isFirst || isLast) "Warehouse" else "Drop-Off"
            Text(
                text = title,
                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )

            // Address and time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Address
                Text(
                    text = place,
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )

                // Scheduled time on the right
                if (scheduledTime.isNotEmpty()) {
                    Text(
                        text = scheduledTime,
                        style = AppTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Note for waypoints
            if (isWaypoint && !note.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: $note",
                    style = AppTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Normal
                )
            }

            // Action buttons for origin
            if (isFirst && shouldShowActionButtons) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Show Accept button if route status is "available"
                    if (isRouteAvailable) {
                        Button(
                            onClick = onAcceptRouteClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !isAcceptingRoute,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            if (isAcceptingRoute) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAcceptingRoute) "Accepting..." else "Accept Route",
                                style = AppTypography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        // Show Navigate, Check In, Load Vehicle buttons if route is not available and not completed
                        // Navigate button
                        Button(
                            onClick = onNavigateClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Navigate",
                                style = AppTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        // Check In and Load Vehicle buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Check In button (disabled after check-in, while starting trip, or if ongoing and not loaded)
                            Button(
                                onClick = onCheckInClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                enabled = !isCheckedIn && !isStartingTrip && !isOngoingAndNotLoaded, // Disable after check-in, while starting trip, or if ongoing and not loaded
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                if (isStartingTrip) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = when {
                                        isStartingTrip -> "Checking In..."
                                        isCheckedIn -> "Checked In"
                                        else -> "Check In"
                                    },
                                    style = AppTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            // Load Vehicle button (enabled after check-in or if ongoing and not loaded)
                            Button(
                                onClick = onLoadVehicleClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                enabled = isCheckedIn || isOngoingAndNotLoaded, // Enabled after check-in or if ongoing and not loaded
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SuccessGreen,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Load Vehicle",
                                    style = AppTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom sheet for selecting a map application
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSelectionBottomSheet(
    onDismiss: () -> Unit,
    onMapSelected: (String?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // List of available map apps
    val mapApps = remember {
        getAvailableMapApps(context)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Map App",
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 20.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (mapApps.isEmpty()) {
                Text(
                    text = "No map apps available",
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                mapApps.forEach { mapApp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable {
                                onMapSelected(mapApp.packageName)
                                onDismiss()
                            }
                            .padding(vertical = 16.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = mapApp.name,
                            style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Data class for map app info
 */
data class MapAppInfo(
    val name: String,
    val packageName: String?
)

/**
 * Get list of installed map applications
 */
fun getAvailableMapApps(context: android.content.Context): List<MapAppInfo> {
    val mapApps = mutableListOf<MapAppInfo>()
    val pm = context.packageManager

    // Google Maps
    try {
        pm.getPackageInfo("com.google.android.apps.maps", 0)
        mapApps.add(MapAppInfo("Google Maps", "com.google.android.apps.maps"))
    } catch (e: PackageManager.NameNotFoundException) {
        // Not installed
    }

    // Waze
    try {
        pm.getPackageInfo("com.waze", 0)
        mapApps.add(MapAppInfo("Waze", "com.waze"))
    } catch (e: PackageManager.NameNotFoundException) {
        // Not installed
    }

    // Apple Maps (via web)
    mapApps.add(MapAppInfo("Apple Maps", null))

    // Default browser (for web-based maps)
    mapApps.add(MapAppInfo("Browser", "default"))

    return mapApps
}

/**
 * Open map application with destination coordinates
 */
fun openMap(
    context: android.content.Context,
    packageName: String?,
    destinationLat: Double,
    destinationLng: Double
) {
    val uri = when (packageName) {
        "com.google.android.apps.maps" -> {
            Uri.parse("google.navigation:q=$destinationLat,$destinationLng")
        }

        "com.waze" -> {
            Uri.parse("waze://?ll=$destinationLat,$destinationLng&navigate=yes")
        }

        null -> {
            // Apple Maps web URL
            Uri.parse("https://maps.apple.com/?daddr=$destinationLat,$destinationLng&dirflg=d")
        }

        "default" -> {
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destinationLat,$destinationLng")
        }

        else -> {
            Uri.parse("geo:$destinationLat,$destinationLng?q=$destinationLat,$destinationLng")
        }
    }

    val intent = when (packageName) {
        null, "default" -> Intent(Intent.ACTION_VIEW, uri)
        else -> {
            Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(packageName)
            }
        }
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to generic geo intent
        val fallbackIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("geo:$destinationLat,$destinationLng"))
        try {
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {
            // Handle error
        }
    }
}