package com.phonebackup.pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phonebackup.transfer.TransferViewModel
import com.phonebackup.transfer.TransferProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: TransferViewModel = hiltViewModel()
) {
    val transferState by viewModel.transferState.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    
    var selectedDevice by remember { mutableStateOf<String?>(null) }
    var transferMethod by remember { mutableStateOf("wifi") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Transfer Backup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Transfer method selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Transfer Method", style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = transferMethod == "wifi",
                        onClick = { transferMethod = "wifi" }
                    )
                    Icon(Icons.Default.Wifi, null, modifier = Modifier.padding(start = 8.dp))
                    Text("WiFi Direct", modifier = Modifier.padding(start = 8.dp))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = transferMethod == "bluetooth",
                        onClick = { transferMethod = "bluetooth" }
                    )
                    Icon(Icons.Default.Bluetooth, null, modifier = Modifier.padding(start = 8.dp))
                    Text("Bluetooth", modifier = Modifier.padding(start = 8.dp))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = transferMethod == "web",
                        onClick = { transferMethod = "web" }
                    )
                    Icon(Icons.Default.Language, null, modifier = Modifier.padding(start = 8.dp))
                    Text("Web Server", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Device discovery
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nearby Devices", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { viewModel.startDiscovery() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
                
                if (discoveredDevices.isEmpty()) {
                    Text(
                        "No devices found. Start discovery.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    discoveredDevices.forEach { device ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedDevice == device.address)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                            onClick = { selectedDevice = device.address }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PhoneAndroid, null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(device.name)
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Transfer button
        Button(
            onClick = {
                selectedDevice?.let { device ->
                    viewModel.startTransfer(device, transferMethod)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedDevice != null
        ) {
            Icon(Icons.Default.SwapHoriz, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Transfer")
        }
        
        // Progress
        when (val state = transferState) {
            is TransferProgress.InProgress -> {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = state.percentage / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${state.percentage}%",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is TransferProgress.Completed -> {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text("Transfer completed!", modifier = Modifier.padding(16.dp))
                }
            }
            else -> {}
        }
    }
}
