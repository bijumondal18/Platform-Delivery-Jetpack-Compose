package com.platform.platformdelivery.presentation.pages.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Route
import androidx.compose.ui.res.painterResource
import com.platform.platformdelivery.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.runtime.snapshotFlow
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Chip state
    var selectedChip by remember { mutableStateOf("All") }
    val chipOptions = listOf("All", "Unread")

    // Mark all as read state
    val isMarkingAllAsRead by notificationViewModel.isMarkingAllAsRead.collectAsState()
    val markAllAsReadError by notificationViewModel.markAllAsReadError.collectAsState()
    
    // LazyListState for scroll detection
    val allNotificationsListState = rememberLazyListState()
    val unreadNotificationsListState = rememberLazyListState()

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

    // Initial load - load "All" notifications when screen first appears
    LaunchedEffect(Unit) {
        notificationViewModel.loadAllNotificationsOnce()
    }

    // Load data when chip changes - only if not already loaded
    LaunchedEffect(selectedChip) {
        when (selectedChip) {
            "All" -> {
                notificationViewModel.resetUnreadNotificationsFlag()
                // Only load if not already loaded
                if (!notificationViewModel.hasLoadedAllNotifications) {
                    notificationViewModel.loadAllNotificationsOnce()
                }
            }
            "Unread" -> {
                notificationViewModel.resetAllNotificationsFlag()
                // Only load if not already loaded
                if (!notificationViewModel.hasLoadedUnreadNotifications) {
                    notificationViewModel.loadUnreadNotificationsOnce()
                }
            }
        }
    }
    
    // Pagination: Load more when scrolling near bottom for All notifications
    LaunchedEffect(selectedChip) {
        if (selectedChip == "All") {
            snapshotFlow {
                val layoutInfo = allNotificationsListState.layoutInfo
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = layoutInfo.totalItemsCount
                lastVisibleItemIndex to totalItems
            }.collect { (lastVisibleItemIndex, totalItems) ->
                // Load more when user scrolls to within 3 items of the end
                if (lastVisibleItemIndex >= totalItems - 3 && 
                    totalItems > 0 && 
                    !isAllNotificationsLoading &&
                    !noMoreAllNotificationsAvailable) {
                    notificationViewModel.loadNextAllNotificationsPage()
                }
            }
        }
    }
    
    // Pagination: Load more when scrolling near bottom for Unread notifications
    LaunchedEffect(selectedChip) {
        if (selectedChip == "Unread") {
            snapshotFlow {
                val layoutInfo = unreadNotificationsListState.layoutInfo
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = layoutInfo.totalItemsCount
                lastVisibleItemIndex to totalItems
            }.collect { (lastVisibleItemIndex, totalItems) ->
                // Load more when user scrolls to within 3 items of the end
                if (lastVisibleItemIndex >= totalItems - 3 && 
                    totalItems > 0 && 
                    !isUnreadNotificationsLoading &&
                    !noMoreUnreadNotificationsAvailable) {
                    notificationViewModel.loadNextUnreadNotificationsPage()
                }
            }
        }
    }

    // Handle mark all as read error
    LaunchedEffect(markAllAsReadError) {
        markAllAsReadError?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Failed to mark all as read: $error")
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
                },
                actions = {
                    // Only show "Mark All as Read" button if there are unread notifications
                    val hasUnreadNotifications = unreadNotifications.isNotEmpty() || 
                        (selectedChip == "All" && allNotifications.any { it.readAt.isNullOrEmpty() })
                    
                    if (hasUnreadNotifications) {
                        TextButton(
                            onClick = {
                                notificationViewModel.markAllAsRead {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("All notifications marked as read")
                                    }
                                }
                            },
                            enabled = !isMarkingAllAsRead
                        ) {
                            if (isMarkingAllAsRead) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Mark All as Read",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mark All Read",
                                style = AppTypography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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

            // Group notifications by date - compute outside LazyColumn
            val allNotificationListItems = remember(allNotifications) {
                if (allNotifications.isNotEmpty()) {
                    val grouped = groupNotificationsByDate(allNotifications)
                    flattenGroupedNotifications(grouped)
                } else {
                    emptyList()
                }
            }
            
            val unreadNotificationListItems = remember(unreadNotifications) {
                if (unreadNotifications.isNotEmpty()) {
                    val grouped = groupNotificationsByDate(unreadNotifications)
                    flattenGroupedNotifications(grouped)
                } else {
                    emptyList()
                }
            }
            
            // Content based on selected chip
            PullToRefreshBox(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    coroutineScope.launch {
                        delay(1000)
                        when (selectedChip) {
                            "All" -> {
                                notificationViewModel.resetAllNotificationsFlag()
                                notificationViewModel.getAllNotifications(1)
                            }
                            "Unread" -> {
                                notificationViewModel.resetUnreadNotificationsFlag()
                                notificationViewModel.getUnreadNotifications(1)
                            }
                        }
                        isRefreshing = false
                    }
                },
            ) {
                when (selectedChip) {
                    "All" -> {
                        LazyColumn(
                            state = allNotificationsListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp)
                        ) {
                            when {
                                isAllNotificationsLoading && allNotifications.isEmpty() && !isRefreshing -> {
                                    // Show loading only when list is empty (initial load)
                                    item {
                                        NotificationShimmerLoader()
                                    }
                                }
                                !allNotificationsError.isNullOrEmpty() && allNotifications.isEmpty() -> {
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
                                allNotificationsEmpty && !isAllNotificationsLoading -> {
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
                                allNotifications.isNotEmpty() -> {
                                    // Only show list when we have data
                                    itemsIndexed(allNotificationListItems) { index, item ->
                                        when (item) {
                                            is NotificationListItem.DateHeader -> {
                                                DateHeaderItem(dateText = item.dateText)
                                            }
                                            is NotificationListItem.NotificationItem -> {
                                                NotificationItem(
                                                    notification = item.notification,
                                                    onNotificationClick = {
                                                        // Navigate to route details using notifiableId as route ID
                                                        val routeId = item.notification.id.toString()
                                                        if (!routeId.isNullOrEmpty()) {
                                                            navController.navigate("routeDetails/$routeId")
                                                        }
                                                    }
                                                )
                                                if (index < allNotificationListItems.size - 1) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(horizontal = 16.dp),
                                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                                        thickness = 1.dp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Loading indicator at bottom when loading more (pagination)
                                    if (isAllNotificationsLoading && allNotifications.isNotEmpty()) {
                                        item {
                                            NotificationShimmerLoader()
                                        }
                                    }
                                    // No more data indicator
                                    if (noMoreAllNotificationsAvailable && allNotifications.isNotEmpty() && !isAllNotificationsLoading) {
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
                    "Unread" -> {
                        LazyColumn(
                            state = unreadNotificationsListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp)
                        ) {
                            when {
                                isUnreadNotificationsLoading && unreadNotifications.isEmpty() && !isRefreshing -> {
                                    // Show loading only when list is empty (initial load)
                                    item {
                                        NotificationShimmerLoader()
                                    }
                                }
                                !unreadNotificationsError.isNullOrEmpty() && unreadNotifications.isEmpty() -> {
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
                                unreadNotificationsEmpty && !isUnreadNotificationsLoading -> {
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
                                unreadNotifications.isNotEmpty() -> {
                                    // Only show list when we have data
                                    itemsIndexed(unreadNotificationListItems) { index, item ->
                                        when (item) {
                                            is NotificationListItem.DateHeader -> {
                                                DateHeaderItem(dateText = item.dateText)
                                            }
                                            is NotificationListItem.NotificationItem -> {
                                                NotificationItem(
                                                    notification = item.notification,
                                                    onNotificationClick = {
                                                        // Navigate to route details using notifiableId as route ID
                                                        val routeId = item.notification.notifiableId?.toString()
                                                        if (!routeId.isNullOrEmpty()) {
                                                            navController.navigate("routeDetails/$routeId")
                                                        }
                                                    }
                                                )
                                                if (index < unreadNotificationListItems.size - 1) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(horizontal = 16.dp),
                                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                                        thickness = 1.dp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Loading indicator at bottom when loading more (pagination)
                                    if (isUnreadNotificationsLoading && unreadNotifications.isNotEmpty()) {
                                        item {
                                            NotificationShimmerLoader()
                                        }
                                    }
                                    // No more data indicator
                                    if (noMoreUnreadNotificationsAvailable && unreadNotifications.isNotEmpty() && !isUnreadNotificationsLoading) {
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
fun DateHeaderItem(dateText: String) {
    Text(
        text = dateText,
        style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun NotificationItem(
    notification: Notification,
    onNotificationClick: () -> Unit = {}
) {
    val isRead = !notification.readAt.isNullOrEmpty()
    
    // Extract title and message from nested data structure
    val title = notification.data?.data?.notificationTitle ?: "Notification"
    val message = notification.data?.data?.notification ?: ""
    
    // Format date
    val formattedDate = remember(notification.createdAt) {
        formatNotificationDate(notification.createdAt)
    }
    
    // Format time
    val formattedTime = remember(notification.createdAt) {
        formatNotificationTime(notification.createdAt)
    }
    
    // All notifications have route ID, so all are route-related
    val routeId = notification.notifiableId?.toString()
    
    // Extract route addresses from routeData if available
    val routeData = notification.data?.data?.routeData
    val originAddress = remember(routeData) {
        extractRouteAddress(routeData, "origin")
    }
    val destinationAddress = remember(routeData) {
        extractRouteAddress(routeData, "destination")
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !routeId.isNullOrEmpty()) {
                if (!routeId.isNullOrEmpty()) {
                    onNotificationClick()
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Route icon - all notifications are route-related
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delivery_truck),
                    contentDescription = "Route",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
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
                    Text(
                        text = title,
                        style = AppTypography.titleMedium.copy(
                            fontWeight = if (isRead) FontWeight.SemiBold else FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time
                        Text(
                            text = formattedTime,
                            style = AppTypography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        
                        // Chevron icon - all notifications are clickable
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Details",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
                
                // Message
                Text(
                    text = message,
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (isRead) 0.6f else 0.8f),
                    maxLines = 2
                )
                
                // Route addresses if available (all notifications are route-related)
                if (originAddress != null || destinationAddress != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (originAddress != null) {
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
                                    text = originAddress,
                                    style = AppTypography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }
                        }
                        if (destinationAddress != null) {
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
                                    text = destinationAddress,
                                    style = AppTypography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                
                // Date below message
                Text(
                    text = formattedDate,
                    style = AppTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
    }
}

/**
 * Extracts origin or destination address from routeData
 * Handles Map, JSON string, or Route object structures
 */
fun extractRouteAddress(routeData: Any?, type: String): String? {
    if (routeData == null) return null
    
    return try {
        when (routeData) {
            is Map<*, *> -> {
                // Handle Map structure (most common case from Gson)
                when (type) {
                    "origin" -> {
                        (routeData["origin_place"] as? String)
                            ?: (routeData["originPlace"] as? String)
                            ?: (routeData["origin"] as? String)
                            ?: (routeData["origin_place"]?.toString())
                    }
                    "destination" -> {
                        (routeData["destination_place"] as? String)
                            ?: (routeData["destinationPlace"] as? String)
                            ?: (routeData["destination"] as? String)
                            ?: (routeData["destination_place"]?.toString())
                    }
                    else -> null
                }
            }
            is String -> {
                // If it's a JSON string, try to parse it
                // For now, return null as we'd need Gson instance
                null
            }
            else -> {
                // Try reflection-based access for Route objects
                try {
                    when (type) {
                        "origin" -> {
                            routeData.javaClass.getMethod("getOriginPlace")?.invoke(routeData) as? String
                                ?: routeData.javaClass.getMethod("getOrigin_place")?.invoke(routeData) as? String
                        }
                        "destination" -> {
                            routeData.javaClass.getMethod("getDestinationPlace")?.invoke(routeData) as? String
                                ?: routeData.javaClass.getMethod("getDestination_place")?.invoke(routeData) as? String
                        }
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Formats a date string from ISO format to "hh:mm a" format (AM/PM)
 */
fun formatNotificationTime(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        
        if (date != null) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.format(date)
        } else {
            // Fallback: try simpler format
            try {
                val inputFormat2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val date2 = inputFormat2.parse(dateString)
                if (date2 != null) {
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    timeFormat.format(date2)
                } else {
                    ""
                }
            } catch (e2: Exception) {
                ""
            }
        }
    } catch (e: Exception) {
        ""
    }
}

/**
 * Sealed class to represent different types of items in the notification list
 */
sealed class NotificationListItem {
    data class DateHeader(val dateText: String) : NotificationListItem()
    data class NotificationItem(val notification: Notification) : NotificationListItem()
}

/**
 * Groups notifications by date
 */
fun groupNotificationsByDate(notifications: List<Notification>): Map<String, List<Notification>> {
    val calendar = Calendar.getInstance()
    
    return notifications.groupBy { notification ->
        val dateString = notification.createdAt
        if (dateString.isNullOrEmpty()) {
            "Unknown Date"
        } else {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                val date = inputFormat.parse(dateString) ?: run {
                    val inputFormat2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    inputFormat2.parse(dateString)
                }
                
                if (date != null) {
                    calendar.time = date
                    val today = Calendar.getInstance()
                    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    
                    when {
                        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> {
                            "Today"
                        }
                        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> {
                            "Yesterday"
                        }
                        else -> {
                            formatNotificationDate(dateString)
                        }
                    }
                } else {
                    "Unknown Date"
                }
            } catch (e: Exception) {
                "Unknown Date"
            }
        }
    }
}

/**
 * Flattens grouped notifications into a list of NotificationListItem
 */
fun flattenGroupedNotifications(grouped: Map<String, List<Notification>>): List<NotificationListItem> {
    val sortedGroups = grouped.toSortedMap(compareBy<String> { dateKey ->
        when (dateKey) {
            "Today" -> 0
            "Yesterday" -> 1
            else -> 2
        }
    }.thenBy { it })
    
    return sortedGroups.flatMap { (dateKey, notifications) ->
        listOf(NotificationListItem.DateHeader(dateKey)) + 
        notifications.map { NotificationListItem.NotificationItem(it) }
    }
}

/**
 * Formats a date string from ISO format to "3rd Nov, 2025" format
 */
fun formatNotificationDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    
    return try {
        // Parse ISO format: "2023-03-11T19:53:20.000000Z"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        
        if (date != null) {
            val day = SimpleDateFormat("d", Locale.getDefault()).format(date).toInt()
            val month = SimpleDateFormat("MMM", Locale.getDefault()).format(date)
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
            
            val ordinalSuffix = when {
                day % 100 in 11..13 -> "th"
                day % 10 == 1 -> "st"
                day % 10 == 2 -> "nd"
                day % 10 == 3 -> "rd"
                else -> "th"
            }
            
            "$day$ordinalSuffix $month, $year"
        } else {
            dateString
        }
    } catch (e: Exception) {
        // Fallback: try simpler format
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            
            if (date != null) {
                val day = SimpleDateFormat("d", Locale.getDefault()).format(date).toInt()
                val month = SimpleDateFormat("MMM", Locale.getDefault()).format(date)
                val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
                
                val ordinalSuffix = when {
                    day % 100 in 11..13 -> "th"
                    day % 10 == 1 -> "st"
                    day % 10 == 2 -> "nd"
                    day % 10 == 3 -> "rd"
                    else -> "th"
                }
                
                "$day$ordinalSuffix $month, $year"
            } else {
                dateString
            }
        } catch (e2: Exception) {
            dateString
        }
    }
}

