package com.seshat.writer.pitch

import android.content.Context
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.corkboard.CorkboardDocument
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

enum class PitchDeckTheme(
    val displayName: String,
    val bgHex: Long,
    val surfaceHex: Long,
    val accentHex: Long,
    val textHex: Long,
    val mutedHex: Long
) {
    CYBER_YELLOW_OBSIDIAN(
        displayName = "Cyber Yellow & Obsidian",
        bgHex = 0xFF1A1B1E,
        surfaceHex = 0xFF25272C,
        accentHex = 0xFFFACC15,
        textHex = 0xFFF8FAFC,
        mutedHex = 0xFF94A3B8
    ),
    OBSIDIAN_NOIR_GOLD(
        displayName = "Obsidian Noir & Gold",
        bgHex = 0xFF0A0F1A,
        surfaceHex = 0xFF111827,
        accentHex = 0xFFEAB308,
        textHex = 0xFFF8FAFC,
        mutedHex = 0xFF94A3B8
    ),
    CARRARA_EDITORIAL_LIGHT(
        displayName = "Carrara Editorial Light",
        bgHex = 0xFFF8FAFC,
        surfaceHex = 0xFFFFFFFF,
        accentHex = 0xFFDC2626,
        textHex = 0xFF0F172A,
        mutedHex = 0xFF475569
    ),
    CRIMSON_PREMIERE(
        displayName = "Crimson Premiere",
        bgHex = 0xFF14070B,
        surfaceHex = 0xFF220B12,
        accentHex = 0xFFFF1E42,
        textHex = 0xFFF8FAFC,
        mutedHex = 0xFFCBD5E1
    ),
    MIDNIGHT_SCIFI(
        displayName = "Midnight Sci-Fi",
        bgHex = 0xFF060B19,
        surfaceHex = 0xFF0F172A,
        accentHex = 0xFF38BDF8,
        textHex = 0xFFF8FAFC,
        mutedHex = 0xFF94A3B8
    )
}

enum class PitchSlideLayout(val label: String) {
    POSTCARD_TITLE("Title & Cover"),
    HERO_HOOK("Logline & Hook"),
    SYNOPSIS_ARC("Synopsis / Story Arc"),
    CHARACTERS_SPOTLIGHT("Characters / Cast"),
    WORLD_STAKES("World & Setting"),
    TONE_VISUAL_STYLE("Tone & Visual Style"),
    COMPS("Comparable Titles (Comps)"),
    AUDIENCE_ROADMAP("Audience & Series Roadmap"),
    WHY_ME_COMPS("Why Me? Why Now?"),
    CLOSING_QA("Closing / Questions?"),
    TURNING_POINT("Turning Point / Midpoint"),
    CENTRAL_DILEMMA("Central Dilemma")
}

data class PitchSlide(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val subtitle: String = "",
    val body: String = "",
    val layout: PitchSlideLayout = PitchSlideLayout.WORLD_STAKES,
    val imagePath: String = "",
    val speakerNotes: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("subtitle", subtitle)
        put("body", body)
        put("layout", layout.name)
        put("imagePath", imagePath)
        put("speakerNotes", speakerNotes)
    }

    companion object {
        fun fromJson(json: JSONObject): PitchSlide {
            val lay = try {
                PitchSlideLayout.valueOf(json.optString("layout", PitchSlideLayout.WORLD_STAKES.name))
            } catch (_: Exception) {
                PitchSlideLayout.WORLD_STAKES
            }
            return PitchSlide(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", ""),
                subtitle = json.optString("subtitle", ""),
                body = json.optString("body", ""),
                layout = lay,
                imagePath = json.optString("imagePath", ""),
                speakerNotes = json.optString("speakerNotes", "")
            )
        }
    }
}

data class ElevatorPitchData(
    val authorAndCourse: String = "",
    val logline: String = "",
    val passionWhyMe: String = "",
    val spokenPitch: String = ""
) {
    fun wordCount(): Int {
        val trimmed = spokenPitch.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split(Regex("\\s+")).size
    }

    fun estimatedSeconds(wpm: Int = 140): Int {
        val words = wordCount()
        if (words == 0) return 0
        return ((words / wpm.toFloat()) * 60f).toInt().coerceAtLeast(1)
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("authorAndCourse", authorAndCourse)
        put("logline", logline)
        put("passionWhyMe", passionWhyMe)
        put("spokenPitch", spokenPitch)
    }

    companion object {
        fun fromJson(json: JSONObject): ElevatorPitchData = ElevatorPitchData(
            authorAndCourse = json.optString("authorAndCourse", ""),
            logline = json.optString("logline", ""),
            passionWhyMe = json.optString("passionWhyMe", ""),
            spokenPitch = json.optString("spokenPitch", "")
        )
    }
}

