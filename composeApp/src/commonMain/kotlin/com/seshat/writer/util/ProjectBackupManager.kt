package com.seshat.writer.util

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Metadata for a timestamped project backup archive.
 */
data class BackupInfo(
    val file: File,
    val title: String,
    val timestamp: Long,
    val sizeBytes: Long
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.US).format(Date(timestamp))
}

/**
 * Automated and on-demand project backup engine.
 * Archives complete manuscript & project states into timestamped zip packages.
 */
object ProjectBackupManager {

    private const val MAX_BACKUPS_TO_KEEP = 25

    fun getBackupDirectory(context: Context? = null): File {
        val base = context?.filesDir ?: com.seshat.writer.platform.AppPlatform.storage.getFilesDir()
        val appDir = File(base, "backups")
        appDir.mkdirs()
        return appDir
    }

    /**
     * Creates a timestamped .zip archive containing the full project data.
     */
    fun createBackup(
        context: Context? = null,
        projectTitle: String,
        projectJson: String
    ): File? {

        return try {
            val dir = getBackupDirectory(context)
            val safeTitle = projectTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "Untitled_Project" }
            val timeTag = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val zipFile = File(dir, "${safeTitle}_backup_${timeTag}.zip")

            FileOutputStream(zipFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    val entry = ZipEntry("project_data.json")
                    zos.putNextEntry(entry)
                    zos.write(projectJson.toByteArray(StandardCharsets.UTF_8))
                    zos.closeEntry()
                }
            }

            pruneOldBackups(context, MAX_BACKUPS_TO_KEEP)
            zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Lists all existing backup archives sorted from newest to oldest.
     */
    fun listBackups(context: Context? = null): List<BackupInfo> {
        return try {
            val dir = getBackupDirectory(context)
            dir.listFiles { f -> f.extension.equals("zip", ignoreCase = true) }
                ?.map { f ->
                    val nameWithoutExt = f.nameWithoutExtension
                    val title = nameWithoutExt.substringBefore("_backup_").replace("_", " ")
                    BackupInfo(
                        file = f,
                        title = title.ifBlank { "Manuscript Backup" },
                        timestamp = f.lastModified(),
                        sizeBytes = f.length()
                    )
                }
                ?.sortedByDescending { it.timestamp }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Extracts and reads the project JSON string from a backup archive.
     */
    fun restoreBackupContent(backupFile: File): String? {
        return try {
            FileInputStream(backupFile).use { fis ->
                ZipInputStream(fis).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (entry.name.endsWith(".json", ignoreCase = true)) {
                            return zis.bufferedReader(StandardCharsets.UTF_8).readText()
                        }
                        entry = zis.nextEntry
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Automatically prunes older backups to keep storage clean and bounded.
     */
    fun pruneOldBackups(context: Context? = null, keepCount: Int = MAX_BACKUPS_TO_KEEP) {

        try {
            val backups = listBackups(context)
            if (backups.size > keepCount) {
                backups.drop(keepCount).forEach { it.file.delete() }
            }
        } catch (_: Exception) {}
    }
}
