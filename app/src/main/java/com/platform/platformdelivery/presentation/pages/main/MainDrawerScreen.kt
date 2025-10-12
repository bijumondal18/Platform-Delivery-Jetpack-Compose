package com.platform.platformdelivery.presentation.pages.main

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.platform.platformdelivery.R
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.data.models.RouteDetails
import com.platform.platformdelivery.presentation.pages.available_routes.AvailableRoutesScreen
import com.platform.platformdelivery.presentation.pages.contact_admin.ContactAdminScreen
import com.platform.platformdelivery.presentation.pages.home.HomeScreen
import com.platform.platformdelivery.presentation.pages.my_accepted_routes.MyAcceptedRoutesScreen
import com.platform.platformdelivery.presentation.pages.my_earnings.MyEarningsScreen
import com.platform.platformdelivery.presentation.pages.my_route_history.MyRouteHistory
import com.platform.platformdelivery.presentation.pages.profile.ProfileScreen
import com.platform.platformdelivery.presentation.pages.route_details.RouteDetailsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: Painter
)

object DrawerDestinations {
    const val Home = "home"
    const val Profile = "profile"
    const val AvailableRoutes = "available_routes"
    const val RouteHistory = "route_history"
    const val MyAcceptedRoutes = "my_accepted_routes"
    const val MyEarnings = "my_earnings"
    const val ContactAdmin = "contact_admin"
    const val Logout = "logout"
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
        else -> ""
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomNavScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: DrawerDestinations.Home

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var dynamicTitle by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val bottomNavItems = listOf(
        BottomNavItem(
            DrawerDestinations.Home,
            "Home",
            icon = painterResource(id = R.drawable.ic_home)
        ),
        BottomNavItem(
            DrawerDestinations.RouteHistory,
            "Routes",
            icon = painterResource(id = R.drawable.ic_available_routes)
        ),
        BottomNavItem(
            DrawerDestinations.MyEarnings,
            "My Earnings",
            icon = painterResource(id = R.drawable.ic_my_earnings)
        ),
        BottomNavItem(
            DrawerDestinations.Profile,
            "Profile",
            icon = painterResource(id = R.drawable.ic_profile)
        ),
    )

    // Handle system back button → show exit confirmation only on Home
    BackHandler(enabled = currentRoute == DrawerDestinations.Home) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            shape = MaterialTheme.shapes.large,
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    "Exit App",
                    style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    "Are you sure you want to close the app?",
                    style = AppTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    ActivityCompat.finishAffinity(context as android.app.Activity)
                }) {
                    Text("Exit", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.tertiary)
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            shape = MaterialTheme.shapes.large,
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "Confirm Logout",
                    style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    "Are you sure you want to logout from this device?",
                    style = AppTypography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.tertiary)
                }
            }
        )
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                title = {
                    Text(
                        dynamicTitle ?: getTitleForRoute(currentRoute),
                        style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
            ) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentBackStackEntry?.destination?.hierarchy?.any {
                            it.route == item.route
                        } == true,
                        onClick = {
                            if (item.route != currentRoute) {
                                navController.navigate(item.route) {
                                    popUpTo(DrawerDestinations.Home) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        )) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = DrawerDestinations.Home
            ) {
                composable(DrawerDestinations.Home) { HomeScreen(navController = navController) }
                composable(DrawerDestinations.Profile) { ProfileScreen() }
                composable(DrawerDestinations.AvailableRoutes) { AvailableRoutesScreen(navController = navController) }
                composable(DrawerDestinations.RouteHistory) { MyRouteHistory(navController = navController) }
                composable(DrawerDestinations.MyAcceptedRoutes) { MyAcceptedRoutesScreen() }
                composable(DrawerDestinations.MyEarnings) { MyEarningsScreen() }
                composable(DrawerDestinations.ContactAdmin) { ContactAdminScreen() }
                composable("routeDetails/{routeId}") { backStackEntry ->
                    val routeId = backStackEntry.arguments?.getString("routeId")
                    RouteDetailsScreen(
                        routeId = routeId,
                        onTitleChange = { title -> dynamicTitle = title }
                    )
                }
            }
        }
    }

    LaunchedEffect(currentRoute) {
        if (!currentRoute.startsWith("routeDetails")) dynamicTitle = null
    }
}


/**
 * Uncomment below code for drawer setup
 * */

