package com.platform.platformdelivery.presentation.pages.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.platform.platformdelivery.core.theme.AppTypography

@Composable
fun DrawerHeader(
    name: String,
    email: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()

    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile placeholder",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
        }

        Spacer(Modifier.width(8.dp))

        // Name + email
        Column(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                style = AppTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = email,
                style = AppTypography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}