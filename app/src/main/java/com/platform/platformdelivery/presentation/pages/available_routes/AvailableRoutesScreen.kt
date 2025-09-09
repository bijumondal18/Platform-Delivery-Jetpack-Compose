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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.platform.platformdelivery.R
import com.platform.platformdelivery.app.MainActivity
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import com.platform.platformdelivery.presentation.widgets.AppTextField
import com.platform.platformdelivery.presentation.widgets.DatePickerBox
import com.platform.platformdelivery.presentation.widgets.RouteItem
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AvailableRoutesScreen(
    routesViewModel: RoutesViewModel = viewModel()
) {

    val context = LocalContext.current
    val activity = context as? MainActivity

    // ✅ collect states from ViewModel
    val routes by routesViewModel.routes.collectAsState()
    val isLoading by routesViewModel.isLoading.collectAsState()
    val isEmpty by routesViewModel.isEmpty.collectAsState()
    val noMoreData by routesViewModel.noMoreDataAvailable.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }


    var pickedDate by remember { mutableStateOf<String?>(null) }

    // ✅ Format current date
    val currentDate = remember {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MM/dd", Locale.getDefault())
        today.format(formatter)
    }

    var zipCode by remember { mutableStateOf("") }


    LaunchedEffect("") {
        routesViewModel.getAvailableRoutes(1)
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AppTextField(
                    value = zipCode,
                    onValueChange = { zipCode = it },
                    label = "Zip Code",
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                initialIndex = 0,
                stepValues = listOf(0, 10, 20, 30, 40, 50),
            ) {

            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            ) {
                Text(
                    "None",
                    style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "50",
                    style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(32.dp))
        }

        item {

            DatePickerBox(
                initialDate = currentDate,
                onDateSelected = { selectedDate ->
                    pickedDate = selectedDate
                    coroutineScope.launch {
                        routesViewModel.getAvailableRoutes(
                            1,
                            date = selectedDate
                        )
                    }
                }
            )
        }

        when {
            isLoading && !isRefreshing -> {
                item {
                    Text(
                        "Loading routes...",
                        style = AppTypography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            isEmpty -> {
                item {
                    Text(
                        "No routes available", style = AppTypography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            else -> {
                items(routes) { route ->
                    RouteItem(route)
                }
                if (noMoreData) {
                    item {
                        Text(
                            "No more routes available",
                            style = AppTypography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }

    }

}