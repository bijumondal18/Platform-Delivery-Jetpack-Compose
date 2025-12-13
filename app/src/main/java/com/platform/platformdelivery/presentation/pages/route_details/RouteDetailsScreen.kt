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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.R
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.core.theme.SuccessGreen
import com.platform.platformdelivery.data.models.RequestRouteDetails
import com.platform.platformdelivery.data.models.Waypoint
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import com.platform.platformdelivery.presentation.widgets.RouteMapBox
import java.time.format.DateTimeFormatter

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
                .padding(horizontal = 16.dp)
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
                            
                            // Map at the top
                            item(key = "routeMap_$routeId") {
                                RouteMapBox(
                                    latitude = routeDetails!!.routeDetailsData?.routeData?.destinationLat ?: 0.0,
                                    longitude = routeDetails!!.routeDetailsData?.routeData?.destinationLng ?: 0.0,
                                    routeId = routeId,
                                    originLat = routeDetails!!.routeDetailsData?.routeData?.originLat,
                                    originLng = routeDetails!!.routeDetailsData?.routeData?.originLng,
                                    destinationLat = routeDetails!!.routeDetailsData?.routeData?.destinationLat,
                                    destinationLng = routeDetails!!.routeDetailsData?.routeData?.destinationLng,
                                    waypoints = routeDetails!!.routeDetailsData?.routeData?.waypoints
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
                                        val formatter = java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd", java.util.Locale.ENGLISH)
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
                                            val totalStops = (1 + (route?.waypoints?.size ?: 0) + if (route?.destinationPlace.isNullOrEmpty()) 0 else 1)
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
                                        
                                        // Edit route button with menu
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(onClick = { /* Edit route */ }) {
                                                Text(
                                                    text = "Edit route",
                                                    style = AppTypography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(onClick = { /* Menu */ }) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "More options",
                                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                                )
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
                                RouteStopsList(
                                    originPlace = route?.originPlace ?: "",
                                    destinationPlace = route?.destinationPlace ?: "",
                                    waypoints = route?.waypoints,
                                    routeStartTime = route?.startTime
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

@Composable
fun RouteStopsList(
    originPlace: String,
    destinationPlace: String,
    waypoints: List<Waypoint>?,
    routeStartTime: String? = null
) {
    // Sort waypoints once and memoize
    val sortedWaypoints = remember(waypoints) {
        waypoints?.sortedBy { waypoint ->
            (waypoint.optimizedOrder as? Number)?.toInt() ?: 0
        } ?: emptyList()
    }
    
    val hasWaypoints = sortedWaypoints.isNotEmpty()
    val hasDestination = destinationPlace.isNotEmpty()
    
    // Build complete list of stops
    val allStops = remember(originPlace, sortedWaypoints, destinationPlace) {
        buildList {
            add(Pair(originPlace, 0)) // Origin is stop 0
            sortedWaypoints.forEachIndexed { index, waypoint ->
                add(Pair(waypoint.place?.toString() ?: "", index + 1))
            }
            if (hasDestination) {
                add(Pair(destinationPlace, sortedWaypoints.size + 1))
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        allStops.forEachIndexed { index, (place, stopNumber) ->
            val isFirst = index == 0
            val isLast = index == allStops.size - 1
            
            RouteStopItem(
                place = place,
                stopNumber = stopNumber + 1, // Display as 01, 02, etc.
                scheduledTime = calculateScheduledTime(routeStartTime, index),
                isFirst = isFirst,
                isLast = isLast
            )
            
            if (!isLast) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Helper function to format date like "Mon Dec 17"
fun formatRouteDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val inputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd", java.util.Locale.ENGLISH)
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
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Stop number with vertical line (like in image)
        Column(
            modifier = Modifier.width(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stop number badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", stopNumber),
                    style = AppTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
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
            // Address
            Text(
                text = place,
                style = AppTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2
            )
            
            // Scheduled time
            if (scheduledTime.isNotEmpty()) {
                Text(
                    text = scheduledTime,
                    style = AppTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            
            // Action buttons for first stop (Start and Done)
            if (isFirst) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Start route */ },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Start",
                            style = AppTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    
                    Button(
                        onClick = { /* Mark done */ },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Done",
                            style = AppTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}