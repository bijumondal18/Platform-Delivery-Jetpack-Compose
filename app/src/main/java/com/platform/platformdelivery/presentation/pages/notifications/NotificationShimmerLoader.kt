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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.presentation.widgets.shimmerPlaceholder

@Composable
fun NotificationShimmerLoader() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(5) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Top row: Title and Date - exactly matching NotificationItem structure
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left side: Title row with unread indicator
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Title shimmer - matches titleMedium text size
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(20.dp)
                                    .shimmerPlaceholder()
                            )
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            // Unread indicator shimmer (small circle) - matches 8.dp circle
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .shimmerPlaceholder()
                            )
                        }
                        // Right side: Date shimmer - matches labelSmall text size
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.25f)
                                .height(14.dp)
                                .shimmerPlaceholder()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Message shimmer - matches bodyMedium text size
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(16.dp)
                            .shimmerPlaceholder()
                    )
                }
            }
            
            if (it < 4) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

