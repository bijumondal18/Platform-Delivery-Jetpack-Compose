package com.platform.platformdelivery.presentation.pages.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.platform.platformdelivery.R
import com.platform.platformdelivery.core.theme.AppTypography
import com.platform.platformdelivery.presentation.widgets.AppTextField
import com.platform.platformdelivery.presentation.widgets.PrimaryButton

@Composable
fun LoginScreen(navController: NavController, modifier: Modifier = Modifier) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_delivery_truck),
            contentDescription = "app_logo",
            modifier = Modifier
                .size(150.dp)
                .align(alignment = Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Welcome To Platform Delivery",
            style = AppTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "Please login to continue",
            style = AppTypography.bodyMedium.copy(fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        AppTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = "Enter email address",
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            errorMessage = emailError
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null
            },
            label = "Enter your password",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            isError = passwordError != null,
            errorMessage = passwordError
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            content = {
                Text(
                    "Forget Password?",
                    style = AppTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            onClick = {
                navController.navigate("forgot_password") {
                    popUpTo("login") { inclusive = false }
                }
            },
            modifier = Modifier.align(alignment = Alignment.End)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = "Login",
            enabled = email.isNotEmpty() && password.isNotEmpty() && password.length >= 6,
            onClick = {
                var valid = true
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailError = "Please enter a valid email"
                    valid = false
                }
                if (password.length < 6) {
                    passwordError = "Password must be at least 6 characters"
                    valid = false
                }

                if (valid) {
                    // Save login state here (DataStore/SharedPreferences)
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }

            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                "Don't have an account?",
                style = AppTypography.labelLarge.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton (
                content = {
                    Text(
                        "Sign Up",
                        style = AppTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    navController.navigate("signup") {
                        popUpTo("login") { inclusive = false }
                    }
                },
            )
        }

    }
}