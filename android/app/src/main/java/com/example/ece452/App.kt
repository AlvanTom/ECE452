package com.example.ece452

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ece452.navigation.AppNavHost
import com.example.ece452.ui.theme.AppTheme

@Composable
fun ClimbrApp() {
    AppTheme {
         AppNavHost(modifier = Modifier)
    }
}