data class PitchDeckDocument(
    val id: String = UUID.randomUUID().toString(),
    val projectTitle: String = "UNTITLED PROJECT",
    val author: String = "",
    val theme: PitchDeckTheme = PitchDeckTheme.CYBER_YELLOW_OBSIDIAN,
    val elevatorPitch: ElevatorPitchData = ElevatorPitchData(),
    val slides: List<PitchSlide> = emptyList(),
    val lastModified: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("projectTitle", projectTitle)
        put("author", author)
        put("theme", theme.name)
        put("elevatorPitch", elevatorPitch.toJson())
        put("lastModified", lastModified)
        val arr = JSONArray()
        slides.forEach { arr.put(it.toJson()) }
        put("slides", arr)
    }

    companion object {
        fun fromJson(json: JSONObject): PitchDeckDocument {
            val theme = try {
                PitchDeckTheme.valueOf(json.optString("theme", PitchDeckTheme.CYBER_YELLOW_OBSIDIAN.name))
            } catch (_: Exception) {
                PitchDeckTheme.CYBER_YELLOW_OBSIDIAN
            }
            val slidesList = mutableListOf<PitchSlide>()
            val arr = json.optJSONArray("slides")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    arr.optJSONObject(i)?.let { slidesList.add(PitchSlide.fromJson(it)) }
                }
            }
            val elev = json.optJSONObject("elevatorPitch")?.let { ElevatorPitchData.fromJson(it) } ?: ElevatorPitchData()
            return PitchDeckDocument(
                id = json.optString("id", UUID.randomUUID().toString()),
                projectTitle = json.optString("projectTitle", "UNTITLED PROJECT"),
                author = json.optString("author", ""),
                theme = theme,
                elevatorPitch = elev,
                slides = slidesList,
                lastModified = json.optLong("lastModified", System.currentTimeMillis())
            )
        }

        /**
         * Seeds an industry-standard 10-slide writer pitch deck pre-populated with any Story Bible & Corkboard info.
         */
        fun defaultForProject(
            title: String,
            author: String,
            bible: StoryBible,
            corkboard: CorkboardDocument
        ): PitchDeckDocument {
            val cleanTitle = title.ifBlank { "UNTITLED PROJECT" }
            val worldSeed = bible.lore.cosmology.ifBlank {
                "Open with a 1–2 sentence high-concept logline: protagonist, inciting incident, goal, and ticking-clock stakes."
            }
            val rulesSeed = listOfNotNull(
                bible.lore.anthropology.takeIf { it.isNotBlank() },
                bible.lore.technology.takeIf { it.isNotBlank() }
            ).joinToString("\n\n").ifBlank {
                "• Where & when does the story take place?\n• What rules, power structures, or dangers make this world a powder keg?"
            }
            val charSeed = if (bible.characters.isNotEmpty()) {
                bible.characters.take(3).joinToString("\n\n") { c ->
                    val bio = listOfNotNull(
                        c.age.takeIf { it.isNotBlank() }?.let { "Age $it" },
                        c.gender.takeIf { it.isNotBlank() }
                    ).joinToString(", ")
                    val wants = c.wants.ifBlank { c.goalAndStakes }
                    val shard = c.shardOfGlass
                    buildString {
                        append(c.name.ifBlank { "PROTAGONIST" }.uppercase())
                        if (bio.isNotBlank()) append(" ($bio)")
                        append(" — ${c.role}")
                        if (wants.isNotBlank()) append("\nWants: $wants")
                        if (shard.isNotBlank()) append("\nShard of Glass: $shard")
                    }
                }
            } else {
                "PROTAGONIST — Core flaw, internal need, external goal, and arc.\n\nANTAGONIST / FOIL — Who opposes them or complicates the mission?"
            }
            val actTurnSeed = bible.beats.firstOrNull()?.let { "${it.title}: ${it.summary}" }
                ?: corkboard.cards.firstOrNull()?.let { "${it.title}: ${it.summary}" }
                ?: "Outline the beginning, middle, and major turning points—plus the pilot / Act I engine."

            val slides = listOf(
                PitchSlide(
                    title = cleanTitle.uppercase(),
                    subtitle = if (author.isNotBlank()) "WRITTEN BY ${author.uppercase()}" else "TITLE & COVER SLIDE",
                    body = "Format / Genre (e.g. One-Hour Drama Pilot / Feature Film)\nAdd a key poster visual using Upload Image →",
                    layout = PitchSlideLayout.POSTCARD_TITLE
                ),
                PitchSlide(
                    title = "THE LOGLINE & HOOK",
                    subtitle = "1–2 SENTENCE HIGH-CONCEPT PITCH",
                    body = worldSeed,
                    layout = PitchSlideLayout.HERO_HOOK
                ),
                PitchSlide(
                    title = "SYNOPSIS & STORY ARC",
                    subtitle = "THE NARRATIVE ENGINE",
                    body = actTurnSeed,
                    layout = PitchSlideLayout.SYNOPSIS_ARC
                ),
                PitchSlide(
                    title = "CHARACTERS & CAST",
                    subtitle = "CORE ENSEMBLE",
                    body = charSeed,
                    layout = PitchSlideLayout.CHARACTERS_SPOTLIGHT
                ),
                PitchSlide(
                    title = "THE WORLD & SETTING",
                    subtitle = "WHY THIS WORLD IS A POWDER KEG",
                    body = rulesSeed,
                    layout = PitchSlideLayout.WORLD_STAKES
                ),
                PitchSlide(
                    title = "TONE & VISUAL STYLE",
                    subtitle = "MOOD BOARD & CINEMATIC LANGUAGE",
                    body = "• Color palette, cinematography, pacing, and soundscape\n• Upload reference stills or mood imagery to show what the project feels like to watch",
                    layout = PitchSlideLayout.TONE_VISUAL_STYLE
                ),
                PitchSlide(
                    title = "COMPARABLE TITLES (COMPS)",
                    subtitle = "[PROJECT A] MEETS [PROJECT B]",
                    body = "• Comp 1 (Tone / Visuals): Why it resonates with the same audience\n• Comp 2 (Story Engine / Stakes): Comparable scale and genre blend\n• The Fresh Twist: What makes this unmistakable in today's market",
                    layout = PitchSlideLayout.COMPS
                ),
                PitchSlide(
                    title = "AUDIENCE & SERIES ROADMAP",
                    subtitle = "MARKET FIT & FUTURE SEASONS",
                    body = "• Target Audience & Platform Fit (e.g. A24, HBO, Netflix, Theatrical)\n• Future Seasons / Production Scope: Where the story goes beyond Act I / Season 1",
                    layout = PitchSlideLayout.AUDIENCE_ROADMAP
                ),
                PitchSlide(
                    title = "WHY ME? WHY NOW?",
                    subtitle = "WRITER'S STATEMENT",
                    body = "• Why you are uniquely qualified to write this story\n• The personal obsession or timely theme driving the project",
                    layout = PitchSlideLayout.WHY_ME_COMPS
                ),
                PitchSlide(
                    title = "THANK YOU • QUESTIONS?",
                    subtitle = cleanTitle.uppercase(),
                    body = if (author.isNotBlank()) "Created by $author\nContact / Representation" else "Thank you for reading.",
                    layout = PitchSlideLayout.CLOSING_QA
                )
            )

            return PitchDeckDocument(
                projectTitle = cleanTitle,
                author = author,
                theme = PitchDeckTheme.CYBER_YELLOW_OBSIDIAN,
                elevatorPitch = ElevatorPitchData(
                    authorAndCourse = author,
                    logline = bible.lore.cosmology.take(240),
                    passionWhyMe = "",
                    spokenPitch = ""
                ),
                slides = slides
            )
        }
    }
}

