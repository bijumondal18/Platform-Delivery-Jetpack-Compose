package com.platform.platformdelivery.presentation.pages.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.presentation.pages.available_routes.AvailableRoutesScreen
import com.platform.platformdelivery.presentation.pages.contact_admin.ContactAdminScreen
import com.platform.platformdelivery.presentation.pages.home.HomeScreen
import com.platform.platformdelivery.presentation.pages.my_accepted_routes.MyAcceptedRoutesScreen
import com.platform.platformdelivery.presentation.pages.my_earnings.MyEarningsScreen
import com.platform.platformdelivery.presentation.pages.my_route_history.MyRouteHistory
import com.platform.platformdelivery.presentation.pages.profile.ProfileScreen
import kotlinx.coroutines.launch


object DrawerDestinations {
    const val Home = "home"
    const val Profile = "profile"
    const val AvailableRoutes = "available_routes"
    const val RouteHistory = "route_history"
    const val MyAcceptedRoutes = "my_accepted_routes"
    const val MyEarnings = "my_earnings"
    const val ContactAdmin = "contact_admin"
}

fun getTitleForRoute(route: String?): String {
    return when (route) {
        DrawerDestinations.Home -> "Home"
        DrawerDestinations.Profile -> "Profile"
        DrawerDestinations.AvailableRoutes -> "Available Routes"
        DrawerDestinations.RouteHistory -> "My Route History"
        DrawerDestinations.MyAcceptedRoutes -> "My Accepted Routes"
        DrawerDestinations.MyEarnings -> "My Earnings"
        DrawerDestinations.ContactAdmin -> "Contact Admin"
        else -> "Platform Delivery"
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDrawerScreen(modifier: Modifier = Modifier) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?: DrawerDestinations.Home

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                selectedItem = currentRoute,
                onItemClick = { route ->
                    scope.launch { drawerState.close() }
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(DrawerDestinations.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),

            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            getTitleForRoute(currentRoute),
                            style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = DrawerDestinations.Home,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(DrawerDestinations.Home) { HomeScreen() }
                composable(DrawerDestinations.Profile) { ProfileScreen() }
                composable(DrawerDestinations.AvailableRoutes) { AvailableRoutesScreen() }
                composable(DrawerDestinations.RouteHistory) { MyRouteHistory() }
                composable(DrawerDestinations.MyAcceptedRoutes) { MyAcceptedRoutesScreen() }
                composable(DrawerDestinations.MyEarnings) { MyEarningsScreen() }
                composable(DrawerDestinations.ContactAdmin) { ContactAdminScreen() }
            }

        }
    }

}