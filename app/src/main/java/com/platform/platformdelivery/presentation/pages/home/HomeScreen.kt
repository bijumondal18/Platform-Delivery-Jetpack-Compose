package com.platform.platformdelivery.presentation.pages.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.core.theme.SuccessGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    var isOnline by remember { mutableStateOf(false) }

    // ✅ Format current date
    val currentDate = remember {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MM/dd", Locale.getDefault())
        today.format(formatter)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Offline",
                style = AppTypography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isOnline,
                onCheckedChange = { isOnline = it },
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = SuccessGreen,
                    checkedTrackColor = SuccessGreen.copy(alpha = 0.5f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.error,
                    uncheckedTrackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Online",
                style = AppTypography.bodyLarge,
                color = SuccessGreen
            )
        }

        Spacer(Modifier.height(16.dp))

        if (isOnline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        currentDate,
                        style = AppTypography.bodyLarge,
                        color = MaterialTheme.colorScheme.background
                    )
                    Text(
                        "0 Offers",
                        style = AppTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                }

            }
        }

        if (!isOnline) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Go Online to Grab a Route!",
                style = AppTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }


    }
}