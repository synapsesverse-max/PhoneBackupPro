package com.phonebackup.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CloudSyncViewModel @Inject constructor(
    private val cloudStorageManager: CloudStorageManager
) : ViewModel() {
    
    private val _uploadState = MutableStateFlow<CloudUploadProgress>(CloudUploadProgress.InProgress(0))
    val uploadState: StateFlow<CloudUploadProgress> = _uploadState.asStateFlow()
    
    private val _downloadState = MutableStateFlow<CloudDownloadProgress>(CloudDownloadProgress.InProgress(0))
    val downloadState: StateFlow<CloudDownloadProgress> = _downloadState.asStateFlow()
    
    private val _syncState = MutableStateFlow<CloudSyncProgress>(CloudSyncProgress.Syncing(0))
    val syncState: StateFlow<CloudSyncProgress> = _syncState.asStateFlow()
    
    fun uploadBackup(file: java.io.File, provider: String) {
        viewModelScope.launch {
            cloudStorageManager.uploadBackup(file, provider).collect { progress ->
                _uploadState.value = progress
            }
        }
    }
    
    fun downloadBackup(url: String) {
        viewModelScope.launch {
            cloudStorageManager.downloadBackup(url).collect { progress ->
                _downloadState.value = progress
            }
        }
    }
    
    fun syncBackups() {
        viewModelScope.launch {
            cloudStorageManager.syncBackups().collect { progress ->
                _syncState.value = progress
            }
        }
    }
}
