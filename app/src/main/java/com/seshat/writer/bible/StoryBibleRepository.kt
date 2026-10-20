package com.seshat.writer.bible

import android.content.Context
import java.io.File

object StoryBibleRepository {

    private const val BIBLES_DIR = "story_bibles"

    private fun getBiblesDir(context: Context): File {
        val dir = File(context.filesDir, BIBLES_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getSafeFileName(title: String): String {
        val safe = title.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return if (safe.isBlank()) "untitled_story_bible.bible.json" else "$safe.bible.json"
    }

    fun loadBible(context: Context, scriptTitle: String): StoryBible {
        return try {
            val file = File(getBiblesDir(context), getSafeFileName(scriptTitle))
            if (file.exists() && file.canRead()) {
                val json = file.readText()
                StoryBible.fromJsonString(json)
            } else {
                StoryBible(scriptTitle = scriptTitle)
            }
        } catch (_: Exception) {
            StoryBible(scriptTitle = scriptTitle)
        }
    }

    fun saveBible(context: Context, bible: StoryBible): Boolean {
        return try {
            val file = File(getBiblesDir(context), getSafeFileName(bible.scriptTitle))
            val updated = bible.copy(lastModified = System.currentTimeMillis())
            file.writeText(updated.toJsonString())
            true
        } catch (_: Exception) {
            false
        }
    }

    fun exportBibleToMarkdown(bible: StoryBible): String {
        return buildString {
            append("# SESHAT STORY BIBLE & LORE DOCUMENT\n")
            append("## **${bible.scriptTitle.uppercase()}**\n")
            if (bible.author.isNotBlank()) append("By: ${bible.author}\n\n") else append("\n")

            append("### 1. COSMOLOGY & METAPHYSICS\n")
            append(bible.lore.cosmology.ifBlank { "*(Not yet specified)*" }).append("\n\n")

            append("### 2. ANTHROPOLOGY & FACTIONS\n")
            append(bible.lore.anthropology.ifBlank { "*(Not yet specified)*" }).append("\n\n")

            append("### 3. TECHNOLOGY & MAGIC LAWS\n")
            append(bible.lore.technology.ifBlank { "*(Not yet specified)*" }).append("\n\n")

            append("### 4. CHRONOLOGY & TIMELINE\n")
            append(bible.lore.chronology.ifBlank { "*(Not yet specified)*" }).append("\n\n")

            if (bible.characters.isNotEmpty()) {
                append("### 5. CHARACTER DOSSIERS\n")
                for (c in bible.characters) {
                    append("#### ${c.name.uppercase()} (${c.role})\n")
                    if (c.factionOrCaste.isNotBlank()) append("- **Faction/Caste:** ${c.factionOrCaste}\n")
                    if (c.physicalDescription.isNotBlank()) append("- **Physical Description:** ${c.physicalDescription}\n")
                    if (c.imageUrl.isNotBlank()) append("- **Portrait Image:** ${c.imageUrl}\n")
                    if (c.arcAndNeed.isNotBlank()) append("- **Internal Need & Arc:** ${c.arcAndNeed}\n")
                    if (c.goalAndStakes.isNotBlank()) append("- **External Goal:** ${c.goalAndStakes}\n")
                    if (c.voiceCadence.isNotBlank()) append("- **Voice & Cadence:** ${c.voiceCadence}\n")
                    if (c.secretsAndLore.isNotBlank()) append("- **Lore Secrets:** ${c.secretsAndLore}\n")
                    append("\n")
                }
            }

            if (bible.locations.isNotEmpty()) {
                append("### 6. LOCATION CODEX\n")
                for (l in bible.locations) {
                    append("#### ${l.sluglinePrefix} ${l.name.uppercase()}\n")
                    if (l.visualAtmosphere.isNotBlank()) append("- **Atmosphere:** ${l.visualAtmosphere}\n")
                    if (l.culturalSignificance.isNotBlank()) append("- **Significance:** ${l.culturalSignificance}\n")
                    if (l.loreAndHazards.isNotBlank()) append("- **Hazards & Lore:** ${l.loreAndHazards}\n")
                    append("\n")
                }
            }

            if (bible.beats.isNotEmpty()) {
                append("### 7. OUTLINE BEAT SHEET\n")
                for (b in bible.beats) {
                    append("- **[${b.act}] ${b.title}:** ${b.summary}")
                    if (b.dramaticShift.isNotBlank()) append(" *(Shift: ${b.dramaticShift})*")
                    append("\n")
                }
                append("\n")
            }

            if (bible.tvConfig.isTvSeries) {
                append("### 8. TELEVISION SERIES BIBLE & EPISODE GRID\n")
                append("- **Series Format:** ${bible.tvConfig.format}\n")
                if (bible.tvConfig.seasonalArcSummary.isNotBlank()) {
                    append("- **Seasonal Arc:** ${bible.tvConfig.seasonalArcSummary}\n\n")
                }
                append("#### Episode Grid:\n")
                for (ep in bible.tvConfig.episodes) {
                    append("1. **Episode ${ep.episodeNumber}: ${ep.title}** [${ep.status}]\n")
                    if (ep.aStory.isNotBlank()) append("   - A-Story: ${ep.aStory}\n")
                    if (ep.bStory.isNotBlank()) append("   - B-Story: ${ep.bStory}\n")
                }
            }
        }
    }
}
