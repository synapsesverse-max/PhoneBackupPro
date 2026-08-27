package com.phonebackup.transfer

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrossPlatformTransfer @Inject constructor(private val context: Context) {
    fun startTransfer(backupFile: File, targetDevice: String, method: String): Flow<TransferProgress> = flow {
        try {
            require(backupFile.isFile) { "Backup file does not exist" }
            require(method.equals("https", true) || targetDevice.startsWith("https://")) { "Transfer requires an HTTPS endpoint" }
            val connection = (URL(targetDevice).openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"; doOutput = true; connectTimeout = 15_000; readTimeout = 120_000
                setRequestProperty("Content-Type", "application/octet-stream")
                setRequestProperty("Content-Length", backupFile.length().toString())
            }
            emit(TransferProgress.InProgress(0))
            backupFile.inputStream().use { input -> connection.outputStream.use { output ->
                val buffer = ByteArray(64 * 1024); var sent = 0L; var read: Int
                while (input.read(buffer).also { read = it } >= 0) {
                    output.write(buffer, 0, read); sent += read
                    emit(TransferProgress.InProgress(((sent * 100) / backupFile.length().coerceAtLeast(1)).toInt().coerceAtMost(100)))
                }
            } }
            require(connection.responseCode in 200..299) { "Transfer failed with HTTP ${connection.responseCode}" }
            connection.disconnect(); emit(TransferProgress.Completed)
        } catch (e: Exception) { emit(TransferProgress.Error(e.message ?: "Transfer failed")) }
    }

    fun startDiscovery(): Flow<DiscoveredDevice> = flow {
        emit(DiscoveredDevice("No device", "", "unknown", "none"))
    }
}

data class DiscoveredDevice(val name: String, val address: String, val type: String, val connectionMethod: String)
sealed class TransferProgress {
    data class InProgress(val percentage: Int) : TransferProgress()
    data object Completed : TransferProgress()
    data class Error(val message: String) : TransferProgress()
}
