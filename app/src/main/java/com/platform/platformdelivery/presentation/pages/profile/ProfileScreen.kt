package com.platform.platformdelivery.presentation.pages.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.data.local.TokenManager
import com.platform.platformdelivery.presentation.view_models.ProfileViewModel
import com.platform.platformdelivery.presentation.widgets.ModernLogoutDialog
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    navController: NavController? = null,
    onDeleteAccount: () -> Unit = {},
    onLogout: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val appPrefs = remember { TokenManager(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Collect state from ViewModel
    val driverDetails by profileViewModel.driverDetails.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val isUpdating by profileViewModel.isUpdating.collectAsState()
    val updateProfileState by profileViewModel.updateProfileState.collectAsState()
    val error by profileViewModel.error.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    
    // State for bottom sheet and dialogs
    var showImagePickerSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Load driver details when screen appears
    LaunchedEffect(Unit) {
        profileViewModel.loadDriverDetailsOnce()
    }

    // Refresh data when screen becomes visible (e.g., when returning from EditProfileScreen)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Refresh driver details when screen resumes
                profileViewModel.refreshDriverDetails()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Helper function to convert Uri to File
    fun uriToFile(uri: Uri): File? {
        return try {
            when (uri.scheme) {
                "file" -> File(uri.path ?: return null)
                "content" -> {
                    // For content URIs, copy to a temporary file
                    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
                    val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { output ->
                        inputStream.copyTo(output)
                    }
                    file
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Handle image upload when selected
    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { uri ->
            val file = uriToFile(uri)
            file?.let {
                // Send all existing data including required fields (email, base_location_lat, base_location_lng)
                profileViewModel.updateProfile(
                    name = driverDetails?.name,
                    email = driverDetails?.email, // Required field
                    phone = driverDetails?.phone,
                    street = driverDetails?.street,
                    city = driverDetails?.city,
                    state = driverDetails?.state,
                    zip = driverDetails?.zip,
                    baseLocation = driverDetails?.baseLocation,
                    baseLocationLat = driverDetails?.baseLocationLat, // Required field
                    baseLocationLng = driverDetails?.baseLocationLng, // Required field
                    profilePicFile = it,
                    onSuccess = {
                        // Refresh driver details after successful upload
                        profileViewModel.getDriverDetails()
                    }
                )
            }
        }
    }

    // Handle update result
    LaunchedEffect(updateProfileState) {
        when (updateProfileState) {
            is Result.Success -> {
                // Image uploaded successfully, details will be refreshed
            }
            is Result.Error -> {
                // Handle error if needed
            }
            else -> Unit
        }
    }
    
    // Get data from API if available, otherwise fallback to local storage
    val name = driverDetails?.name ?: appPrefs.getName() ?: ""
    val email = driverDetails?.email ?: appPrefs.getEmail() ?: ""
    val profilePic = driverDetails?.profilePic ?: appPrefs.getProfilePic() ?: ""
    val phone = driverDetails?.phone ?: ""
    val baseLocation = driverDetails?.baseLocation ?: ""
    val street = driverDetails?.street ?: ""
    val city = driverDetails?.city ?: ""
    val state = driverDetails?.state ?: ""
    val zip = driverDetails?.zip ?: ""

    // Create a temporary file for camera images
    val tempImageFile = remember {
        File(context.cacheDir, "temp_profile_image_${System.currentTimeMillis()}.jpg")
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

    // Helper function to save bitmap to file and return URI
    fun saveBitmapToUri(bitmap: Bitmap): Uri? {
        return try {
            val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            null
        }
    }

    // Image picker launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = tempImageUri
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            // Profile Image with Camera Icon - Larger size
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (profilePic.isNotEmpty()) {
                        AsyncImage(
                            model = profilePic,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile placeholder",
                            modifier = Modifier.size(160.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Camera Icon Overlay - Adjusted position for larger avatar
                Box(
                    modifier = Modifier
                        .offset(x = 55.dp, y = 55.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            showImagePickerSheet = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change Profile Picture",
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Name
                    ProfileDetailItem(
                        label = "Name",
                        value = name.ifEmpty { "Not provided" }
                    )

                    // Email
                    ProfileDetailItem(
                        label = "Email",
                        value = email.ifEmpty { "Not provided" }
                    )

                    // Phone Number
                    ProfileDetailItem(
                        label = "Phone Number",
                        value = phone.ifEmpty { "Not provided" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Base Location
                    ProfileDetailItem(
                        label = "Base Location",
                        value = baseLocation.ifEmpty { "Not provided" }
                    )

                    // Street
                    ProfileDetailItem(
                        label = "Street",
                        value = street.ifEmpty { "Not provided" }
                    )

                    // City
                    ProfileDetailItem(
                        label = "City",
                        value = city.ifEmpty { "Not provided" }
                    )

                    // State
                    ProfileDetailItem(
                        label = "State",
                        value = state.ifEmpty { "Not provided" }
                    )

                    // Zip Code
                    ProfileDetailItem(
                        label = "Zip Code",
                        value = zip.ifEmpty { "Not provided" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // First Row: Edit and Refer & Earn
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Edit Profile Button
                    Button(
                        onClick = {
                            navController?.navigate("editProfile")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Edit",
                            style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    // Refer & Earn Button
                    Button(
                        onClick = {
                            // TODO: Navigate to Refer & Earn screen
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = "Refer & Earn",
                            style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                // Second Row: Tutorials and Contact Admin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tutorials Button
                    Button(
                        onClick = {
                            // TODO: Navigate to Tutorials screen
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text(
                            text = "Tutorials",
                            style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    // Contact Admin Button
                    Button(
                        onClick = {
                            navController?.navigate("contact_admin")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = "Contact Admin",
                            style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                // Logout Button (Full Width)
                Button(
                    onClick = {
                        showLogoutDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = "Logout",
                        style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
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
                galleryLauncher.launch("image/*")
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDeleteAccount()
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        ModernLogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                onLogout()
            }
        )
    }
}

@Composable
fun ProfileDetailItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = AppTypography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        )
        Text(
            text = value,
            style = AppTypography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}
