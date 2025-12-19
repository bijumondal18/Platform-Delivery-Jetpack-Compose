package com.platform.platformdelivery.presentation.pages.failed_delivery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.FileProvider
import java.io.File
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.activity.compose.BackHandler
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
import com.platform.platformdelivery.presentation.pages.profile.ImagePickerBottomSheet
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
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var notes by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showImagePickerSheet by remember { mutableStateOf(false) }
    
    val deliveryUpdateResult by routesViewModel.deliveryUpdateResult.collectAsState()
    val isUpdatingDelivery by routesViewModel.isUpdatingDelivery.collectAsState()
    
    // Create a temporary file for camera images
    val tempImageFile = remember {
        File(context.cacheDir, "temp_failed_image_${System.currentTimeMillis()}.jpg")
    }
    
    val tempImageUri = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempImageFile
            )
        } else {
            Uri.fromFile(tempImageFile)
        }
    }
    
    val failedReasons = listOf(
        FailedReason("1", "Recipient unavailable/ No answer"),
        FailedReason("2", "Recipient changed his/her address"),
        FailedReason("3", "Refused delivery"),
        FailedReason("4", "Access issue"),
        FailedReason("5", "Missing package"),
        FailedReason("6", "Payment required"),
        FailedReason("7", "Package damaged"),
        FailedReason("8", "Delivery timeframe missed"),
        FailedReason("9", "Incorrect address on maps"),
        FailedReason("10", "Incorrect package"),
        FailedReason("11", "Animal interference"),
        FailedReason("12", "Weather/ road condition"),
        FailedReason("13", "Other")
    )
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUris = selectedImageUris + tempImageUri
        }
    }
    
    // Multi-select gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris = selectedImageUris + uris
        }
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
    
    // Handle system back button - navigate back to route details
    BackHandler(enabled = true) {
        navController?.let { controller ->
            // Try to pop back stack first (normal back behavior)
            val popped = controller.popBackStack()
            if (!popped) {
                // If nothing to pop (would close app), navigate to route details
                val routeDetailsRoute = "routeDetails/$routeId"
                controller.navigate(routeDetailsRoute) {
                    launchSingleTop = true
                }
            }
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
                        navController?.let { controller ->
                            // Try to pop back stack first (normal back behavior)
                            val popped = controller.popBackStack()
                            if (!popped) {
                                // If nothing to pop (would close app), navigate to route details
                                val routeDetailsRoute = "routeDetails/$routeId"
                                controller.navigate(routeDetailsRoute) {
                                    launchSingleTop = true
                                }
                            }
                        }
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
            Spacer(modifier = Modifier.height(4.dp))

            // Photo upload section
            Text(
                text = "Add Photos",
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Add photo button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clickable {
                        showImagePickerSheet = true
                    },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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

            // Horizontal scrollable list of photos
            if (selectedImageUris.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    selectedImageUris.forEachIndexed { index, uri ->
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(120.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = "Selected photo ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // Close button on top right
                            IconButton(
                                onClick = {
                                    selectedImageUris = selectedImageUris.filterIndexed { i, _ -> i != index }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(32.dp)
                                    .padding(4.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.error,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
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
            
            var expanded by remember { mutableStateOf(false) }
            val selectedReasonText = remember(selectedReason) {
                failedReasons.find { it.id == selectedReason }?.text ?: ""
            }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedReasonText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Failure Reason") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    failedReasons.forEach { reason ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Text(
                                    text = reason.text,
                                    style = AppTypography.bodyMedium
                                )
                            },
                            onClick = {
                                selectedReason = reason.id
                                expanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Notes section
            Text(
                text = "Notes (Optional)",
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Add notes") },
                placeholder = { Text("Enter any additional notes...") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )
            
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
                    containerColor = MaterialTheme.colorScheme.primary,
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
    
    // Image Picker Bottom Sheet
    if (showImagePickerSheet) {
        ImagePickerBottomSheet(
            onDismiss = { showImagePickerSheet = false },
            onCameraClick = {
                cameraLauncher.launch(tempImageUri)
            },
            onGalleryClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}

