package com.example.ece452.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ece452.ui.login.LoginState
import com.example.ece452.ui.login.LoginViewModel
import com.example.ece452.ui.theme.AppTypography
import com.example.ece452.ui.theme.backgroundLight
import com.example.ece452.ui.theme.primaryLight

@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by loginViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            navController.navigate("feed") {
                // Clear back stack
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo/Title
            Text(
                text = "Climbr",
                style = AppTypography.displayMedium.copy(
                    color = primaryLight,
                    fontSize = 36.sp
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Sign in box
            Surface(
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp,
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign in",
                        style = AppTypography.titleMedium.copy(color = primaryLight),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = {
                            loginViewModel.signIn(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(6.dp),
                        enabled = loginState !is LoginState.Loading
                    ) {
                        if (loginState is LoginState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Sign in")
                        }
                    }
                    if (loginState is LoginState.Error) {
                        Text(
                            text = (loginState as LoginState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "or sign in with",
                style = AppTypography.bodyMedium.copy(color = primaryLight),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* Handle Google sign-in */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Google")
                }
                OutlinedButton(
                    onClick = { /* Handle Facebook sign-in */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Facebook")
                }
                OutlinedButton(
                    onClick = { /* Handle Apple sign-in */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apple")
                }
                OutlinedButton(
                    onClick = { /* Handle Microsoft sign-in */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Microsoft")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Don't have an account? Sign up instead",
                style = AppTypography.bodyMedium.copy(color = primaryLight),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable { navController.navigate("signup") }
            )
        }
    }
}

@Composable
fun SocialLoginButton(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        shadowElevation = 2.dp,
        color = Color.White,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 20.sp, color = primaryLight)
        }
    }
}