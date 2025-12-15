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
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import android.content.pm.PackageManager
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
    profileViewModel: ProfileViewModel = viewModel(),
    onThemeChange: ((Boolean) -> Unit)? = null
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
                        .offset(x = 50.dp, y = 50.dp)
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.background,
                            shape = CircleShape
                        )
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

            Spacer(modifier = Modifier.height(24.dp))

            // Main Menu Items Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Edit Profile
                    ProfileMenuItem(
                        icon = Icons.Default.Edit,
                        title = "Edit Profile",
                        onClick = {
                            navController?.navigate("editProfile") {
                                popUpTo("main") { inclusive = false }
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Refer & Earn
                    ProfileMenuItem(
                        icon = Icons.Default.Share,
                        title = "Refer & Earn",
                        onClick = {
                            navController?.navigate("refer_earn") {
                                popUpTo("main") { inclusive = false }
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Settings
                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        onClick = {
                            navController?.navigate("settings") {
                                popUpTo("main") { inclusive = false }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Support Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Tutorials
                    ProfileMenuItem(
                        icon = Icons.Default.School,
                        title = "Tutorials",
                        onClick = {
                            navController?.navigate("tutorials") {
                                popUpTo("main") { inclusive = false }
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Contact Admin
                    ProfileMenuItem(
                        icon = Icons.Default.ContactSupport,
                        title = "Contact Admin",
                        onClick = {
                            navController?.navigate("contact_admin") {
                                popUpTo("main") { inclusive = false }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legal & Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // About Us
                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "About Us",
                        onClick = {
                            navController?.navigate("about_us") {
                                popUpTo("main") { inclusive = false }
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Privacy Policy
                    ProfileMenuItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        onClick = {
                            navController?.navigate("privacy_policy") {
                                popUpTo("main") { inclusive = false }
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Terms & Conditions
                    ProfileMenuItem(
                        icon = Icons.Default.Description,
                        title = "Terms & Conditions",
                        onClick = {
                            navController?.navigate("terms_conditions") {
                                popUpTo("main") { inclusive = false }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
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

            Spacer(modifier = Modifier.height(16.dp))

            // App Version
            val appVersion = try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionName ?: "1.0"
            } catch (e: PackageManager.NameNotFoundException) {
                "1.0"
            }

            Text(
                text = "Version $appVersion",
                style = AppTypography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            )
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

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
    }
}

