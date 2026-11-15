package com.seshat.writer.importer

import com.seshat.writer.export.FinalDraftSerializer
import com.seshat.writer.export.FountainSerializer
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.titlepage.TitlePageData
import com.seshat.writer.tv.TvSeriesProject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipInputStream
import org.json.JSONObject

enum class ScriptFormat {
    FOUNTAIN,
    FINAL_DRAFT,
    PDF,
    SESHAT,
    UNKNOWN
}

data class ScreenplayImportDocument(
    val titlePage: TitlePageData,
    val blocks: List<ScreenplayBlock>
)

data class ScriptImportResult(
    val isSuccess: Boolean,
    val document: ScreenplayImportDocument? = null,
    val detectedFormat: ScriptFormat = ScriptFormat.UNKNOWN,
    val errorMessage: String? = null
)

object ScriptImporter {

    fun detectFormat(content: String, fileName: String = ""): ScriptFormat {
        val lowerName = fileName.lowercase(Locale.US)
        if (lowerName.endsWith(".seshat")) {
            return ScriptFormat.SESHAT
        }
        if (lowerName.endsWith(".pdf")) {
            return ScriptFormat.PDF
        }
        if (lowerName.endsWith(".fdx") || lowerName.endsWith(".xml")) {
            return ScriptFormat.FINAL_DRAFT
        }
        if (lowerName.endsWith(".fountain") || lowerName.endsWith(".spmd") || lowerName.endsWith(".txt")) {
            return ScriptFormat.FOUNTAIN
        }

        val trimmed = content.trimStart()
        if (trimmed.startsWith("<?xml") || trimmed.contains("<FinalDraft", ignoreCase = true) || trimmed.contains("<Content>", ignoreCase = true)) {
            return ScriptFormat.FINAL_DRAFT
        }

        // Fountain heuristic (Title:, Credit:, Author:, INT., EXT.)
        if (trimmed.startsWith("Title:", ignoreCase = true) ||
            trimmed.contains("\nINT. ") || trimmed.contains("\nEXT. ") ||
            trimmed.startsWith("INT. ") || trimmed.startsWith("EXT. ")) {
            return ScriptFormat.FOUNTAIN
        }

        return ScriptFormat.FOUNTAIN // Default fallback to Fountain parser
    }

