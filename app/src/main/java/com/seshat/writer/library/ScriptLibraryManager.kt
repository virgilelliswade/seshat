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
import org.json.JSONObject
import java.io.File
import java.util.Locale

object ScriptLibraryManager {

    fun getScriptsDirectory(context: Context): File {
        return ScriptFileManager.getScriptsDirectory(context)
    }

    fun listProjects(context: Context, activeTitle: String = ""): List<ScriptProject> {
        val base = context.filesDir
        val allProjects = mutableListOf<ScriptProject>()

        // 1. Primary scripts directory
        val scriptsDir = getScriptsDirectory(context)
        if (scriptsDir.exists()) {
            allProjects.addAll(listProjectsInDir(scriptsDir, activeTitle))
        }

        // 2. Bundles, Universes, Novels & root studio directories
        if (base != null && base.exists()) {
            val bundlesDir = File(base, "bundles")
            if (bundlesDir.exists() && bundlesDir != scriptsDir) {
                allProjects.addAll(listProjectsInDir(bundlesDir, activeTitle))
            }

            val universeDir = File(base, "blended_universe_projects")
            if (universeDir.exists() && universeDir != scriptsDir) {
                allProjects.addAll(listProjectsInDir(universeDir, activeTitle))
            }

            val novelsDir = File(base, "novels")
            if (novelsDir.exists() && novelsDir != scriptsDir) {
                allProjects.addAll(listProjectsInDir(novelsDir, activeTitle))
            }

            if (base != scriptsDir) {
                allProjects.addAll(listProjectsInDir(base, activeTitle))
            }
        }

        return allProjects
            .distinctBy { it.file.absolutePath }
            .sortedByDescending { it.lastModified }
    }

