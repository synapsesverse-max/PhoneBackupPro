package com.phonebackup.core.util

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// File extensions
fun File.ensureDir(): File {
    if (!exists()) mkdirs()
    return this
}

fun File.humanReadableSize(): String {
    return when {
        length() < 1024 -> "$length() B"
        length() < 1024 * 1024 -> "${length() / 1024} KB"
        length() < 1024 * 1024 * 1024 -> "${length() / (1024 * 1024)} MB"
        else -> "%.2f GB".format(length().toDouble() / (1024 * 1024 * 1024))
    }
}

// Date extensions
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

// Context extensions
fun Context.getBackupDirectory(): File {
    return File(getExternalFilesDir(null), "backups").ensureDir()
}

fun Context.getRestoreDirectory(): File {
    return File(getExternalFilesDir(null), "restore").ensureDir()
}

// String extensions
fun String.toSHA256(): String {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(this.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}

fun String.isValidBackupPath(): Boolean {
    return File(this).exists()
}

// Device info
fun getDeviceInfo(): String {
    return """
        Manufacturer: ${Build.MANUFACTURER}
        Model: ${Build.MODEL}
        Brand: ${Build.BRAND}
        Device: ${Build.DEVICE}
        Android: ${Build.VERSION.RELEASE}
        SDK: ${Build.VERSION.SDK_INT}
    """.trimIndent()
}
