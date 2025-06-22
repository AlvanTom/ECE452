package com.example.ece452.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ece452.ui.components.BottomBar
import com.example.ece452.ui.screens.FeedScreen
import com.example.ece452.ui.screens.LoginScreen
import com.example.ece452.ui.screens.PostScreen
import com.example.ece452.ui.screens.NewSessionScreen
import com.example.ece452.ui.screens.RouteScreen
import com.example.ece452.ui.screens.SignupScreen
import com.example.ece452.ui.screens.SessionHistoryScreen
import com.example.ece452.ui.screens.ActiveSessionScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ece452.ui.viewmodels.SessionViewModel

@Composable
fun AppNavHost(modifier: Modifier = Modifier){
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val sessionViewModel: SessionViewModel = viewModel()

    Scaffold(
        bottomBar = {
            if (currentRoute != Routes.Login.name && currentRoute != Routes.Signup.name) {
                BottomBar(navController = navController)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Login.name,
            modifier = modifier.padding(innerPadding),
        ) {
            composable(Routes.Login.name) {
                LoginScreen(navController = navController)
            }
            composable(Routes.Signup.name) {
                SignupScreen(navController = navController)
            }
            composable(Routes.Feed.name) {
                FeedScreen()
            }
            composable(Routes.Sessions.name) {
                SessionHistoryScreen(navController = navController, sessionViewModel = sessionViewModel)
            }
            composable(Routes.Posts.name) {
                PostScreen()
            }
            composable(Routes.NewSession.name) {
                NewSessionScreen(navController = navController, sessionViewModel = sessionViewModel)
            }
            composable(Routes.Route.name) {
                RouteScreen(navController = navController, sessionViewModel = sessionViewModel)
            }
//            composable("${Routes.ActiveSession.name}/{sessionId}") { backStackEntry ->
//                val sessionId = backStackEntry.arguments?.getString("sessionId")
//                ActiveSessionScreen(navController = navController, sessionId = sessionId)
//            }
            composable(Routes.ActiveSession.name) {
                ActiveSessionScreen(navController = navController, sessionViewModel = sessionViewModel)
            }
        }
    }
}

