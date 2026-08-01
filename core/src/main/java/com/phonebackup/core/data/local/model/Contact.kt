package com.phonebackup.core.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "contact_backups")
data class ContactBackup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "backup_uuid")
    val backupUUID: String,
    
    @ColumnInfo(name = "contact_id")
    val contactId: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "phone_numbers")
    val phoneNumbers: String, // JSON array of phone numbers
    
    @ColumnInfo(name = "emails")
    val emails: String, // JSON array of emails
    
    @ColumnInfo(name = "photo_uri")
    val photoUri: String?,
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
