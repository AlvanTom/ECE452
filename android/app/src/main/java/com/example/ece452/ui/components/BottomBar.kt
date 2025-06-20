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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.ece452.navigation.Routes

@Composable
fun BottomBar(navController: NavHostController, modifier: Modifier = Modifier){
    var selectedItem by remember { mutableIntStateOf(0) }
    val routes = arrayOf(Routes.Feed, Routes.Posts, Routes.Sessions).map { it.name }
    val routeIcons = arrayOf(Icons.Outlined.BookmarkBorder, Icons.Outlined.FileUpload, Icons.Outlined.Add)

    NavigationBar (modifier = modifier){
        for ((index, route) in routes.withIndex()) {
            NavigationBarItem(
                icon = { Icon(routeIcons[index], contentDescription = route) },
                label = { Text(route) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(route)
                }
            )
        }
    }
}