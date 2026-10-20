package com.seshat.writer.bible

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * World-building lore document covering the four core pillars:
 * Cosmology, Anthropology, Technology, and Chronology.
 */
data class LoreDocument(
    val cosmology: String = "",
    val anthropology: String = "",
    val technology: String = "",
    val chronology: String = ""
) {
    fun wordCount(): Int {
        val totalText = "$cosmology $anthropology $technology $chronology".trim()
        if (totalText.isBlank()) return 0
        return totalText.split(Regex("\\s+")).count { it.isNotBlank() }
    }

    fun completenessScore(): Int {
        var filledPillars = 0
        if (cosmology.isNotBlank()) filledPillars++
        if (anthropology.isNotBlank()) filledPillars++
        if (technology.isNotBlank()) filledPillars++
        if (chronology.isNotBlank()) filledPillars++
        return (filledPillars * 25)
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("cosmology", cosmology)
        json.put("anthropology", anthropology)
        json.put("technology", technology)
        json.put("chronology", chronology)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): LoreDocument {
            return LoreDocument(
                cosmology = json.optString("cosmology", ""),
                anthropology = json.optString("anthropology", ""),
                technology = json.optString("technology", ""),
                chronology = json.optString("chronology", "")
            )
        }
    }
}

/**
 * Deep character dossier tracking internal need, external goal, faction allegiance, and vocal cadence.
 */
data class CharacterDossier(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val role: String = "Protagonist", // Protagonist, Antagonist, Ally, Mentor, Foil
    val factionOrCaste: String = "",
    val physicalDescription: String = "",
    val imageUrl: String = "",
    val arcAndNeed: String = "",
    val goalAndStakes: String = "",
    val voiceCadence: String = "",
    val secretsAndLore: String = ""
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("role", role)
        json.put("factionOrCaste", factionOrCaste)
        json.put("physicalDescription", physicalDescription)
        json.put("imageUrl", imageUrl)
        json.put("arcAndNeed", arcAndNeed)
        json.put("goalAndStakes", goalAndStakes)
        json.put("voiceCadence", voiceCadence)
        json.put("secretsAndLore", secretsAndLore)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): CharacterDossier {
            return CharacterDossier(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", ""),
                role = json.optString("role", "Protagonist"),
                factionOrCaste = json.optString("factionOrCaste", ""),
                physicalDescription = json.optString("physicalDescription", ""),
                imageUrl = json.optString("imageUrl", ""),
                arcAndNeed = json.optString("arcAndNeed", ""),
                goalAndStakes = json.optString("goalAndStakes", ""),
                voiceCadence = json.optString("voiceCadence", ""),
                secretsAndLore = json.optString("secretsAndLore", "")
            )
        }
    }
}

/**
 * World location entry with atmospheric and cultural details.
 */
data class LocationCodex(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val sluglinePrefix: String = "INT.",
    val visualAtmosphere: String = "",
    val culturalSignificance: String = "",
    val loreAndHazards: String = ""
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("sluglinePrefix", sluglinePrefix)
        json.put("visualAtmosphere", visualAtmosphere)
        json.put("culturalSignificance", culturalSignificance)
        json.put("loreAndHazards", loreAndHazards)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): LocationCodex {
            return LocationCodex(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", ""),
                sluglinePrefix = json.optString("sluglinePrefix", "INT."),
                visualAtmosphere = json.optString("visualAtmosphere", ""),
                culturalSignificance = json.optString("culturalSignificance", ""),
                loreAndHazards = json.optString("loreAndHazards", "")
            )
        }
    }
}

/**
 * An outline beat replacing/enriching corkboard index cards.
 */
data class StoryBeat(
    val id: String = UUID.randomUUID().toString(),
    val act: String = "Act I", // Act I, Act II-A, Act II-B, Act III, Climax
    val title: String = "",
    val summary: String = "",
    val charactersInvolved: String = "",
    val dramaticShift: String = "+ / -",
    val colorTag: String = "#D4AF37"
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("act", act)
        json.put("title", title)
        json.put("summary", summary)
        json.put("charactersInvolved", charactersInvolved)
        json.put("dramaticShift", dramaticShift)
        json.put("colorTag", colorTag)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): StoryBeat {
            return StoryBeat(
                id = json.optString("id", UUID.randomUUID().toString()),
                act = json.optString("act", "Act I"),
                title = json.optString("title", ""),
                summary = json.optString("summary", ""),
                charactersInvolved = json.optString("charactersInvolved", ""),
                dramaticShift = json.optString("dramaticShift", "+ / -"),
                colorTag = json.optString("colorTag", "#D4AF37")
            )
        }
    }
}

/**
 * TV Series / Episodic configuration tracking multi-episode outlines & seasonal arcs.
 */
