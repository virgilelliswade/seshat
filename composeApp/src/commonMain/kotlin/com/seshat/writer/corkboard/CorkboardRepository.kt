package com.seshat.writer.corkboard

import android.content.Context
import org.json.JSONObject
import java.io.File

object CorkboardRepository {

    private const val DIRECTORY_NAME = "corkboards"

    private fun getStorageDirectory(context: Context? = null): File {
        val base = context?.filesDir ?: com.seshat.writer.platform.AppPlatform.storage.getFilesDir()
        val dir = File(base, DIRECTORY_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun sanitizeTitle(title: String): String {
        return title.trim().replace(Regex("[^a-zA-Z0-9._-]"), "_").take(60)
            .ifBlank { "UNTITLED_SCREENPLAY" }
    }

    fun loadCorkboard(context: Context? = null, scriptTitle: String): CorkboardDocument {
        return try {
            val safe = sanitizeTitle(scriptTitle)
            val file = File(getStorageDirectory(context), "$safe.corkboard.json")
            if (!file.exists()) {
                return CorkboardDocument(scriptTitle = scriptTitle)
            }
            val content = file.readText()
            if (content.isBlank()) {
                return CorkboardDocument(scriptTitle = scriptTitle)
            }
            val json = JSONObject(content)
            CorkboardDocument.fromJson(json).copy(scriptTitle = scriptTitle)
        } catch (_: Exception) {
            CorkboardDocument(scriptTitle = scriptTitle)
        }
    }

    fun saveCorkboard(context: Context? = null, corkboard: CorkboardDocument): Boolean {
        return try {
            val safe = sanitizeTitle(corkboard.scriptTitle)
            val file = File(getStorageDirectory(context), "$safe.corkboard.json")
            file.writeText(corkboard.toJson().toString(2))
            true

        } catch (_: Exception) {
            false
        }
    }

    fun exportCorkboardToMarkdown(
        corkboard: CorkboardDocument,
        cards: List<SceneCard>
    ): String {
        return buildString {
            append("# SESHAT CORKBOARD OUTLINE: ${corkboard.scriptTitle.uppercase()}\n\n")
            append("Act Structure: ${corkboard.actCount} Acts | Total Scenes: ${cards.size}\n\n")

            for (act in corkboard.acts) {
                append("## ${act.title.uppercase()}")
                if (act.subtitle.isNotBlank()) append(" — *${act.subtitle}*")
                append("\n\n")

                val actCards = cards.filter { it.actIndex == act.actIndex }
                if (actCards.isEmpty()) {
                    append("*(No scenes currently placed in this act)*\n\n")
                } else {
                    for (card in actCards) {
                        append("### Scene ${card.sceneNumber}: ${card.headingText}\n")
                        if (card.summary.isNotBlank()) {
                            append("> ${card.summary}\n")
                        } else {
                            append("> *(No summary written)*\n")
                        }
                        if (card.charactersInScene.isNotEmpty()) {
                            append("- **Characters:** ${card.charactersInScene.joinToString(", ")}\n")
                        }
                        append("- **Est. Time:** ~${card.estimatedMinutes} min | **Blocks:** ${card.blockCount}\n\n")
                    }
                }
            }
        }
    }
}
