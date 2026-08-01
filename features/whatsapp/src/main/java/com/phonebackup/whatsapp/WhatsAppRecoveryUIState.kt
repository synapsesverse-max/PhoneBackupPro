package com.phonebackup.whatsapp

sealed class WhatsAppRecoveryUIState {
    object Idle : WhatsAppRecoveryUIState()
    data class InProgress(val phase: String, val progress: Int) : WhatsAppRecoveryUIState()
    data class Completed(val messagesCount: Int, val mediaCount: Int) : WhatsAppRecoveryUIState()
    data class Error(val message: String) : WhatsAppRecoveryUIState()
}
