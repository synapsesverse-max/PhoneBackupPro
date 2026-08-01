package com.phonebackup.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "message_backups")
data class MessageBackup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "backup_uuid")
    val backupUUID: String,
    
    @ColumnInfo(name = "message_id")
    val messageId: Long,
    
    @ColumnInfo(name = "address")
    val address: String,
    
    @ColumnInfo(name = "body")
    val body: String,
    
    @ColumnInfo(name = "date")
    val date: Long,
    
    @ColumnInfo(name = "type")
    val type: String, // "sms", "mms", "sent", "received"
    
    @ColumnInfo(name = "read")
    val read: Boolean = false,
    
    @ColumnInfo(name = "thread_id")
    val threadId: Long? = null
)
