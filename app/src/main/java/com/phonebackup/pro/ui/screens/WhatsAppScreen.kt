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
import com.phonebackup.whatsapp.WhatsAppRecoveryViewModel
import com.phonebackup.whatsapp.RecoveryProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppScreen(
    viewModel: WhatsAppRecoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var backupPath by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "WhatsApp Recovery",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Recovery Options", style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { viewModel.recoverFromLocalBackup() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhoneAndroid, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recover from Local Backup")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { viewModel.recoverFromGoogleDrive() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Cloud, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recover from Google Drive")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = backupPath,
                    onValueChange = { backupPath = it },
                    label = { Text("Third-party backup path") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { viewModel.recoverFromThirdParty(backupPath) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = backupPath.isNotEmpty()
                ) {
                    Icon(Icons.Default.FolderOpen, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recover from Third-Party Backup")
                }
                
                Text(
                    text = "Supports: Dr.Fone, AnyDroid, MobileTrans, iMyFone",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (val state = uiState) {
            is WhatsAppRecoveryUIState.InProgress -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LinearProgressIndicator(
                            progress = state.progress / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(state.phase, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            is WhatsAppRecoveryUIState.Completed -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Recovery Completed!", fontWeight = FontWeight.Bold)
                        }
                        Text("Messages: ${state.messagesCount}")
                        Text("Media: ${state.mediaCount}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.restoreToWhatsApp() }) {
                            Text("Restore to WhatsApp")
                        }
                    }
                }
            }
            is WhatsAppRecoveryUIState.Error -> {
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

sealed class WhatsAppRecoveryUIState {
    object Idle : WhatsAppRecoveryUIState()
    data class InProgress(val phase: String, val progress: Int) : WhatsAppRecoveryUIState()
    data class Completed(val messagesCount: Int, val mediaCount: Int) : WhatsAppRecoveryUIState()
    data class Error(val message: String) : WhatsAppRecoveryUIState()
}
