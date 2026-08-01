package com.phonebackup.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonebackup.core.data.local.model.BackupRecord
import com.phonebackup.core.data.local.model.BackupType
import com.phonebackup.core.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupEngine: BackupEngine,
    private val backupRepository: BackupRepository
) : ViewModel() {
    
    private val _backupState = MutableStateFlow<BackupProgress>(BackupProgress.Initializing)
    val backupState: StateFlow<BackupProgress> = _backupState.asStateFlow()
    
    private val _backupHistory = MutableStateFlow<List<BackupRecord>>(emptyList())
    val backupHistory: StateFlow<List<BackupRecord>> = _backupHistory.asStateFlow()
    
    init {
        loadBackupHistory()
    }
    
    private fun loadBackupHistory() {
        viewModelScope.launch {
            backupRepository.getAllBackups().collect { backups ->
                _backupHistory.value = backups
            }
        }
    }
    
    fun startBackup(backupType: BackupType, options: BackupOptions) {
        viewModelScope.launch {
            val backupRecord = BackupRecord(
                backupType = backupType,
                deviceInfo = android.os.Build.MODEL,
                osVersion = android.os.Build.VERSION.SDK_INT.toString()
            )
            
            val backupId = backupRepository.insertBackup(backupRecord)
            val record = backupRecord.copy(id = backupId)
            
            backupEngine.executeFullBackup(record, options).collect { progress ->
                _backupState.value = progress
            }
        }
    }
    
    fun cancelBackup() {
        // Cancel backup operation
    }
}
