package com.platform.platformdelivery.presentation.pages.profile

import androidx.compose.runtime.Composable
import com.platform.platformdelivery.presentation.widgets.ModernDeleteAccountDialog

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModernDeleteAccountDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

