package com.platform.platformdelivery.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.platform.platformdelivery.presentation.pages.main.MainDrawerScreen
import com.platform.platformdelivery.core.theme.AppTheme

@Composable
fun PlatformDeliveryApp(modifier: Modifier = Modifier) {
    AppTheme {
        MainDrawerScreen()
    }
}