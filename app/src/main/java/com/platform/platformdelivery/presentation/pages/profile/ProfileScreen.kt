package com.platform.platformdelivery.presentation.pages.profile

import android.util.Patterns
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.presentation.widgets.AppTextField
import com.platform.platformdelivery.presentation.widgets.PrimaryButton

@Composable
fun ProfileScreen() {

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    var address by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf<String?>(null) }

    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
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
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = "Email",
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            errorMessage = emailError,
            enabled = false
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

        AppTextField(
            value = phone,
            onValueChange = {
                phone = it
                phoneError = null
            },
            label = "Phone",
            keyboardType = KeyboardType.Phone,
            isError = phoneError != null,
            errorMessage = phoneError
        )

        PrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Update",
            onClick = {

            },
        )

    }

}