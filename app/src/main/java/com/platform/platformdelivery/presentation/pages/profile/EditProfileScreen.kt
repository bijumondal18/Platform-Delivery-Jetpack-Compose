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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.presentation.widgets.AppTextField
import com.platform.platformdelivery.presentation.widgets.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    initialName: String = "",
    initialPhone: String = "",
    initialAddress: String = "",
    onSave: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var name by remember { mutableStateOf(initialName) }
    var nameError by remember { mutableStateOf<String?>(null) }
    
    var phone by remember { mutableStateOf(initialPhone) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    
    var address by remember { mutableStateOf(initialAddress) }
    var addressError by remember { mutableStateOf<String?>(null) }

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
        }
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
                value = address,
                onValueChange = {
                    address = it
                    addressError = null
                },
                label = "Address",
                keyboardType = KeyboardType.Text,
                isError = addressError != null,
                errorMessage = addressError
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    
                    if (address.isBlank()) {
                        addressError = "Address is required"
                        isValid = false
                    }
                    
                    if (isValid) {
                        onSave(name, phone, address)
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

