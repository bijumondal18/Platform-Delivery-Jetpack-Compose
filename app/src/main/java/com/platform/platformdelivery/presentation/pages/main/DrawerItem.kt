package com.platform.platformdelivery.presentation.pages.main

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.core.theme.AppTypography


@Composable
fun DrawerItem(
    label: String,
    icon: Painter,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground,
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(start = 4.dp)
            )
        },
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        },
        modifier = modifier,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(
                alpha = 0.12f
            ),
            unselectedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
        ),
        shape = MaterialTheme.shapes.medium
    )
}