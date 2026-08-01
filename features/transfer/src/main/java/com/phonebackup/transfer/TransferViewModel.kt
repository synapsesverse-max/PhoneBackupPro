package com.phonebackup.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val crossPlatformTransfer: CrossPlatformTransfer
) : ViewModel() {
    
    private val _transferState = MutableStateFlow<TransferProgress>(TransferProgress.InProgress(0))
    val transferState: StateFlow<TransferProgress> = _transferState.asStateFlow()
    
    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()
    
    fun startDiscovery() {
        viewModelScope.launch {
            crossPlatformTransfer.startDiscovery().collect { device ->
                _discoveredDevices.value = _discoveredDevices.value + device
            }
        }
    }
    
    fun startTransfer(deviceAddress: String, method: String) {
        viewModelScope.launch {
            // Start transfer
            crossPlatformTransfer.startTransfer(
                backupFile = java.io.File(""),
                targetDevice = deviceAddress,
                method = method
            ).collect { progress ->
                _transferState.value = progress
            }
        }
    }
}
