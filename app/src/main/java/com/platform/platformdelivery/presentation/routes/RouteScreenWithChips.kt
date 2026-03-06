package com.platform.platformdelivery.presentation.routes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.presentation.pages.available_routes.AvailableRoutesScreen
import com.platform.platformdelivery.presentation.pages.my_accepted_routes.MyAcceptedRoutesScreen
import com.platform.platformdelivery.presentation.pages.my_route_history.MyRouteHistory
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoutesScreenWithTabs(
    modifier: Modifier = Modifier,
    routesViewModel: RoutesViewModel = viewModel(),
    navController: NavController
) {

    val selectedTabState by routesViewModel.selectedChip.collectAsState()
    var selectedTab by remember { mutableStateOf(selectedTabState) }

    val tabs = listOf("Available", "Accepted", "Route History")

    LaunchedEffect(selectedTabState) {
        selectedTab = selectedTabState
    }

    LaunchedEffect(selectedTab) {

        when (selectedTab) {

            "Available" -> {
                if (!routesViewModel.hasLoadedAvailableRoutes) {
                    routesViewModel.getAvailableRoutes()
                    routesViewModel.hasLoadedAvailableRoutes = true
                }
            }

            "Accepted" -> {
                if (!routesViewModel.hasLoadedAcceptedTrips) {
                    routesViewModel.getAcceptedTrips()
                    routesViewModel.hasLoadedAcceptedTrips = true
                }
            }

            "Route History" -> {
                if (!routesViewModel.hasLoadedRouteHistory) {
                    routesViewModel.getRouteHistory()
                    routesViewModel.hasLoadedRouteHistory = true
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        TabRow(
            selectedTabIndex = tabs.indexOf(selectedTab),
            containerColor = MaterialTheme.colorScheme.background,
            indicator = @androidx.compose.runtime.Composable { tabPositions ->

                val index = tabs.indexOf(selectedTab)

                TabRowDefaults.Indicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[index])
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            divider = {}
        ) {

            tabs.forEach {  title ->

                val selected = selectedTab == title

                Tab(
                    selected = selected,
                    selectedContentColor = MaterialTheme.colorScheme.onSecondary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    onClick = {
                        selectedTab = title
                        routesViewModel.setSelectedChip(title)
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            "Available" -> AvailableRoutesScreen(navController = navController)
            "Accepted" -> MyAcceptedRoutesScreen(navController = navController)
            "Route History" -> MyRouteHistory(navController = navController)
        }
    }
}