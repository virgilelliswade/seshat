package com.seshat.writer.importer

import com.seshat.writer.export.FinalDraftSerializer
import com.seshat.writer.export.FountainSerializer
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.titlepage.TitlePageData
import java.io.File
import java.util.Locale

enum class ScriptFormat {
    FOUNTAIN,
    FINAL_DRAFT,
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
                ScriptFormat.FOUNTAIN, ScriptFormat.UNKNOWN -> {
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
        } else {
            val content = file.readText()
            parseScriptContent(content, file.name)
        }
    }
}
