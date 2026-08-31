package com.phonebackup.cloud

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun uploadBackup(file: File, provider: String): Flow<CloudUploadProgress> = flow {
        try {
            require(file.isFile) { "Backup file does not exist" }
            val endpoint = URL(provider)
            require(endpoint.protocol == "https") { "Cloud upload requires HTTPS" }
            emit(CloudUploadProgress.InProgress(0))
            val connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 120_000
                setRequestProperty("Content-Type", "application/octet-stream")
                setRequestProperty("Content-Length", file.length().toString())
            }
            file.inputStream().use { input -> connection.outputStream.use { output ->
                val buffer = ByteArray(64 * 1024); var sent = 0L; var read: Int
                while (input.read(buffer).also { read = it } >= 0) {
                    output.write(buffer, 0, read); sent += read
                    emit(CloudUploadProgress.InProgress(((sent * 100) / file.length().coerceAtLeast(1)).toInt().coerceAtMost(100)))
                }
            } }
            require(connection.responseCode in 200..299) { "Cloud upload failed with HTTP ${connection.responseCode}" }
            connection.disconnect()
            emit(CloudUploadProgress.Completed(provider))
        } catch (e: Exception) { emit(CloudUploadProgress.Error(e.message ?: "Cloud upload failed")) }
    }

    fun downloadBackup(url: String): Flow<CloudDownloadProgress> = flow {
        try {
            val endpoint = URL(url); require(endpoint.protocol == "https") { "Cloud download requires HTTPS" }
            val connection = (endpoint.openConnection() as HttpURLConnection).apply { connectTimeout = 30_000; readTimeout = 120_000 }
            require(connection.responseCode in 200..299) { "Cloud download failed with HTTP ${connection.responseCode}" }
            val total = connection.contentLengthLong
            val destination = File(context.cacheDir, "download-${System.currentTimeMillis()}.pbp")
            emit(CloudDownloadProgress.InProgress(0))
            connection.inputStream.use { input -> destination.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024); var received = 0L; var read: Int
                while (input.read(buffer).also { read = it } >= 0) {
                    output.write(buffer, 0, read); received += read
                    val progress = if (total > 0) ((received * 100) / total).toInt() else 0
                    emit(CloudDownloadProgress.InProgress(progress.coerceAtMost(100)))
                }
            } }
            connection.disconnect(); emit(CloudDownloadProgress.Completed(destination))
        } catch (e: Exception) { emit(CloudDownloadProgress.Error(e.message ?: "Cloud download failed")) }
    }

    fun syncBackups(): Flow<CloudSyncProgress> = flow {
        emit(CloudSyncProgress.Error("Cloud sync requires a configured provider and authenticated account"))
    }
}

sealed class CloudUploadProgress {
    data class InProgress(val percentage: Int) : CloudUploadProgress()
    data class Completed(val url: String) : CloudUploadProgress()
    data class Error(val message: String) : CloudUploadProgress()
}
sealed class CloudDownloadProgress {
    data class InProgress(val percentage: Int) : CloudDownloadProgress()
    data class Completed(val file: File) : CloudDownloadProgress()
    data class Error(val message: String) : CloudDownloadProgress()
}
sealed class CloudSyncProgress {
    data class Syncing(val percentage: Int) : CloudSyncProgress()
    data object Completed : CloudSyncProgress()
    data class Error(val message: String) : CloudSyncProgress()
}
