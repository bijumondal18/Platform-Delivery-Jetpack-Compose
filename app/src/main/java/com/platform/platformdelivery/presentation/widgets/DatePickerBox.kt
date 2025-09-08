package com.platform.platformdelivery.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.core.theme.AppTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerBox(
    initialDate: String = "Select Date",
    onDateSelected: (String) -> Unit = {}
) {
    var currentDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // --- Date Picker Dialog ---
    if (showDatePicker) {
        DatePickerDialog(
            shape = MaterialTheme.shapes.large,
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("EEEE, MM/dd", Locale.getDefault())
                            val formattedDate = sdf.format(Date(millis))
                            currentDate = formattedDate
                            onDateSelected(formattedDate) // callback for parent
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(
                        "OK",
                        style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        "Cancel",
                        style = AppTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                    )
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    // --- Clickable Box ---
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.medium
            )
            .clickable { showDatePicker = true } // open dialog
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