//@RequiresApi(Build.VERSION_CODES.O)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MainDrawerScreen(
//    onLogout: () -> Unit
//) {
//    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//    val scope = rememberCoroutineScope()
//    val navController = rememberNavController()
//
//    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
//        ?: DrawerDestinations.Home
//
//    var showLogoutDialog by remember { mutableStateOf(false) }
//
//    var dynamicTitle by remember { mutableStateOf<String?>(null) }
//
//
//    val context = LocalContext.current
//    var showExitDialog by remember { mutableStateOf(false) }
//
//    BackHandler(enabled = currentRoute == DrawerDestinations.Home) {
//        // Only show dialog when user is on Home
//        showExitDialog = true
//    }
//
//
//    if (showExitDialog) {
//        AlertDialog(
//            shape = MaterialTheme.shapes.large,
//            onDismissRequest = { showExitDialog = false },
//            title = {
//                Text(
//                    "Exit App",
//                    style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                    color = MaterialTheme.colorScheme.onBackground
//                )
//            },
//            text = {
//                Text(
//                    "Are you sure you want to close the app?",
//                    style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Normal),
//                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
//                )
//            },
//            confirmButton = {
//                TextButton(onClick = {
//                    showExitDialog = false
//                    ActivityCompat.finishAffinity((context as android.app.Activity))
//                }) {
//                    Text(
//                        "Exit",
//                        style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showExitDialog = false }) {
//                    Text(
//                        "Cancel",
//                        style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
//                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
//                    )
//                }
//            }
//        )
//    }
//
//
//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        drawerContent = {
//            DrawerContent(
//                selectedItem = currentRoute,
//                onItemClick = { route ->
//                    scope.launch { drawerState.close() }
//                    if (route != currentRoute) {
//                        navController.navigate(route) {
//                            popUpTo(DrawerDestinations.Home) { saveState = true }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                },
//                onLogout = {
//                    scope.launch {
//                        drawerState.close()
//                        delay(500)
//                    }
//                    showLogoutDialog = true
//                }
//            )
//        },
//    ) {
//        Scaffold(
//            modifier = Modifier.fillMaxSize(),
//
//            topBar = {
//                TopAppBar(
//                    title = {
//                        Text(
//                            dynamicTitle ?: getTitleForRoute(currentRoute),
//                            style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
//                            color = MaterialTheme.colorScheme.onBackground
//                        )
//                    },
//                    navigationIcon = {
//                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
//                            Icon(Icons.Default.Menu, contentDescription = "Menu")
//                        }
//                    }
//                )
//            }
//        ) { innerPadding ->
//            NavHost(
//                navController = navController,
//                startDestination = DrawerDestinations.Home,
//                modifier = Modifier.padding(innerPadding)
//            ) {
//                composable(DrawerDestinations.Home) { HomeScreen(navController = navController) }
//                composable(DrawerDestinations.Profile) { ProfileScreen() }
//                composable(DrawerDestinations.AvailableRoutes) { AvailableRoutesScreen(navController = navController) }
//                composable(DrawerDestinations.RouteHistory) { MyRouteHistory(navController = navController) }
//                composable(DrawerDestinations.MyAcceptedRoutes) { MyAcceptedRoutesScreen() }
//                composable(DrawerDestinations.MyEarnings) { MyEarningsScreen() }
//                composable(DrawerDestinations.ContactAdmin) { ContactAdminScreen() }
//                composable("routeDetails/{routeId}") { backStackEntry ->
//                    val routeId = backStackEntry.arguments?.getString("routeId")
//                    RouteDetailsScreen(
//                        routeId = routeId,
//                        onTitleChange = { title ->
//                            dynamicTitle = title
//                        })
//                }
//            }
//
//            LaunchedEffect(currentRoute) {
//                if (!currentRoute.startsWith("routeDetails")) {
//                    dynamicTitle = null
//                }
//            }
//
//        }
//    }
//
//    if (showLogoutDialog) {
//
//        androidx.compose.material3.AlertDialog(
//            shape = MaterialTheme.shapes.large,
//            onDismissRequest = { showLogoutDialog = false },
//            title = {
//                Text(
//                    "Confirm Logout",
//                    style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                    color = MaterialTheme.colorScheme.onBackground
//                )
//            },
//            text = {
//                Text(
//                    "Are you sure you want to logout from this device?",
//                    style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Normal),
//                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
//                )
//            },
//            confirmButton = {
//                androidx.compose.material3.TextButton(
//                    onClick = {
//                        showLogoutDialog = false
//                        onLogout() // 👈 trigger real logout
//                    }
//                ) {
//                    Text(
//                        "Logout",
//                        style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            },
//            dismissButton = {
//                androidx.compose.material3.TextButton(
//                    onClick = { showLogoutDialog = false }
//                ) {
//                    Text(
//                        "Cancel",
//                        style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
//                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
//                    )
//                }
//            }
//        )
//    }
//
//}