    fun listProjectsInDir(directory: File, activeTitle: String = ""): List<ScriptProject> {
        val files = directory.listFiles { f ->
            f.isFile && (
                f.extension.equals("fountain", ignoreCase = true) ||
                f.extension.equals("fdx", ignoreCase = true) ||
                f.extension.equals("seshat", ignoreCase = true) ||
                f.extension.equals("spmd", ignoreCase = true) ||
                f.name.endsWith(".universe.json", ignoreCase = true) ||
                f.name.endsWith(".novel.json", ignoreCase = true)
            )
        } ?: return emptyList()

        return files.mapNotNull { file ->
            try {
                if (file.name.endsWith(".universe.json", ignoreCase = true)) {
                    val json = try { JSONObject(file.readText()) } catch (_: Throwable) { JSONObject() }
                    val title = json.optString("title", file.nameWithoutExtension.removeSuffix(".universe"))
                    val assignmentsArr = json.optJSONArray("assignments")
                    val branchCount = assignmentsArr?.length() ?: 1
                    val isActive = activeTitle.isNotBlank() && (title.equals(activeTitle, ignoreCase = true) || activeTitle.startsWith(title, ignoreCase = true))
                    ScriptProject(
                        id = file.name,
                        title = title,
                        author = "Story Universe (${branchCount} branches)",
                        file = file,
                        format = "UNIVERSE",
                        lastModified = file.lastModified(),
                        fileSizeBytes = file.length(),
                        sceneCount = branchCount,
                        pageCount = branchCount,
                        revisionColor = RevisionColor.GOLDENROD,
                        draftTag = "Living World Tree",
                        isActive = isActive
                    )
                } else if (file.name.endsWith(".novel.json", ignoreCase = true)) {
                    val json = try { JSONObject(file.readText()) } catch (_: Throwable) { JSONObject() }
                    val title = json.optString("title", file.nameWithoutExtension.removeSuffix(".novel"))
                    val author = json.optString("author", "")
                    val chaptersArr = json.optJSONArray("chapters")
                    val chCount = chaptersArr?.length() ?: 1
                    val isActive = activeTitle.isNotBlank() && title.equals(activeTitle, ignoreCase = true)
                    ScriptProject(
                        id = file.name,
                        title = title,
                        author = author.ifBlank { "Novel Manuscript" },
                        file = file,
                        format = "NOVEL",
                        lastModified = file.lastModified(),
                        fileSizeBytes = file.length(),
                        sceneCount = chCount,
                        pageCount = chCount * 10,
                        revisionColor = RevisionColor.CHERRY,
                        draftTag = "Novel Chapters ($chCount)",
                        isActive = isActive
                    )
                } else {
                    val importResult = ScriptImporter.importFromFile(file)
                    val doc = importResult.document
                    val title = doc?.titlePage?.title?.ifBlank { file.nameWithoutExtension } ?: file.nameWithoutExtension
                    val author = doc?.titlePage?.author ?: ""
                    val sceneCount = doc?.blocks?.count { it.type == BlockType.SCENE_HEADING } ?: 1
                    val estimatedPageCount = (doc?.blocks?.size ?: 10) / 25 + 1
                    val formatStr = when {
                        file.extension.equals("seshat", ignoreCase = true) -> "SESHAT"
                        importResult.detectedFormat == ScriptFormat.FINAL_DRAFT -> "FDX"
                        importResult.detectedFormat == ScriptFormat.PDF -> "PDF"
                        else -> "FOUNTAIN"
                    }
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
                }
            } catch (_: Throwable) {
                null
            }
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
        val targetDir = sourceFile.parentFile ?: getScriptsDirectory(context)
        return duplicateProjectInDir(targetDir, sourceFile, newTitle)
    }

    fun duplicateProjectInDir(
        directory: File,
        sourceFile: File,
        newTitle: String
    ): File {
        if (!directory.exists()) directory.mkdirs()
        val isUniverse = sourceFile.name.endsWith(".universe.json", ignoreCase = true)
        val isNovel = sourceFile.name.endsWith(".novel.json", ignoreCase = true)
        val ext = when {
            isUniverse -> "universe.json"
            isNovel -> "novel.json"
            else -> sourceFile.extension.ifBlank { "fountain" }
        }
        val safeTitle = sanitizeFileName(newTitle.ifBlank {
            when {
                isUniverse -> "${sourceFile.name.removeSuffix(".universe.json")}_Copy"
                isNovel -> "${sourceFile.name.removeSuffix(".novel.json")}_Copy"
                else -> "${sourceFile.nameWithoutExtension}_Copy"
            }
        })
        val targetFile = File(directory, "$safeTitle.$ext")

        if (sourceFile.exists()) {
            val content = sourceFile.readText()
            val updatedContent = when {
                isUniverse -> {
                    try {
                        val json = JSONObject(content)
                        json.put("title", newTitle)
                        json.toString(2)
                    } catch (_: Throwable) { content }
                }
                isNovel -> {
                    try {
                        val json = JSONObject(content)
                        json.put("title", newTitle)
                        json.toString(2)
                    } catch (_: Throwable) { content }
                }
                ext.equals("fountain", ignoreCase = true) -> {
                    if (content.contains("Title:")) {
                        content.replaceFirst(Regex("Title:[^\\n]*"), "Title: $newTitle")
                    } else {
                        "Title: $newTitle\n\n$content"
                    }
                }
                else -> content
            }

            targetFile.writeText(updatedContent)
        }
        return targetFile
    }

    fun renameProject(
        context: Context,
        file: File,
        newTitle: String
    ): File {
        val targetDir = file.parentFile ?: getScriptsDirectory(context)
        return renameProjectInDir(targetDir, file, newTitle)
    }

    fun renameProjectInDir(
        directory: File,
        file: File,
        newTitle: String
    ): File {
        val isUniverse = file.name.endsWith(".universe.json", ignoreCase = true)
        val isNovel = file.name.endsWith(".novel.json", ignoreCase = true)
        val ext = when {
            isUniverse -> "universe.json"
            isNovel -> "novel.json"
            else -> file.extension.ifBlank { "fountain" }
        }
        val safeTitle = sanitizeFileName(newTitle.ifBlank { "Renamed_Script" })
        val newFile = File(directory, "$safeTitle.$ext")

        if (file.exists()) {
            val content = file.readText()
            val updatedContent = when {
                isUniverse -> {
                    try {
                        val json = JSONObject(content)
                        json.put("title", newTitle)
                        json.toString(2)
                    } catch (_: Throwable) { content }
                }
                isNovel -> {
                    try {
                        val json = JSONObject(content)
                        json.put("title", newTitle)
                        json.toString(2)
                    } catch (_: Throwable) { content }
                }
                ext.equals("fountain", ignoreCase = true) -> {
                    if (content.contains("Title:")) {
                        content.replaceFirst(Regex("Title:[^\\n]*"), "Title: $newTitle")
                    } else {
                        "Title: $newTitle\n\n$content"
                    }
                }
                else -> content
            }
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
