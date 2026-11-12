package com.seshat.writer.sync

import android.content.Context
import androidx.compose.runtime.Composable
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.bundle.SeshatBundleManager
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.firstdraft.FirstDraftState
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.novel.NovelManuscript
import com.seshat.writer.platform.AppPlatform
import com.seshat.writer.tv.TvSeriesProject
import com.seshat.writer.universe.BlendedUniverseProject
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CloudSyncSettings(
    val mirrorFolderPath: String = "",
    val autoMirrorEnabled: Boolean = false,
    val lastMirroredAt: Long = 0L,
    val lastMirrorStatus: String = "Not configured"
) {
    val isConfigured: Boolean get() = mirrorFolderPath.isNotBlank()

    fun formattedLastMirrored(): String {
        if (lastMirroredAt <= 0L) return lastMirrorStatus
        val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.US)
        return "Mirrored ${fmt.format(Date(lastMirroredAt))}"
    }
}

data class CloudSyncResult(
    val success: Boolean,
    val targetDescription: String,
    val message: String
)

object CloudSyncManager {
    private const val KEY_FOLDER = "cloud_sync_folder_path"
    private const val KEY_AUTO = "cloud_sync_auto_mirror"
    private const val KEY_LAST_AT = "cloud_sync_last_at"
    private const val KEY_LAST_STATUS = "cloud_sync_last_status"

    fun loadSettings(context: Context? = null): CloudSyncSettings {
        val s = AppPlatform.storage
        return CloudSyncSettings(
            mirrorFolderPath = s.getString(KEY_FOLDER, ""),
            autoMirrorEnabled = s.getBoolean(KEY_AUTO, false),
            lastMirroredAt = s.getString(KEY_LAST_AT, "0").toLongOrNull() ?: 0L,
            lastMirrorStatus = s.getString(KEY_LAST_STATUS, "Not configured")
        )
    }

    fun saveSettings(context: Context? = null, settings: CloudSyncSettings) {
        val s = AppPlatform.storage
        s.putString(KEY_FOLDER, settings.mirrorFolderPath)
        s.putBoolean(KEY_AUTO, settings.autoMirrorEnabled)
        s.putString(KEY_LAST_AT, settings.lastMirroredAt.toString())
        s.putString(KEY_LAST_STATUS, settings.lastMirrorStatus)
    }

    fun detectSuggestedCloudFolders(): List<Pair<String, String>> {
        val home = System.getProperty("user.home") ?: return emptyList()
        val candidates = listOf(
            "☁️ iCloud Drive" to "$home/Library/Mobile Documents/com~apple~CloudDocs",
            "📂 Google Drive" to "$home/Library/CloudStorage",
            "📦 Dropbox" to "$home/Dropbox",
            "🟦 OneDrive" to "$home/OneDrive"
        )
        return candidates.filter { File(it.second).exists() }
    }

    fun sanitizeFileName(name: String): String {
        val clean = name.trim().replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return if (clean.isBlank()) "untitled_project" else clean
    }

    /**
     * Builds the project's portable .seshat bundle into [outputFile].
     */
    fun buildBundleToFile(
        context: Context? = null,
        outputFile: File,
        projectFormat: ProjectFormat,
        screenplayType: ScreenplayType?,
        title: String,
        author: String,
        blocks: List<ScreenplayBlock>,
        corkboard: CorkboardDocument,
        storyBible: StoryBible,
        novelManuscript: NovelManuscript,
        tvSeriesProject: TvSeriesProject?,
        blendedUniverseProject: BlendedUniverseProject?,
        firstDraftState: FirstDraftState?
    ): Boolean {
        return when {
            projectFormat == ProjectFormat.TV_SERIES && tvSeriesProject != null -> {
                SeshatBundleManager.exportTvSeriesBundle(
                    context = context,
                    project = tvSeriesProject,
                    outputFile = outputFile,
                    firstDraftState = firstDraftState
                ) != null
            }
            projectFormat == ProjectFormat.NOVEL -> {
                SeshatBundleManager.exportNovelBundle(
                    context = context,
                    manuscript = novelManuscript.copy(title = title.ifBlank { novelManuscript.title }),
                    corkboard = corkboard,
                    storyBible = storyBible,
                    outputFile = outputFile,
                    firstDraftState = firstDraftState
                ) != null
            }
            else -> {
                SeshatBundleManager.exportScreenplayBundle(
                    context = context,
                    title = title.ifBlank { "UNTITLED" },
                    author = author,
                    screenplayType = screenplayType ?: ScreenplayType.FEATURE,
                    blocks = blocks,
                    corkboard = corkboard,
                    storyBible = storyBible,
                    outputFile = outputFile,
                    firstDraftState = firstDraftState
                ) != null
            }
        }
    }

