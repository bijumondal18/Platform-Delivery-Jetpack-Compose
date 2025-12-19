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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
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
import androidx.compose.ui.platform.LocalConfiguration
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
            // Stream route details from Firestore instead of API
            routesViewModel.startStreamingRouteDetails(routeId)
        }
    }

    // Bottom sheet state
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    // Calculate 40% of screen height for minimum bottom sheet height
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val minSheetHeight = screenHeight * 0.45f

    // Map parameters
    val route = routeDetails?.routeDetailsData?.routeData
    val mapParams: MapParams? = remember(routeId, route?.originLat, route?.originLng) {
        route?.let {
            MapParams(
                latitude = it.destinationLat ?: 0.0,
                longitude = it.destinationLng ?: 0.0,
                originLat = it.originLat,
                originLng = it.originLng,
                destinationLat = it.destinationLat,
                destinationLng = it.destinationLng,
                waypoints = it.waypoints
            )
        }
    }

    // Create shape with medium top radius and no bottom radius
    val sheetShape = remember {
        RoundedCornerShape(
            topStart = 16.dp, // Medium radius
            topEnd = 16.dp,   // Medium radius
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        )
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContainerColor = MaterialTheme.colorScheme.background,
        sheetShape = sheetShape,
        sheetPeekHeight = minSheetHeight,
        sheetContent = {
            // Bottom sheet content with route details
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.background)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Loading route details...",
                                style = AppTypography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.background)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$error",
                            style = AppTypography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                routeDetails != null && route != null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Route Summary Section
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Date row
                                route.startDate?.let { startDate ->
                                    Text(
                                        text = formatRouteDate(startDate),
                                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                } ?: route.startTime?.let { startTime ->
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
                                        val totalStops = (1 + (route.waypoints?.size ?: 0) +
                                                if (route.destinationPlace.isNullOrEmpty()) 0 else 1)
                                        route.distance?.let { distance ->
                                            route.estimatedTotalTime?.let { totalTime ->
                                                Text(
                                                    text = "$totalStops stops • $distance • $totalTime",
                                                    style = AppTypography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }

                        // Route Stops List
                        item {
                            val routeStatus = route.status ?: ""
                            val isCompleted = routeStatus.equals("completed", ignoreCase = true) ||
                                    routeStatus.equals("compleated", ignoreCase = true)
                            RouteStopsList(
                                originPlace = route.originPlace ?: "",
                                destinationPlace = route.destinationPlace ?: "",
                                waypoints = route.waypoints,
                                routeStartTime = route.startTime,
                                originLat = route.originLat,
                                originLng = route.originLng,
                                routeStatus = routeStatus,
                                routeId = route.id?.toString() ?: "",
                                routesViewModel = routesViewModel,
                                navController = navController,
                                shouldShowActionButtons = !isCompleted,
                                onRouteAccepted = {
                                    routeId?.let { id ->
                                        routesViewModel.getRouteDetails(RequestRouteDetails(routeId = id))
                                    }
                                }
                            )
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Something went wrong...",
                            style = AppTypography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        }
    ) { paddingValues ->
        // Main content - Full screen map
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (mapParams != null) {
                RouteMapBox(
                    latitude = mapParams.latitude,
                    longitude = mapParams.longitude,
                    routeId = routeId,
                    originLat = mapParams.originLat,
                    originLng = mapParams.originLng,
                    destinationLat = mapParams.destinationLat,
                    destinationLng = mapParams.destinationLng,
                    waypoints = mapParams.waypoints,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Loading or error state for map
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (error != null) {
                        Text(
                            text = "Unable to load map",
                            style = AppTypography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Floating circular back button over the map
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Card(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            navController?.popBackStack()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
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
    navController: NavController? = null,
    shouldShowActionButtons: Boolean = true,
    onRouteAccepted: () -> Unit = {}
) {
    var showMapBottomSheet by remember { mutableStateOf(false) }
    var showLoadVehicleBottomSheet by remember { mutableStateOf(false) }
    var showDeliveryOptionsBottomSheet by remember { mutableStateOf(false) }
    var selectedWaypointId by remember { mutableStateOf<String?>(null) }
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

    // Get route data for status and isloaded check - use Firestore data directly
    val routeData = routeDetails?.routeDetailsData?.routeData
    val routeStatusFromDetails = routeData?.status ?: routeStatus

    // Use Firestore data directly - no local state needed
    // Status and isloaded will persist from Firestore when navigating back and forth
    val isloaded = (routeData?.isloaded as? Number)?.toInt() ?: 0

    // Get current waypoint from Firestore data
    val currentWaypointId = routeData?.currentWaypoint?.let { (it as? Number)?.toInt() }

    // Check if already checked in based on Firestore data
    // Check both status == "ongoing" OR trip_start_time exists (for backward compatibility)
    val isCheckedIn = routeStatusFromDetails.equals(
        "ongoing",
        ignoreCase = true
    ) || routeData?.tripStartTime != null

    // If status == "ongoing" and isloaded == 0, disable check-in and enable load vehicle
    val isOngoingAndNotLoaded =
        routeStatusFromDetails.equals("ongoing", ignoreCase = true) && isloaded == 0

    // Hide origin buttons (Check In, Load Vehicle) if vehicle is loaded
    // Show waypoint buttons only if vehicle is loaded (isloaded == 1)
    val shouldShowOriginButtons = isloaded == 0
    val shouldShowWaypointButtons = isloaded == 1

    // Handle trip start result
    LaunchedEffect(tripStartResult) {
        when (tripStartResult) {
            is com.platform.platformdelivery.core.network.Result.Success -> {
                // Don't call route details API - data is streaming from Firestore
                // Firestore will automatically update the UI when status changes to 'ongoing'
                android.util.Log.d(
                    "RouteDetailsScreen",
                    "Checkin successful. Firestore will update automatically."
                )
            }

            is com.platform.platformdelivery.core.network.Result.Error -> {
                // Handle error - could show a snackbar
                android.util.Log.e(
                    "RouteDetailsScreen",
                    "Failed to start trip: ${(tripStartResult as Result.Error).message}"
                )
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

    // Handle load vehicle result - Firestore will update automatically
    LaunchedEffect(loadVehicleResult) {
        if (loadVehicleResult != null && !hasHandledLoadVehicle) {
            hasHandledLoadVehicle = true
            when (loadVehicleResult) {
                is com.platform.platformdelivery.core.network.Result.Success -> {
                    showLoadVehicleBottomSheet = false
                    // Firestore will automatically update isloaded to 1, which will trigger UI update
                    snackbarScope.launch {
                        snackbarHostState.showSnackbar("Vehicle loaded successfully")
                    }
                }

                is com.platform.platformdelivery.core.network.Result.Error -> {
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
                    // Don't call route details API - data is streaming from Firestore
                    // Firestore will automatically update the UI when delivery status changes
                    android.util.Log.d(
                        "RouteDetailsScreen",
                        "Delivery update successful. Firestore will update automatically."
                    )
                }

                is com.platform.platformdelivery.core.network.Result.Error -> {
                    // Handle error - could show a snackbar
                    android.util.Log.e(
                        "RouteDetailsScreen",
                        "Failed to update delivery: ${(deliveryUpdateResult as Result.Error).message}"
                    )
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
                add(
                    StopInfo(
                        waypoint.place?.toString() ?: "",
                        index + 1,
                        note,
                        waypoint,
                        waypointId
                    )
                )
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp)
        ) {
            allStops.forEachIndexed { index, stopInfo ->
                val isFirst = index == 0 // Origin
                val isLast = index == allStops.size - 1
                val isWaypoint = !isFirst && !isLast

                // Check if this waypoint is the active/current waypoint
                // currentWaypoint might be an index (0-based) or a waypoint ID
                // Only show waypoint buttons if vehicle is loaded (isloaded == 1)
                // Button visibility is controlled by shouldShowWaypointButtons which is based on Firestore isloaded
                val waypointIndex =
                    if (isWaypoint) index - 1 else null // Subtract 1 because index 0 is origin
                val isActiveWaypoint =
                    if (isWaypoint && currentWaypointId != null && shouldShowWaypointButtons) {
                        // Try matching by waypoint ID first
                        val matchesById =
                            stopInfo.waypointId != null && stopInfo.waypointId == currentWaypointId
                        // Or match by index (currentWaypoint might be 0-based index)
                        val matchesByIndex =
                            waypointIndex != null && waypointIndex == currentWaypointId
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
                    isVehicleLoaded = shouldShowWaypointButtons, // Use Firestore-based flag
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
                            selectedWaypointId = stopInfo.waypointId.toString()
                            showDeliveryOptionsBottomSheet = true
                        }
                    },
                    onFailedClick = {
                        if (routeId.isNotEmpty() && stopInfo.waypointId != null) {
                            // Navigate to failed delivery screen
                            navController?.navigate("failedDelivery/$routeId/${stopInfo.waypointId}")
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
                containerColor = if (snackbarData.visuals.message.contains(
                        "Failed",
                        ignoreCase = true
                    )
                ) {
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

    // Delivery Options bottom sheet
    if (showDeliveryOptionsBottomSheet && selectedWaypointId != null) {
        DeliveryOptionsBottomSheet(
            onDismiss = {
                showDeliveryOptionsBottomSheet = false
                selectedWaypointId = null
            },
            onOptionSelected = { deliveryType ->
                if (routeId.isNotEmpty() && selectedWaypointId != null) {
                    // Get the delivery option text from the delivery type
                    val deliveryOptionText = when (deliveryType) {
                        "recipient" -> "Deliver to recipient"
                        "third_party" -> "Deliver to third party"
                        "mailbox" -> "Left in mailbox"
                        "safe_place" -> "Left in safe place"
                        "other" -> "Other"
                        else -> "Other"
                    }

                    // URL encode the delivery option text to handle spaces
                    val encodedText = java.net.URLEncoder.encode(deliveryOptionText, "UTF-8")

                    // Navigate to success delivery screen
                    navController?.navigate("successDelivery/$routeId/${selectedWaypointId}/$encodedText") {
                        popUpTo("routeDetails/$routeId") { inclusive = false }
                    }

                    showDeliveryOptionsBottomSheet = false
                    selectedWaypointId = null
                }
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
    val isUpdatingDelivery by routesViewModel?.isUpdatingDelivery?.collectAsState()
        ?: remember { mutableStateOf(false) }
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
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.12f
                                ),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.38f
                                )
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
                                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.12f
                                    ),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.38f
                                    )
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
                                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.12f
                                    ),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.38f
                                    )
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
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.12f
                                ),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.38f
                                )
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
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.12f
                                ),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.38f
                                )
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
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // App icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                mapApp.icon?.let { drawable ->
                                    val bitmap = drawable.toBitmap(40, 40)
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = mapApp.name,
                                        modifier = Modifier.size(40.dp)
                                    )
                                } ?: run {
                                    // Fallback icon if drawable is null
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = mapApp.name,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = mapApp.name,
                                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Data class for delivery option with icon
 */
data class DeliveryOption(
    val text: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val deliveryType: String
)

/**
 * Bottom sheet for selecting delivery options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryOptionsBottomSheet(
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val deliveryOptions = remember {
        listOf(
            DeliveryOption(
                text = "Deliver to recipient",
                icon = Icons.Default.Person,
                deliveryType = "recipient"
            ),
            DeliveryOption(
                text = "Deliver to third party",
                icon = Icons.Default.Group,
                deliveryType = "third_party"
            ),
            DeliveryOption(
                text = "Left in mailbox",
                icon = Icons.Default.Mail,
                deliveryType = "mailbox"
            ),
            DeliveryOption(
                text = "Left in safe place",
                icon = Icons.Default.Lock,
                deliveryType = "safe_place"
            ),
            DeliveryOption(
                text = "Other",
                icon = Icons.Default.MoreHoriz,
                deliveryType = "other"
            )
        )
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
                text = "Select Delivery Option",
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 20.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            deliveryOptions.forEach { option ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            onOptionSelected(option.deliveryType)
                            onDismiss()
                        }
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = option.text,
                            style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
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
    val packageName: String?,
    val icon: Drawable?
)

/**
 * Get list of installed map applications
 */
fun getAvailableMapApps(context: android.content.Context): List<MapAppInfo> {
    val mapApps = mutableListOf<MapAppInfo>()
    val pm = context.packageManager

    // Google Maps
    try {
        val packageInfo = pm.getPackageInfo("com.google.android.apps.maps", 0)
        val icon = pm.getApplicationIcon("com.google.android.apps.maps")
        mapApps.add(MapAppInfo("Google Maps", "com.google.android.apps.maps", icon))
    } catch (e: PackageManager.NameNotFoundException) {
        // Not installed
    }

    // Waze
    try {
        val packageInfo = pm.getPackageInfo("com.waze", 0)
        val icon = pm.getApplicationIcon("com.waze")
        mapApps.add(MapAppInfo("Waze", "com.waze", icon))
    } catch (e: PackageManager.NameNotFoundException) {
        // Not installed
    }

    // Apple Maps (via web) - use Navigation icon
    val appleMapsIcon = try {
        ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
    } catch (e: Exception) {
        null
    }
    mapApps.add(MapAppInfo("Apple Maps", null, appleMapsIcon))

    // Default browser (for web-based maps) - use Web icon
    val browserIcon = try {
        ContextCompat.getDrawable(context, android.R.drawable.ic_menu_search)
    } catch (e: Exception) {
        null
    }
    mapApps.add(MapAppInfo("Browser", "default", browserIcon))

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
                    .padding(bottom = 4.dp),
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
                            var location =
                                suspendCancellableCoroutine<android.location.Location?> { continuation ->
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
                                    val dateFormat =
                                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
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
                            android.util.Log.e(
                                "LoadVehicleBottomSheet",
                                "Error getting location: ${e.message}"
                            )
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