data class TvEpisodeOutline(
    val episodeNumber: Int = 1,
    val title: String = "Pilot",
    val aStory: String = "",
    val bStory: String = "",
    val linkedScriptFile: String = "",
    val status: String = "Outlining" // Outlining, In Draft, Revised, Locked
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("episodeNumber", episodeNumber)
        json.put("title", title)
        json.put("aStory", aStory)
        json.put("bStory", bStory)
        json.put("linkedScriptFile", linkedScriptFile)
        json.put("status", status)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): TvEpisodeOutline {
            return TvEpisodeOutline(
                episodeNumber = json.optInt("episodeNumber", 1),
                title = json.optString("title", "Pilot"),
                aStory = json.optString("aStory", ""),
                bStory = json.optString("bStory", ""),
                linkedScriptFile = json.optString("linkedScriptFile", ""),
                status = json.optString("status", "Outlining")
            )
        }
    }
}

data class TvSeriesBibleConfig(
    val isTvSeries: Boolean = false,
    val seriesTitle: String = "",
    val format: String = "1-Hour Drama (60 min)", // 1-Hour Drama, 30-Min Comedy, Limited Series, Anthology
    val seasonalArcSummary: String = "",
    val episodes: List<TvEpisodeOutline> = listOf(
        TvEpisodeOutline(episodeNumber = 1, title = "Pilot", aStory = "Inciting incident introduces the central conflict.")
    )
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("isTvSeries", isTvSeries)
        json.put("seriesTitle", seriesTitle)
        json.put("format", format)
        json.put("seasonalArcSummary", seasonalArcSummary)
        val epArr = JSONArray()
        for (ep in episodes) epArr.put(ep.toJson())
        json.put("episodes", epArr)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): TvSeriesBibleConfig {
            val episodesList = mutableListOf<TvEpisodeOutline>()
            val epArr = json.optJSONArray("episodes")
            if (epArr != null) {
                for (i in 0 until epArr.length()) {
                    val epObj = epArr.optJSONObject(i)
                    if (epObj != null) episodesList.add(TvEpisodeOutline.fromJson(epObj))
                }
            }
            if (episodesList.isEmpty()) {
                episodesList.add(TvEpisodeOutline(episodeNumber = 1, title = "Pilot", aStory = "Inciting incident introduces the central conflict."))
            }
            return TvSeriesBibleConfig(
                isTvSeries = json.optBoolean("isTvSeries", false),
                seriesTitle = json.optString("seriesTitle", ""),
                format = json.optString("format", "1-Hour Drama (60 min)"),
                seasonalArcSummary = json.optString("seasonalArcSummary", ""),
                episodes = episodesList
            )
        }
    }
}

/**
 * Top-level Story Bible aggregating the Lore Document, Characters, Locations,
 * Story Beats, and TV Series outlines.
 */
