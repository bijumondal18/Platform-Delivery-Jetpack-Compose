package com.platform.platformdelivery.presentation.pages.my_accepted_routes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import com.platform.platformdelivery.presentation.widgets.DatePickerBox
import com.platform.platformdelivery.presentation.widgets.ModernCancelRouteDialog
import com.platform.platformdelivery.presentation.widgets.RouteItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyAcceptedRoutesScreen(
    modifier: Modifier = Modifier,
    routesViewModel: RoutesViewModel = viewModel(),
    navController: NavController
) {

    // ✅ collect states from ViewModel
    val acceptedTrips by routesViewModel.acceptedTrips.collectAsState()
    val isLoading by routesViewModel.isAcceptedTripsLoading.collectAsState()
    val isEmpty by routesViewModel.acceptedTripsEmpty.collectAsState()
    val noMoreData by routesViewModel.noMoreAcceptedTripsAvailable.collectAsState()
    val error by routesViewModel.acceptedTripsError.collectAsState()
    val isCancellingRoute by routesViewModel.isCancellingRoute.collectAsState()
    val cancelRouteResult by routesViewModel.cancelRouteResult.collectAsState()

    // Show all accepted trips (no filtering by status)
    val displayedTrips = remember(acceptedTrips) {
        acceptedTrips
    }

    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    var pickedDate by remember { mutableStateOf<String?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var routeToCancel by remember { mutableStateOf<com.platform.platformdelivery.data.models.Route?>(null) }

    // ✅ Format current date
    val currentDate = remember {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MM/dd", Locale.getDefault())
        today.format(formatter)
    }

    LaunchedEffect(Unit) {
        // Reset flag and reload when screen is navigated to
        routesViewModel.resetAcceptedTripsFlag()
        routesViewModel.getAcceptedTrips(
            1,
            date = pickedDate ?: LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        )
    }

    // Handle cancel route result
    LaunchedEffect(cancelRouteResult) {
        when (cancelRouteResult) {
            is Result.Success -> {
                // Route cancelled successfully, list will be refreshed automatically
                routeToCancel = null
            }
            is Result.Error -> {
                // Handle error - could show a snackbar or toast
                android.util.Log.e("MyAcceptedRoutes", "Failed to cancel route: ${(cancelRouteResult as Result.Error).message}")
            }
            else -> Unit
        }
    }

    // Cancel Route Confirmation Dialog
    if (showCancelDialog && routeToCancel != null) {
        ModernCancelRouteDialog(
            onDismiss = {
                showCancelDialog = false
                routeToCancel = null
            },
            onConfirm = {
                routeToCancel?.id?.toString()?.let { routeId ->
                    routesViewModel.cancelRoute(routeId) {
                        // Route will be removed from list automatically
                    }
                }
                showCancelDialog = false
            }
        )
    }

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                delay(1000)
                routesViewModel.getAcceptedTrips(
                    1,
                    date = pickedDate ?: LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
                isRefreshing = false
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                DatePickerBox(
                    initialDate = currentDate,
                    onDateSelected = { selectedDate ->
                        pickedDate = selectedDate
                        coroutineScope.launch {
                            routesViewModel.getAcceptedTrips(
                                1,
                                date = selectedDate
                            )
                        }
                    }
                )
            }

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

                !error.isNullOrEmpty() -> {
                    item {
                        Text(
                            error ?: "An error occurred",
                            style = AppTypography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                isEmpty || displayedTrips.isEmpty() -> {
                    item {
                        Text(
                            "No Accepted Routes available",
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
                    itemsIndexed(displayedTrips) { index, route ->
                        var visible by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            delay(index * 10L) // stagger effect
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut(),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            RouteItem(
                                route = route,
                                onClick = { selectedRoute ->
                                    coroutineScope.launch {
                                        // Ensure Firestore document exists before navigation
                                        routesViewModel.ensureRouteDocumentInFirestore(selectedRoute)
                                        navController.navigate("routeDetails/${selectedRoute.id}")
                                    }
                                },
                                showCancelButton = true,
                                onCancelClick = { routeToCancelRoute ->
                                    routeToCancel = routeToCancelRoute
                                    showCancelDialog = true
                                }
                            )
                        }
//                        if (index < displayedTrips.size - 1) {
//                            HorizontalDivider(
//                                modifier = Modifier.padding(horizontal = 16.dp),
//                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
//                                thickness = 1.dp
//                            )
//                        }
                    }
                    if (noMoreData) {
                        item {
                            Text(
                                "No more trips available",
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