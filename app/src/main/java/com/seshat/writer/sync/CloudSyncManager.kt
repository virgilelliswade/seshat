package com.seshat.writer.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.bundle.SeshatBundleManager
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.firstdraft.FirstDraftState
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.novel.NovelManuscript
import com.seshat.writer.tv.TvSeriesProject
import com.seshat.writer.universe.BlendedUniverseProject
import java.io.File
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
    private const val PREFS_NAME = "seshat_cloud_sync"
    private const val KEY_FOLDER = "cloud_sync_folder_path"
    private const val KEY_AUTO = "cloud_sync_auto_mirror"
    private const val KEY_LAST_AT = "cloud_sync_last_at"
    private const val KEY_LAST_STATUS = "cloud_sync_last_status"

    fun loadSettings(context: Context? = null): CloudSyncSettings {
        val ctx = context ?: return CloudSyncSettings()
        val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return CloudSyncSettings(
            mirrorFolderPath = prefs.getString(KEY_FOLDER, "") ?: "",
            autoMirrorEnabled = prefs.getBoolean(KEY_AUTO, false),
            lastMirroredAt = prefs.getLong(KEY_LAST_AT, 0L),
            lastMirrorStatus = prefs.getString(KEY_LAST_STATUS, "Not configured") ?: "Not configured"
        )
    }

    fun saveSettings(context: Context? = null, settings: CloudSyncSettings) {
        val ctx = context ?: return
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_FOLDER, settings.mirrorFolderPath)
            .putBoolean(KEY_AUTO, settings.autoMirrorEnabled)
            .putLong(KEY_LAST_AT, settings.lastMirroredAt)
            .putString(KEY_LAST_STATUS, settings.lastMirrorStatus)
            .apply()
    }

    fun detectSuggestedCloudFolders(): List<Pair<String, String>> = emptyList()

    fun sanitizeFileName(name: String): String {
        val clean = name.trim().replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return if (clean.isBlank()) "untitled_project" else clean
    }

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
        val ctx = context ?: return CloudSyncResult(false, "", "Missing context")
        val settings = loadSettings(ctx)
        if (settings.mirrorFolderPath.isBlank()) {
            return CloudSyncResult(false, "", "No Cloud Sync folder selected")
        }

        val safeName = sanitizeFileName(blendedUniverseProject?.title ?: title)
        val tmpBundle = File(ctx.cacheDir, "$safeName.seshat.tmp")
        val ok = buildBundleToFile(
            context = ctx,
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
            return CloudSyncResult(false, "", "Failed to package .seshat bundle")
        }

        return try {
            val fileName = "$safeName.seshat"
            if (settings.mirrorFolderPath.startsWith("content://")) {
                writeTempFileToSafTree(ctx, Uri.parse(settings.mirrorFolderPath), fileName, "application/octet-stream", tmpBundle)
                if (blendedUniverseProject != null) {
                    val tmpUniv = File(ctx.cacheDir, "$safeName.universe.json.tmp")
                    tmpUniv.writeText(blendedUniverseProject.toJson().toString(2))
                    writeTempFileToSafTree(ctx, Uri.parse(settings.mirrorFolderPath), "$safeName.universe.json", "application/json", tmpUniv)
                    tmpUniv.delete()
                }
            } else {
                val folder = File(settings.mirrorFolderPath)
                if (!folder.exists()) folder.mkdirs()
                val destTmp = File(folder, "$fileName.tmp")
                val destFinal = File(folder, fileName)
                tmpBundle.copyTo(destTmp, overwrite = true)
                destTmp.renameTo(destFinal)
            }
            tmpBundle.delete()
            val now = System.currentTimeMillis()
            saveSettings(
                ctx,
                settings.copy(
                    lastMirroredAt = now,
                    lastMirrorStatus = "Mirrored $fileName"
                )
            )
            CloudSyncResult(true, settings.mirrorFolderPath, "Synced $fileName")
        } catch (e: Exception) {
            tmpBundle.delete()
            CloudSyncResult(false, settings.mirrorFolderPath, e.message ?: "Sync failed")
        }
    }

    private fun writeTempFileToSafTree(
        context: Context,
        treeUri: Uri,
        displayName: String,
        mimeType: String,
        sourceFile: File
    ) {
        val resolver = context.contentResolver
        val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocId)

        var existingDocUri: Uri? = null
        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ),
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == displayName) {
                    existingDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(0))
                    break
                }
            }
        }

        val targetUri = existingDocUri ?: DocumentsContract.createDocument(
            resolver,
            DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId),
            mimeType,
            displayName
        ) ?: throw IllegalStateException("Could not create $displayName in selected folder")

        resolver.openOutputStream(targetUri, "wt")?.use { out ->
            sourceFile.inputStream().use { input ->
                input.copyTo(out)
            }
        } ?: throw IllegalStateException("Unable to open output stream for $displayName")
    }
}

@Composable
fun rememberCloudSyncFolderPicker(
    onFolderSelected: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Throwable) {}
            onFolderSelected(uri.toString())
        }
    }
    return {
        launcher.launch(null)
    }
}

@Composable
fun rememberOneTapCloudBackupLauncher(
    defaultFileName: String,
    onExportToTempAndCopy: (File) -> Boolean
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            try {
                val tmp = File(context.cacheDir, "one_tap_backup.seshat")
                if (onExportToTempAndCopy(tmp) && tmp.exists()) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        tmp.inputStream().use { input -> input.copyTo(out) }
                    }
                    tmp.delete()
                }
            } catch (_: Throwable) {}
        }
    }
    return {
        launcher.launch(defaultFileName)
    }
}
