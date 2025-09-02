package com.platform.platformdelivery.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.platform.platformdelivery.R
import com.platform.platformdelivery.core.theme.SuccessGreen

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = singleLine,
            maxLines = maxLines,
            isError = isError,
            shape = MaterialTheme.shapes.medium,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible) {
                        painterResource(id = R.drawable.ic_visibility_off)
                    } else {
                        painterResource(id = R.drawable.ic_visibility)
                    }
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = image, contentDescription = null)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),   // When active
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), // When inactive
                disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
    }

    if (isError && errorMessage != null) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

