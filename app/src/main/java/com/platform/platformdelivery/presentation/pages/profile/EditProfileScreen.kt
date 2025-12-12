package com.platform.platformdelivery.presentation.pages.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.core.theme.AppTypography
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
    
    var zip by remember { mutableStateOf(driverDetails?.zip ?: "") }
    var zipError by remember { mutableStateOf<String?>(null) }
    
    var baseLocation by remember { mutableStateOf(driverDetails?.baseLocation ?: "") }
    var baseLocationError by remember { mutableStateOf<String?>(null) }

    // Load driver details when screen appears
    LaunchedEffect(Unit) {
        profileViewModel.loadDriverDetailsOnce()
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

            AppTextField(
                value = state,
                onValueChange = {
                    state = it
                    stateError = null
                },
                label = "State",
                keyboardType = KeyboardType.Text,
                isError = stateError != null,
                errorMessage = stateError
            )

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

            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Save Changes",
                    onClick = {
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
                )
            }
        }
    }
}
