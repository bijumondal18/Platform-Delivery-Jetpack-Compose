package com.platform.platformdelivery.app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.platform.platformdelivery.presentation.pages.main.MainDrawerScreen
import com.platform.platformdelivery.core.theme.AppTheme
import com.platform.platformdelivery.presentation.pages.auth.forgot_password.ForgotPasswordScreen
import com.platform.platformdelivery.presentation.pages.auth.login.LoginScreen
import com.platform.platformdelivery.presentation.pages.auth.register.SignupScreen
import com.platform.platformdelivery.presentation.view_models.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlatformDeliveryApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val isLoggedIn = remember { mutableStateOf(false) }

    val startDestination = if (isLoggedIn.value) "main" else "login"

    // Provide your shared AuthViewModel here
    val authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()


    AppTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("login") {
                LoginScreen(
                    modifier = Modifier,
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
                MainDrawerScreen() // Your drawer with Home, Profile, etc.
            }
        }
    }
}