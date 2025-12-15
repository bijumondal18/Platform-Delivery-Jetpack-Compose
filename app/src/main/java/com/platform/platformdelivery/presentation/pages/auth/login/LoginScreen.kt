package com.platform.platformdelivery.presentation.pages.auth.login

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.platform.platformdelivery.R
import com.platform.platformdelivery.core.network.Result
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.presentation.view_models.AuthViewModel
import com.platform.platformdelivery.presentation.widgets.AppTextField
import com.platform.platformdelivery.presentation.widgets.SocialLoginButton
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val loginState by viewModel.loginState.collectAsState()
    val isLoading = loginState is Result.Loading

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_delivery_truck),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Heading
            Text(
                text = "Log in or sign up",
                style = AppTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email Input
            AppTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = "Email",
                keyboardType = KeyboardType.Email,
                isError = emailError != null,
                errorMessage = emailError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            AppTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = "Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                isError = passwordError != null,
                errorMessage = passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Continue Button (Dark Gray Primary)
            Button(
                onClick = {
                    var valid = true
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailError = "Please enter a valid email"
                        valid = false
                    }
                    if (password.length < 6) {
                        passwordError = "Password must be at least 6 characters"
                        valid = false
                    }

                    if (valid) {
                        viewModel.login(email = email, password = password)
                    }
                },
                enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && password.length >= 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.background,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Continue",
                        style = AppTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Separator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "or",
                    style = AppTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Login Buttons
            SocialLoginButton(
                text = "Continue with Google",
                onClick = {
                    // TODO: Implement Google Sign In
                },
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    // Google G icon placeholder - you can replace with actual icon
                    Box(modifier = Modifier.size(20.dp)) {
                        Text("G", style = AppTypography.bodyMedium)
                    }
                }
            )

//            Spacer(modifier = Modifier.height(12.dp))
//
//            SocialLoginButton(
//                text = "Continue with Apple",
//                onClick = {
//                    // TODO: Implement Apple Sign In
//                },
//                modifier = Modifier.fillMaxWidth(),
//                icon = {
//                    // Apple icon placeholder
//                    Box(modifier = Modifier.size(20.dp)) {
//                        Text("🍎", style = AppTypography.bodyMedium)
//                    }
//                }
//            )

            Spacer(modifier = Modifier.height(12.dp))

            SocialLoginButton(
                text = "Continue with Facebook",
                onClick = {
                    // TODO: Implement Facebook Sign In
                },
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    // Facebook icon placeholder
                    Box(modifier = Modifier.size(20.dp)) {
                        Text("f", style = AppTypography.bodyMedium.copy(color = androidx.compose.ui.graphics.Color(0xFF1877F2)))
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Need help signing in?
            TextButton(
                onClick = {
                    navController.navigate("forgot_password") {
                        popUpTo("login") { inclusive = false }
                    }
                }
            ) {
                Text(
                    text = "Forgot Password?",
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Terms and Privacy Policy
            val termsText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))) {
                    append("By signing up, you are creating an account and agree to our ")
                }
                // Terms link
                pushStringAnnotation(tag = "terms", annotation = "terms_conditions")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Terms")
                }
                pop()
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))) {
                    append(" and ")
                }
                // Privacy Policy link
                pushStringAnnotation(tag = "privacy", annotation = "privacy_policy")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Privacy Policy")
                }
                pop()
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))) {
                    append(".")
                }
            }

            ClickableText(
                text = termsText,
                style = AppTypography.bodySmall.copy(textAlign = TextAlign.Center),
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = { offset ->
                    termsText.getStringAnnotations(
                        tag = "terms",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        navController.navigate("terms_conditions") {
                            popUpTo("login") { inclusive = false }
                        }
                        return@ClickableText
                    }
                    termsText.getStringAnnotations(
                        tag = "privacy",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        navController.navigate("privacy_policy") {
                            popUpTo("login") { inclusive = false }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Handle login result
    LaunchedEffect(loginState) {
        when (loginState) {
            is Result.Success -> {
                val data = (loginState as Result.Success).data
                if (data.data != null && data.data.status == true) {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    val error = data.data?.msg ?: "Invalid Credentials"
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = error,
                            withDismissAction = false
                        )
                    }
                    Log.e("LoginError", error)
                }
            }
            is Result.Error -> {
                val error = (loginState as Result.Error).message
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = error,
                        withDismissAction = true
                    )
                }
                Log.e("LoginError", error)
            }
            else -> Unit
        }
    }
}
