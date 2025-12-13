package com.platform.platformdelivery.presentation.pages.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.app.MainActivity
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.core.theme.SuccessGreen
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.presentation.widgets.RouteItem
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import com.platform.platformdelivery.presentation.widgets.DatePickerBox
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    routesViewModel: RoutesViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val tokenManager = remember { TokenManager(context) }
    var isOnline by remember { mutableStateOf(tokenManager.isOnline()) }

    // ✅ collect states from ViewModel
    val routes by routesViewModel.routes.collectAsState()
    val isLoading by routesViewModel.isLoading.collectAsState()
    val isEmpty by routesViewModel.isEmpty.collectAsState()
    val noMoreData by routesViewModel.noMoreDataAvailable.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }


    var pickedDate by remember { mutableStateOf<String?>(null) }


    // ✅ Format current date
    val currentDate = remember {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MM/dd", Locale.getDefault())
        today.format(formatter)
    }


    LaunchedEffect(Unit) {
        if (isOnline) {
            routesViewModel.loadAvailableRoutesOnce()
        }
    }


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
                .padding(16.dp)
        ) {

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Offline",
                        style = AppTypography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = isOnline,
                        onCheckedChange = { status ->
                            if (status) {
                                // User wants Online
                                activity?.requestOrStartLocationService()
                                tokenManager.saveOnlineStatus(true)
                                isOnline = true
                                coroutineScope.launch { routesViewModel.getAvailableRoutes(1) }
                            } else {
                                // User goes Offline
                                activity?.stopLocationService()
                                tokenManager.saveOnlineStatus(false)
                                isOnline = false
                            }

                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SuccessGreen,
                            checkedTrackColor = SuccessGreen.copy(alpha = 0.5f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.error,
                            uncheckedTrackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Online",
                        style = AppTypography.bodyLarge,
                        color = SuccessGreen
                    )
                }
                Spacer(Modifier.height(16.dp))

            }


            if (isOnline) {
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
                                RouteItem(
                                    route = route,
                                    onClick = { selectedRoute ->
                                        coroutineScope.launch {
                                            navController.navigate("routeDetails/${selectedRoute.id}")
                                        }
                                    }
                                )
                            }
                            if (index < routes.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    thickness = 1.dp
                                )
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
            } else {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Go Online to Grab a Route!",
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