package com.seshat.writer.bundle

import android.content.Context
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.export.FountainSerializer
import com.seshat.writer.firstdraft.FirstDraftController
import com.seshat.writer.firstdraft.FirstDraftState
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.novel.NovelManuscript
import com.seshat.writer.titlepage.TitlePageData
import com.seshat.writer.tv.TvEpisode
import com.seshat.writer.tv.TvSeriesProject
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Handles exporting and importing self-contained .seshat project bundles.
 * A .seshat file is a compressed ZIP archive containing:
 * - manifest.json (project metadata & format)
 * - tv_project.json or novel.json or script.fountain
 * - story_bible.json (unified Lore Bible & worldbuilding)
 * - corkboards/ (per-episode or act outlines)
 * - episodes/ (Fountain scripts for external editing & writers' room review)
 */
object SeshatBundleManager {

    private const val BUNDLES_DIR = "seshat_bundles"
    const val BUNDLE_EXTENSION = ".seshat"

    fun getBundlesDirectory(context: Context): File {
        val dir = File(context.filesDir, BUNDLES_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun sanitizeFileName(name: String): String {
        val clean = name.trim().replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return if (clean.isBlank()) "untitled_project" else clean
    }

    /**
     * Export a TV Series Project into a unified .seshat bundle file.
     */
    fun exportTvSeriesBundle(
        context: Context? = null,
        project: TvSeriesProject,
        outputFile: File? = null,
        firstDraftState: FirstDraftState? = null
    ): File? {
        return try {
            val targetFile = outputFile ?: if (context != null) {
                File(getBundlesDirectory(context), "${sanitizeFileName(project.title)}$BUNDLE_EXTENSION")
            } else null
            if (targetFile == null) return null

            val fdState = firstDraftState ?: if (context != null) {
                FirstDraftController.loadState(context, project.title, ProjectFormat.TV_SERIES, project.format)
            } else FirstDraftController.createForNewProject(ProjectFormat.TV_SERIES, project.format)

            FileOutputStream(targetFile).use { fos ->
                ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                    // 1. manifest.json
                    val manifest = JSONObject().apply {
                        put("seshatVersion", "2.0.0")
                        put("projectFormat", ProjectFormat.TV_SERIES.name)
                        put("screenplayType", project.format.name)
                        put("title", project.title)
                        put("showrunner", project.showrunner)
                        put("episodeCount", project.episodes.size)
                        put("exportedAt", System.currentTimeMillis())
                        put("firstDraft", fdState.toJson())
                    }
                    writeZipEntry(zos, "manifest.json", manifest.toString(2))
                    writeZipEntry(zos, "first_draft.json", fdState.toJson().toString(2))

                    // 2. tv_project.json (Complete project state)
                    writeZipEntry(zos, "tv_project.json", project.toJson().toString(2))

                    // 3. story_bible.json (Unified series lore bible)
                    writeZipEntry(zos, "bible/story_bible.json", project.seriesBible.toJsonString())

                    // 4. Per-episode Fountain scripts & corkboards for writers' room interoperability
                    for (ep in project.episodes) {
                        val safeEpNumber = sanitizeFileName(ep.episodeNumber)
                        val safeEpTitle = sanitizeFileName(ep.title)
                        val baseName = "ep_${safeEpNumber}_$safeEpTitle"

                        // Episode Fountain script
                        val titleData = TitlePageData(
                            title = "${project.title} - Ep ${ep.episodeNumber}: ${ep.title}",
                            author = ep.writerCredit.ifBlank { project.showrunner },
                            draftInfo = ep.draftTag
                        )
                        val fountainText = FountainSerializer.exportToFountain(ep.blocks, titleData)
                        writeZipEntry(zos, "episodes/$baseName.fountain", fountainText)

                        // Episode Corkboard Outline
                        writeZipEntry(zos, "corkboards/$baseName.corkboard.json", ep.corkboard.toJson().toString(2))
                    }
                }
            }
            targetFile
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Import a TV Series Project from a .seshat bundle file.
     */
    fun importTvSeriesBundle(bundleFile: File): TvSeriesProject? {
        if (!bundleFile.exists() || !bundleFile.canRead()) return null
        return try {
            var tvProjectJson: String? = null
            var manifestJson: String? = null

            FileInputStream(bundleFile).use { fis ->
                ZipInputStream(BufferedInputStream(fis)).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            "manifest.json" -> {
                                manifestJson = readZipEntryString(zis)
                            }
                            "tv_project.json" -> {
                                tvProjectJson = readZipEntryString(zis)
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }

            if (tvProjectJson != null) {
                TvSeriesProject.fromJson(JSONObject(tvProjectJson!!))
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Export a standalone Feature Screenplay into a .seshat bundle.
     */
    fun exportScreenplayBundle(
        context: Context? = null,
        title: String,
        author: String,
        screenplayType: ScreenplayType,
        blocks: List<ScreenplayBlock>,
        corkboard: CorkboardDocument,
        storyBible: StoryBible,
        outputFile: File? = null,
        firstDraftState: FirstDraftState? = null
    ): File? {
        return try {
            val targetFile = outputFile ?: if (context != null) {
                File(getBundlesDirectory(context), "${sanitizeFileName(title)}$BUNDLE_EXTENSION")
            } else null
            if (targetFile == null) return null

            val fdState = firstDraftState ?: if (context != null) {
                FirstDraftController.loadState(context, title, ProjectFormat.SCREENPLAY, screenplayType)
            } else FirstDraftController.createForNewProject(ProjectFormat.SCREENPLAY, screenplayType)

            FileOutputStream(targetFile).use { fos ->
                ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                    val manifest = JSONObject().apply {
                        put("seshatVersion", "2.0.0")
                        put("projectFormat", ProjectFormat.SCREENPLAY.name)
                        put("screenplayType", screenplayType.name)
                        put("title", title)
                        put("author", author)
                        put("exportedAt", System.currentTimeMillis())
                        put("firstDraft", fdState.toJson())
                    }
                    writeZipEntry(zos, "manifest.json", manifest.toString(2))
                    writeZipEntry(zos, "first_draft.json", fdState.toJson().toString(2))

                    val titleData = TitlePageData(title = title, author = author)
                    val fountainText = FountainSerializer.exportToFountain(blocks, titleData)
                    writeZipEntry(zos, "screenplay.fountain", fountainText)
                    writeZipEntry(zos, "corkboard.json", corkboard.toJson().toString(2))
                    writeZipEntry(zos, "story_bible.json", storyBible.toJsonString())
                }
            }
            targetFile
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Export a Novel Manuscript into a .seshat bundle.
     */
    fun exportNovelBundle(
        context: Context? = null,
        manuscript: NovelManuscript,
        corkboard: CorkboardDocument,
        storyBible: StoryBible,
        outputFile: File? = null,
        firstDraftState: FirstDraftState? = null
    ): File? {
        return try {
            val targetFile = outputFile ?: if (context != null) {
                File(getBundlesDirectory(context), "${sanitizeFileName(manuscript.title)}$BUNDLE_EXTENSION")
            } else null
            if (targetFile == null) return null

            val fdState = firstDraftState ?: if (context != null) {
                FirstDraftController.loadState(context, manuscript.title, ProjectFormat.NOVEL, ScreenplayType.FEATURE)
            } else FirstDraftController.createForNewProject(ProjectFormat.NOVEL, ScreenplayType.FEATURE)

            FileOutputStream(targetFile).use { fos ->
                ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                    val manifest = JSONObject().apply {
                        put("seshatVersion", "2.0.0")
                        put("projectFormat", ProjectFormat.NOVEL.name)
                        put("title", manuscript.title)
                        put("author", manuscript.author)
                        put("chapterCount", manuscript.chapters.size)
                        put("exportedAt", System.currentTimeMillis())
                        put("firstDraft", fdState.toJson())
                    }
                    writeZipEntry(zos, "manifest.json", manifest.toString(2))
                    writeZipEntry(zos, "first_draft.json", fdState.toJson().toString(2))
                    writeZipEntry(zos, "novel.json", manuscript.toJson().toString(2))
                    writeZipEntry(zos, "corkboard.json", corkboard.toJson().toString(2))
                    writeZipEntry(zos, "story_bible.json", storyBible.toJsonString())
                }
            }
            targetFile
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Import FirstDraftState from a .seshat bundle file if present.
     */
    fun importFirstDraftState(bundleFile: File): FirstDraftState? {
        if (!bundleFile.exists() || !bundleFile.canRead()) return null
        return try {
            var fdJsonStr: String? = null
            var manifestJsonStr: String? = null
            FileInputStream(bundleFile).use { fis ->
                ZipInputStream(BufferedInputStream(fis)).use { zis ->
                    var entry: ZipEntry? = zis.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            "first_draft.json" -> fdJsonStr = readZipEntryString(zis)
                            "manifest.json" -> manifestJsonStr = readZipEntryString(zis)
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            when {
                fdJsonStr != null -> FirstDraftState.fromJson(JSONObject(fdJsonStr!!))
                manifestJsonStr != null -> {
                    val mObj = JSONObject(manifestJsonStr!!)
                    if (mObj.has("firstDraft")) {
                        FirstDraftState.fromJson(mObj.optJSONObject("firstDraft"))
                    } else null
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun writeZipEntry(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        zos.write(bytes, 0, bytes.size)
        zos.closeEntry()
    }

    private fun readZipEntryString(zis: ZipInputStream): String {
        val buffer = ByteArray(4096)
        val out = java.io.ByteArrayOutputStream()
        var read: Int
        while (zis.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
        return out.toString(StandardCharsets.UTF_8.name())
    }
}
