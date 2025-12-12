package com.platform.platformdelivery.presentation.pages.profile

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.data.local.TokenManager

@Composable
fun ProfileScreen(
    navController: NavController? = null,
    onDeleteAccount: () -> Unit = {}
) {
    val context = LocalContext.current
    val appPrefs = remember { TokenManager(context) }
    
    val name = appPrefs.getName() ?: ""
    val email = appPrefs.getEmail() ?: ""
    val profilePic = appPrefs.getProfilePic() ?: ""
    
    // TODO: Fetch phone and address from API
    val phone = "" // Placeholder - to be fetched from API
    val address = "" // Placeholder - to be fetched from API

    // State for bottom sheet and dialogs
    var showImagePickerSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Image picker launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            // Convert bitmap to URI or handle directly
            // For now, we'll need to save it to a file and get URI
            // TODO: Save bitmap to file and get URI, then upload to server
            // selectedImageUri = saveBitmapToFile(it)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // TODO: Upload image to server
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            // Profile Image with Camera Icon
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    } else if (profilePic.isNotEmpty()) {
                        AsyncImage(
                            model = profilePic,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile placeholder",
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Camera Icon Overlay
                Box(
                    modifier = Modifier
                        .offset(x = 40.dp, y = 40.dp)
                        .size(36.dp)
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
                        modifier = Modifier.size(20.dp),
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

                    // Address
                    ProfileDetailItem(
                        label = "Address",
                        value = address.ifEmpty { "Not provided" }
                    )
                }
            }
        }

    // Image Picker Bottom Sheet
    if (showImagePickerSheet) {
        ImagePickerBottomSheet(
            onDismiss = { showImagePickerSheet = false },
            onCameraClick = {
                cameraLauncher.launch(null)
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
