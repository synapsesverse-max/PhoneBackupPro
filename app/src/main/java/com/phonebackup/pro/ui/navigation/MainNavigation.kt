package com.phonebackup.pro.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.phonebackup.pro.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CloudUpload, "Backup") },
                    label = { Text("Backup") },
                    selected = currentRoute == "backup",
                    onClick = { navController.navigate("backup") { popUpTo("backup") { inclusive = true } } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CloudDownload, "Restore") },
                    label = { Text("Restore") },
                    selected = currentRoute == "restore",
                    onClick = { navController.navigate("restore") { popUpTo("restore") { inclusive = true } } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SwapHoriz, "Transfer") },
                    label = { Text("Transfer") },
                    selected = currentRoute == "transfer",
                    onClick = { navController.navigate("transfer") { popUpTo("transfer") { inclusive = true } } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, "WhatsApp") },
                    label = { Text("WhatsApp") },
                    selected = currentRoute == "whatsapp",
                    onClick = { navController.navigate("whatsapp") { popUpTo("whatsapp") { inclusive = true } } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") { popUpTo("settings") { inclusive = true } } }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "backup",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("backup") { BackupScreen() }
            composable("restore") { RestoreScreen() }
            composable("transfer") { TransferScreen() }
            composable("whatsapp") { WhatsAppScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
