package com.phonebackup.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "media_backups")
data class MediaBackup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "backup_uuid")
    val backupUUID: String,
    
    @ColumnInfo(name = "original_path")
    val originalPath: String,
    
    @ColumnInfo(name = "file_name")
    val fileName: String,
    
    @ColumnInfo(name = "media_type")
    val mediaType: String, // "photo", "video", "audio", "document"
    
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long,
    
    @ColumnInfo(name = "date_added")
    val dateAdded: Long,
    
    @ColumnInfo(name = "date_modified")
    val dateModified: Long,
    
    @ColumnInfo(name = "backup_path")
    val backupPath: String
)
