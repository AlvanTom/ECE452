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
import com.example.ece452.ui.screens.PersonalPostsScreen
import com.example.ece452.ui.screens.NewSessionScreen
import com.example.ece452.ui.screens.RouteScreen
import com.example.ece452.ui.screens.SignupScreen
import com.example.ece452.ui.screens.SessionHistoryScreen
import com.example.ece452.ui.screens.ActiveSessionScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ece452.ui.screens.AttemptScreen
import com.example.ece452.ui.viewmodels.SessionViewModel
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import com.example.ece452.firebase.FirebaseConfig
import kotlinx.coroutines.launch
import com.example.ece452.ui.components.AppDrawer
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.ece452.ui.components.TopBar

@Composable
fun AppNavHost(modifier: Modifier = Modifier){
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val sessionViewModel: SessionViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    AppDrawer(
        drawerState = drawerState,
        scope = scope,
        onProfileClick = { /* Add profile logic if needed */ },
        onMyPostsClick = {
            navController.navigate(Routes.PersonalPosts.name)
        },
        onLogoutClick = {
            FirebaseConfig.auth.signOut()
            navController.navigate(Routes.Login.name) {
                popUpTo(0) { inclusive = true }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentRoute != Routes.Login.name && currentRoute != Routes.Signup.name) {
                    TopBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onProfileClick = { scope.launch { drawerState.open() } },
                    )
                }
            },
            bottomBar = {
                if (currentRoute != Routes.Login.name && currentRoute != Routes.Signup.name && currentRoute != Routes.PersonalPosts.name) {
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
                composable(Routes.PersonalPosts.name) {
                    PersonalPostsScreen(navController = navController)
                }
                composable(Routes.Sessions.name) {
                    SessionHistoryScreen(navController = navController, sessionViewModel = sessionViewModel)
                }
                composable(Routes.Posts.name) {
                    PostScreen()
                }
                composable(
                    "${Routes.NewSession.name}?sessionId={sessionId}",
                    arguments = listOf(navArgument("sessionId") { type = NavType.StringType; defaultValue = "" })
                ) { backStackEntry ->
                    val sessionId = backStackEntry.arguments?.getString("sessionId")
                    NewSessionScreen(navController = navController, sessionViewModel = sessionViewModel, sessionId = sessionId)
                }
                composable(Routes.Route.name) {
                    RouteScreen(navController = navController, sessionViewModel = sessionViewModel)
                }
                composable(
                    "Route/{routeIdx}",
                    arguments = listOf(navArgument("routeIdx") { type = NavType.IntType })
                ) { backStackEntry ->
                    val routeIdx = backStackEntry.arguments?.getInt("routeIdx")
                    RouteScreen(navController = navController, sessionViewModel = sessionViewModel, routeIdx = routeIdx)
                }
//            composable("${Routes.ActiveSession.name}/{sessionId}") { backStackEntry ->
//                val sessionId = backStackEntry.arguments?.getString("sessionId")
//                ActiveSessionScreen(navController = navController, sessionId = sessionId)
//            }
                composable(Routes.ActiveSession.name) {
                    ActiveSessionScreen(navController = navController, sessionViewModel = sessionViewModel)
                }
                composable(Routes.Attempt.name) {
                    AttemptScreen(navController = navController, sessionViewModel = sessionViewModel)
                }
                composable(
                    "Attempt/{routeIdx}",
                    arguments = listOf(navArgument("routeIdx") { type = NavType.IntType })
                ) { backStackEntry ->
                    val routeIdx = backStackEntry.arguments?.getInt("routeIdx")
                    AttemptScreen(navController = navController, sessionViewModel = sessionViewModel, routeIdx = routeIdx)
                }
            }
        }
    }
}

