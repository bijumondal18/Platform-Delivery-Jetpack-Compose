package com.platform.platformdelivery.presentation.pages.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.data.models.State
import com.platform.platformdelivery.presentation.view_models.ProfileViewModel
import com.platform.platformdelivery.presentation.widgets.AppTextField
import com.platform.platformdelivery.presentation.widgets.PrimaryButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    // Collect state from ViewModel
    val driverDetails by profileViewModel.driverDetails.collectAsState()
    val isUpdating by profileViewModel.isUpdating.collectAsState()
    val updateProfileState by profileViewModel.updateProfileState.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val stateList by profileViewModel.stateList.collectAsState()
    val isLoadingStates by profileViewModel.isLoadingStates.collectAsState()

    // Initialize form fields with current driver details
    var name by remember { mutableStateOf(driverDetails?.name ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    
    var phone by remember { mutableStateOf(driverDetails?.phone ?: "") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    
    var street by remember { mutableStateOf(driverDetails?.street ?: "") }
    var streetError by remember { mutableStateOf<String?>(null) }
    
    var city by remember { mutableStateOf(driverDetails?.city ?: "") }
    var cityError by remember { mutableStateOf<String?>(null) }
    
    var state by remember { mutableStateOf(driverDetails?.state ?: "") }
    var stateError by remember { mutableStateOf<String?>(null) }
    var expandedStateDropdown by remember { mutableStateOf(false) }
    
    var zip by remember { mutableStateOf(driverDetails?.zip ?: "") }
    var zipError by remember { mutableStateOf<String?>(null) }
    
    var baseLocation by remember { mutableStateOf(driverDetails?.baseLocation ?: "") }
    var baseLocationError by remember { mutableStateOf<String?>(null) }

    // Load driver details and state list when screen appears
    LaunchedEffect(Unit) {
        profileViewModel.loadDriverDetailsOnce()
        profileViewModel.loadStateListOnce()
    }

    // Update form fields when driver details change
    LaunchedEffect(driverDetails) {
        driverDetails?.let {
            name = it.name ?: ""
            phone = it.phone ?: ""
            street = it.street ?: ""
            city = it.city ?: ""
            state = it.state ?: ""
            zip = it.zip ?: ""
            baseLocation = it.baseLocation ?: ""
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle update result
    LaunchedEffect(updateProfileState) {
        when (updateProfileState) {
            is Result.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Profile updated successfully")
                }
                navController.popBackStack()
            }
            is Result.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        (updateProfileState as Result.Error).message
                    )
                }
            }
            else -> Unit
        }
    }

    // Bottom sheet state
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = "Name",
                keyboardType = KeyboardType.Text,
                isError = nameError != null,
                errorMessage = nameError
            )

            AppTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError = null
                },
                label = "Phone Number",
                keyboardType = KeyboardType.Phone,
                isError = phoneError != null,
                errorMessage = phoneError
            )

            AppTextField(
                value = baseLocation,
                onValueChange = {
                    baseLocation = it
                    baseLocationError = null
                },
                label = "Base Location",
                keyboardType = KeyboardType.Text,
                isError = baseLocationError != null,
                errorMessage = baseLocationError
            )

            AppTextField(
                value = street,
                onValueChange = {
                    street = it
                    streetError = null
                },
                label = "Street",
                keyboardType = KeyboardType.Text,
                isError = streetError != null,
                errorMessage = streetError
            )

            AppTextField(
                value = city,
                onValueChange = {
                    city = it
                    cityError = null
                },
                label = "City",
                keyboardType = KeyboardType.Text,
                isError = cityError != null,
                errorMessage = cityError
            )

            // State Dropdown
            Column {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state,
                        onValueChange = { }, // Completely disabled - no manual editing
                        readOnly = true,
                        enabled = false,
                        label = { Text("State") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (expandedStateDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            disabledTextColor = MaterialTheme.colorScheme.onBackground,
                            disabledLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        ),
                        isError = stateError != null
                    )
                    // Invisible clickable overlay to handle clicks
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { expandedStateDropdown = true }
                    )
                }
                
                if (stateError != null) {
                    Text(
                        text = stateError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            AppTextField(
                value = zip,
                onValueChange = {
                    zip = it
                    zipError = null
                },
                label = "Zip Code",
                keyboardType = KeyboardType.Text,
                isError = zipError != null,
                errorMessage = zipError
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isUpdating) {
                        // Validation
                        var isValid = true
                        
                        if (name.isBlank()) {
                            nameError = "Name is required"
                            isValid = false
                        }
                        
                        if (phone.isBlank()) {
                            phoneError = "Phone number is required"
                            isValid = false
                        }
                        
                        if (isValid) {
                            profileViewModel.updateProfile(
                                name = name.takeIf { it.isNotBlank() },
                                email = driverDetails?.email, // Required field - always send existing value
                                phone = phone.takeIf { it.isNotBlank() },
                                street = street.takeIf { it.isNotBlank() },
                                city = city.takeIf { it.isNotBlank() },
                                state = state.takeIf { it.isNotBlank() },
                                zip = zip.takeIf { it.isNotBlank() },
                                baseLocation = baseLocation.takeIf { it.isNotBlank() },
                                baseLocationLat = driverDetails?.baseLocationLat, // Required field - always send existing value
                                baseLocationLng = driverDetails?.baseLocationLng, // Required field - always send existing value
                                profilePicFile = null // Image upload handled separately
                            )
                        }
                    }
                },
                enabled = !isUpdating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Save Changes",
                        style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
        
        // State Selection Bottom Sheet (outside Column for proper rendering)
        if (expandedStateDropdown) {
            // Calculate dynamic height based on list size
            val itemHeight = 56.dp // Height per item (16dp padding * 2 + text height)
            val headerHeight = 68.dp // Header + divider height
            val maxHeight = 500.dp // Maximum height
            val calculatedHeight = if (stateList.isEmpty() || isLoadingStates) {
                200.dp // Minimum height for loading/empty states
            } else {
                (headerHeight + (itemHeight * stateList.size)).coerceAtMost(maxHeight)
            }
            
            ModalBottomSheet(
                onDismissRequest = { expandedStateDropdown = false },
                sheetState = bottomSheetState,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(calculatedHeight)
                ) {
                    // Header
                    Text(
                        text = "Select State",
                        style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    HorizontalDivider()
                    
                    // Content
                    if (isLoadingStates) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (stateList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No states available (List size: ${stateList.size})",
                                style = AppTypography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(stateList) { index, stateItem ->
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    // Set the state slug into the text field
                                                    state = stateItem.slug ?: ""
                                                    expandedStateDropdown = false
                                                    stateError = null
                                                }
                                                .padding(horizontal = 16.dp, vertical = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stateItem.title ?: "Unknown",
                                                style = AppTypography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                }
                                if (index < stateList.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
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
