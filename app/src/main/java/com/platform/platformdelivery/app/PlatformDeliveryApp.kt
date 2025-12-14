package com.platform.platformdelivery.app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
    val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    val appPrefs = remember { TokenManager(context) }

    // Check permissions when screen becomes visible
    LaunchedEffect(Unit) {
        onCheckPermissions()
    }

    // Check if all permissions are granted
    val allPermissionsGranted = PermissionUtils.hasAllRequiredPermissions(context)

    AppTheme {
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
                        }
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
