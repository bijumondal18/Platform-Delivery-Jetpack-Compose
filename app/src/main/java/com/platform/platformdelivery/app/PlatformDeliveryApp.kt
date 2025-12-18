package com.platform.platformdelivery.app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import android.view.WindowInsetsController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.platform.platformdelivery.core.theme.AppTheme
import com.platform.platformdelivery.core.utils.PermissionUtils
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.presentation.pages.auth.forgot_password.ForgotPasswordScreen
import com.platform.platformdelivery.presentation.pages.auth.login.LoginScreen
import com.platform.platformdelivery.presentation.pages.auth.otp_verification.OtpVerificationScreen
import com.platform.platformdelivery.presentation.pages.auth.reset_password.ResetPasswordScreen
import com.platform.platformdelivery.presentation.pages.auth.register.SignupScreen
import com.platform.platformdelivery.presentation.pages.about_us.AboutUsScreen
import com.platform.platformdelivery.presentation.pages.contact_admin.ContactAdminScreen
import com.platform.platformdelivery.presentation.pages.main.MainBottomNavScreen
import com.platform.platformdelivery.presentation.pages.notifications.NotificationScreen
import com.platform.platformdelivery.presentation.pages.permissions.PermissionScreen
import com.platform.platformdelivery.presentation.pages.privacy_policy.PrivacyPolicyScreen
import com.platform.platformdelivery.presentation.pages.profile.EditProfileScreen
import com.platform.platformdelivery.presentation.pages.refer_earn.ReferEarnScreen
import com.platform.platformdelivery.presentation.pages.route_details.RouteDetailsScreen
import com.platform.platformdelivery.presentation.pages.settings.SettingsScreen
import com.platform.platformdelivery.presentation.pages.terms_conditions.TermsConditionsScreen
import com.platform.platformdelivery.presentation.pages.tutorials.TutorialsScreen
import com.platform.platformdelivery.presentation.view_models.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlatformDeliveryApp(
    startDestination: String,
    hasLocationPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestPermissions: () -> Unit,
    onCheckPermissions: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Provide your shared AuthViewModel here
    val authViewModel: AuthViewModel = viewModel()

    val appPrefs = remember { TokenManager(context) }

    // Get system theme preference (composable function)
    val systemIsDarkTheme = isSystemInDarkTheme()

    // Get theme mode from preferences
    val initialThemeMode = remember { appPrefs.getThemeMode() }
    
    // Theme state - update when system theme changes (for SYSTEM mode)
    var currentThemeMode by remember { mutableStateOf(initialThemeMode) }
    
    // Calculate effective dark theme based on current mode
    var effectiveDarkTheme by remember(currentThemeMode, systemIsDarkTheme) {
        mutableStateOf(
            when (currentThemeMode) {
                TokenManager.ThemeMode.LIGHT -> false
                TokenManager.ThemeMode.DARK -> true
                TokenManager.ThemeMode.SYSTEM -> systemIsDarkTheme
            }
        )
    }
    
    // Watch for system theme changes when in SYSTEM mode
    LaunchedEffect(systemIsDarkTheme, currentThemeMode) {
        if (currentThemeMode == TokenManager.ThemeMode.SYSTEM) {
            effectiveDarkTheme = systemIsDarkTheme
        }
    }
    
    // Function to update theme
    val updateTheme: (TokenManager.ThemeMode) -> Unit = { mode ->
        currentThemeMode = mode
        appPrefs.setThemeMode(mode)
        effectiveDarkTheme = when (mode) {
            TokenManager.ThemeMode.LIGHT -> false
            TokenManager.ThemeMode.DARK -> true
            TokenManager.ThemeMode.SYSTEM -> systemIsDarkTheme
        }
    }

    // Check permissions when screen becomes visible
    LaunchedEffect(Unit) {
        onCheckPermissions()
    }

    // Check if all permissions are granted
    val allPermissionsGranted = PermissionUtils.hasAllRequiredPermissions(context)

    // Update status bar icon color based on theme
    SetStatusBarIconColor(effectiveDarkTheme)

    AppTheme(darkTheme = effectiveDarkTheme) {
        // Show permission screen if permissions are not granted
        if (!allPermissionsGranted) {
            PermissionScreen(
                onRequestPermissions = onRequestPermissions,
                hasLocationPermission = hasLocationPermission,
                hasNotificationPermission = hasNotificationPermission
            )
        } else {
            // Show main app content when permissions are granted
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
                composable("otp_verification/{email}/{userId}") { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    OtpVerificationScreen(
                        navController = navController,
                        email = email,
                        userId = userId
                    )
                }
                composable("reset_password/{email}") { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    ResetPasswordScreen(
                        navController = navController,
                        email = email
                    )
                }
                composable("signup") {
                    SignupScreen(navController)
                }
                composable("main") {
                    MainBottomNavScreen(
                        rootNavController = navController,
                        onLogout = {
                            // Explicitly set logged in state to false first
                            appPrefs.setIsLoggedIn(false)
                            // Clear all user data
                            appPrefs.clear()
                            // Navigate to login and clear entire back stack
                            navController.navigate("login") {
                                // Pop all destinations including the start destination
                                // This ensures login is the only screen in the stack
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                // Prevent multiple instances
                                launchSingleTop = true
                                // Don't restore previous state
                                restoreState = false
                            }
                        },
                        onThemeChange = updateTheme
                    )
                }
                composable("routeDetails/{routeId}") { backStackEntry ->
                    val routeId = backStackEntry.arguments?.getString("routeId")
                    RouteDetailsScreen(
                        routeId = routeId,
                        navController = navController
                    )
                }
                composable("failedDelivery/{routeId}/{waypointId}") { backStackEntry ->
                    val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
                    val waypointId = backStackEntry.arguments?.getString("waypointId") ?: ""
                    com.platform.platformdelivery.presentation.pages.failed_delivery.FailedDeliveryScreen(
                        routeId = routeId,
                        waypointId = waypointId,
                        navController = navController
                    )
                }
                composable("notifications") {
                    NotificationScreen(navController = navController)
                }
                composable("editProfile") {
                    EditProfileScreen(navController = navController)
                }
                composable("settings") {
                    SettingsScreen(
                        navController = navController,
                        onThemeChange = updateTheme
                    )
                }
                composable("contact_admin") {
                    ContactAdminScreen(navController = navController)
                }
                composable("refer_earn") {
                    ReferEarnScreen(navController = navController)
                }
                composable("tutorials") {
                    TutorialsScreen(navController = navController)
                }
                composable("about_us") {
                    AboutUsScreen(navController = navController)
                }
                composable("privacy_policy") {
                    PrivacyPolicyScreen(navController = navController)
                }
                composable("terms_conditions") {
                    TermsConditionsScreen(navController = navController)
                }
            }
        }
    }
}

/**
 * Sets the status bar icon color based on the theme
 * Light theme = dark icons, Dark theme = light icons (white)
 */
@Composable
fun SetStatusBarIconColor(isDarkTheme: Boolean) {
    val view = LocalView.current
    
    LaunchedEffect(isDarkTheme) {
        val window = (view.context as? android.app.Activity)?.window ?: return@LaunchedEffect
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            val insetsController = window.insetsController
            insetsController?.setSystemBarsAppearance(
                if (!isDarkTheme) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            // Android 10 and below
            @Suppress("DEPRECATION")
            WindowCompat.getInsetsController(window, view)?.apply {
                // When dark theme: light status bars (white icons)
                // When light theme: dark status bars (black icons)
                isAppearanceLightStatusBars = !isDarkTheme
            }
        }
    }
}
