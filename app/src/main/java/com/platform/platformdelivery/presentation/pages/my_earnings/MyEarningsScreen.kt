package com.platform.platformdelivery.presentation.pages.my_earnings

import android.widget.Space
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import com.platform.platformdelivery.R
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.data.models.RequestRouteDetails
import com.platform.platformdelivery.presentation.pages.my_earnings.widgets.CurrentDueCard
import com.platform.platformdelivery.presentation.pages.my_earnings.widgets.EarningsShimmerLoader
import com.platform.platformdelivery.presentation.pages.my_earnings.widgets.LastPayoutCard
import com.platform.platformdelivery.presentation.pages.my_earnings.widgets.LifetimeEarningsCard
import com.platform.platformdelivery.presentation.pages.my_earnings.widgets.RouteAndTimeCard
import com.platform.platformdelivery.presentation.view_models.EarningViewModel
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import com.platform.platformdelivery.presentation.widgets.PrimaryButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEarningsScreen(
    earningViewModel: EarningViewModel = viewModel(),
) {

    val earningDetails by earningViewModel.earningDetails.collectAsState()
    val isLoading by earningViewModel.isEarningDetailsLoading.collectAsState()
    val isError by earningViewModel.earningDetailsError.collectAsState()

    val coroutineScope = rememberCoroutineScope()


    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        earningViewModel.loadEarningDetailsOnce()
    }

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                delay(1000)
                earningViewModel.getEarningDetails()
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth().padding(vertical = 16.dp)
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
//                        EarningsShimmerLoader()
                    }
                }

                else -> {
                    if (isError != null) {
                        item {
                            Text(
                                "$isError",
                                style = AppTypography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    } else if (earningDetails != null) {

                        val items = listOf(
                            "lifetime",
                            "lastPayout",
                            "routeStats",
                            "dueStats",
                            "instantPayout"
                        )

                        itemsIndexed(items) { index, type ->

                            var visible by remember { mutableStateOf(false) }

                            LaunchedEffect(Unit) {
                                delay(index * 10L) // stagger effect between each card
                                visible = true
                            }

                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                                exit = fadeOut()
                            ) {
                                when (type) {
                                    "lifetime" -> LifetimeEarningsCard(earningDetails?.earning)
                                    "lastPayout" -> LastPayoutCard(earningDetails?.earning)
                                    "routeStats" -> RouteAndTimeCard(earningDetails?.earning)
                                    "dueStats" -> CurrentDueCard(earningDetails?.earning)
                                    "instantPayout" -> PrimaryButton(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "Enable Instant Payout",
                                        onClick = {}
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }

        }
    }
}