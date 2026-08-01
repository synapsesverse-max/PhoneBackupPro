package com.phonebackup.pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phonebackup.restore.RestoreViewModel
import com.phonebackup.restore.RestoreProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreScreen(
    viewModel: RestoreViewModel = hiltViewModel()
) {
    val restoreState by viewModel.restoreState.collectAsState()
    val availableBackups by viewModel.availableBackups.collectAsState()
    
    var selectedBackup by remember { mutableStateOf<String?>(null) }
    var restoreContacts by remember { mutableStateOf(true) }
    var restoreMessages by remember { mutableStateOf(true) }
    var restoreMedia by remember { mutableStateOf(true) }
    var restoreWhatsApp by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Restore Backup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Select backup
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Backup", style = MaterialTheme.typography.titleMedium)
                
                if (availableBackups.isEmpty()) {
                    Text(
                        text = "No backups available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(availableBackups) { backup ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedBackup == backup.backupUUID)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                ),
                                onClick = { selectedBackup = backup.backupUUID }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = backup.backupType.name,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = java.text.SimpleDateFormat(
                                                "dd/MM/yyyy HH:mm",
                                                java.util.Locale.getDefault()
                                            ).format(java.util.Date(backup.startTime)),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(
                                        text = formatSize(backup.backupSize),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Restore options
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Restore Options", style = MaterialTheme.typography.titleMedium)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = restoreContacts, onCheckedChange = { restoreContacts = it })
                    Text("Contacts")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = restoreMessages, onCheckedChange = { restoreMessages = it })
                    Text("Messages")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = restoreMedia, onCheckedChange = { restoreMedia = it })
                    Text("Media")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = restoreWhatsApp, onCheckedChange = { restoreWhatsApp = it })
                    Text("WhatsApp")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Restore button
        Button(
            onClick = {
                selectedBackup?.let { uuid ->
                    viewModel.startRestore(
                        backupUUID = uuid,
                        restoreContacts = restoreContacts,
                        restoreMessages = restoreMessages,
                        restoreMedia = restoreMedia,
                        restoreWhatsApp = restoreWhatsApp
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedBackup != null && restoreState !is RestoreProgress.Processing
        ) {
            if (restoreState is RestoreProgress.Processing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restoring...")
            } else {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore Backup")
            }
        }
        
        // Progress
        when (val state = restoreState) {
            is RestoreProgress.Processing -> {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = state.progress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = state.phase,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is RestoreProgress.Completed -> {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore completed!")
                    }
                }
            }
            is RestoreProgress.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(state.message, modifier = Modifier.padding(16.dp))
                }
            }
            else -> {}
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "%.1f GB".format(bytes.toDouble() / (1024 * 1024 * 1024))
    }
}
