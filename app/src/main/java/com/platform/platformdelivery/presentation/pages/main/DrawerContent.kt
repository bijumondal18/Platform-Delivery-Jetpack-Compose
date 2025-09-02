package com.platform.platformdelivery.presentation.pages.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.platform.platformdelivery.R

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
            .statusBarsPadding()

    ) {

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(start = 8.dp, top = 16.dp, end = 8.dp, bottom = 16.dp)
        ) {
            DrawerItem(
                label = "Home",
                icon = painterResource(id = R.drawable.ic_home),
                selected = selectedItem == DrawerDestinations.Home,
                onClick = { onItemClick(DrawerDestinations.Home) },
                modifier = Modifier
            )

            DrawerItem(
                label = "Profile",
                icon = painterResource(id = R.drawable.ic_profile),
                selected = selectedItem == DrawerDestinations.Profile,
                onClick = { onItemClick(DrawerDestinations.Profile) },
                modifier = Modifier
            )

            DrawerItem(
                label = "Available Routes",
                icon = painterResource(id = R.drawable.ic_available_routes),
                selected = selectedItem == DrawerDestinations.AvailableRoutes,
                onClick = { onItemClick(DrawerDestinations.AvailableRoutes) },
                modifier = Modifier
            )

            DrawerItem(
                label = "Route History",
                icon = painterResource(id = R.drawable.ic_route_history),
                selected = selectedItem == DrawerDestinations.RouteHistory,
                onClick = { onItemClick(DrawerDestinations.RouteHistory) },
                modifier = Modifier
            )

            DrawerItem(
                label = "My Accepted Routes",
                icon = painterResource(id = R.drawable.ic_accepted_route),
                selected = selectedItem == DrawerDestinations.MyAcceptedRoutes,
                onClick = { onItemClick(DrawerDestinations.MyAcceptedRoutes) },
                modifier = Modifier
            )

            DrawerItem(
                label = "My Earnings",
                icon = painterResource(id = R.drawable.ic_my_earnings),
                selected = selectedItem == DrawerDestinations.MyEarnings,
                onClick = { onItemClick(DrawerDestinations.MyEarnings,) },
                modifier = Modifier
            )

            DrawerItem(
                label = "Contact Admin",
                icon = painterResource(id = R.drawable.ic_contact_admin),
                selected = selectedItem == DrawerDestinations.ContactAdmin,
                onClick = { onItemClick(DrawerDestinations.ContactAdmin) },
                modifier = Modifier
            )
        }

    }
}