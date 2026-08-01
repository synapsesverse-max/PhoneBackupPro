package com.phonebackup.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.UUID

@Entity(tableName = "backup_records")
data class BackupRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "backup_uuid")
    val backupUUID: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "backup_type")
    val backupType: BackupType,
    
    @ColumnInfo(name = "device_info")
    val deviceInfo: String,
    
    @ColumnInfo(name = "os_version")
    val osVersion: String,
    
    @ColumnInfo(name = "backup_size")
    val backupSize: Long = 0,
    
    @ColumnInfo(name = "compressed_size")
    val compressedSize: Long = 0,
    
    @ColumnInfo(name = "file_count")
    val fileCount: Int = 0,
    
    @ColumnInfo(name = "start_time")
    val startTime: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "end_time")
    val endTime: Long = 0,
    
    @ColumnInfo(name = "status")
    val status: BackupStatus = BackupStatus.IN_PROGRESS,
    
    @ColumnInfo(name = "encryption_type")
    val encryptionType: EncryptionType = EncryptionType.AES_256_GCM,
    
    @ColumnInfo(name = "checksum")
    val checksum: String = "",
    
    @ColumnInfo(name = "metadata")
    val metadata: String = "{}"
)

enum class BackupType {
    FULL_PHONE,
    INCREMENTAL,
    APP_DATA,
    WHATSAPP_ONLY,
    CONTACTS_ONLY,
    MEDIA_ONLY,
    CUSTOM
}

enum class BackupStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    VERIFIED,
    RESTORING,
    RESTORED
}

enum class EncryptionType {
    NONE,
    AES_256,
    AES_256_GCM,
    RSA_AES
}
