package com.platform.platformdelivery.app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
//import com.platform.platformdelivery.presentation.pages.main.MainDrawerScreen
import com.platform.platformdelivery.core.theme.AppTheme
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.presentation.pages.auth.forgot_password.ForgotPasswordScreen
import com.platform.platformdelivery.presentation.pages.auth.login.LoginScreen
import com.platform.platformdelivery.presentation.pages.auth.register.SignupScreen
import com.platform.platformdelivery.presentation.pages.main.MainBottomNavScreen
import com.platform.platformdelivery.presentation.pages.notifications.NotificationScreen
import com.platform.platformdelivery.presentation.pages.route_details.RouteDetailsScreen
import com.platform.platformdelivery.presentation.view_models.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlatformDeliveryApp(startDestination: String) {
    val navController = rememberNavController()

    // Provide your shared AuthViewModel here
    val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val context = LocalContext.current
    val appPrefs = remember { TokenManager(context) }


    AppTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    viewModel = authViewModel
                )
            }
            composable("forgot_password") {
                ForgotPasswordScreen(navController)
            }
            composable("signup") {
                SignupScreen(navController)
            }
            composable("main") {
                MainBottomNavScreen(
                    rootNavController = navController,
                    onLogout = {
                        appPrefs.clear()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                ) // Your drawer with Home, Profile, Available Routes, Route History Accepted Routes, Earnings etc.
            }
            composable("routeDetails/{routeId}") { backStackEntry ->
                val routeId = backStackEntry.arguments?.getString("routeId")
                RouteDetailsScreen(
                    routeId = routeId,
                    navController = navController
                )
            }
            composable("notifications") {
                NotificationScreen(navController = navController)
            }
        }
    }
}