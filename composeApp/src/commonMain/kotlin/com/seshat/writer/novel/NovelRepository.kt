package com.seshat.writer.novel

import android.content.Context
import com.seshat.writer.corkboard.CorkboardAct
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.corkboard.CorkboardRepository
import com.seshat.writer.corkboard.SceneCard
import org.json.JSONObject
import java.io.File

object NovelRepository {

    private const val DIRECTORY_NAME = "novels"

    private fun getStorageDirectory(context: Context? = null): File {
        val base = context?.filesDir ?: com.seshat.writer.platform.AppPlatform.storage.getFilesDir()
        val dir = File(base, DIRECTORY_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun sanitizeTitle(title: String): String {
        return title.trim().replace(Regex("[^a-zA-Z0-9._-]"), "_").take(60)
            .ifBlank { "UNTITLED_NOVEL" }
    }

    fun loadManuscript(context: Context? = null, title: String): NovelManuscript {
        return try {
            val safe = sanitizeTitle(title)
            val file = File(getStorageDirectory(context), "$safe.novel.json")
            if (!file.exists()) {
                return NovelManuscript(title = title)
            }
            val content = file.readText()
            if (content.isBlank()) {
                return NovelManuscript(title = title)
            }
            val json = JSONObject(content)
            NovelManuscript.fromJson(json).copy(title = title)
        } catch (_: Exception) {
            NovelManuscript(title = title)
        }
    }

    fun saveManuscript(context: Context? = null, manuscript: NovelManuscript): Boolean {
        return try {
            val safe = sanitizeTitle(manuscript.title)
            val file = File(getStorageDirectory(context), "$safe.novel.json")
            file.writeText(manuscript.toJson().toString(2))
            // Also sync chapters to Corkboard
            syncManuscriptToCorkboard(context, manuscript)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Bridges the Scrivener-style Novel Binder with the Corkboard.
     * Each chapter is represented as an index card inside its Act/Part swimlane.
     */
    fun syncManuscriptToCorkboard(context: Context? = null, manuscript: NovelManuscript): CorkboardDocument {
        val currentCorkboard = CorkboardRepository.loadCorkboard(context, manuscript.title)
        val updatedMeta = currentCorkboard.cardMetadata.toMutableMap()

        for (ch in manuscript.chapters) {
            val existing = updatedMeta[ch.id]
            updatedMeta[ch.id] = CorkboardDocument.CardMetadata(
                summary = ch.synopsis.ifBlank { existing?.summary ?: "" },
                actIndex = ch.actIndex,
                colorTag = ch.colorTag
            )
        }

        val updatedDoc = currentCorkboard.copy(
            scriptTitle = manuscript.title,
            actCount = manuscript.acts.size,
            acts = manuscript.acts,
            cardMetadata = updatedMeta
        )
        CorkboardRepository.saveCorkboard(context, updatedDoc)
        return updatedDoc
    }

    /**
     * Converts novel binder chapters to corkboard scene cards.
     */
    fun chaptersToSceneCards(manuscript: NovelManuscript): List<SceneCard> {
        return manuscript.chapters.map { ch ->
            SceneCard(
                sceneHeadingBlockId = ch.id,
                sceneNumber = ch.chapterNumber,
                headingText = ch.title.uppercase(java.util.Locale.US),
                summary = ch.synopsis,
                actIndex = ch.actIndex,
                colorTag = ch.colorTag,
                blockCount = ch.paragraphs.size,
                estimatedMinutes = (ch.wordCount() / 225.0f).coerceAtLeast(1.0f)
            )
        }
    }

    /**
     * Exports the compiled manuscript to an output file in the device's public or app document directory.
     */
    fun compileAndExport(
        context: Context? = null,
        manuscript: NovelManuscript,
        options: CompilerOptions
    ): File? {
        val safe = sanitizeTitle(manuscript.title)
        val baseDir = context?.filesDir ?: com.seshat.writer.platform.AppPlatform.storage.getFilesDir()
        val exportDir = File(baseDir, "compiled_manuscripts")
        if (!exportDir.exists()) exportDir.mkdirs()


        val ext = options.format.extension
        val outputFile = File(exportDir, "$safe.$ext")

        return when (options.format) {
            CompilerFormat.EPUB -> {
                val success = EpubCompiler.compileToEpub(manuscript, options, outputFile)
                if (success) outputFile else null
            }
            CompilerFormat.PDF_MANUSCRIPT -> {
                val success = NovelPdfCompiler.compileToPdf(context ?: Context(), manuscript, options, outputFile)
                if (success) outputFile else null
            }
            CompilerFormat.SHUNN_MANUSCRIPT -> {
                val text = ShunnCompiler.compileToShunnFormat(manuscript, options)
                outputFile.writeText(text)
                outputFile
            }
            CompilerFormat.MARKDOWN -> {
                val md = ShunnCompiler.compileToMarkdown(manuscript, options)
                outputFile.writeText(md)
                outputFile
            }
            CompilerFormat.HTML -> {
                val html = ShunnCompiler.compileToHtml(manuscript, options)
                outputFile.writeText(html)
                outputFile
            }
        }
    }
}
