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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.core.theme.SuccessGreen
import com.platform.platformdelivery.data.models.RequestRouteDetails
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
    onTitleChange: (String) -> Unit

) {

    val routeDetails by routesViewModel.routeDetails.collectAsState()
    val isLoading by routesViewModel.isRouteDetailsLoading.collectAsState()
    val error by routesViewModel.routeDetailsError.collectAsState()

    val coroutineScope = rememberCoroutineScope()


    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }


    LaunchedEffect(routeDetails) {
        routeDetails?.routeDetailsData?.routeData?.id?.let{ id ->
            onTitleChange("Route #$id")
        }
    }

    LaunchedEffect(Unit) {
        routesViewModel.getRouteDetails(RequestRouteDetails(routeId = routeId!!))
    }


    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                delay(1000)
                routesViewModel.getRouteDetails(RequestRouteDetails(routeId = routeId!!))
                isRefreshing = false // ✅ stop indicator when refresh completes
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text("Pickup Point: ${routeDetails!!.routeDetailsData?.routeData?.originPlace}")
                                    Text("Delivery Point: ${routeDetails!!.routeDetailsData?.routeData?.destinationPlace}")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                        }

                        item(key = "routeMap_$routeId"){
                            RouteMapBox(
                                latitude = routeDetails!!.routeDetailsData?.routeData?.destinationLat!!,
                                longitude = routeDetails!!.routeDetailsData?.routeData?.destinationLng!!,
                                routeId = routeId
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