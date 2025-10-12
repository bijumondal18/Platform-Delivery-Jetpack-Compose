package com.platform.platformdelivery.presentation.pages.my_route_history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.app.MainActivity
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import com.platform.platformdelivery.presentation.widgets.DatePickerBox
import com.platform.platformdelivery.presentation.widgets.RouteItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyRouteHistory(
    modifier: Modifier = Modifier,
    routesViewModel: RoutesViewModel = viewModel(),
    navController: NavController
) {

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
        routesViewModel.loadRouteHistory()
    }



    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                delay(1000)
                routesViewModel.getRouteHistory(
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

//            if (isOnline) {
                item {
                    DatePickerBox(
                        initialDate = currentDate,
                        onDateSelected = { selectedDate ->
                            pickedDate = selectedDate
                            coroutineScope.launch {
                                routesViewModel.getRouteHistory(
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                                    .align(alignment = Alignment.Center),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
//                            Text(
//                                "Loading routes...",
//                                style = AppTypography.bodyLarge,
//                                textAlign = TextAlign.Center,
//                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(16.dp)
//                            )
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
                        itemsIndexed(routes) {index, route ->

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
//            } else {
//                item {
//                    Spacer(Modifier.height(16.dp))
//                    Text(
//                        "Go Online to Grab a Route!",
//                        style = AppTypography.bodyLarge,
//                        textAlign = TextAlign.Center,
//                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp)
//                    )
//                }
//            }
        }
    }

}