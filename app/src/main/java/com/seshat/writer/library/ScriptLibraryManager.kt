package com.seshat.writer.library

import android.content.Context
import com.seshat.writer.export.FinalDraftSerializer
import com.seshat.writer.export.FountainSerializer
import com.seshat.writer.export.ScriptFileManager
import com.seshat.writer.importer.ScriptFormat
import com.seshat.writer.importer.ScriptImporter
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.RevisionColor
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.titlepage.TitlePageData
import java.io.File
import java.util.Locale

object ScriptLibraryManager {

    fun getScriptsDirectory(context: Context): File {
        return ScriptFileManager.getScriptsDirectory(context)
    }

    fun listProjects(context: Context, activeTitle: String = ""): List<ScriptProject> {
        val dir = getScriptsDirectory(context)
        return listProjectsInDir(dir, activeTitle)
    }

    fun listProjectsInDir(directory: File, activeTitle: String = ""): List<ScriptProject> {
        val files = directory.listFiles { f ->
            f.isFile && (f.extension.equals("fountain", ignoreCase = true) || f.extension.equals("fdx", ignoreCase = true))
        } ?: return emptyList()

        return files.map { file ->
            val importResult = ScriptImporter.importFromFile(file)
            val doc = importResult.document
            val title = doc?.titlePage?.title?.ifBlank { file.nameWithoutExtension } ?: file.nameWithoutExtension
            val author = doc?.titlePage?.author ?: ""
            val sceneCount = doc?.blocks?.count { it.type == BlockType.SCENE_HEADING } ?: 1
            val estimatedPageCount = (doc?.blocks?.size ?: 10) / 25 + 1
            val formatStr = if (importResult.detectedFormat == ScriptFormat.FINAL_DRAFT) "FDX" else "FOUNTAIN"
            val isActive = activeTitle.isNotBlank() && title.equals(activeTitle, ignoreCase = true)

            ScriptProject(
                id = file.name,
                title = title,
                author = author,
                file = file,
                format = formatStr,
                lastModified = file.lastModified(),
                fileSizeBytes = file.length(),
                sceneCount = sceneCount,
                pageCount = estimatedPageCount,
                revisionColor = RevisionColor.WHITE,
                draftTag = doc?.titlePage?.draftInfo?.ifBlank { "White Draft" } ?: "White Draft",
                isActive = isActive
            )
        }.sortedByDescending { it.lastModified }
    }

    fun createNewProject(
        context: Context,
        title: String,
        author: String = "",
        template: ScriptTemplate = ScriptTemplate.BLANK
    ): File {
        return createNewProjectInDir(getScriptsDirectory(context), title, author, template)
    }

    fun createNewProjectInDir(
        directory: File,
        title: String,
        author: String = "",
        template: ScriptTemplate = ScriptTemplate.BLANK
    ): File {
        if (!directory.exists()) directory.mkdirs()
        val safeTitle = sanitizeFileName(title.ifBlank { "Untitled_Screenplay" })
        val file = File(directory, "$safeTitle.fountain")

        val titlePage = TitlePageData(
            title = title.ifBlank { "UNTITLED SCREENPLAY" },
            author = author,
            writtenByLabel = "written by",
            draftInfo = "First Draft"
        )

        val blocks = buildTemplateBlocks(template)
        val content = FountainSerializer.exportToFountain(blocks, titlePage)
        file.writeText(content)
        return file
    }

    fun duplicateProject(
        context: Context,
        sourceFile: File,
        newTitle: String
    ): File {
        return duplicateProjectInDir(getScriptsDirectory(context), sourceFile, newTitle)
    }

    fun duplicateProjectInDir(
        directory: File,
        sourceFile: File,
        newTitle: String
    ): File {
        if (!directory.exists()) directory.mkdirs()
        val ext = sourceFile.extension.ifBlank { "fountain" }
        val safeTitle = sanitizeFileName(newTitle.ifBlank { "${sourceFile.nameWithoutExtension}_Copy" })
        val targetFile = File(directory, "$safeTitle.$ext")

        if (sourceFile.exists()) {
            val content = sourceFile.readText()
            // If it's Fountain, update Title header
            val updatedContent = if (ext.equals("fountain", ignoreCase = true)) {
                if (content.contains("Title:")) {
                    content.replaceFirst(Regex("Title:[^\\n]*"), "Title: $newTitle")
                } else {
                    "Title: $newTitle\n\n$content"
                }
            } else content

            targetFile.writeText(updatedContent)
        }
        return targetFile
    }

    fun renameProject(
        context: Context,
        file: File,
        newTitle: String
    ): File {
        return renameProjectInDir(getScriptsDirectory(context), file, newTitle)
    }

    fun renameProjectInDir(
        directory: File,
        file: File,
        newTitle: String
    ): File {
        val ext = file.extension.ifBlank { "fountain" }
        val safeTitle = sanitizeFileName(newTitle.ifBlank { "Renamed_Script" })
        val newFile = File(directory, "$safeTitle.$ext")

        if (file.exists()) {
            val content = file.readText()
            val updatedContent = if (ext.equals("fountain", ignoreCase = true)) {
                if (content.contains("Title:")) {
                    content.replaceFirst(Regex("Title:[^\\n]*"), "Title: $newTitle")
                } else {
                    "Title: $newTitle\n\n$content"
                }
            } else content
            newFile.writeText(updatedContent)
            if (file.absolutePath != newFile.absolutePath) {
                file.delete()
            }
        }
        return newFile
    }

    fun deleteProject(file: File): Boolean {
        return if (file.exists()) file.delete() else false
    }

    private fun buildTemplateBlocks(template: ScriptTemplate): List<ScreenplayBlock> {
        return when (template) {
            ScriptTemplate.STAGE_PLAY -> listOf(
                ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 1"),
                ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "")
            )
            ScriptTemplate.COMIC_BOOK -> listOf(
                ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 1"),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "")
            )
            else -> listOf(
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")
            )
        }
    }

    private fun sanitizeFileName(name: String): String {
        val cleaned = name.replace(Regex("[^a-zA-Z0-9._-]"), "_").trim('_')
        return cleaned.ifBlank { "Screenplay" }
    }
}
