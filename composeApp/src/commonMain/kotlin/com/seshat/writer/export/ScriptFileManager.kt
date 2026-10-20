package com.seshat.writer.export

import android.content.Context
import com.seshat.writer.model.ScreenplayBlock

import com.seshat.writer.titlepage.TitlePageData
import java.io.File
import java.util.Locale

data class SavedScriptInfo(
    val fileName: String,
    val title: String,
    val author: String,
    val lastModified: Long,
    val file: File
)

object ScriptFileManager {

    private const val SCRIPTS_DIR = "scripts"
    private const val EXPORTS_DIR = "exports"

    fun getScriptsDirectory(context: Context? = null): File {
        val base = context?.filesDir ?: com.seshat.writer.platform.AppPlatform.storage.getFilesDir()
        val dir = File(base, SCRIPTS_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getExportsDirectory(context: Context? = null): File {
        val base = context?.cacheDir ?: com.seshat.writer.platform.AppPlatform.storage.getCacheDir()
        val dir = File(base, EXPORTS_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun listSavedScripts(context: Context? = null): List<SavedScriptInfo> {
        val dir = getScriptsDirectory(context)

        val files = dir.listFiles { f -> f.extension == "fountain" || f.extension == "fdx" } ?: return emptyList()

        return files.map { file ->
            val (title, author) = try {
                if (file.extension == "fdx") {
                    val doc = FinalDraftSerializer.importFromFdx(file.readText())
                    Pair(doc.titlePage.title.ifBlank { file.nameWithoutExtension }, doc.titlePage.author)
                } else {
                    val doc = FountainSerializer.importFromFountain(file.readText())
                    Pair(doc.titlePage.title.ifBlank { file.nameWithoutExtension }, doc.titlePage.author)
                }
            } catch (e: Exception) {
                Pair(file.nameWithoutExtension, "")
            }
            SavedScriptInfo(
                fileName = file.name,
                title = title,
                author = author,
                lastModified = file.lastModified(),
                file = file
            )
        }.sortedByDescending { it.lastModified }
    }

    fun saveScript(
        context: Context? = null,
        blocks: List<ScreenplayBlock>,
        titlePageData: TitlePageData,
        format: String = "fountain"
    ): File {
        val dir = getScriptsDirectory(context)
        val safeTitle = sanitizeFileName(titlePageData.title.ifBlank { "Untitled_Script" })
        val ext = if (format.lowercase(Locale.US) == "fdx") "fdx" else "fountain"
        val file = File(dir, "$safeTitle.$ext")

        val content = if (ext == "fdx") {
            FinalDraftSerializer.exportToFdx(blocks, titlePageData)
        } else {
            FountainSerializer.exportToFountain(blocks, titlePageData)
        }
        file.writeText(content)
        return file
    }

    fun exportPdf(
        context: Context,
        blocks: List<ScreenplayBlock>,
        titlePageData: TitlePageData,
        includeTitlePage: Boolean = true,
        showSceneNumbers: Boolean = false,
        watermarkText: String? = null,
        watermarkOpacity: Float = 0.25f,
        watermarkRecipient: String? = null,
        headerText: String? = null,
        includeNotes: Boolean = false,
        revisionColor: com.seshat.writer.model.RevisionColor = com.seshat.writer.model.RevisionColor.WHITE,
        revisionDate: String? = null,
        printRevisionAsterisks: Boolean = true,
        printColoredPages: Boolean = false
    ): File {
        val dir = getExportsDirectory(context)
        val safeTitle = sanitizeFileName(titlePageData.title.ifBlank { "Untitled_Script" })
        val file = File(dir, "$safeTitle.pdf")

        PdfExporter.exportToFile(
            blocks = blocks,
            titlePageData = titlePageData,
            destinationFile = file,
            options = PdfExporter.PdfExportOptions(
                includeTitlePage = includeTitlePage,
                showSceneNumbers = showSceneNumbers,
                watermarkText = watermarkText,
                watermarkOpacity = watermarkOpacity,
                watermarkRecipient = watermarkRecipient,
                headerText = headerText,
                includeNotes = includeNotes,
                revisionColor = revisionColor,
                revisionDate = revisionDate,
                printRevisionAsterisks = printRevisionAsterisks,
                printColoredPages = printColoredPages
            ),
            context = context
        )
        return file
    }

    fun exportCharacterReport(
        context: Context,
        analytics: com.seshat.writer.analytics.ScriptAnalytics,
        title: String
    ): File {
        val dir = getExportsDirectory(context)
        val safeTitle = sanitizeFileName(title.ifBlank { "Screenplay" })
        val file = File(dir, "${safeTitle}_Character_Report.pdf")
        PdfExporter.exportCharacterReportToFile(analytics, title, file, context)
        return file
    }

    fun exportLocationReport(
        context: Context,
        analytics: com.seshat.writer.analytics.LocationAnalytics,
        title: String
    ): File {
        val dir = getExportsDirectory(context)
        val safeTitle = sanitizeFileName(title.ifBlank { "Screenplay" })
        val file = File(dir, "${safeTitle}_Location_Report.pdf")
        PdfExporter.exportLocationReportToFile(analytics, title, file, context)
        return file
    }

    fun exportCastMatrix(
        context: Context,
        matrix: com.seshat.writer.analytics.CastSceneMatrix,
        title: String
    ): File {
        val dir = getExportsDirectory(context)
        val safeTitle = sanitizeFileName(title.ifBlank { "Screenplay" })
        val file = File(dir, "${safeTitle}_Cast_Scene_Matrix.pdf")
        PdfExporter.exportCastMatrixToFile(matrix, title, file, context)
        return file
    }

    fun exportFountain(
        context: Context,
        blocks: List<ScreenplayBlock>,
        titlePageData: TitlePageData
    ): File {
        val dir = getExportsDirectory(context)
        val safeTitle = sanitizeFileName(titlePageData.title.ifBlank { "Untitled_Script" })
        val file = File(dir, "$safeTitle.fountain")
        file.writeText(FountainSerializer.exportToFountain(blocks, titlePageData))
        return file
    }

    fun exportFdx(
        context: Context,
        blocks: List<ScreenplayBlock>,
        titlePageData: TitlePageData
    ): File {
        val dir = getExportsDirectory(context)
        val safeTitle = sanitizeFileName(titlePageData.title.ifBlank { "Untitled_Script" })
        val file = File(dir, "$safeTitle.fdx")
        file.writeText(FinalDraftSerializer.exportToFdx(blocks, titlePageData))
        return file
    }

    fun shareFile(context: Context? = null, file: File, mimeType: String, title: String = "Share Screenplay") {
        if (com.seshat.writer.platform.AppPlatform.isMac) {
            try {
                java.awt.Desktop.getDesktop().open(file)
                return
            } catch (_: Throwable) {}
        }
        com.seshat.writer.platform.AppPlatform.showToast("Exported: ${file.name}")
    }


    fun deleteScript(file: File): Boolean {
        return file.delete()
    }

    private fun sanitizeFileName(name: String): String {
        val cleaned = name.replace(Regex("[^a-zA-Z0-9._-]"), "_").trim('_')
        return cleaned.ifBlank { "Screenplay" }
    }
}
