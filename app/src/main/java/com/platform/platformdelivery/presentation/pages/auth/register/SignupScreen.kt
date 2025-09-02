package com.platform.platformdelivery.presentation.pages.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
fun SignupScreen(navController: NavController, modifier: Modifier = Modifier) {

    var currentStep by remember { mutableStateOf(1) }


    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    var street by remember { mutableStateOf("") }
    var streetError by remember { mutableStateOf<String?>(null) }

    var homeAddress by remember { mutableStateOf("") }
    var homeAddressError by remember { mutableStateOf<String?>(null) }

    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var ssn by remember { mutableStateOf("") }
    var ssnError by remember { mutableStateOf<String?>(null) }

    var referralCode by remember { mutableStateOf("") }
    var referralCodeError by remember { mutableStateOf<String?>(null) }

    var zipCode by remember { mutableStateOf("") }
    var zipCodeError by remember { mutableStateOf<String?>(null) }


    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Signup Step $currentStep of 4") },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            PrimaryButton(
                text = if (currentStep < 4) "Next" else "Register",
                onClick = {
                    // Simple validation per step
                    when (currentStep) {
                        1 -> {
                            var valid = true
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Enter valid email"
                                valid = false
                            }
                            if (password.isEmpty()) {
                                passwordError = "Password required"
                                valid = false
                            }
                            if (confirmPassword != password) {
                                confirmPasswordError = "Passwords do not match"
                                valid = false
                            }
                            if (valid) currentStep++
                        }
                        2 -> {
                            if (phone.isNotEmpty() && name.isNotEmpty()) {
                                currentStep++
                            } else {
                                if (phone.isEmpty()) phoneError = "Please enter your name"
                                if (name.isEmpty()) nameError = "Please enter your phone number"
                            }
                        }
                        3 -> currentStep++ // you can add validation here
                        4 -> {
                            // Final submit
                            navController.navigate("main"){
                                popUpTo("signup") { inclusive = true }
                            } // or call ViewModel
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (currentStep) {
                // STEP 1: Email + Password
                1 -> {
                    AppTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = "Email",
                        keyboardType = KeyboardType.Email,
                        isError = emailError != null,
                        errorMessage = emailError
                    )
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = "Password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        isError = passwordError != null,
                        errorMessage = passwordError
                    )
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmPasswordError = null },
                        label = "Confirm Password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        isError = confirmPasswordError != null,
                        errorMessage = confirmPasswordError
                    )
                }

                // STEP 2: Phone + Name
                2 -> {
                    AppTextField(
                        value = name,
                        onValueChange = { name = it; nameError = null },
                        label = "Name",
                        keyboardType = KeyboardType.Text,
                        isError = nameError != null,
                        errorMessage = nameError
                    )
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = phone,
                        onValueChange = { phone = it; phoneError = null },
                        label = "Phone",
                        keyboardType = KeyboardType.Phone,
                        isError = phoneError != null,
                        errorMessage = phoneError
                    )
                }

                // STEP 3: Address
                3 -> {
                    AppTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = "Street",
                        keyboardType = KeyboardType.Text
                    )
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = zipCode,
                        onValueChange = { zipCode = it },
                        label = "Zip Code",
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = homeAddress,
                        onValueChange = { homeAddress = it },
                        label = "Home Address",
                        keyboardType = KeyboardType.Text
                    )
                }

                // STEP 4: SSN + Referral
                4 -> {
                    AppTextField(
                        value = ssn,
                        onValueChange = { ssn = it },
                        label = "SSN",
                        keyboardType = KeyboardType.Text
                    )
                    Spacer(Modifier.height(8.dp))
                    AppTextField(
                        value = referralCode,
                        onValueChange = { referralCode = it },
                        label = "Referral Code",
                        keyboardType = KeyboardType.Text
                    )
                }
            }
        }
    }

}