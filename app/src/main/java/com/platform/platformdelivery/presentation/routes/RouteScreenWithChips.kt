package com.platform.platformdelivery.presentation.routes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.presentation.pages.available_routes.AvailableRoutesScreen
import com.platform.platformdelivery.presentation.pages.my_accepted_routes.MyAcceptedRoutesScreen
import com.platform.platformdelivery.presentation.pages.my_route_history.MyRouteHistory
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RoutesScreenWithChips(
    modifier: Modifier = Modifier,
    routesViewModel: RoutesViewModel = viewModel(),
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()

    // Chip state
    var selectedChip by remember { mutableStateOf("Available") }
    val chipOptions = listOf("Available", "Accepted", "Route History")

    Column(modifier = Modifier.fillMaxSize()) {

        // --- Chips Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            chipOptions.forEach { chip ->
                androidx.compose.material3.FilterChip(
                    selected = selectedChip == chip,
                    onClick = { selectedChip = chip },
                    label = { Text(chip) }
                )
            }
        }

        // --- Divider ---
        Spacer(modifier = Modifier.height(8.dp))

        // --- Content Below Chips ---
        when (selectedChip) {
            "Available" -> AvailableRoutesScreen(navController = navController)
            "Accepted" -> MyAcceptedRoutesScreen()
            "Route History" -> MyRouteHistory(navController = navController)
        }
    }
}
