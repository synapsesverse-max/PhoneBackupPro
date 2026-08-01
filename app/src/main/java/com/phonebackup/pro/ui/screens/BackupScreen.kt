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
import com.phonebackup.backup.BackupViewModel
import com.phonebackup.backup.BackupOptions
import com.phonebackup.backup.BackupProgress
import com.phonebackup.backup.MediaType
import com.phonebackup.core.data.local.model.BackupType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = hiltViewModel()
) {
    val backupState by viewModel.backupState.collectAsState()
    val backupHistory by viewModel.backupHistory.collectAsState()
    
    var selectedBackupType by remember { mutableStateOf(BackupType.FULL_PHONE) }
    var includeContacts by remember { mutableStateOf(true) }
    var includeMessages by remember { mutableStateOf(true) }
    var includeMedia by remember { mutableStateOf(true) }
    var includeWhatsApp by remember { mutableStateOf(true) }
    var encryptBackup by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Create Backup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Backup Type Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Backup Type", style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedBackupType == BackupType.FULL_PHONE,
                        onClick = { selectedBackupType = BackupType.FULL_PHONE }
                    )
                    Text("Full Phone Backup", modifier = Modifier.padding(start = 8.dp))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedBackupType == BackupType.WHATSAPP_ONLY,
                        onClick = { selectedBackupType = BackupType.WHATSAPP_ONLY }
                    )
                    Text("WhatsApp Only", modifier = Modifier.padding(start = 8.dp))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedBackupType == BackupType.CONTACTS_ONLY,
                        onClick = { selectedBackupType = BackupType.CONTACTS_ONLY }
                    )
                    Text("Contacts Only", modifier = Modifier.padding(start = 8.dp))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedBackupType == BackupType.MEDIA_ONLY,
                        onClick = { selectedBackupType = BackupType.MEDIA_ONLY }
                    )
                    Text("Media Only", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Options", style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeContacts, onCheckedChange = { includeContacts = it })
                    Text("Include Contacts")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeMessages, onCheckedChange = { includeMessages = it })
                    Text("Include Messages")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeMedia, onCheckedChange = { includeMedia = it })
                    Text("Include Media")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = includeWhatsApp, onCheckedChange = { includeWhatsApp = it })
                    Text("Include WhatsApp")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = encryptBackup, onCheckedChange = { encryptBackup = it })
                    Text("Encrypt Backup")
                }
                
                if (encryptBackup) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Encryption Password") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Start Backup Button
        Button(
            onClick = {
                viewModel.startBackup(
                    backupType = selectedBackupType,
                    options = BackupOptions(
                        includeContacts = includeContacts,
                        includeMessages = includeMessages,
                        includeMedia = includeMedia,
                        includeWhatsApp = includeWhatsApp,
                        encryptionPassword = if (encryptBackup) password else null
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = backupState !is BackupProgress.Processing
        ) {
            if (backupState is BackupProgress.Processing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Backing up...")
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Backup")
            }
        }
        
        // Progress
        when (val state = backupState) {
            is BackupProgress.Processing -> {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = state.progress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = state.phase,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is BackupProgress.Completed -> {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backup completed successfully!",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            is BackupProgress.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            else -> {}
        }
        
        // Backup History
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Backup History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        LazyColumn {
            items(backupHistory) { backup ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                        
                        AssistChip(
                            onClick = {},
                            label = { Text(backup.status.name) }
                        )
                    }
                }
            }
        }
    }
}
