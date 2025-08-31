package com.platform.platformdelivery.presentation.pages.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContent(
    selectedItem: String,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(fraction = 0.65f)
            .background(color = MaterialTheme.colorScheme.background)

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.17f)
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 16.dp)
        ) {
            //TODO: Add Drawer Header Content Here (If Any)
        }

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(start = 8.dp, top = 16.dp, end = 8.dp, bottom = 16.dp)
        ) {
            DrawerItem(
                label = "Home",
                icon = Icons.Default.Home,
                selected = selectedItem == DrawerDestinations.Home,
                onClick = { onItemClick("Home") },
                modifier = Modifier
            )

            DrawerItem(
                label = "Profile",
                icon = Icons.Default.Person,
                selected = selectedItem == DrawerDestinations.Profile,
                onClick = { onItemClick("Profile") },
                modifier = Modifier
            )

            DrawerItem(
                label = "Available Routes",
                icon = Icons.Filled.Place,
                selected = selectedItem == DrawerDestinations.AvailableRoutes,
                onClick = { onItemClick("Available Routes") },
                modifier = Modifier
            )

            DrawerItem(
                label = "Route History",
                icon = Icons.Filled.List,
                selected = selectedItem == DrawerDestinations.RouteHistory,
                onClick = { onItemClick("Route History") },
                modifier = Modifier
            )

            DrawerItem(
                label = "My Accepted Routes",
                icon = Icons.Default.Home,
                selected = selectedItem == DrawerDestinations.MyAcceptedRoutes,
                onClick = { onItemClick("My Accepted Routes") },
                modifier = Modifier
            )

            DrawerItem(
                label = "My Earnings",
                icon = Icons.Filled.ThumbUp,
                selected = selectedItem == DrawerDestinations.MyEarnings,
                onClick = { onItemClick("My Earnings") },
                modifier = Modifier
            )

            DrawerItem(
                label = "Contact Admin",
                icon = Icons.Default.Call,
                selected = selectedItem == DrawerDestinations.ContactAdmin,
                onClick = { onItemClick("Contact Admin") },
                modifier = Modifier
            )
        }

    }
}