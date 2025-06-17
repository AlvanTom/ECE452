package com.example.ece452.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ece452.ui.components.BottomBar
import com.example.ece452.ui.screens.FeedScreen
import com.example.ece452.ui.screens.LoginScreen
import com.example.ece452.ui.screens.PostScreen
import com.example.ece452.ui.screens.SessionsScreen

@Composable
fun AppNavHost(modifier: Modifier = Modifier){
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Feed.name,
            modifier = modifier.padding(innerPadding),
        ) {
            composable(Routes.Login.name) {
                LoginScreen()
            }
            composable(Routes.Feed.name) {
                FeedScreen()
            }
            composable(Routes.Sessions.name) {
                SessionsScreen()
            }
            composable(Routes.Posts.name) {
                PostScreen()
            }
        }
    }
}

