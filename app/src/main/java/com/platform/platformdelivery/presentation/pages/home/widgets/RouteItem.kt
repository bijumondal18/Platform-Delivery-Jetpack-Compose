package com.platform.platformdelivery.presentation.pages.home.widgets

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.data.models.Route

@Composable
fun RouteItem(route: Route) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "${route.startTime} - ${route.estimatedEndTime}",
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "${route.estimatedTotalTime} - ${route.distance}",
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Box(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium)
                    .padding(12.dp)
            ) {
                Text(
                    "$${route.driverPrice}",
                    style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "${route.originPlace}",
            style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(12.dp))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
    }
}