    fun parseScriptContent(content: String, fileName: String = ""): ScriptImportResult {
        if (content.isBlank()) {
            return ScriptImportResult(
                isSuccess = false,
                errorMessage = "The selected file is empty."
            )
        }

        val format = detectFormat(content, fileName)
        return try {
            when (format) {
                ScriptFormat.FINAL_DRAFT -> {
                    val fdxDoc = FinalDraftSerializer.importFromFdx(content)
                    ScriptImportResult(
                        isSuccess = true,
                        document = ScreenplayImportDocument(
                            titlePage = fdxDoc.titlePage,
                            blocks = fdxDoc.blocks
                        ),
                        detectedFormat = ScriptFormat.FINAL_DRAFT
                    )
                }
                ScriptFormat.PDF -> {
                    ScriptImportResult(
                        isSuccess = false,
                        errorMessage = "Use importFromFile or importFromUri for binary PDF scripts."
                    )
                }
                ScriptFormat.SESHAT,
                ScriptFormat.FOUNTAIN,
                ScriptFormat.UNKNOWN -> {
                    val fountainDoc = FountainSerializer.importFromFountain(content)
                    ScriptImportResult(
                        isSuccess = true,
                        document = ScreenplayImportDocument(
                            titlePage = fountainDoc.titlePage,
                            blocks = fountainDoc.blocks
                        ),
                        detectedFormat = ScriptFormat.FOUNTAIN
                    )
                }
            }
        } catch (e: Exception) {
            ScriptImportResult(
                isSuccess = false,
                errorMessage = "Failed to parse script: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    fun importFromFile(file: File): ScriptImportResult {
        return if (!file.exists() || !file.canRead()) {
            ScriptImportResult(isSuccess = false, errorMessage = "File not found or unreadable: ${file.name}")
        } else if (file.name.lowercase(Locale.US).endsWith(".pdf")) {
            PdfScriptExtractor.extractFromFile(file)
        } else if (file.name.lowercase(Locale.US).endsWith(".seshat")) {
            try {
                FileInputStream(file).use { fis ->
                    importFromSeshatStream(fis, file.nameWithoutExtension)
                }
            } catch (e: Exception) {
                ScriptImportResult(isSuccess = false, errorMessage = "Failed to read .seshat bundle: ${e.message}")
            }
        } else {
            val content = file.readText()
            parseScriptContent(content, file.name)
        }
    }

    fun importFromSeshatStream(inputStream: InputStream, fallbackTitle: String = "Untitled"): ScriptImportResult {
        return try {
            var manifestJson: String? = null
            var fountainText: String? = null
            var novelJson: String? = null
            var tvJson: String? = null

            ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "manifest.json" -> manifestJson = readZipEntryString(zis)
                        "screenplay.fountain" -> fountainText = readZipEntryString(zis)
                        "novel.json" -> novelJson = readZipEntryString(zis)
                        "tv_project.json" -> tvJson = readZipEntryString(zis)
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            var title = fallbackTitle
            var author = ""

            if (!manifestJson.isNullOrBlank()) {
                try {
                    val mObj = JSONObject(manifestJson)
                    if (mObj.has("title")) title = mObj.getString("title").ifBlank { title }
                    if (mObj.has("author")) author = mObj.getString("author")
                } catch (_: Throwable) {}
            }

            if (!fountainText.isNullOrBlank()) {
                val fountainDoc = FountainSerializer.importFromFountain(fountainText)
                val finalTitle = fountainDoc.titlePage.title.ifBlank { title }
                val finalAuthor = fountainDoc.titlePage.author.ifBlank { author }
                ScriptImportResult(
                    isSuccess = true,
                    document = ScreenplayImportDocument(
                        titlePage = fountainDoc.titlePage.copy(title = finalTitle, author = finalAuthor),
                        blocks = fountainDoc.blocks
                    ),
                    detectedFormat = ScriptFormat.SESHAT
                )
            } else if (!novelJson.isNullOrBlank()) {
                try {
                    val nObj = JSONObject(novelJson)
                    val nTitle = if (nObj.has("title")) nObj.getString("title") else title
                    val nAuthor = if (nObj.has("author")) nObj.getString("author") else author
                    val blocks = mutableListOf<ScreenplayBlock>()
                    if (nObj.has("chapters")) {
                        val chs = nObj.getJSONArray("chapters")
                        for (i in 0 until chs.length()) {
                            val ch = chs.getJSONObject(i)
                            val chTitle = ch.optString("title", "Chapter ${i + 1}")
                            val chContent = ch.optString("content", "")
                            blocks.add(ScreenplayBlock(type = com.seshat.writer.model.BlockType.SCENE_HEADING, text = chTitle))
                            if (chContent.isNotBlank()) {
                                blocks.add(ScreenplayBlock(type = com.seshat.writer.model.BlockType.ACTION, text = chContent))
                            }
                        }
                    }
                    if (blocks.isEmpty()) {
                        blocks.add(ScreenplayBlock(type = com.seshat.writer.model.BlockType.SCENE_HEADING, text = "CHAPTER 1"))
                    }
                    ScriptImportResult(
                        isSuccess = true,
                        document = ScreenplayImportDocument(
                            titlePage = TitlePageData(title = nTitle, author = nAuthor),
                            blocks = blocks
                        ),
                        detectedFormat = ScriptFormat.SESHAT
                    )
                } catch (e: Exception) {
                    ScriptImportResult(isSuccess = false, errorMessage = "Failed to parse novel bundle: ${e.message}")
                }
            } else if (!tvJson.isNullOrBlank()) {
                try {
                    val tvProj = TvSeriesProject.fromJson(JSONObject(tvJson))
                    val activeEp = tvProj.getActiveEpisode() ?: tvProj.episodes.firstOrNull()
                    val blocks = activeEp?.blocks ?: listOf(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "TEASER"))
                    ScriptImportResult(
                        isSuccess = true,
                        document = ScreenplayImportDocument(
                            titlePage = TitlePageData(title = tvProj.title, author = tvProj.showrunner),
                            blocks = blocks
                        ),
                        detectedFormat = ScriptFormat.SESHAT
                    )
                } catch (e: Exception) {
                    ScriptImportResult(isSuccess = false, errorMessage = "Failed to parse TV series bundle: ${e.message}")
                }
            } else {
                ScriptImportResult(
                    isSuccess = false,
                    errorMessage = "No readable manuscript found in .seshat bundle."
                )
            }
        } catch (e: Exception) {
            ScriptImportResult(
                isSuccess = false,
                errorMessage = "Failed to open .seshat bundle: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    private fun readZipEntryString(zis: ZipInputStream): String {
        val buffer = ByteArray(4096)
        val out = ByteArrayOutputStream()
        var read: Int
        while (zis.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
        return out.toString(StandardCharsets.UTF_8.name())
    }
}
