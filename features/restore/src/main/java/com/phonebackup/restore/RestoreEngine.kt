package com.phonebackup.restore

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import com.phonebackup.core.data.repository.RestoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreEngine @Inject constructor(
    private val context: Context,
    private val restoreRepository: RestoreRepository
) {
    suspend fun executeRestore(backupUUID: String, options: RestoreOptions): Flow<RestoreProgress> = flow {
        emit(RestoreProgress.Initializing)
        val root = File(context.getExternalFilesDir(null), "backups/$backupUUID")
        try {
            require(root.isDirectory) { "Backup data directory not found" }
            restoreRepository.markBackupAsRestoring(backupUUID)
            if (options.restoreContacts) {
                emit(RestoreProgress.Processing("Restoring contacts", 25))
                restoreContacts(File(root, "contacts/contacts.vcf"))
            }
            if (options.restoreMessages) {
                emit(RestoreProgress.Processing("Restoring messages", 45))
                restoreMessages(File(root, "messages/sms.tsv"))
            }
            if (options.restoreMedia) {
                emit(RestoreProgress.Processing("Restoring media", 70))
                restoreMedia(File(root, "media"))
            }
            if (options.restoreWhatsApp) {
                throw UnsupportedOperationException("WhatsApp message restore requires an authorized, device-supported import path")
            }
            emit(RestoreProgress.Processing("Verifying restore", 90))
            restoreRepository.markBackupAsRestored(backupUUID)
            emit(RestoreProgress.Completed)
        } catch (e: Exception) {
            emit(RestoreProgress.Error(e.message ?: "Restore failed"))
        }
    }

    private fun restoreContacts(vcf: File) {
        if (!vcf.isFile) return
        var rawContactId: Long? = null
        vcf.useLines { lines ->
            lines.forEach { line ->
                when {
                    line == "BEGIN:VCARD" -> rawContactId = context.contentResolver.insert(
                        ContactsContract.RawContacts.CONTENT_URI, ContentValues()
                    )?.lastPathSegment?.toLongOrNull()
                    line.startsWith("FN:") -> rawContactId?.let { insertContactName(it, line.substringAfter("FN:")) }
                    line.startsWith("TEL:") -> rawContactId?.let { insertContactData(it, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, line.substringAfter("TEL:"), ContactsContract.CommonDataKinds.Phone.NUMBER) }
                    line.startsWith("EMAIL:") -> rawContactId?.let { insertContactData(it, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, line.substringAfter("EMAIL:"), ContactsContract.CommonDataKinds.Email.ADDRESS) }
                }
            }
        }
    }

    private fun insertContactName(rawId: Long, value: String) {
        val values = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawId)
            put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, unescape(value))
        }
        context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
    }

    private fun insertContactData(rawId: Long, mime: String, value: String, valueColumn: String) {
        val values = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawId)
            put(ContactsContract.Data.MIMETYPE, mime)
            put(valueColumn, unescape(value))
        }
        context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
    }

    private fun restoreMessages(file: File) {
        if (!file.isFile) return
        file.useLines { lines ->
            lines.drop(1).forEach { row ->
                val c = row.split('\t')
                if (c.size >= 7) context.contentResolver.insert(Uri.parse("content://sms/inbox"), ContentValues().apply {
                    put("address", c[1]); put("body", c[2]); put("date", c[3].toLongOrNull() ?: 0L)
                    put("type", c[4].toIntOrNull() ?: 1); put("read", c[5].toIntOrNull() ?: 1); put("thread_id", c[6].toLongOrNull() ?: 0L)
                })
            }
        }
    }

    private fun restoreMedia(mediaDir: File) {
        if (!mediaDir.isDirectory) return
        mediaDir.walkTopDown().filter { it.isFile && it.extension != "tsv" }.forEach { source ->
            val relative = source.relativeTo(mediaDir).invariantSeparatorsPath
            val collection = when (source.parentFile?.name) {
                "photos" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                "videos" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> return@forEach
            }
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, source.name)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeFor(source))
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(collection, values) ?: return@forEach
            try {
                context.contentResolver.openOutputStream(uri)?.use { output -> source.inputStream().use { it.copyTo(output) } }
                values.clear(); values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            } catch (e: Exception) {
                context.contentResolver.delete(uri, null, null)
                throw e
            }
        }
    }

    private fun mimeFor(file: File): String = when (file.extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"; "png" -> "image/png"; "webp" -> "image/webp"
        "mp4" -> "video/mp4"; "m4a" -> "audio/mp4"; "mp3" -> "audio/mpeg"; else -> "application/octet-stream"
    }
    private fun unescape(value: String): String = value.replace("\\n", "\n").replace("\\,", ",").replace("\\;", ";").replace("\\\\", "\\")
}

data class RestoreOptions(
    val restoreContacts: Boolean = true, val restoreMessages: Boolean = true, val restoreMedia: Boolean = true, val restoreWhatsApp: Boolean = false
)
sealed class RestoreProgress {
    data object Initializing : RestoreProgress()
    data class Processing(val phase: String, val progress: Int) : RestoreProgress()
    data object Completed : RestoreProgress()
    data class Error(val message: String) : RestoreProgress()
}
