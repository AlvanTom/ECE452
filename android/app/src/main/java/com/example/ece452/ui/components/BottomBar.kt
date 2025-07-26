package com.example.ece452.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ece452.navigation.Routes

@Composable
fun BottomBar(navController: NavHostController, modifier: Modifier = Modifier) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination

    val routes = listOf(Routes.Feed.name, Routes.Posts.name, Routes.Sessions.name)
    val routeIcons = listOf(Icons.Outlined.BookmarkBorder, Icons.Outlined.FileUpload, Icons.Outlined.Add)
    val routeLabels = listOf("Feed", "Post", "Sessions")

    NavigationBar(modifier = modifier) {
        routes.forEachIndexed { index, route ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true
            NavigationBarItem(
                icon = { Icon(routeIcons[index], contentDescription = routeLabels[index]) },
                label = { Text(routeLabels[index]) },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(route)
                    }
                }
            )
        }
    }
}