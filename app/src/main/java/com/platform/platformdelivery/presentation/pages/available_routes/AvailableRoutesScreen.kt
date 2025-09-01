package com.platform.platformdelivery.presentation.pages.available_routes

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.R
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.presentation.widgets.AppTextField
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AvailableRoutesScreen(modifier: Modifier = Modifier) {

    // ✅ Format current date
    val currentDate = remember {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MM/dd", Locale.getDefault())
        today.format(formatter)
    }

    var zipCode by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {


        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppTextField(
                value = zipCode,
                onValueChange = { zipCode = it },
                label = "Enter Zip Code",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gps),
                        contentDescription = "gps",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(0.6f)
                            .size(34.dp)
                    )
                },
                onClick = {})
        }

        Spacer(Modifier.height(16.dp))


        Text(
            "Choose Delivery Radius (Mi)",
            style = AppTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        StepSlider(
            initialIndex = 0,
            stepValues = listOf(0, 10, 20, 30, 40, 50),
        ) {

        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "None",
                style = AppTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "50",
                style = AppTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.secondary,
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "0 Offers",
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

        }
    }

}