data class StoryBible(
    val scriptTitle: String = "UNTITLED SCREENPLAY",
    val author: String = "",
    val lore: LoreDocument = LoreDocument(),
    val characters: List<CharacterDossier> = emptyList(),
    val locations: List<LocationCodex> = emptyList(),
    val beats: List<StoryBeat> = emptyList(),
    val tvConfig: TvSeriesBibleConfig = TvSeriesBibleConfig(),
    val lastModified: Long = System.currentTimeMillis()
) {
    /**
     * Checks if this Story Bible has any written lore, dossiers, locations, beats, or TV config.
     */
    fun hasContent(): Boolean {
        return lore.wordCount() > 0 ||
                characters.any { it.name.isNotBlank() } ||
                locations.any { it.name.isNotBlank() } ||
                beats.any { it.title.isNotBlank() || it.summary.isNotBlank() } ||
                (tvConfig.isTvSeries && (tvConfig.seasonalArcSummary.isNotBlank() || tvConfig.episodes.isNotEmpty()))
    }

    /**
     * Formats the story bible into a rich contextual summary for the AI Script Doctor.
     */
    fun toAiContextSnippet(maxWords: Int = 1200): String {
        if (!hasContent()) return ""
        val sb = StringBuilder()
        sb.append("=== STORY BIBLE & WORLD LORE REFERENCE ===\n")
        sb.append("TITLE: ").append(scriptTitle).append("\n\n")

        if (lore.cosmology.isNotBlank()) {
            sb.append("1. COSMOLOGY & METAPHYSICAL LAWS:\n")
            sb.append(lore.cosmology.trim()).append("\n\n")
        }

        if (lore.anthropology.isNotBlank()) {
            sb.append("2. ANTHROPOLOGY, FACTIONS & SOCIAL CUSTOMS:\n")
            sb.append(lore.anthropology.trim()).append("\n\n")
        }

        if (lore.technology.isNotBlank()) {
            sb.append("3. TECHNOLOGY LEVEL & MAGIC/WEAPONS:\n")
            sb.append(lore.technology.trim()).append("\n\n")
        }

        if (lore.chronology.isNotBlank()) {
            sb.append("4. CHRONOLOGY & WORLD TIMELINE:\n")
            sb.append(lore.chronology.trim()).append("\n\n")
        }

        if (characters.isNotEmpty()) {
            sb.append("KEY CHARACTER DOSSIERS:\n")
            for (c in characters.take(8)) {
                if (c.name.isNotBlank()) {
                    sb.append("• ").append(c.name.uppercase())
                    if (c.role.isNotBlank()) sb.append(" (").append(c.role).append(")")
                    if (c.factionOrCaste.isNotBlank()) sb.append(" [").append(c.factionOrCaste).append("]")
                    if (c.arcAndNeed.isNotBlank()) sb.append(" - Need: ").append(c.arcAndNeed)
                    if (c.secretsAndLore.isNotBlank()) sb.append(" - Secret: ").append(c.secretsAndLore)
                    sb.append("\n")
                }
            }
            sb.append("\n")
        }

        if (beats.isNotEmpty()) {
            sb.append("OUTLINE BEATS:\n")
            for (b in beats.take(12)) {
                sb.append("• [").append(b.act).append("] ").append(b.title)
                if (b.summary.isNotBlank()) sb.append(": ").append(b.summary)
                sb.append("\n")
            }
            sb.append("\n")
        }

        if (tvConfig.isTvSeries) {
            sb.append("TV SERIES BIBLE:\n")
            sb.append("Format: ").append(tvConfig.format).append("\n")
            if (tvConfig.seasonalArcSummary.isNotBlank()) {
                sb.append("Seasonal Arc: ").append(tvConfig.seasonalArcSummary).append("\n")
            }
            sb.append("\n")
        }

        val fullText = sb.toString()
        val words = fullText.split(Regex("\\s+"))
        return if (words.size > maxWords) {
            words.take(maxWords).joinToString(" ") + " ...[truncated]"
        } else {
            fullText
        }
    }

    fun toJson(): JSONObject {
        val root = JSONObject()
        root.put("scriptTitle", scriptTitle)
        root.put("author", author)
        root.put("lore", lore.toJson())

        val charArr = JSONArray()
        for (c in characters) charArr.put(c.toJson())
        root.put("characters", charArr)

        val locArr = JSONArray()
        for (l in locations) locArr.put(l.toJson())
        root.put("locations", locArr)

        val beatArr = JSONArray()
        for (b in beats) beatArr.put(b.toJson())
        root.put("beats", beatArr)

        root.put("tvConfig", tvConfig.toJson())
        root.put("lastModified", lastModified)
        return root
    }

    fun toJsonString(): String {
        return toJson().toString(2)
    }

    companion object {
        fun fromJson(root: JSONObject): StoryBible {
            val loreObj = root.optJSONObject("lore") ?: JSONObject()
            val lore = LoreDocument.fromJson(loreObj)

            val characters = mutableListOf<CharacterDossier>()
            val charArr = root.optJSONArray("characters")
            if (charArr != null) {
                for (i in 0 until charArr.length()) {
                    val cObj = charArr.optJSONObject(i)
                    if (cObj != null) characters.add(CharacterDossier.fromJson(cObj))
                }
            }

            val locations = mutableListOf<LocationCodex>()
            val locArr = root.optJSONArray("locations")
            if (locArr != null) {
                for (i in 0 until locArr.length()) {
                    val lObj = locArr.optJSONObject(i)
                    if (lObj != null) locations.add(LocationCodex.fromJson(lObj))
                }
            }

            val beats = mutableListOf<StoryBeat>()
            val beatArr = root.optJSONArray("beats")
            if (beatArr != null) {
                for (i in 0 until beatArr.length()) {
                    val bObj = beatArr.optJSONObject(i)
                    if (bObj != null) beats.add(StoryBeat.fromJson(bObj))
                }
            }

            val tvObj = root.optJSONObject("tvConfig") ?: JSONObject()
            val tvConfig = TvSeriesBibleConfig.fromJson(tvObj)

            return StoryBible(
                scriptTitle = root.optString("scriptTitle", "UNTITLED SCREENPLAY"),
                author = root.optString("author", ""),
                lore = lore,
                characters = characters,
                locations = locations,
                beats = beats,
                tvConfig = tvConfig,
                lastModified = root.optLong("lastModified", System.currentTimeMillis())
            )
        }

        fun fromJsonString(jsonStr: String): StoryBible {
            return try {
                val root = JSONObject(jsonStr)
                fromJson(root)
            } catch (_: Exception) {
                StoryBible()
            }
        }
    }
}
