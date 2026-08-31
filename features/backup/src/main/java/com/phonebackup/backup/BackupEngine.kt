package com.phonebackup.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import com.phonebackup.core.compression.CompressionService
import com.phonebackup.core.data.local.model.BackupRecord
import com.phonebackup.core.data.local.model.BackupStatus
import com.phonebackup.core.data.repository.BackupRepository
import com.phonebackup.core.encryption.EncryptionService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionService: EncryptionService,
    private val compressionService: CompressionService,
    private val backupRepository: BackupRepository
) {
    companion object {
        private const val MAX_BACKUP_SIZE = 10L * 1024 * 1024 * 1024
        private const val BUFFER_SIZE = 64 * 1024
    }

    suspend fun executeFullBackup(backupRecord: BackupRecord, options: BackupOptions = BackupOptions()): Flow<BackupProgress> = flow {
        emit(BackupProgress.Initializing)
        val root = File(context.getExternalFilesDir(null), "backups/${backupRecord.backupUUID}")
        try {
            require(root.mkdirs() || root.isDirectory) { "Unable to create backup directory" }
            if (options.includeContacts) {
                emit(BackupProgress.Processing("Contacts", 10)); backupContacts(root)
            }
            if (options.includeMessages) {
                emit(BackupProgress.Processing("Messages", 25)); backupMessages(root)
            }
            if (options.includeCallLogs) {
                emit(BackupProgress.Processing("Call logs", 35)); backupCallLogs(root)
            }
            if (options.includeMedia) {
                emit(BackupProgress.Processing("Media files", 55)); backupMediaFiles(root, options.mediaTypes)
            }
            emit(BackupProgress.Processing("Compressing", 75))
            val zip = compressionService.compressDirectory(root)
            require(zip.length() <= MAX_BACKUP_SIZE) { "Backup exceeds maximum configured size" }
            val finalFile = if (options.encryptionPassword != null) {
                emit(BackupProgress.Processing("Encrypting", 90))
                val encrypted = File(root.parentFile, "${backupRecord.backupUUID}.pbp")
                encryptionService.encryptFile(zip, encrypted, options.encryptionPassword)
                zip.delete()
                encrypted
            } else zip
            val checksum = sha256(finalFile)
            require(finalFile.isFile && finalFile.length() > 0) { "Backup artifact is empty" }
            backupRepository.updateBackupStatus(backupRecord.backupUUID, BackupStatus.VERIFIED)
            emit(BackupProgress.Completed(root, finalFile, checksum))
        } catch (e: Exception) {
            backupRepository.updateBackupStatus(backupRecord.backupUUID, BackupStatus.FAILED)
            emit(BackupProgress.Error(e.message ?: "Backup failed"))
        }
    }

    private fun backupContacts(root: File) {
        val file = File(root, "contacts/contacts.vcf").also { it.parentFile?.mkdirs() }
        val resolver = context.contentResolver
        val out = StringBuilder()
        resolver.query(ContactsContract.Contacts.CONTENT_URI, arrayOf(
            ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME
        ), null, null, null)?.use { contacts ->
            val idCol = contacts.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameCol = contacts.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            while (contacts.moveToNext()) {
                val id = contacts.getString(idCol)
                val name = contacts.getString(nameCol).orEmpty()
                out.appendLine("BEGIN:VCARD").appendLine("VERSION:3.0")
                    .appendLine("UID:${escapeVcf(id)}").appendLine("FN:${escapeVcf(name)}")
                resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?", arrayOf(id), null)?.use { phones ->
                    val numberCol = phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (phones.moveToNext()) out.appendLine("TEL:${escapeVcf(phones.getString(numberCol).orEmpty())}")
                }
                resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                    "${ContactsContract.CommonDataKinds.Email.CONTACT_ID}=?", arrayOf(id), null)?.use { emails ->
                    val emailCol = emails.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)
                    while (emails.moveToNext()) out.appendLine("EMAIL:${escapeVcf(emails.getString(emailCol).orEmpty())}")
                }
                out.appendLine("END:VCARD")
            }
        }
        file.writeText(out.toString())
    }

    private fun backupMessages(root: File) {
        val file = File(root, "messages/sms.tsv").also { it.parentFile?.mkdirs() }
        context.contentResolver.query(Uri.parse("content://sms"), arrayOf("_id", "address", "body", "date", "type", "read", "thread_id"), null, null, "date ASC")?.use { cursor ->
            FileOutputStream(file).bufferedWriter().use { out ->
                out.appendLine("id\taddress\tbody\tdate\ttype\tread\tthread_id")
                val indexes = arrayOf("_id", "address", "body", "date", "type", "read", "thread_id").map(cursor::getColumnIndexOrThrow)
                while (cursor.moveToNext()) out.appendLine(indexes.joinToString("\t") { escapeTsv(cursor.getString(it).orEmpty()) })
            }
        } ?: file.writeText("id\taddress\tbody\tdate\ttype\tread\tthread_id\n")
    }

    private fun backupCallLogs(root: File) {
        val file = File(root, "call_logs/calls.tsv").also { it.parentFile?.mkdirs() }
        context.contentResolver.query(CallLog.Calls.CONTENT_URI, arrayOf(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE), null, null, "date ASC")?.use { cursor ->
            FileOutputStream(file).bufferedWriter().use { out ->
                out.appendLine("id\tnumber\tdate\tduration\ttype")
                val indexes = arrayOf(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE).map(cursor::getColumnIndexOrThrow)
                while (cursor.moveToNext()) out.appendLine(indexes.joinToString("\t") { escapeTsv(cursor.getString(it).orEmpty()) })
            }
        } ?: file.writeText("id\tnumber\tdate\tduration\ttype\n")
    }

    private fun backupMediaFiles(root: File, types: List<MediaType>) {
        val mediaRoot = File(root, "media").also { it.mkdirs() }
        types.forEach { type ->
            val collection = when (type) {
                MediaType.PHOTOS -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                MediaType.VIDEOS -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> null
            } ?: return@forEach
            val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.MIME_TYPE)
            context.contentResolver.query(collection, projection, null, null, "${MediaStore.MediaColumns.DATE_ADDED} ASC")?.use { cursor ->
                val id = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val name = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val mime = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                while (cursor.moveToNext()) {
                    val uri = Uri.withAppendedPath(collection, cursor.getString(id))
                    val safeName = cursor.getString(name).orEmpty().replace(Regex("[^A-Za-z0-9._-]"), "_")
                    val destination = File(mediaRoot, type.name.lowercase()).also { it.mkdirs() }.resolve(safeName)
                    context.contentResolver.openInputStream(uri)?.use { input -> destination.outputStream().use { output -> input.copyTo(output, BUFFER_SIZE) } }
                    File(mediaRoot, "${type.name.lowercase()}.tsv").appendText("$safeName\t${cursor.getString(mime).orEmpty()}\n")
                }
            }
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    private fun escapeVcf(value: String): String = value.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n")
    private fun escapeTsv(value: String): String = value.replace("\\", "\\\\").replace("\t", " ").replace("\r", " ").replace("\n", " ")
}

data class BackupOptions(
    val includeContacts: Boolean = true, val includeMessages: Boolean = true, val includeCallLogs: Boolean = true,
    val includeCalendar: Boolean = true, val includeApps: Boolean = false, val selectedApps: List<String> = emptyList(),
    val includeMedia: Boolean = true, val mediaTypes: List<MediaType> = listOf(MediaType.PHOTOS, MediaType.VIDEOS),
    val includeWhatsApp: Boolean = false, val encryptionPassword: String? = null
)
enum class MediaType { PHOTOS, VIDEOS, AUDIO, DOCUMENTS, DOWNLOADS }
sealed class BackupProgress {
    data object Initializing : BackupProgress()
    data class Processing(val phase: String, val progress: Int) : BackupProgress()
    data class Completed(val backupDir: File, val artifact: File, val checksum: String) : BackupProgress()
    data class Error(val message: String) : BackupProgress()
}
