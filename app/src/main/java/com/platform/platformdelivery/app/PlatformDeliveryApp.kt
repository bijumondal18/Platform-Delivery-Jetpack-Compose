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
import android.view.View
import android.view.WindowInsetsController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.platform.platformdelivery.core.theme.AppTheme
import com.platform.platformdelivery.core.utils.PermissionUtils
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.presentation.pages.auth.forgot_password.ForgotPasswordScreen
import com.platform.platformdelivery.presentation.pages.auth.login.LoginScreen
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
    
    // Handle notification navigation
    LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        val routeId = activity?.intent?.getStringExtra("route_id")
        val fromNotification = activity?.intent?.getBooleanExtra("from_notification", false) ?: false
        
        if (fromNotification && !routeId.isNullOrEmpty() && startDestination == "main") {
            // Navigate to route details after a short delay to ensure app is initialized
            kotlinx.coroutines.delay(500)
            navController.navigate("routeDetails/$routeId") {
                popUpTo("main") { inclusive = false }
            }
        }
    }

    // Provide your shared AuthViewModel here
    val authViewModel: AuthViewModel = viewModel()

    val appPrefs = remember { TokenManager(context) }

    // Get system theme preference (composable function)
    val systemIsDarkTheme = isSystemInDarkTheme()

    // Theme state - check if user has set a preference, otherwise use system theme
    var isDarkTheme by remember(appPrefs, systemIsDarkTheme) {
        val savedTheme = appPrefs.isDarkTheme()
        // Use saved preference if it exists, otherwise use system theme
        mutableStateOf(
            savedTheme ?: systemIsDarkTheme
        )
    }
    
    // Function to update theme
    val updateTheme: (Boolean) -> Unit = { isDark ->
        isDarkTheme = isDark
        appPrefs.setDarkTheme(isDark)
    }

    // Check permissions when screen becomes visible
    LaunchedEffect(Unit) {
        onCheckPermissions()
    }

    // Check if all permissions are granted
    val allPermissionsGranted = PermissionUtils.hasAllRequiredPermissions(context)

    // Update status bar icon color based on theme
    SetStatusBarIconColor(isDarkTheme)

    AppTheme(darkTheme = isDarkTheme) {
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