    /**
     * Atomically mirrors the active project (.seshat bundle + .universe.json if Blended Universe)
     * into the user's chosen sync folder via write-to-.tmp + atomic rename so cloud daemons never
     * upload a half-written file.
     */
    fun mirrorProjectAtomic(
        context: Context? = null,
        projectFormat: ProjectFormat,
        screenplayType: ScreenplayType?,
        title: String,
        author: String,
        blocks: List<ScreenplayBlock>,
        corkboard: CorkboardDocument,
        storyBible: StoryBible,
        novelManuscript: NovelManuscript,
        tvSeriesProject: TvSeriesProject?,
        blendedUniverseProject: BlendedUniverseProject?,
        firstDraftState: FirstDraftState?
    ): CloudSyncResult {
        val settings = loadSettings(context)
        if (settings.mirrorFolderPath.isBlank()) {
            return CloudSyncResult(false, "", "No Cloud Sync folder selected")
        }

        val folder = File(settings.mirrorFolderPath)
        if (!folder.exists() && !folder.mkdirs()) {
            return CloudSyncResult(false, folder.absolutePath, "Sync folder is unavailable")
        }

        val safeName = sanitizeFileName(blendedUniverseProject?.title ?: title)
        val tmpBundle = File(folder, "$safeName.seshat.tmp")
        val finalBundle = File(folder, "$safeName.seshat")

        val ok = buildBundleToFile(
            context = context,
            outputFile = tmpBundle,
            projectFormat = projectFormat,
            screenplayType = screenplayType,
            title = title,
            author = author,
            blocks = blocks,
            corkboard = corkboard,
            storyBible = storyBible,
            novelManuscript = novelManuscript,
            tvSeriesProject = tvSeriesProject,
            blendedUniverseProject = blendedUniverseProject,
            firstDraftState = firstDraftState
        )
        if (!ok || !tmpBundle.exists()) {
            tmpBundle.delete()
            return CloudSyncResult(false, finalBundle.absolutePath, "Failed to package .seshat bundle")
        }

        return try {
            Files.move(
                tmpBundle.toPath(),
                finalBundle.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
            // If Blended Universe, also atomically mirror the complete .universe.json archive
            if (blendedUniverseProject != null) {
                val tmpUniverse = File(folder, "$safeName.universe.json.tmp")
                val finalUniverse = File(folder, "$safeName.universe.json")
                tmpUniverse.writeText(blendedUniverseProject.toJson().toString(2))
                Files.move(
                    tmpUniverse.toPath(),
                    finalUniverse.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
            }
            val now = System.currentTimeMillis()
            saveSettings(
                context,
                settings.copy(
                    lastMirroredAt = now,
                    lastMirrorStatus = "Mirrored ${finalBundle.name}"
                )
            )
            CloudSyncResult(true, finalBundle.absolutePath, "Synced ${finalBundle.name}")
        } catch (e: Exception) {
            tmpBundle.delete()
            CloudSyncResult(false, finalBundle.absolutePath, e.message ?: "Atomic sync failed")
        }
    }
}

@Composable
fun rememberCloudSyncFolderPicker(
    onFolderSelected: (String) -> Unit
): () -> Unit {
    return {
        try {
            val prevProp = System.getProperty("apple.awt.fileDialogForDirectories")
            System.setProperty("apple.awt.fileDialogForDirectories", "true")
            val dialog = java.awt.FileDialog(
                null as java.awt.Frame?,
                "Choose Cloud Sync Folder (iCloud, Google Drive, Dropbox...)",
                java.awt.FileDialog.LOAD
            )
            val icloudDir = File(System.getProperty("user.home"), "Library/Mobile Documents/com~apple~CloudDocs")
            if (icloudDir.exists()) {
                dialog.directory = icloudDir.absolutePath
            }
            dialog.isVisible = true
            if (prevProp == null) {
                System.clearProperty("apple.awt.fileDialogForDirectories")
            } else {
                System.setProperty("apple.awt.fileDialogForDirectories", prevProp)
            }
            val dir = dialog.directory
            val file = dialog.file
            if (!dir.isNullOrBlank() && !file.isNullOrBlank()) {
                val chosen = File(dir, file)
                if (chosen.isDirectory) {
                    onFolderSelected(chosen.absolutePath)
                } else {
                    onFolderSelected(File(dir).absolutePath)
                }
            }
        } catch (_: Throwable) {
            // Fallback to Swing JFileChooser
            val chooser = javax.swing.JFileChooser().apply {
                fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
                dialogTitle = "Choose Cloud Sync Folder"
            }
            if (chooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile?.absolutePath?.let(onFolderSelected)
            }
        }
    }
}

@Composable
fun rememberOneTapCloudBackupLauncher(
    defaultFileName: String,
    onExportToTempAndCopy: (File) -> Boolean
): () -> Unit {
    return {
        try {
            val dialog = java.awt.FileDialog(
                null as java.awt.Frame?,
                "Save Backup Copy (.seshat)",
                java.awt.FileDialog.SAVE
            )
            dialog.file = defaultFileName
            dialog.isVisible = true
            val dir = dialog.directory
            val file = dialog.file
            if (!dir.isNullOrBlank() && !file.isNullOrBlank()) {
                val target = File(dir, if (file.endsWith(".seshat")) file else "$file.seshat")
                onExportToTempAndCopy(target)
            }
        } catch (_: Throwable) {}
    }
}
