package com.platform.platformdelivery.presentation.pages.notifications

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.presentation.widgets.shimmerPlaceholder

@Composable
fun NotificationShimmerLoader() {
    val lightGrayColor = Color.LightGray.copy(alpha = 0.4f)
    val darkGrayColor = Color.DarkGray.copy(alpha = 0.3f)
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(5) { index ->
            val isRouteRelated = index % 2 == 0 // Alternate between route and non-route
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Route icon or unread indicator dot
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .shimmerPlaceholder(
                                backgroundColor = lightGrayColor,
                                highlightColor = darkGrayColor
                            )
                    )
                    
                    // Content Column
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
                            // Title shimmer
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(20.dp)
                                    .shimmerPlaceholder(
                                        backgroundColor = lightGrayColor,
                                        highlightColor = darkGrayColor
                                    )
                            )
                            
                            // Time and chevron shimmer
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Time shimmer
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(14.dp)
                                        .shimmerPlaceholder(
                                            backgroundColor = lightGrayColor,
                                            highlightColor = darkGrayColor
                                        )
                                )
                                
                                // Chevron shimmer (if route-related)
                                if (isRouteRelated) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .shimmerPlaceholder(
                                                backgroundColor = lightGrayColor,
                                                highlightColor = darkGrayColor
                                            )
                                    )
                                }
                            }
                        }
                        
                        // Message shimmer (2 lines)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.95f)
                                    .height(16.dp)
                                    .shimmerPlaceholder(
                                        backgroundColor = lightGrayColor,
                                        highlightColor = darkGrayColor
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(16.dp)
                                    .shimmerPlaceholder(
                                        backgroundColor = lightGrayColor,
                                        highlightColor = darkGrayColor
                                    )
                            )
                        }
                        
                        // Route addresses shimmer (if route-related)
                        if (isRouteRelated) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // From address shimmer
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(35.dp)
                                            .height(12.dp)
                                            .shimmerPlaceholder(
                                                backgroundColor = lightGrayColor,
                                                highlightColor = darkGrayColor
                                            )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .height(12.dp)
                                            .shimmerPlaceholder(
                                                backgroundColor = lightGrayColor,
                                                highlightColor = darkGrayColor
                                            )
                                    )
                                }
                                // To address shimmer
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height(12.dp)
                                            .shimmerPlaceholder(
                                                backgroundColor = lightGrayColor,
                                                highlightColor = darkGrayColor
                                            )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.65f)
                                            .height(12.dp)
                                            .shimmerPlaceholder(
                                                backgroundColor = lightGrayColor,
                                                highlightColor = darkGrayColor
                                            )
                                    )
                                }
                            }
                        }
                        
                        // Date shimmer
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(12.dp)
                                .shimmerPlaceholder(
                                    backgroundColor = lightGrayColor,
                                    highlightColor = darkGrayColor
                                )
                        )
                    }
                }
            }
            
            if (index < 4) {
                Spacer(modifier = Modifier.height(1.dp)) // Divider space
            }
        }
    }
}

