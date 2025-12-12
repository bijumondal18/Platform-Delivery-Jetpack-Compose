package com.platform.platformdelivery.presentation.pages.notifications

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.data.models.Notification
import com.platform.platformdelivery.presentation.view_models.NotificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Chip state
    var selectedChip by remember { mutableStateOf("All") }
    val chipOptions = listOf("All", "Unread")

    // Collect states for All notifications
    val allNotifications by notificationViewModel.allNotifications.collectAsState()
    val isAllNotificationsLoading by notificationViewModel.isAllNotificationsLoading.collectAsState()
    val allNotificationsEmpty by notificationViewModel.allNotificationsEmpty.collectAsState()
    val noMoreAllNotificationsAvailable by notificationViewModel.noMoreAllNotificationsAvailable.collectAsState()
    val allNotificationsError by notificationViewModel.allNotificationsError.collectAsState()

    // Collect states for Unread notifications
    val unreadNotifications by notificationViewModel.unreadNotifications.collectAsState()
    val isUnreadNotificationsLoading by notificationViewModel.isUnreadNotificationsLoading.collectAsState()
    val unreadNotificationsEmpty by notificationViewModel.unreadNotificationsEmpty.collectAsState()
    val noMoreUnreadNotificationsAvailable by notificationViewModel.noMoreUnreadNotificationsAvailable.collectAsState()
    val unreadNotificationsError by notificationViewModel.unreadNotificationsError.collectAsState()

    // Load data when chip changes
    LaunchedEffect(selectedChip) {
        when (selectedChip) {
            "All" -> {
                notificationViewModel.resetUnreadNotificationsFlag()
                notificationViewModel.getAllNotifications(1)
            }
            "Unread" -> {
                notificationViewModel.resetAllNotificationsFlag()
                notificationViewModel.getUnreadNotifications(1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Text(
                        "Notifications",
                        style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                chipOptions.forEach { chip ->
                    val selected = chip == selectedChip
                    FilterChip(
                        selected = selected,
                        onClick = { selectedChip = chip },
                        label = {
                            Text(
                                chip,
                                color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        shape = MaterialTheme.shapes.extraExtraLarge,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.1f
                            ),
                            selectedContainerColor = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.1f
                            ),
                            selectedLabelColor = MaterialTheme.colorScheme.onBackground,
                            labelColor = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.5f
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content based on selected chip
            PullToRefreshBox(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    coroutineScope.launch {
                        delay(1000)
                        when (selectedChip) {
                            "All" -> notificationViewModel.getAllNotifications(1)
                            "Unread" -> notificationViewModel.getUnreadNotifications(1)
                        }
                        isRefreshing = false
                    }
                },
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    when (selectedChip) {
                        "All" -> {
                            when {
                                isAllNotificationsLoading && !isRefreshing -> {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                !allNotificationsError.isNullOrEmpty() -> {
                                    item {
                                        Text(
                                            text = "Error: $allNotificationsError",
                                            style = AppTypography.bodyLarge,
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        )
                                    }
                                }
                                allNotificationsEmpty -> {
                                    item {
                                        Text(
                                            text = "No notifications yet",
                                            style = AppTypography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        )
                                    }
                                }
                                else -> {
                                    itemsIndexed(allNotifications) { index, notification ->
                                        NotificationItem(notification = notification)
                                        if (index < allNotifications.size - 1) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                    if (noMoreAllNotificationsAvailable && allNotifications.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "No more notifications",
                                                style = AppTypography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        "Unread" -> {
                            when {
                                isUnreadNotificationsLoading && !isRefreshing -> {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                !unreadNotificationsError.isNullOrEmpty() -> {
                                    item {
                                        Text(
                                            text = "Error: $unreadNotificationsError",
                                            style = AppTypography.bodyLarge,
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        )
                                    }
                                }
                                unreadNotificationsEmpty -> {
                                    item {
                                        Text(
                                            text = "No unread notifications",
                                            style = AppTypography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        )
                                    }
                                }
                                else -> {
                                    itemsIndexed(unreadNotifications) { index, notification ->
                                        NotificationItem(notification = notification)
                                        if (index < unreadNotifications.size - 1) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                    if (noMoreUnreadNotificationsAvailable && unreadNotifications.isNotEmpty()) {
                                        item {
                                            Text(
                                                text = "No more notifications",
                                                style = AppTypography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                                textAlign = TextAlign.Center,
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
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    val isRead = !notification.readAt.isNullOrEmpty()
    
    // Extract title and message from nested data structure
    val title = notification.data?.data?.notificationTitle ?: "Notification"
    val message = notification.data?.data?.notification ?: ""

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = AppTypography.titleMedium.copy(
                        fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                if (!isRead) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = AppTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notification.createdAt ?: "",
                style = AppTypography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

