package com.platform.platformdelivery.presentation.pages.auth.reset_password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.presentation.view_models.AuthViewModel
import com.platform.platformdelivery.presentation.widgets.AppTextField
import com.platform.platformdelivery.presentation.widgets.PrimaryButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    navController: NavController,
    email: String,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val resetPasswordState by viewModel.resetPasswordState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isLoading = resetPasswordState is Result.Loading

    // Handle reset password result
    LaunchedEffect(resetPasswordState) {
        val state = resetPasswordState
        when (state) {
            is Result.Success -> {
                val successMessage = state.data?.data?.msg 
                    ?: state.data?.message 
                    ?: "Password reset successfully!"
                
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = successMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                
                // Navigate to login screen after success
                delay(1500)
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is Result.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.resetResetPasswordState()
                        navController.popBackStack() 
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Reset Password",
                style = AppTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Enter your new password for $email",
                style = AppTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AppTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                    viewModel.resetResetPasswordState()
                },
                label = "New Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                isError = passwordError != null,
                errorMessage = passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                    viewModel.resetResetPasswordState()
                },
                label = "Confirm Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Reset Password",
                enabled = password.isNotEmpty() && confirmPassword.isNotEmpty() && !isLoading,
                isLoading = isLoading,
                onClick = {
                    var valid = true
                    
                    // Validate password
                    if (password.length < 6) {
                        passwordError = "Password must be at least 6 characters"
                        valid = false
                    }
                    
                    // Validate password match
                    if (password != confirmPassword) {
                        confirmPasswordError = "Passwords do not match"
                        valid = false
                    }
                    
                    if (valid) {
                        // Use email as token (or get token from OTP verification response if available)
                        // For now, using email as token - adjust based on your API requirements
                        viewModel.resetPassword(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

