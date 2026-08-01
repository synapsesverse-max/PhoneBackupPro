package com.phonebackup.core.util

object Constants {
    // App
    const val APP_NAME = "PhoneBackup Pro"
    const val APP_VERSION = "1.0.0"
    const val APP_PACKAGE = "com.phonebackup.pro"
    
    // Backup
    const val MAX_BACKUP_SIZE = 10L * 1024 * 1024 * 1024 // 10GB
    const val BACKUP_FORMAT_VERSION = "1.0"
    const val DEFAULT_BUFFER_SIZE = 8192
    
    // Encryption
    const val ENCRYPTION_ALGORITHM = "AES-256-GCM"
    const val KEY_ITERATION_COUNT = 10000
    const val KEY_LENGTH = 256
    const val SALT_SIZE = 16
    const val IV_SIZE = 12
    const val GCM_TAG_LENGTH = 128
    
    // Network
    const val DEFAULT_TRANSFER_PORT = 9876
    const val TRANSFER_BUFFER_SIZE = 65536
    const val CONNECTION_TIMEOUT = 30 // seconds
    
    // WhatsApp
    const val WHATSAPP_PACKAGE = "com.whatsapp"
    const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    const val WHATSAPP_DB_PATTERN = "msgstore*.db*"
    const val WHATSAPP_CRYPT_VERSIONS = listOf("crypt12", "crypt14", "crypt15")
    
    // Third-party tools
    const val DRFONE_PACKAGE = "com.wondershare.drfone"
    const val ANYDROID_PACKAGE = "com.imobie.anydroid"
    const val MOBILETRANS_PACKAGE = "com.wondershare.mobiletrans"
    const val IMYFONE_PACKAGE = "com.imobie.dback"
    
    // Database
    const val DATABASE_NAME = "phonebackup_pro.db"
    const val DATABASE_VERSION = 1
    
    // Notification channels
    const val BACKUP_CHANNEL_ID = "backup_channel"
    const val RESTORE_CHANNEL_ID = "restore_channel"
    const val TRANSFER_CHANNEL_ID = "transfer_channel"
    const val NOTIFICATION_ID_BACKUP = 1001
    const val NOTIFICATION_ID_RESTORE = 1002
    const val NOTIFICATION_ID_TRANSFER = 1003
}
