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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.R
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.core.theme.SuccessGreen
import com.platform.platformdelivery.data.models.Route

@Composable
fun RouteItem(
    route: Route,
    onClick: (Route) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(route) }
            .padding( vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Route icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_delivery_truck),
                    contentDescription = "Route",
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Route title/time
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${route.startTime ?: ""} - ${route.estimatedEndTime ?: ""}",
                            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${route.estimatedTotalTime ?: ""} - ${route.distance ?: ""}",
                            style = AppTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Price
                        Text(
                            text = "$${route.driverPrice ?: ""}",
                            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Chevron icon
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Details",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
                
                // Origin and Destination addresses
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Origin address
                    if (!route.originPlace.isNullOrEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "From:",
                                style = AppTypography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = route.originPlace,
                                style = AppTypography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                    
                    // Destination address
                    if (!route.destinationPlace.isNullOrEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "To:",
                                style = AppTypography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = route.destinationPlace,
                                style = AppTypography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
                
                // Status badge
                if (!route.status.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status:",
                            style = AppTypography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (route.status.lowercase() == "completed") 
                                        SuccessGreen.copy(alpha = 0.2f) 
                                    else 
                                        MaterialTheme.colorScheme.surfaceContainer,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = route.status.replaceFirstChar { it.uppercaseChar() },
                                style = AppTypography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (route.status.lowercase() == "completed") 
                                    SuccessGreen 
                                else 
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}