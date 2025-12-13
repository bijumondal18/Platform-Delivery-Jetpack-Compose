package com.platform.platformdelivery.presentation.pages.route_details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.core.theme.SuccessGreen
import com.platform.platformdelivery.data.models.RequestRouteDetails
import com.platform.platformdelivery.data.models.Waypoint
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import com.platform.platformdelivery.presentation.widgets.RouteMapBox
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
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

    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

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
        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    delay(1000)
                    if (!routeId.isNullOrEmpty()) {
                        routesViewModel.getRouteDetails(RequestRouteDetails(routeId = routeId))
                    }
                    isRefreshing = false
                }
            },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {


                when {
                    isLoading && !isRefreshing -> {
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
                            item {

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(
                                                "${routeDetails!!.routeDetailsData?.routeData?.name}",

                                                )
                                            if (!routeDetails!!.routeDetailsData?.routeData?.details.isNullOrEmpty()) {
                                                Text(
                                                    "${routeDetails!!.routeDetailsData?.routeData?.details}",
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = if (routeDetails!!.routeDetailsData?.routeData?.status == "completed") SuccessGreen.copy(
                                                        alpha = 0.5f
                                                    ) else MaterialTheme.colorScheme.surfaceContainer,
                                                    shape = MaterialTheme.shapes.small
                                                )
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                "${routeDetails!!.routeDetailsData?.routeData?.status}",
                                                style = AppTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                                color = SuccessGreen
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Route stops card with origin, waypoints, and destination
                                RouteStopsCard(
                                    originPlace = routeDetails!!.routeDetailsData?.routeData?.originPlace ?: "",
                                    destinationPlace = routeDetails!!.routeDetailsData?.routeData?.destinationPlace ?: "",
                                    waypoints = routeDetails!!.routeDetailsData?.routeData?.waypoints
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                            }

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
}

@Composable
fun RouteStopsCard(
    originPlace: String,
    destinationPlace: String,
    waypoints: List<Waypoint>?
) {
    // Sort waypoints once and memoize
    val sortedWaypoints = remember(waypoints) {
        waypoints?.sortedBy { waypoint ->
            (waypoint.optimizedOrder as? Number)?.toInt() ?: 0
        } ?: emptyList()
    }
    
    val hasWaypoints = sortedWaypoints.isNotEmpty()
    val hasDestination = destinationPlace.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Origin
            RouteStopItem(
                place = originPlace,
                isOrigin = true,
                isLast = !hasWaypoints && !hasDestination
            )

            // Waypoints - use items() for better performance with many items
            sortedWaypoints.forEachIndexed { index, waypoint ->
                RouteStopItem(
                    place = waypoint.place?.toString() ?: "",
                    isWaypoint = true,
                    waypointNumber = index + 1,
                    isLast = index == sortedWaypoints.size - 1 && !hasDestination
                )
            }

            // Destination
            if (hasDestination) {
                RouteStopItem(
                    place = destinationPlace,
                    isDestination = true,
                    isLast = true
                )
            }
        }
    }
}

@Composable
fun RouteStopItem(
    place: String,
    isOrigin: Boolean = false,
    isDestination: Boolean = false,
    isWaypoint: Boolean = false,
    waypointNumber: Int = 0,
    isLast: Boolean = false
) {
    // Get theme colors (composable read)
    val colorScheme = MaterialTheme.colorScheme
    
    // Memoize expensive color calculations
    val iconBackgroundColor = remember(isOrigin, isDestination, colorScheme) {
        when {
            isOrigin -> colorScheme.primary.copy(alpha = 0.2f)
            isDestination -> SuccessGreen.copy(alpha = 0.2f)
            else -> colorScheme.secondary.copy(alpha = 0.2f)
        }
    }
    
    val iconTint = remember(isOrigin, isDestination, colorScheme) {
        when {
            isOrigin -> colorScheme.primary
            isDestination -> SuccessGreen
            else -> colorScheme.secondary
        }
    }
    
    val iconVector = remember(isOrigin, isDestination) {
        when {
            isOrigin -> Icons.Default.Place
            isDestination -> Icons.Default.LocationOn
            else -> Icons.Default.Navigation
        }
    }
    
    val labelText = remember(isWaypoint, waypointNumber, isOrigin, isDestination) {
        when {
            isWaypoint && waypointNumber > 0 -> "Stop $waypointNumber"
            isOrigin -> "Origin"
            isDestination -> "Destination"
            else -> ""
        }
    }
    
    val labelColor = remember(isOrigin, isDestination, colorScheme) {
        when {
            isOrigin -> colorScheme.primary
            isDestination -> SuccessGreen
            else -> colorScheme.primary
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Icon and connecting line column
        Column(
            modifier = Modifier.width(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = iconBackgroundColor,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Connecting line (if not last item)
            if (!isLast) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Place text
        Column(
            modifier = Modifier.weight(1f)
        ) {
            if (labelText.isNotEmpty()) {
                Text(
                    text = labelText,
                    style = AppTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = labelColor
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            Text(
                text = place,
                style = AppTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    // Divider between stops (except after last)
    if (!isLast) {
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(
            modifier = Modifier.padding(start = 52.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}