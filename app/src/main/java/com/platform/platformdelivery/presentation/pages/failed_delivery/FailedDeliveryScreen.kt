package com.platform.platformdelivery.presentation.pages.failed_delivery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.presentation.view_models.RoutesViewModel
import kotlinx.coroutines.launch

data class FailedReason(
    val id: String,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FailedDeliveryScreen(
    routeId: String,
    waypointId: String,
    navController: NavController? = null,
    routesViewModel: RoutesViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showError by remember { mutableStateOf(false) }
    
    val deliveryUpdateResult by routesViewModel.deliveryUpdateResult.collectAsState()
    val isUpdatingDelivery by routesViewModel.isUpdatingDelivery.collectAsState()
    
    val failedReasons = listOf(
        FailedReason("1", "Recipient unavailable/ No answer"),
        FailedReason("2", "Recipient changed his/her address"),
        FailedReason("3", "Refused delivery"),
        FailedReason("4", "Access issue"),
        FailedReason("5", "Missing package"),
        FailedReason("6", "Other")
    )
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    // Handle delivery update result
    LaunchedEffect(deliveryUpdateResult) {
        when (deliveryUpdateResult) {
            is Result.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed delivery submitted successfully")
                }
                // Navigate back after a short delay
                kotlinx.coroutines.delay(1000)
                navController?.popBackStack()
            }
            is Result.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to submit: ${(deliveryUpdateResult as Result.Error).message}")
                }
                showError = true
            }
            else -> Unit
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Text(
                        "Failed Delivery",
                        style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController?.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = if (snackbarData.visuals.message.contains("Failed", ignoreCase = true)) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Photo upload section
            Text(
                text = "Add Photo",
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        imagePickerLauncher.launch("image/*")
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Selected photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Add photo",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap to add photo",
                                style = AppTypography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Failed reason section
            Text(
                text = "Select Failed Reason",
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                failedReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedReason = reason.id
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason.id,
                            onClick = {
                                selectedReason = reason.id
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = reason.text,
                            style = AppTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit button
            Button(
                onClick = {
                    if (selectedReason == null) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please select a failed reason")
                        }
                        return@Button
                    }
                    
                    // Call failed API with reason
                    routesViewModel.updateWaypointDelivery(
                        routeId = routeId,
                        waypointId = waypointId,
                        deliveryStatus = "failed",
                        deliveryType = selectedReason,
                        onSuccess = {
                            // Success handled in LaunchedEffect
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isUpdatingDelivery && selectedReason != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                if (isUpdatingDelivery) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Submitting...",
                        style = AppTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                } else {
                    Text(
                        text = "Submit",
                        style = AppTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

