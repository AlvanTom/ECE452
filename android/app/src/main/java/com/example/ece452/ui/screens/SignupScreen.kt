package com.example.ece452.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ece452.R
import com.example.ece452.ui.login.SignupState
import com.example.ece452.ui.login.SignupViewModel
import com.example.ece452.ui.theme.AppTypography
import com.example.ece452.ui.theme.backgroundLight
import com.example.ece452.ui.theme.primaryLight

@Composable
fun SignupScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    signupViewModel: SignupViewModel = viewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val signupState by signupViewModel.signupState.collectAsState()

    LaunchedEffect(signupState) {
        if (signupState is SignupState.Success) {
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
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Climbr Logo",
                modifier = Modifier
                    .height(72.dp)
                    .padding(bottom = 32.dp)
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
                        text = "Create Account",
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
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
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
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        isError = password != confirmPassword
                    )
                    Button(
                        onClick = {
                            if (password == confirmPassword) {
                                signupViewModel.signup(email, password, username)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(6.dp),
                        enabled = password == confirmPassword && signupState !is SignupState.Loading
                    ) {
                        if (signupState is SignupState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Create Account")
                        }
                    }
                    if (signupState is SignupState.Error) {
                        Text(
                            text = (signupState as SignupState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Have an account? Log in instead",
                style = AppTypography.bodyMedium.copy(color = primaryLight),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable { navController.navigate("login") }
            )

        }
    }
}

@Composable
fun SocialSignupButton(label: String) {
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