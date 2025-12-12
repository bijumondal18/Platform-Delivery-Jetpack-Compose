package com.platform.platformdelivery.presentation.pages.available_routes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.platform.platformdelivery.R
import com.platform.platformdelivery.app.MainActivity
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.core.utils.GeocoderUtils
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import com.platform.platformdelivery.presentation.widgets.AppTextField
import com.platform.platformdelivery.presentation.widgets.DatePickerBox
import com.platform.platformdelivery.presentation.widgets.RouteItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AvailableRoutesScreen(
    routesViewModel: RoutesViewModel = viewModel(),
    navController: NavController
) {

    val context = LocalContext.current
    val activity = context as? MainActivity

    // ✅ collect states from ViewModel
    val routes by routesViewModel.routes.collectAsState()
    val isLoading by routesViewModel.isLoading.collectAsState()
    val isEmpty by routesViewModel.isEmpty.collectAsState()
    val noMoreData by routesViewModel.noMoreDataAvailable.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    var pickedDate by remember { mutableStateOf<String?>(null) }

    // ✅ Format current date
    val currentDate = remember {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MM/dd", Locale.getDefault())
        today.format(formatter)
    }

    var zipCode by remember { mutableStateOf("") }
    var isFetchingLocation by remember { mutableStateOf(false) }
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }


    LaunchedEffect(Unit) {
        routesViewModel.loadAvailableRoutesOnce(date = pickedDate ?: LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }


    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    delay(1000)
                    routesViewModel.getAvailableRoutes(
                        1,
                        date = pickedDate ?: LocalDate.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    )
                    isRefreshing = false // ✅ stop indicator when refresh completes
                }
            },
        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppTextField(
                        value = zipCode,
                        onValueChange = { zipCode = it },
                        label = "Zip Code",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isFetchingLocation = true
                                try {
                                    // Check location permission
                                    val hasLocationPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED ||
                                            ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            ) == PackageManager.PERMISSION_GRANTED

                                    if (!hasLocationPermission) {
                                        snackbarHostState.showSnackbar("Location permission is required")
                                        isFetchingLocation = false
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
                                                continuation.resume(null) // Return null on failure instead of exception
                                            }
                                        }
                                    }
                                    
                                    if (location != null) {
                                        // Reverse geocode to get zip code
                                        val zip = GeocoderUtils.getZipCodeFromLocation(
                                            context,
                                            location.latitude,
                                            location.longitude
                                        )
                                        
                                        if (zip != null) {
                                            zipCode = zip
                                            snackbarHostState.showSnackbar("Zip code updated: $zip")
                                        } else {
                                            snackbarHostState.showSnackbar("Could not find zip code for this location")
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar("Unable to get current location. Please try again.")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                } finally {
                                    isFetchingLocation = false
                                }
                            }
                        }
                    ) {
                        if (isFetchingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_gps),
                                contentDescription = "Get current location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .weight(0.6f)
                                    .size(34.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))


                Text(
                    "Choose Delivery Radius (Mi)",
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(16.dp))

                StepSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    initialIndex = 0,
                    stepValues = listOf(0, 10, 20, 30, 40, 50),
                ) {

                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        "None",
                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "50",
                        style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(Modifier.height(32.dp))
            }

            item {

                DatePickerBox(
                    initialDate = currentDate,
                    onDateSelected = { selectedDate ->
                        pickedDate = selectedDate
                        coroutineScope.launch {
                            routesViewModel.getAvailableRoutes(
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

                isEmpty -> {
                    item {
                        Text(
                            "No routes available", style = AppTypography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                else -> {
                    itemsIndexed(routes) { index, route ->
                        var visible by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            delay(index * 10L) // stagger effect
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut()
                        ) {
                            RouteItem(route) { selectedRoute ->
                                coroutineScope.launch {
                                    navController.navigate("routeDetails/${selectedRoute.id}")
                                }
                            }
                        }
                    }
                    if (noMoreData) {
                        item {
                            Text(
                                "No more routes available",
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
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}