object PitchDeckRepository {
    private const val PITCH_DIR = "pitch_decks"

    private fun getPitchDir(context: Context? = null): File {
        val base = context?.filesDir ?: File(System.getProperty("java.io.tmpdir"), "seshat_files")
        val dir = File(base, PITCH_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getSafeFileName(title: String): String {
        val safe = title.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return if (safe.isBlank()) "untitled_pitch.pitch.json" else "$safe.pitch.json"
    }

    fun saveDeck(context: Context? = null, deck: PitchDeckDocument): Boolean {
        return try {
            val file = File(getPitchDir(context), getSafeFileName(deck.projectTitle))
            file.writeText(deck.copy(lastModified = System.currentTimeMillis()).toJson().toString(2))
            true
        } catch (_: Exception) {
            false
        }
    }

    fun loadDeck(
        context: Context? = null,
        title: String,
        author: String = "",
        bible: StoryBible = StoryBible(),
        corkboard: CorkboardDocument = CorkboardDocument()
    ): PitchDeckDocument {
        return try {
            val file = File(getPitchDir(context), getSafeFileName(title))
            if (!file.exists()) {
                return PitchDeckDocument.defaultForProject(title, author, bible, corkboard)
            }
            PitchDeckDocument.fromJson(JSONObject(file.readText()))
        } catch (_: Exception) {
            PitchDeckDocument.defaultForProject(title, author, bible, corkboard)
        }
    }
}
