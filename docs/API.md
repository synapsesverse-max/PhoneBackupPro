# API Documentation

## Backup Engine API

### BackupEngine

```kotlin
class BackupEngine {
    suspend fun executeFullBackup(
        backupRecord: BackupRecord,
        options: BackupOptions
    ): Flow<BackupProgress>
}
```

### BackupOptions

```kotlin
data class BackupOptions(
    val includeContacts: Boolean = true,
    val includeMessages: Boolean = true,
    val includeCallLogs: Boolean = true,
    val includeCalendar: Boolean = true,
    val includeApps: Boolean = false,
    val selectedApps: List<String> = emptyList(),
    val includeMedia: Boolean = true,
    val mediaTypes: List<MediaType> = listOf(PHOTOS, VIDEOS),
    val includeWhatsApp: Boolean = false,
    val encryptionPassword: String? = null
)
```

## Encryption API

### EncryptionService

```kotlin
class EncryptionService {
    fun encrypt(data: ByteArray, password: String): ByteArray
    fun decrypt(encryptedData: ByteArray, password: String): ByteArray
    fun encryptFile(inputFile: File, outputFile: File, password: String)
    fun decryptFile(inputFile: File, outputFile: File, password: String)
    fun generateKey(): ByteArray
}
```

## Compression API

### CompressionService

```kotlin
class CompressionService {
    fun compressDirectory(sourceDir: File): File
    fun decompressToDirectory(zipFile: File, outputDir: File)
    fun compress(data: ByteArray): ByteArray
    fun decompress(data: ByteArray): ByteArray
}
```

## WhatsApp Recovery API

### WhatsAppBackupManager

```kotlin
class WhatsAppBackupManager {
    suspend fun createBackup(backupDir: File): Result<WhatsAppBackupResult>
    fun detectBackups(): List<WhatsAppBackupInfo>
}
```

### ThirdPartyRecovery

```kotlin
class ThirdPartyRecovery {
    fun detectBackupType(backupPath: String): String
    fun recoverFromBackup(backupPath: String): Flow<RecoveryProgress>
}
```

Supported third-party tools:

- Dr.Fone (Wondershare)
- AnyDroid (iMobie)
- MobileTrans (Wondershare)
- iMyFone

## Transfer API

### CrossPlatformTransfer

```kotlin
class CrossPlatformTransfer {
    fun startTransfer(
        backupFile: File,
        targetDevice: String,
        method: String
    ): Flow<TransferProgress>
    
    fun startDiscovery(): Flow<DiscoveredDevice>
}
```

Transfer methods:

- WiFi Direct
- Bluetooth
- Web Server (HTTP)
- QR Code
