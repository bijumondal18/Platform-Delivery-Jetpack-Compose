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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import android.Manifest
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

                            Spacer(modifier = Modifier.height(16.dp))
                            
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
    var showLoadVehicleBottomSheet by remember { mutableStateOf(false) }
    var navigationLat by remember { mutableStateOf<Double?>(null) }
    var navigationLng by remember { mutableStateOf<Double?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isAcceptingRoute by routesViewModel.isAcceptingRoute.collectAsState()
    val isStartingTrip by routesViewModel.isStartingTrip.collectAsState()
    val tripStartResult by routesViewModel.tripStartResult.collectAsState()
    val isLoadingVehicle by routesViewModel.isLoadingVehicle.collectAsState()
    val loadVehicleResult by routesViewModel.loadVehicleResult.collectAsState()
    val routeDetails by routesViewModel.routeDetails.collectAsState()
    val isRouteAvailable = routeStatus.equals("available", ignoreCase = true)
    
    // Get route data for status and isloaded check
    val routeData = routeDetails?.routeDetailsData?.routeData
    val routeStatusFromDetails = routeData?.status ?: routeStatus
    
    // Track isloaded state locally - update when load vehicle succeeds to avoid API call
    var localIsloaded by remember(routeId) { 
        mutableStateOf(routeData?.isloaded ?: 0) 
    }
    
    // Update localIsloaded when routeData changes
    LaunchedEffect(routeData?.isloaded) {
        localIsloaded = routeData?.isloaded ?: 0
    }
    
    // Update localIsloaded to 1 when load vehicle succeeds
    LaunchedEffect(loadVehicleResult) {
        if (loadVehicleResult is com.platform.platformdelivery.core.network.Result.Success) {
            localIsloaded = 1
        }
    }
    
    val isloaded = localIsloaded
    
    // Track current waypoint locally - set to first waypoint after load vehicle
    var localCurrentWaypointId by remember(routeId) { 
        mutableStateOf<Int?>(routeData?.currentWaypoint?.let { (it as? Number)?.toInt() })
    }
    
    // Update localCurrentWaypointId when routeData changes
    LaunchedEffect(routeData?.currentWaypoint) {
        localCurrentWaypointId = routeData?.currentWaypoint?.let { (it as? Number)?.toInt() }
    }
    
    val currentWaypointId = localCurrentWaypointId
    
    // Check if already checked in based on route details (trip_start_time exists)
    val isCheckedIn = routeData?.tripStartTime != null
    
    // If status == "ongoing" and isloaded == 0, disable check-in and enable load vehicle
    val isOngoingAndNotLoaded = routeStatusFromDetails.equals("ongoing", ignoreCase = true) && isloaded == 0
    
    // Hide origin buttons if vehicle is loaded
    val shouldShowOriginButtons = isloaded == 0
    
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
    
    // Snackbar for load vehicle success
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    
    // Sort waypoints once and memoize
    val sortedWaypoints = remember(waypoints) {
        waypoints?.sortedBy { waypoint ->
            (waypoint.optimizedOrder as? Number)?.toInt() ?: 0
        } ?: emptyList()
    }
    
    // Track if we've handled load vehicle result for this routeId
    var hasHandledLoadVehicle by remember(routeId) { mutableStateOf(false) }
    
    // Handle load vehicle result - update local state only, NO API CALL
    LaunchedEffect(loadVehicleResult, sortedWaypoints) {
        if (loadVehicleResult != null && !hasHandledLoadVehicle) {
            hasHandledLoadVehicle = true
            when (loadVehicleResult) {
                is com.platform.platformdelivery.core.network.Result.Success -> {
                    // Update local state: set isloaded = 1 to hide origin buttons
                    showLoadVehicleBottomSheet = false
                    
                    // Set first waypoint as active (index 0 or first waypoint ID)
                    sortedWaypoints.firstOrNull()?.let { firstWaypoint ->
                        val firstWaypointId = (firstWaypoint.id as? Number)?.toInt()
                        if (firstWaypointId != null) {
                            localCurrentWaypointId = firstWaypointId
                        } else {
                            // If waypoint ID is not available, use index 0
                            localCurrentWaypointId = 0
                        }
                    } ?: run {
                        // If no waypoints, set to 0
                        localCurrentWaypointId = 0
                    }
                    
                    // Show success snackbar
                    snackbarScope.launch {
                        snackbarHostState.showSnackbar("Vehicle loaded successfully")
                    }
                }
                is com.platform.platformdelivery.core.network.Result.Error -> {
                    // Show error snackbar
                    snackbarScope.launch {
                        snackbarHostState.showSnackbar("Failed to load vehicle: ${(loadVehicleResult as Result.Error).message}")
                    }
                }
                else -> Unit
            }
        }
    }
    
    // Reset flag when loadVehicleResult becomes null (new operation started)
    LaunchedEffect(loadVehicleResult) {
        if (loadVehicleResult == null) {
            hasHandledLoadVehicle = false
        }
    }
    
    // Handle delivery update result - only refresh once
    val deliveryUpdateResult by routesViewModel.deliveryUpdateResult.collectAsState()
    var hasHandledDelivery by remember(routeId) { mutableStateOf(false) }
    LaunchedEffect(deliveryUpdateResult) {
        if (deliveryUpdateResult != null && !hasHandledDelivery) {
            hasHandledDelivery = true
            when (deliveryUpdateResult) {
                is com.platform.platformdelivery.core.network.Result.Success -> {
                    // Refresh route details to get updated current_waypoint (only once)
                    if (routeId.isNotEmpty()) {
                        routesViewModel.getRouteDetails(RequestRouteDetails(routeId = routeId))
                    }
                }
                is com.platform.platformdelivery.core.network.Result.Error -> {
                    // Handle error - could show a snackbar
                    android.util.Log.e("RouteDetailsScreen", "Failed to update delivery: ${(deliveryUpdateResult as Result.Error).message}")
                }
                else -> Unit
            }
        }
    }
    
    // Reset flag when deliveryUpdateResult becomes null (new operation started)
    LaunchedEffect(deliveryUpdateResult) {
        if (deliveryUpdateResult == null) {
            hasHandledDelivery = false
        }
    }

    val hasWaypoints = sortedWaypoints.isNotEmpty()
    val hasDestination = destinationPlace.isNotEmpty()

    // Build complete list of stops with notes and waypoint info
    data class StopInfo(
        val place: String, 
        val stopNumber: Int, 
        val note: String?,
        val waypoint: Waypoint? = null,
        val waypointId: Int? = null
    )

    val allStops = remember(originPlace, sortedWaypoints, destinationPlace) {
        buildList {
            add(StopInfo(originPlace, 0, null, null, null)) // Origin is stop 0, no note
            sortedWaypoints.forEachIndexed { index, waypoint ->
                val note = waypoint.noteForDrivers?.toString()?.takeIf { it.isNotEmpty() }
                    ?: waypoint.noteForInternalUse?.toString()?.takeIf { it.isNotEmpty() }
                val waypointId = (waypoint.id as? Number)?.toInt()
                add(StopInfo(waypoint.place?.toString() ?: "", index + 1, note, waypoint, waypointId))
            }
            if (hasDestination) {
                add(
                    StopInfo(
                        destinationPlace,
                        sortedWaypoints.size + 1,
                        null,
                        null,
                        null
                    )
                ) // Destination, no note
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
        allStops.forEachIndexed { index, stopInfo ->
            val isFirst = index == 0 // Origin
            val isLast = index == allStops.size - 1
            val isWaypoint = !isFirst && !isLast

            // Check if this waypoint is the active/current waypoint
            // currentWaypoint might be an index (0-based) or a waypoint ID
            // Only show buttons if vehicle is loaded (isloaded == 1)
            val waypointIndex = if (isWaypoint) index - 1 else null // Subtract 1 because index 0 is origin
            val isActiveWaypoint = if (isWaypoint && currentWaypointId != null && isloaded == 1) {
                // Try matching by waypoint ID first
                val matchesById = stopInfo.waypointId != null && stopInfo.waypointId == currentWaypointId
                // Or match by index (currentWaypoint might be 0-based index)
                val matchesByIndex = waypointIndex != null && waypointIndex == currentWaypointId
                matchesById || matchesByIndex
            } else {
                false
            }
            
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
                shouldShowActionButtons = shouldShowActionButtons && shouldShowOriginButtons,
                isStartingTrip = isStartingTrip,
                isOngoingAndNotLoaded = isOngoingAndNotLoaded,
                isVehicleLoaded = isloaded == 1,
                isActiveWaypoint = isActiveWaypoint,
                waypointId = stopInfo.waypointId?.toString() ?: "",
                routesViewModel = routesViewModel,
                onNavigateClick = { 
                    // Navigate to waypoint if it's a waypoint, otherwise to origin
                    if (isWaypoint && stopInfo.waypoint != null) {
                        val lat = parseCoordinate(stopInfo.waypoint.destinationLat)?.toDouble()
                        val lng = parseCoordinate(stopInfo.waypoint.destinationLng)?.toDouble()
                        if (lat != null && lng != null) {
                            navigationLat = lat
                            navigationLng = lng
                            showMapBottomSheet = true
                        }
                    } else {
                        // Navigate to origin
                        val lat = parseCoordinate(originLat)?.toDouble()
                        val lng = parseCoordinate(originLng)?.toDouble()
                        if (lat != null && lng != null) {
                            navigationLat = lat
                            navigationLng = lng
                            showMapBottomSheet = true
                        }
                    }
                },
                onCheckInClick = {
                    if (routeId.isNotEmpty()) {
                        routesViewModel.tripStartTime(routeId) {
                            // Success handled in LaunchedEffect
                        }
                    }
                },
                onLoadVehicleClick = { showLoadVehicleBottomSheet = true },
                onAcceptRouteClick = {
                    if (routeId.isNotEmpty()) {
                        routesViewModel.acceptRoute(routeId) {
                            onRouteAccepted()
                        }
                    }
                },
                onDeliverClick = {
                    if (routeId.isNotEmpty() && stopInfo.waypointId != null) {
                        routesViewModel.updateWaypointDelivery(
                            routeId,
                            stopInfo.waypointId.toString(),
                            "delivered"
                        ) {
                            // Success handled in LaunchedEffect
                        }
                    }
                },
                onFailedClick = {
                    if (routeId.isNotEmpty() && stopInfo.waypointId != null) {
                        routesViewModel.updateWaypointDelivery(
                            routeId,
                            stopInfo.waypointId.toString(),
                            "failed"
                        ) {
                            // Success handled in LaunchedEffect
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
            } else {
                // Add extra space after destination
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        }
        
        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                shape = RoundedCornerShape(8.dp),
                containerColor = if (snackbarData.visuals.message.contains("Failed", ignoreCase = true)) {
                    MaterialTheme.colorScheme.error
                } else {
                    SuccessGreen
                },
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    // Map selection bottom sheet
    if (showMapBottomSheet && navigationLat != null && navigationLng != null) {
        MapSelectionBottomSheet(
            onDismiss = { 
                showMapBottomSheet = false
                navigationLat = null
                navigationLng = null
            },
            onMapSelected = { mapPackage ->
                if (navigationLat != null && navigationLng != null) {
                    openMap(context, mapPackage, navigationLat!!, navigationLng!!)
                }
                showMapBottomSheet = false
                navigationLat = null
                navigationLng = null
            }
        )
    }
    
    // Load Vehicle bottom sheet with waypoint checklist
    if (showLoadVehicleBottomSheet) {
        LoadVehicleBottomSheet(
            waypoints = sortedWaypoints,
            routeId = routeId,
            isLoadingVehicle = isLoadingVehicle,
            routesViewModel = routesViewModel,
            onDismiss = { showLoadVehicleBottomSheet = false }
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

        // Convert to AM/PM format
        val hour12 = when {
            finalHour == 0 -> 12
            finalHour > 12 -> finalHour - 12
            else -> finalHour
        }
        val amPm = if (finalHour < 12) "AM" else "PM"
        String.format("%d:%02d %s", hour12, finalMinute, amPm)
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
    isVehicleLoaded: Boolean = false,
    isActiveWaypoint: Boolean = false,
    waypointId: String = "",
    routesViewModel: RoutesViewModel? = null,
    onNavigateClick: () -> Unit = {},
    onCheckInClick: () -> Unit = {},
    onLoadVehicleClick: () -> Unit = {},
    onAcceptRouteClick: () -> Unit = {},
    onDeliverClick: () -> Unit = {},
    onFailedClick: () -> Unit = {}
) {
    val isUpdatingDelivery by routesViewModel?.isUpdatingDelivery?.collectAsState() ?: remember { mutableStateOf(false) }
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
                        color = if (isActiveWaypoint) 
                            SuccessGreen.copy(alpha = 0.2f)
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isFirst || isLast) {
                    // Home icon for origin, Flag icon for destination
                    Icon(
                        imageVector = if (isFirst) Icons.Default.Home else Icons.Default.Flag,
                        contentDescription = if (isFirst) "Origin" else "Destination",
                        modifier = Modifier.size(20.dp),
                        tint = if (isActiveWaypoint) SuccessGreen else MaterialTheme.colorScheme.primary
                    )
                } else {
                    // Number badge for waypoints (starting from 01)
                    Text(
                        text = String.format("%02d", stopNumber),
                        style = AppTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isActiveWaypoint) SuccessGreen else MaterialTheme.colorScheme.primary
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
                            color = if (isActiveWaypoint) 
                                SuccessGreen.copy(alpha = 0.6f)
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
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
                color = if (isActiveWaypoint) SuccessGreen else MaterialTheme.colorScheme.primary
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

            // Action buttons for origin (only show if vehicle is not loaded)
            if (isFirst && shouldShowActionButtons && !isVehicleLoaded) {
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
            
            // Action buttons for active waypoint (when vehicle is loaded)
            if (isWaypoint && isActiveWaypoint && isVehicleLoaded) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    
                    // Deliver and Failed buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Deliver button
                        Button(
                            onClick = onDeliverClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            enabled = !isUpdatingDelivery,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (isUpdatingDelivery) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isUpdatingDelivery) "Delivering..." else "Deliver",
                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        
                        // Failed button
                        Button(
                            onClick = onFailedClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            enabled = !isUpdatingDelivery,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (isUpdatingDelivery) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onError,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (isUpdatingDelivery) "Updating..." else "Failed",
                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
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

/**
 * Bottom sheet for loading vehicle with waypoint checklist
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadVehicleBottomSheet(
    waypoints: List<Waypoint>,
    routeId: String,
    isLoadingVehicle: Boolean,
    routesViewModel: RoutesViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Track checked waypoints by their IDs
    val checkedWaypoints = remember { mutableStateMapOf<Int, Boolean>() }
    
    // Initialize all waypoints as unchecked
    LaunchedEffect(waypoints) {
        waypoints.forEach { waypoint ->
            val waypointId = (waypoint.id as? Number)?.toInt()
            if (waypointId != null) {
                checkedWaypoints[waypointId] = false
            }
        }
    }
    
    // Check if all waypoints are checked
    val allChecked = waypoints.isNotEmpty() && waypoints.all { waypoint ->
        val waypointId = (waypoint.id as? Number)?.toInt()
        waypointId != null && checkedWaypoints[waypointId] == true
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
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        },
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Title row with Check All button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Load Vehicle",
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // Check All button
                if (waypoints.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            // Toggle all waypoints
                            val shouldCheckAll = !allChecked
                            waypoints.forEach { waypoint ->
                                val waypointId = (waypoint.id as? Number)?.toInt()
                                if (waypointId != null) {
                                    checkedWaypoints[waypointId] = shouldCheckAll
                                }
                            }
                        }
                    ) {
                        Text(
                            text = if (allChecked) "Uncheck All" else "Check All",
                            style = AppTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Text(
                text = "Please check all waypoints to confirm vehicle loading",
                style = AppTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (waypoints.isEmpty()) {
                Text(
                    text = "No waypoints available",
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Waypoint checklist
                waypoints.forEachIndexed { index, waypoint ->
                    val waypointId = (waypoint.id as? Number)?.toInt()
                    val isChecked = waypointId != null && checkedWaypoints[waypointId] == true
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                if (waypointId != null) {
                                    checkedWaypoints[waypointId] = !isChecked
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (waypointId != null) {
                                    checkedWaypoints[waypointId] = it
                                }
                            }
                        )
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Waypoint ${index + 1}",
                                style = AppTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = waypoint.place?.toString() ?: "",
                                style = AppTypography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }
                    }
                    
                    if (index < waypoints.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit button
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            // Get current location
                            val hasLocationPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED

                            if (!hasLocationPermission) {
                                // Handle permission error - could show snackbar
                                return@launch
                            }

                            // Get current location
                            var location = suspendCancellableCoroutine<android.location.Location?> { continuation ->
                                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                                    continuation.resume(loc)
                                }.addOnFailureListener { e ->
                                    continuation.resumeWithException(e)
                                }
                            }
                            
                            // If lastLocation is null, request a fresh location update
                            if (location == null) {
                                location = suspendCancellableCoroutine { continuation ->
                                    fusedLocationClient.getCurrentLocation(
                                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                                        null
                                    ).addOnSuccessListener { loc ->
                                        continuation.resume(loc)
                                    }.addOnFailureListener { e ->
                                        continuation.resume(null)
                                    }
                                }
                            }

                            if (location != null) {
                                val checkedIds = waypoints.mapNotNull { waypoint ->
                                    val waypointId = (waypoint.id as? Number)?.toInt()
                                    if (waypointId != null && checkedWaypoints[waypointId] == true) {
                                        waypointId
                                    } else {
                                        null
                                    }
                                }
                                
                                if (routeId.isNotEmpty() && checkedIds.isNotEmpty()) {
                                    // Format datetime as yyyy-MM-dd HH:mm:ss
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    val datetime = dateFormat.format(Date())
                                    
                                    routesViewModel.loadVehicle(
                                        routeId, 
                                        checkedIds,
                                        location.latitude,
                                        location.longitude,
                                        datetime
                                    ) {
                                        // Success handled in LaunchedEffect
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Handle error - could show snackbar
                            android.util.Log.e("LoadVehicleBottomSheet", "Error getting location: ${e.message}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = allChecked && !isLoadingVehicle && waypoints.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (isLoadingVehicle) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLoadingVehicle) "Loading Vehicle..." else "Submit",
                    style = AppTypography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}