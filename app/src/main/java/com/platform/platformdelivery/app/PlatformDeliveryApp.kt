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
import com.platform.platformdelivery.presentation.pages.auth.login.LoginScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlatformDeliveryApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val isLoggedIn = remember { mutableStateOf(false) }

    val startDestination = if (isLoggedIn.value) "main" else "login"

    AppTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("login") {
                LoginScreen(navController)
            }
            composable("main") {
                MainDrawerScreen() // Your drawer with Home, Profile, etc.
            }
        }
//        MainDrawerScreen()
    }
}