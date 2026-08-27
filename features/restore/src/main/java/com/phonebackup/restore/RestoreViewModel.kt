package com.phonebackup.restore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonebackup.core.data.local.model.BackupRecord
import com.phonebackup.core.data.repository.BackupRepository
import com.phonebackup.core.data.repository.RestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestoreViewModel @Inject constructor(
    private val restoreEngine: RestoreEngine,
    private val backupRepository: BackupRepository,
    private val restoreRepository: RestoreRepository
) : ViewModel() {
    
    private val _restoreState = MutableStateFlow<RestoreProgress>(RestoreProgress.Initializing)
    val restoreState: StateFlow<RestoreProgress> = _restoreState.asStateFlow()
    
    private val _availableBackups = MutableStateFlow<List<BackupRecord>>(emptyList())
    val availableBackups: StateFlow<List<BackupRecord>> = _availableBackups.asStateFlow()
    
    init {
        loadAvailableBackups()
    }
    
    private fun loadAvailableBackups() {
        viewModelScope.launch {
            backupRepository.getAllBackups().collect { backups ->
                _availableBackups.value = backups.filter {
                    it.status == com.phonebackup.core.data.local.model.BackupStatus.COMPLETED ||
                        it.status == com.phonebackup.core.data.local.model.BackupStatus.VERIFIED
                }
            }
        }
    }
    
    fun startRestore(
        backupUUID: String,
        restoreContacts: Boolean,
        restoreMessages: Boolean,
        restoreMedia: Boolean,
        restoreWhatsApp: Boolean
    ) {
        viewModelScope.launch {
            val options = RestoreOptions(
                restoreContacts = restoreContacts,
                restoreMessages = restoreMessages,
                restoreMedia = restoreMedia,
                restoreWhatsApp = restoreWhatsApp
            )
            
            restoreEngine.executeRestore(backupUUID, options).collect { progress ->
                _restoreState.value = progress
            }
        }
    }
}
