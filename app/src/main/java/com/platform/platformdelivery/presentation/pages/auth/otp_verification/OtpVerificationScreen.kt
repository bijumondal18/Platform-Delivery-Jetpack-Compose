package com.platform.platformdelivery.presentation.pages.auth.otp_verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.presentation.view_models.AuthViewModel
import com.platform.platformdelivery.presentation.widgets.PrimaryButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    navController: NavController,
    email: String,
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val otpDigits = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val verifyOtpState by viewModel.verifyOtpState.collectAsState()
    val resendOtpState by viewModel.resendOtpState.collectAsState()
    val isLoading = verifyOtpState is Result.Loading
    val isResending = resendOtpState is Result.Loading

    // Handle OTP verification result
    LaunchedEffect(verifyOtpState) {
        val state = verifyOtpState
        when (state) {
            is Result.Success -> {
                val successMessage = state.data?.data?.msg 
                    ?: state.data?.message 
                    ?: "OTP verified successfully!"
                
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = successMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                
                // Navigate to reset password screen
                delay(1500)
                navController.navigate("reset_password/$email") {
                    popUpTo("forgot_password") { inclusive = false }
                }
            }
            is Result.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Long
                    )
                }
                // Clear OTP on error
                otpDigits.replaceAll { "" }
                focusRequesters[0].requestFocus()
            }
            else -> {}
        }
    }

    // Handle resend OTP result
    LaunchedEffect(resendOtpState) {
        val state = resendOtpState
        when (state) {
            is Result.Success -> {
                val successMessage = state.data?.data?.msg 
                    ?: state.data?.message 
                    ?: "OTP resent successfully!"
                
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = successMessage,
                        duration = SnackbarDuration.Short
                    )
                }
                // Clear OTP fields
                otpDigits.replaceAll { "" }
                focusRequesters[0].requestFocus()
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

    // Auto-focus first field on launch
    LaunchedEffect(Unit) {
        delay(100)
        focusRequesters[0].requestFocus()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.resetVerifyOtpState()
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
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Verify OTP",
                style = AppTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    append("Enter the 6-digit code sent to ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(email)
                    }
                },
                style = AppTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // OTP Input Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                otpDigits.forEachIndexed { index, digit ->
                    OutlinedTextField(
                        value = digit,
                        onValueChange = { newValue ->
                            // Only allow single digit
                            if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                                otpDigits[index] = newValue
                                
                                // Auto-move to next field
                                if (newValue.isNotEmpty() && index < 5) {
                                    focusRequesters[index + 1].requestFocus()
                                }
                                
                                // Auto-submit when all fields are filled
                                if (index == 5 && newValue.isNotEmpty()) {
                                    val otp = otpDigits.joinToString("")
                                    if (otp.length == 6) {
                                        viewModel.verifyOtp(userId, otp)
                                    }
                                }
                            }
                            
                            // Handle backspace - move to previous field
                            if (newValue.isEmpty() && index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequesters[index]),
                        textStyle = AppTypography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            autoCorrect = false
                        ),
                        singleLine = true,
                        maxLines = 1,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Verify",
                enabled = otpDigits.all { it.isNotEmpty() } && !isLoading,
                isLoading = isLoading,
                onClick = {
                    val otp = otpDigits.joinToString("")
                    if (otp.length == 6) {
                        viewModel.verifyOtp(userId, otp)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        viewModel.resendOtp(email)
                    },
                    enabled = !isResending
                ) {
                    if (isResending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Resend OTP",
                            style = AppTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

