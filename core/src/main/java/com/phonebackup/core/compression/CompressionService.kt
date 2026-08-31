package com.phonebackup.core.compression

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompressionService @Inject constructor() {
    companion object {
        private const val BUFFER_SIZE = 8192
        private const val MAX_ENTRIES = 100_000
        private const val MAX_EXTRACTED_BYTES = 20L * 1024 * 1024 * 1024
    }

    fun compressDirectory(sourceDir: File): File {
        require(sourceDir.isDirectory) { "Source directory does not exist: ${sourceDir.absolutePath}" }
        val zipFile = File(sourceDir.parentFile, "${sourceDir.name}.zip")
        ZipArchiveOutputStream(FileOutputStream(zipFile)).use { zos ->
            sourceDir.walkTopDown().filter { it.isFile }.forEach { file ->
                val entryName = file.relativeTo(sourceDir).invariantSeparatorsPath
                zos.putArchiveEntry(ZipArchiveEntry(entryName).apply { size = file.length() })
                FileInputStream(file).use { it.copyTo(zos, BUFFER_SIZE) }
                zos.closeArchiveEntry()
            }
        }
        return zipFile
    }

    fun decompressToDirectory(zipFile: File, outputDir: File = File(zipFile.parentFile, zipFile.nameWithoutExtension)) {
        require(zipFile.isFile) { "Archive does not exist: ${zipFile.absolutePath}" }
        val root = outputDir.canonicalFile
        root.mkdirs()
        var entryCount = 0
        var extractedBytes = 0L
        ZipFile(zipFile).use { zip ->
            val entries = zip.entries
            while (entries.hasMoreElements()) {
                require(++entryCount <= MAX_ENTRIES) { "Archive contains too many entries" }
                val entry = entries.nextElement()
                val destination = safeDestination(root, entry.name)
                if (entry.isDirectory) {
                    destination.mkdirs()
                    continue
                }
                destination.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input ->
                    FileOutputStream(destination).use { output ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            extractedBytes += read
                            require(extractedBytes <= MAX_EXTRACTED_BYTES) { "Archive expands beyond safety limit" }
                            output.write(buffer, 0, read)
                        }
                    }
                }
            }
        }
    }

    fun compress(data: ByteArray): ByteArray = ByteArrayOutputStream().use { output ->
        GZIPOutputStream(output).use { it.write(data) }
        output.toByteArray()
    }

    fun decompress(data: ByteArray): ByteArray = GZIPInputStream(ByteArrayInputStream(data)).use { it.readBytes() }

    private fun safeDestination(root: File, entryName: String): File {
        require(!entryName.startsWith('/') && !entryName.startsWith("\\")) { "Absolute archive path rejected" }
        val destination = File(root, entryName).canonicalFile
        val prefix = root.path + File.separator
        require(destination.path == root.path || destination.path.startsWith(prefix)) { "Archive path traversal rejected" }
        return destination
    }
}
