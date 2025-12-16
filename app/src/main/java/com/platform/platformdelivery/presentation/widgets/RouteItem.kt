package com.platform.platformdelivery.presentation.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.R
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.core.theme.SuccessGreen
import com.platform.platformdelivery.data.models.Route

@Composable
fun RouteItem(
    route: Route,
    onClick: (Route) -> Unit,
    showCancelButton: Boolean = false,
    onCancelClick: ((Route) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(route) }
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        // Route name row with status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Route name
            if (!route.name.isNullOrEmpty()) {
                Text(
                    text = route.name,
                    style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }

            // Status badge on the right
            if (!route.status.isNullOrEmpty()) {
                val statusColor = if (route.status.lowercase() == "completed" ||
                    route.status.lowercase() == "compleated"
                )
                    SuccessGreen
                else
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)

                Box(
                    modifier = Modifier
                        .background(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "•",
                            style = AppTypography.labelSmall,
                            color = statusColor
                        )
                        Text(
                            text = route.status.replaceFirstChar { it.uppercaseChar() },
                            style = AppTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Location row with pin icon
        val locationText = route.originPlace ?: route.destinationPlace ?: ""
        if (locationText.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            shape = MaterialTheme.shapes.medium
                        ).padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = locationText,
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Details row: START, DURATION, STOPS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // START
            DetailItem(
                iconDrawable = R.drawable.ic_clock,
                label = "START",
                value = route.startTime ?: "--"
            )

            // DURATION
            DetailItem(
                icon = Icons.Default.Refresh,
                label = "DURATION",
                value = route.estimatedTotalTime ?: "0h 0m"
            )

            // STOPS
            val totalStops = (route.waypoints?.size ?: 0) +
                    if (route.destinationPlace.isNullOrEmpty()) 0 else 1
            DetailItem(
                icon = Icons.Default.AltRoute,
                label = "STOPS",
                value = totalStops.toString()
            )
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector? = null,
    iconDrawable: Int? = null,
    label: String,
    value: String
) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                } else if (iconDrawable != null) {
                    Image(
                        painter = painterResource(id = iconDrawable),
                        contentDescription = label,
                        modifier = Modifier.size(14.dp),
                        colorFilter = ColorFilter.tint(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    )
                }
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = value,
                style = AppTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
        }
    }
}