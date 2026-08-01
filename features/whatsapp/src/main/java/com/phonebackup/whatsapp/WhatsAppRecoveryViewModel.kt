package com.phonebackup.whatsapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonebackup.pro.ui.screens.WhatsAppRecoveryUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhatsAppRecoveryViewModel @Inject constructor(
    private val whatsAppBackupManager: WhatsAppBackupManager,
    private val thirdPartyRecovery: ThirdPartyRecovery
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WhatsAppRecoveryUIState>(WhatsAppRecoveryUIState.Idle)
    val uiState: StateFlow<WhatsAppRecoveryUIState> = _uiState.asStateFlow()
    
    fun recoverFromLocalBackup() {
        viewModelScope.launch {
            _uiState.value = WhatsAppRecoveryUIState.InProgress("Scanning local backups", 20)
            
            try {
                val backups = whatsAppBackupManager.detectBackups()
                if (backups.isNotEmpty()) {
                    val backupDir = java.io.File(backups.first().path).parentFile
                    val result = whatsAppBackupManager.createBackup(backupDir!!)
                    
                    result.onSuccess { data ->
                        _uiState.value = WhatsAppRecoveryUIState.Completed(
                            messagesCount = data.messagesCount,
                            mediaCount = data.mediaCount
                        )
                    }.onFailure { error ->
                        _uiState.value = WhatsAppRecoveryUIState.Error(error.message ?: "Recovery failed")
                    }
                } else {
                    _uiState.value = WhatsAppRecoveryUIState.Error("No local backups found")
                }
            } catch (e: Exception) {
                _uiState.value = WhatsAppRecoveryUIState.Error(e.message ?: "Error")
            }
        }
    }
    
    fun recoverFromGoogleDrive() {
        viewModelScope.launch {
            _uiState.value = WhatsAppRecoveryUIState.InProgress("Connecting to Google Drive", 10)
            // Google Drive recovery implementation
            _uiState.value = WhatsAppRecoveryUIState.Error("Google Drive recovery not yet implemented")
        }
    }
    
    fun recoverFromThirdParty(backupPath: String) {
        viewModelScope.launch {
            thirdPartyRecovery.recoverFromBackup(backupPath).collect { progress ->
                when (progress) {
                    is RecoveryProgress.Detecting -> {
                        _uiState.value = WhatsAppRecoveryUIState.InProgress("Detecting backup type", 10)
                    }
                    is RecoveryProgress.Detected -> {
                        _uiState.value = WhatsAppRecoveryUIState.InProgress(
                            "Detected: ${progress.type}", 20
                        )
                    }
                    is RecoveryProgress.Extracting -> {
                        _uiState.value = WhatsAppRecoveryUIState.InProgress(
                            "Extracting data...", 50
                        )
                    }
                    is RecoveryProgress.Completed -> {
                        _uiState.value = WhatsAppRecoveryUIState.Completed(
                            messagesCount = progress.result.messages.size,
                            mediaCount = progress.result.mediaFiles.size
                        )
                    }
                    is RecoveryProgress.Error -> {
                        _uiState.value = WhatsAppRecoveryUIState.Error(progress.message)
                    }
                }
            }
        }
    }
    
    fun restoreToWhatsApp() {
        viewModelScope.launch {
            _uiState.value = WhatsAppRecoveryUIState.InProgress("Restoring to WhatsApp", 80)
            // Restore implementation
        }
    }
}
