package com.platform.platformdelivery.presentation.pages.my_earnings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.presentation.pages.my_earnings.widgets.CurrentDueCard
import com.platform.platformdelivery.presentation.pages.my_earnings.widgets.LastPayoutCard
import com.platform.platformdelivery.presentation.pages.my_earnings.widgets.LifetimeEarningsCard
import com.platform.platformdelivery.presentation.pages.my_earnings.widgets.RouteAndTimeCard
import com.platform.platformdelivery.presentation.view_models.EarningViewModel
import com.platform.platformdelivery.presentation.widgets.PrimaryButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

    var selectedTab by remember { mutableStateOf("Expenses") }
    var selectedPeriod by remember { mutableStateOf("Month") }

    val chartData = listOf(
        ChartSlice("Translations", 18f, Color(0xFFB277FF)),
        ChartSlice("Taxes",        12f, Color(0xFF7EA6FF)),
        ChartSlice("Supermarkets", 28f, Color(0xFF7B5CFF)),
        ChartSlice("Restaurants",  42f, Color(0xFFFF8EC3)),
    )


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
                .padding(vertical = 16.dp)
        ) {

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
                            "new",
                            "new1",
                            "new2",
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
                                    "new" -> {
                                        // Tabs (Expenses / Income)
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .clip(MaterialTheme.shapes.large),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            listOf("Expenses", "Income").forEach { tab ->
                                                val selected = tab == selectedTab
                                                TextButton(
                                                    onClick = { selectedTab = tab },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(horizontal = 8.dp)
                                                        .clip(MaterialTheme.shapes.large)
                                                        .background(
                                                            if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(
                                                                alpha = 0.1f
                                                            )
                                                        )
                                                ) {
                                                    Text(
                                                        text = tab,
                                                        color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    "new1" -> {
                                        // Time Filters (Day / Week / Month / 6 month)
                                        LazyRow(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            listOf(
                                                "Day",
                                                "Week",
                                                "Month",
                                                "6 month",
                                                "All time",
                                            ).forEach { period ->
                                                val selected = period == selectedPeriod
                                                item {
                                                    FilterChip(
                                                        modifier = Modifier.padding(end = 12.dp),
                                                        selected = selected,
                                                        onClick = { selectedPeriod = period },
                                                        shape = MaterialTheme.shapes.extraExtraLarge,
                                                        label = {
                                                            Text(
                                                                text = period,
                                                                color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                                                                style = MaterialTheme.typography.labelLarge
                                                            )
                                                        },
                                                        colors = FilterChipDefaults.filterChipColors(
                                                            containerColor = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(
                                                                alpha = 0.1f
                                                            ),
                                                            selectedContainerColor = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(
                                                                alpha = 0.1f
                                                            ),
                                                            selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                                                            labelColor = MaterialTheme.colorScheme.onBackground.copy(
                                                                alpha = 0.5f
                                                            )
                                                        )
                                                    )
                                                }

                                            }
                                        }
                                    }
                                    "new2" ->{
                                        Box(
                                            modifier = Modifier.fillMaxWidth()
                                                .size(220.dp)
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            DonutChartWithCenterText(  data = chartData,
                                                total = "${earningDetails?.earning?.earnings}",
                                                ringWidth = 24.dp,
                                                gapDegrees = 4f,
                                                showOuterRim = true,        // ← this gives you the multi-color outside ring
                                                outerRimOffset = 4.dp,
                                                outerRimWidth = 6.dp)
                                        }
                                    }
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


data class ChartSlice(val label: String, val value: Float, val color: Color)

/**
 * Draws a multi-segment donut with an optional colorful outside rim (like your screenshot).
 */
@Composable
fun DonutChart(
    data: List<ChartSlice>,
    modifier: Modifier = Modifier.size(220.dp),
    ringWidth: Dp = 16.dp,
    gapDegrees: Float = 3f,           // small gap between slices
    showOuterRim: Boolean = true,
    outerRimOffset: Dp = 4.dp,        // how far outside the main ring
    outerRimWidth: Dp = 6.dp          // thickness of the outer colorful rim
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    val gapCount = data.count { it.value > 0f }.coerceAtLeast(0)
    val totalGap = gapDegrees * gapCount
    val usable = 360f - totalGap

    Canvas(modifier = modifier) {
        val ringPx = ringWidth.toPx()
        val rimPx = outerRimWidth.toPx()
        val offsetPx = outerRimOffset.toPx()

        // square bounds
        val sizeMin = min(size.width, size.height)
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = sizeMin / 2f

        // main donut bounds (stroke draws centered on the rect edge, so inset by stroke/2)
        val ringRect = Rect(
            center = center,
            radius = radius - ringPx / 2f - offsetPx // leave room for the outer rim
        )

        // Optional: a dark track under the donut
        drawArc(
            color = Color(0xFF1E1E1E),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = ringPx, cap = StrokeCap.Round),
            topLeft = ringRect.topLeft,
            size = ringRect.size
        )

        // Draw outer colorful rim first (so it peeks outside)
        if (showOuterRim) {
            val rimRect = Rect(
                center = center,
                radius = radius - rimPx / 2f
            )
            var start = -90f
            data.filter { it.value > 0f }.forEachIndexed { idx, slice ->
                val sweep = (slice.value / total) * usable
                drawArc(
                    color = slice.color.copy(alpha = 0.9f),
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = rimPx, cap = StrokeCap.Round),
                    topLeft = rimRect.topLeft,
                    size = rimRect.size
                )
                start += sweep + gapDegrees
            }
        }

        // Draw the main donut segments
        var start = -90f
        data.filter { it.value > 0f }.forEach { slice ->
            val sweep = (slice.value / total) * usable
            drawArc(
                color = slice.color,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = ringPx, cap = StrokeCap.Round),
                topLeft = ringRect.topLeft,
                size = ringRect.size
            )
            start += sweep + gapDegrees
        }
    }
}

@Composable
fun DonutChartWithCenterText(
    data: List<ChartSlice>,
    total: String,
    modifier: Modifier = Modifier.size(220.dp),
    ringWidth: Dp = 22.dp,
    gapDegrees: Float = 3f,
    showOuterRim: Boolean = true,
    outerRimOffset: Dp = 4.dp,
    outerRimWidth: Dp = 6.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        DonutChart(
            data = data,
            ringWidth = ringWidth,
            gapDegrees = gapDegrees,
            showOuterRim = showOuterRim,
            outerRimOffset = outerRimOffset,
            outerRimWidth = outerRimWidth
        )

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "$${total}",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}