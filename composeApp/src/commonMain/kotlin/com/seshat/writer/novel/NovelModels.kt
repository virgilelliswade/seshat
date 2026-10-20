package com.seshat.writer.novel

import com.seshat.writer.corkboard.CorkboardAct
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.library.ScriptTemplate
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Types of paragraphs supported in the Seshat Novel Prose Engine.
 */
enum class NovelParagraphType(val displayName: String) {
    CHAPTER_HEADER("Chapter Header"),
    NARRATIVE("Narration"),
    DIALOGUE("Dialogue"),
    SCENE_BREAK("Scene Break")
}

/**
 * Progress status for a novel chapter in the Scrivener-style binder.
 */
enum class ChapterStatus(val label: String, val colorHex: String) {
    IDEA("Idea", "#64748B"),
    OUTLINED("Outlined", "#3B82F6"),
    FIRST_DRAFT("First Draft", "#F59E0B"),
    REVISED("Revised", "#8B5CF6"),
    COMPLETED("Completed", "#10B981")
}

/**
 * Visual and typographic styles for scene break transitions.
 */
enum class SceneBreakStyle(val displayName: String, val symbol: String) {
    ASTERISKS("Classic Asterisks (* * *)", "* * *"),
    FLEURON("Literary Fleuron (❦)", "❦"),
    DIAMONDS("Triple Diamonds (❖ ❖ ❖)", "❖ ❖ ❖"),
    STARS("Celestial Stars (✦ ✦ ✦)", "✦ ✦ ✦"),
    FLOURISH("Ornamental Dash (— ✧ —)", "— ✧ —"),
    BLANK_SPACE("Blank Line Spacing", "")
}

/**
 * Scrivener-style chapter version snapshot for safe rewriting and history comparison.
 */
data class ChapterSnapshot(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val label: String = "Draft Snapshot",
    val wordCount: Int = 0,
    val paragraphs: List<NovelParagraph> = emptyList()
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("timestamp", timestamp)
        json.put("label", label)
        json.put("wordCount", wordCount)
        val arr = JSONArray()
        for (p in paragraphs) arr.put(p.toJson())
        json.put("paragraphs", arr)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): ChapterSnapshot {
            val pList = mutableListOf<NovelParagraph>()
            val arr = json.optJSONArray("paragraphs")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i)
                    if (o != null) pList.add(NovelParagraph.fromJson(o))
                }
            }
            return ChapterSnapshot(
                id = json.optString("id", UUID.randomUUID().toString()),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                label = json.optString("label", "Draft Snapshot"),
                wordCount = json.optInt("wordCount", 0),
                paragraphs = pList
            )
        }
    }
}

/**
 * The "Graveyard" scraps bin for preserved deleted scenes, cut dialogue, and ideas.
 */
data class NovelScrap(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Cut Scene",
    val content: String = "",
    val originalChapterTitle: String = "",
    val wordCount: Int = 0,
    val deletedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("content", content)
        json.put("originalChapterTitle", originalChapterTitle)
        json.put("wordCount", wordCount)
        json.put("deletedAt", deletedAt)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): NovelScrap {
            return NovelScrap(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "Cut Scene"),
                content = json.optString("content", ""),
                originalChapterTitle = json.optString("originalChapterTitle", ""),
                wordCount = json.optInt("wordCount", 0),
                deletedAt = json.optLong("deletedAt", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Configuration for professional book front matter (Title, Copyright, Dedication, Epigraph, TOC).
 */
data class NovelFrontMatter(
    val includeTitlePage: Boolean = true,
    val includeCopyrightPage: Boolean = true,
    val copyrightYear: String = "2026",
    val copyrightHolder: String = "",
    val publisherName: String = "SESHAT PRESS",
    val fictionDisclaimer: Boolean = true,
    val allRightsReserved: Boolean = true,
    val editionNotice: String = "First Edition",
    val includeDedication: Boolean = false,
    val dedicationText: String = "",
    val includeEpigraph: Boolean = false,
    val epigraphText: String = "",
    val epigraphSource: String = "",
    val includeTableOfContents: Boolean = true
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("includeTitlePage", includeTitlePage)
        json.put("includeCopyrightPage", includeCopyrightPage)
        json.put("copyrightYear", copyrightYear)
        json.put("copyrightHolder", copyrightHolder)
        json.put("publisherName", publisherName)
        json.put("fictionDisclaimer", fictionDisclaimer)
        json.put("allRightsReserved", allRightsReserved)
        json.put("editionNotice", editionNotice)
        json.put("includeDedication", includeDedication)
        json.put("dedicationText", dedicationText)
        json.put("includeEpigraph", includeEpigraph)
        json.put("epigraphText", epigraphText)
        json.put("epigraphSource", epigraphSource)
        json.put("includeTableOfContents", includeTableOfContents)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): NovelFrontMatter {
            return NovelFrontMatter(
                includeTitlePage = json.optBoolean("includeTitlePage", true),
                includeCopyrightPage = json.optBoolean("includeCopyrightPage", true),
                copyrightYear = json.optString("copyrightYear", "2026"),
                copyrightHolder = json.optString("copyrightHolder", ""),
                publisherName = json.optString("publisherName", "SESHAT PRESS"),
                fictionDisclaimer = json.optBoolean("fictionDisclaimer", true),
                allRightsReserved = json.optBoolean("allRightsReserved", true),
                editionNotice = json.optString("editionNotice", "First Edition"),
                includeDedication = json.optBoolean("includeDedication", false),
                dedicationText = json.optString("dedicationText", ""),
                includeEpigraph = json.optBoolean("includeEpigraph", false),
                epigraphText = json.optString("epigraphText", ""),
                epigraphSource = json.optString("epigraphSource", ""),
                includeTableOfContents = json.optBoolean("includeTableOfContents", true)
            )
        }
    }
}

/**
 * Configuration for back matter call-to-actions, acknowledgments, and author profiles.
 */
data class NovelBackMatter(
    val includeAuthorBio: Boolean = true,
    val authorBioText: String = "",
    val includeAlsoByAuthor: Boolean = false,
    val alsoByAuthorTitles: String = "",
    val includeNewsletterCta: Boolean = false,
    val newsletterCtaText: String = "Sign up for new release alerts and exclusive bonus epilogues.",
    val newsletterLink: String = "",
    val includeAcknowledgments: Boolean = false,
    val acknowledgmentsText: String = ""
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("includeAuthorBio", includeAuthorBio)
        json.put("authorBioText", authorBioText)
        json.put("includeAlsoByAuthor", includeAlsoByAuthor)
        json.put("alsoByAuthorTitles", alsoByAuthorTitles)
        json.put("includeNewsletterCta", includeNewsletterCta)
        json.put("newsletterCtaText", newsletterCtaText)
        json.put("newsletterLink", newsletterLink)
        json.put("includeAcknowledgments", includeAcknowledgments)
        json.put("acknowledgmentsText", acknowledgmentsText)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): NovelBackMatter {
            return NovelBackMatter(
                includeAuthorBio = json.optBoolean("includeAuthorBio", true),
                authorBioText = json.optString("authorBioText", ""),
                includeAlsoByAuthor = json.optBoolean("includeAlsoByAuthor", false),
                alsoByAuthorTitles = json.optString("alsoByAuthorTitles", ""),
                includeNewsletterCta = json.optBoolean("includeNewsletterCta", false),
                newsletterCtaText = json.optString("newsletterCtaText", "Sign up for new release alerts and exclusive bonus epilogues."),
                newsletterLink = json.optString("newsletterLink", ""),
                includeAcknowledgments = json.optBoolean("includeAcknowledgments", false),
                acknowledgmentsText = json.optString("acknowledgmentsText", "")
            )
        }
    }
}

/**
 * Individual paragraph block in a novel chapter.
 */
data class NovelParagraph(
    val id: String = UUID.randomUUID().toString(),
    val type: NovelParagraphType = NovelParagraphType.NARRATIVE,
    val text: String = "",
    val speaker: String? = null
) {
    fun wordCount(): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).size
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("type", type.name)
        json.put("text", text)
        if (speaker != null) json.put("speaker", speaker)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): NovelParagraph {
            val typeStr = json.optString("type", NovelParagraphType.NARRATIVE.name)
            val type = try {
                NovelParagraphType.valueOf(typeStr)
            } catch (_: Exception) {
                NovelParagraphType.NARRATIVE
            }
            return NovelParagraph(
                id = json.optString("id", UUID.randomUUID().toString()),
                type = type,
                text = json.optString("text", ""),
                speaker = if (json.has("speaker")) json.optString("speaker") else null
            )
        }
    }
}

/**
 * Represents a single Chapter in the novel manuscript (Scrivener binder item).
 */
data class NovelChapter(
    val id: String = UUID.randomUUID().toString(),
    val chapterNumber: Int = 1,
    val title: String = "Chapter 1",
    val synopsis: String = "",
    val paragraphs: List<NovelParagraph> = defaultParagraphs(chapterNumber),
    val actIndex: Int = 0, // Maps to Corkboard Part / Act swimlane
    val colorTag: String = "#F59E0B",
    val status: ChapterStatus = ChapterStatus.FIRST_DRAFT,
    val isOmitted: Boolean = false,
    val snapshots: List<ChapterSnapshot> = emptyList()
) {
    fun wordCount(): Int = paragraphs.sumOf { it.wordCount() }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("chapterNumber", chapterNumber)
        json.put("title", title)
        json.put("synopsis", synopsis)
        val pArray = JSONArray()
        for (p in paragraphs) pArray.put(p.toJson())
        json.put("paragraphs", pArray)
        json.put("actIndex", actIndex)
        json.put("colorTag", colorTag)
        json.put("status", status.name)
        json.put("isOmitted", isOmitted)
        val snapArray = JSONArray()
        for (s in snapshots) snapArray.put(s.toJson())
        json.put("snapshots", snapArray)
        return json
    }

    companion object {
        fun defaultParagraphs(number: Int = 1): List<NovelParagraph> {
            return listOf(
                NovelParagraph(
                    type = NovelParagraphType.NARRATIVE,
                    text = ""
                )
            )
        }

        fun isChapterHeaderTrigger(text: String): Boolean {
            val trimmed = text.trim()
            val upper = trimmed.uppercase()
            return upper.startsWith("CHAPTER") ||
                   upper.startsWith("PROLOGUE") ||
                   upper.startsWith("EPILOGUE") ||
                   upper.startsWith("PART ") ||
                   upper.startsWith("ACT ")
        }

        fun fromJson(json: JSONObject): NovelChapter {
            val paragraphsList = mutableListOf<NovelParagraph>()
            val pArray = json.optJSONArray("paragraphs")
            if (pArray != null) {
                for (i in 0 until pArray.length()) {
                    val pObj = pArray.optJSONObject(i)
                    if (pObj != null) paragraphsList.add(NovelParagraph.fromJson(pObj))
                }
            }
            val chNum = json.optInt("chapterNumber", 1)
            val statusStr = json.optString("status", ChapterStatus.FIRST_DRAFT.name)
            val status = try {
                ChapterStatus.valueOf(statusStr)
            } catch (_: Exception) {
                ChapterStatus.FIRST_DRAFT
            }

            val snapshotsList = mutableListOf<ChapterSnapshot>()
            val snapArray = json.optJSONArray("snapshots")
            if (snapArray != null) {
                for (i in 0 until snapArray.length()) {
                    val sObj = snapArray.optJSONObject(i)
                    if (sObj != null) snapshotsList.add(ChapterSnapshot.fromJson(sObj))
                }
            }

            return NovelChapter(
                id = json.optString("id", UUID.randomUUID().toString()),
                chapterNumber = chNum,
                title = json.optString("title", "Chapter $chNum"),
                synopsis = json.optString("synopsis", ""),
                paragraphs = if (paragraphsList.isNotEmpty()) paragraphsList else defaultParagraphs(chNum),
                actIndex = json.optInt("actIndex", 0),
                colorTag = json.optString("colorTag", "#F59E0B"),
                status = status,
                isOmitted = json.optBoolean("isOmitted", false),
                snapshots = snapshotsList
            )
        }
    }
}

/**
 * Top-level Novel Manuscript data structure containing all binder chapters and parts.
 */
data class NovelManuscript(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "UNTITLED NOVEL",
    val subtitle: String = "",
    val author: String = "",
    val genre: String = "Fiction",
    val targetWordCount: Int = 80000,
    val acts: List<CorkboardAct> = CorkboardDocument.actsForNovel(),
    val chapters: List<NovelChapter> = defaultChapters(),
    val coverDesign: NovelCoverDesign = NovelCoverDesign(),
    val frontMatter: NovelFrontMatter = NovelFrontMatter(),
    val backMatter: NovelBackMatter = NovelBackMatter(),
    val sceneBreakStyle: SceneBreakStyle = SceneBreakStyle.ASTERISKS,
    val scraps: List<NovelScrap> = emptyList(),
    val dailyWordCountGoal: Int = 1667,
    val sessionWordCount: Int = 0,
    val isTypewriterMode: Boolean = false
) {
    fun totalWordCount(): Int = chapters.filterNot { it.isOmitted }.sumOf { it.wordCount() }

    fun estimatedPages(): Int = Math.ceil(totalWordCount() / 250.0).toInt().coerceAtLeast(1)

    fun estimatedReadingMinutes(): Int = Math.ceil(totalWordCount() / 225.0).toInt().coerceAtLeast(1)

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("subtitle", subtitle)
        json.put("author", author)
        json.put("genre", genre)
        json.put("targetWordCount", targetWordCount)

        val actsArray = JSONArray()
        for (a in acts) actsArray.put(a.toJson())
        json.put("acts", actsArray)

        val chaptersArray = JSONArray()
        for (ch in chapters) chaptersArray.put(ch.toJson())
        json.put("chapters", chaptersArray)
        json.put("coverDesign", coverDesign.toJson())
        json.put("frontMatter", frontMatter.toJson())
        json.put("backMatter", backMatter.toJson())
        json.put("sceneBreakStyle", sceneBreakStyle.name)

        val scrapsArray = JSONArray()
        for (s in scraps) scrapsArray.put(s.toJson())
        json.put("scraps", scrapsArray)

        json.put("dailyWordCountGoal", dailyWordCountGoal)
        json.put("sessionWordCount", sessionWordCount)
        json.put("isTypewriterMode", isTypewriterMode)

        return json
    }

    companion object {
        fun defaultChapters(): List<NovelChapter> {
            return listOf(
                NovelChapter(
                    chapterNumber = 1,
                    title = "Chapter 1",
                    synopsis = "",
                    actIndex = 0,
                    paragraphs = NovelChapter.defaultParagraphs(1)
                )
            )
        }

        fun chaptersForTemplate(template: ScriptTemplate): List<NovelChapter> {
            return when (template) {
                ScriptTemplate.NOVEL_HEROS_JOURNEY -> listOf(
                    NovelChapter(chapterNumber = 1, title = "Chapter 1: The Ordinary World", synopsis = "Establish the protagonist's status quo, fatal flaw, and daily routine", actIndex = 0, paragraphs = NovelChapter.defaultParagraphs(1)),
                    NovelChapter(chapterNumber = 2, title = "Chapter 2: The Call to Adventure", synopsis = "A disruption arrives; initial hesitation or refusal of the call", actIndex = 0, paragraphs = NovelChapter.defaultParagraphs(2)),
                    NovelChapter(chapterNumber = 3, title = "Chapter 3: Meeting the Mentor", synopsis = "Guidance, talisman, or wisdom received from an experienced guide", actIndex = 0, paragraphs = NovelChapter.defaultParagraphs(3)),
                    NovelChapter(chapterNumber = 4, title = "Chapter 4: Crossing the First Threshold", synopsis = "Committing to the special world; the definitive point of no return", actIndex = 0, paragraphs = NovelChapter.defaultParagraphs(4)),
                    NovelChapter(chapterNumber = 5, title = "Chapter 5: Tests, Allies & Enemies", synopsis = "Learning rules of the new world; forming the team and meeting adversaries", actIndex = 1, paragraphs = NovelChapter.defaultParagraphs(5)),
                    NovelChapter(chapterNumber = 6, title = "Chapter 6: Approach to the Inmost Cave", synopsis = "Preparation for the central ordeal; highest stakes and personal doubts", actIndex = 1, paragraphs = NovelChapter.defaultParagraphs(6)),
                    NovelChapter(chapterNumber = 7, title = "Chapter 7: The Supreme Ordeal", synopsis = "Confronting the greatest fear; metaphorical death and rebirth", actIndex = 1, paragraphs = NovelChapter.defaultParagraphs(7)),
                    NovelChapter(chapterNumber = 8, title = "Chapter 8: Seizing the Reward", synopsis = "Claiming the elixir, truth, or breakthrough weapon", actIndex = 1, paragraphs = NovelChapter.defaultParagraphs(8)),
                    NovelChapter(chapterNumber = 9, title = "Chapter 9: The Road Back", synopsis = "Repercussions of the ordeal; danger redoubles as the enemy rallies", actIndex = 2, paragraphs = NovelChapter.defaultParagraphs(9)),
                    NovelChapter(chapterNumber = 10, title = "Chapter 10: The Resurrection", synopsis = "Final test of character growth; ultimate life-or-death climax", actIndex = 2, paragraphs = NovelChapter.defaultParagraphs(10)),
                    NovelChapter(chapterNumber = 11, title = "Chapter 11: Return with the Elixir", synopsis = "Return transformed to heal and protect the original world", actIndex = 2, paragraphs = NovelChapter.defaultParagraphs(11))
                )
                ScriptTemplate.NOVEL_THRILLER_MYSTERY -> listOf(
                    NovelChapter(chapterNumber = 1, title = "Chapter 1: The Inciting Hook", synopsis = "A crime or shocking disturbance shatters the peace", actIndex = 0, paragraphs = NovelChapter.defaultParagraphs(1)),
                    NovelChapter(chapterNumber = 2, title = "Chapter 2: The Reluctant Investigator", synopsis = "Protagonist takes the case; initial clues and buried secrets uncovered", actIndex = 0, paragraphs = NovelChapter.defaultParagraphs(2)),
                    NovelChapter(chapterNumber = 3, title = "Chapter 3: The Red Herring", synopsis = "A compelling false trail complicates the hunt and misleads authorities", actIndex = 1, paragraphs = NovelChapter.defaultParagraphs(3)),
                    NovelChapter(chapterNumber = 4, title = "Chapter 4: The Threat Closes In", synopsis = "The clock is ticking; personal stakes turn deadly as the hunter becomes hunted", actIndex = 1, paragraphs = NovelChapter.defaultParagraphs(4)),
                    NovelChapter(chapterNumber = 5, title = "Chapter 5: The Unmasking", synopsis = "Truth revealed in a high-tension confrontation and moral reckoning", actIndex = 2, paragraphs = NovelChapter.defaultParagraphs(5))
                )
                ScriptTemplate.NOVEL_DUAL_POV_ROMANCE -> listOf(
                    NovelChapter(chapterNumber = 1, title = "Chapter 1: POV A - The Collision", synopsis = "First meet-cute or professional rivalry with unspoken tension", actIndex = 0, paragraphs = NovelChapter.defaultParagraphs(1)),
                    NovelChapter(chapterNumber = 2, title = "Chapter 2: POV B - The Other Perspective", synopsis = "The counter-perspective revealing private vulnerability and secret attraction", actIndex = 0, paragraphs = NovelChapter.defaultParagraphs(2)),
                    NovelChapter(chapterNumber = 3, title = "Chapter 3: POV A - Forced Proximity", synopsis = "Circumstances force both together; defensive walls begin to crack", actIndex = 1, paragraphs = NovelChapter.defaultParagraphs(3)),
                    NovelChapter(chapterNumber = 4, title = "Chapter 4: POV B - The Misunderstanding", synopsis = "Past baggage threatens the blossoming connection; painful separation", actIndex = 1, paragraphs = NovelChapter.defaultParagraphs(4)),
                    NovelChapter(chapterNumber = 5, title = "Chapter 5: Dual POV - The Grand Gesture", synopsis = "Risking everything for love; emotional reconciliation and shared future", actIndex = 2, paragraphs = NovelChapter.defaultParagraphs(5))
                )
                else -> defaultChapters()
            }
        }

        fun fromJson(json: JSONObject): NovelManuscript {
            val actsList = mutableListOf<CorkboardAct>()
            val actsArr = json.optJSONArray("acts")
            if (actsArr != null && actsArr.length() > 0) {
                for (i in 0 until actsArr.length()) {
                    val aObj = actsArr.optJSONObject(i)
                    if (aObj != null) actsList.add(CorkboardAct.fromJson(aObj))
                }
            } else {
                actsList.addAll(CorkboardDocument.actsForNovel())
            }

            val chaptersList = mutableListOf<NovelChapter>()
            val chArr = json.optJSONArray("chapters")
            if (chArr != null && chArr.length() > 0) {
                for (i in 0 until chArr.length()) {
                    val cObj = chArr.optJSONObject(i)
                    if (cObj != null) chaptersList.add(NovelChapter.fromJson(cObj))
                }
            } else {
                chaptersList.addAll(defaultChapters())
            }

            val coverDesign = if (json.has("coverDesign")) {
                val cObj = json.optJSONObject("coverDesign")
                if (cObj != null) NovelCoverDesign.fromJson(cObj) else NovelCoverDesign()
            } else {
                NovelCoverDesign()
            }

            val frontMatter = if (json.has("frontMatter")) {
                val fObj = json.optJSONObject("frontMatter")
                if (fObj != null) NovelFrontMatter.fromJson(fObj) else NovelFrontMatter()
            } else NovelFrontMatter()

            val backMatter = if (json.has("backMatter")) {
                val bObj = json.optJSONObject("backMatter")
                if (bObj != null) NovelBackMatter.fromJson(bObj) else NovelBackMatter()
            } else NovelBackMatter()

            val sceneBreakStyleStr = json.optString("sceneBreakStyle", SceneBreakStyle.ASTERISKS.name)
            val sceneBreakStyle = try {
                SceneBreakStyle.valueOf(sceneBreakStyleStr)
            } catch (_: Exception) {
                SceneBreakStyle.ASTERISKS
            }

            val scrapsList = mutableListOf<NovelScrap>()
            val scrapsArr = json.optJSONArray("scraps")
            if (scrapsArr != null) {
                for (i in 0 until scrapsArr.length()) {
                    val sObj = scrapsArr.optJSONObject(i)
                    if (sObj != null) scrapsList.add(NovelScrap.fromJson(sObj))
                }
            }

            return NovelManuscript(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "UNTITLED NOVEL"),
                subtitle = json.optString("subtitle", ""),
                author = json.optString("author", ""),
                genre = json.optString("genre", "Fiction"),
                targetWordCount = json.optInt("targetWordCount", 80000),
                acts = actsList,
                chapters = chaptersList,
                coverDesign = coverDesign,
                frontMatter = frontMatter,
                backMatter = backMatter,
                sceneBreakStyle = sceneBreakStyle,
                scraps = scrapsList,
                dailyWordCountGoal = json.optInt("dailyWordCountGoal", 1667),
                sessionWordCount = json.optInt("sessionWordCount", 0),
                isTypewriterMode = json.optBoolean("isTypewriterMode", false)
            )
        }
    }
}

/**
 * Aesthetic presets for cover & dust jacket design.
 */
enum class CoverThemePreset(
    val displayName: String,
    val bgHex: String,
    val accentHex: String,
    val textHex: String,
    val subHex: String
) {
    OBSIDIAN_GOLD("Obsidian & Gold", "#0A0D12", "#E5C158", "#FFFFFF", "#94A3B8"),
    NEON_SCARLET("Neon Scarlet Noir", "#060608", "#EF4444", "#FFFFFF", "#F87171"),
    CELESTIAL_CYAN("Celestial Cyan", "#040914", "#00F0FF", "#FFFFFF", "#38BDF8"),
    EMERALD_GOTHIC("Emerald Gothic", "#030C08", "#10B981", "#FFFFFF", "#6EE7B7"),
    ROYAL_VELVET("Royal Velvet", "#0B0414", "#A855F7", "#FFFFFF", "#C084FC"),
    LITERARY_LINEN("Literary Linen", "#FAF6EE", "#854D0E", "#1C1917", "#78716C")
}

/**
 * Standard paper stocks used in self-publishing (KDP / IngramSpark) for spine width calculation.
 */
enum class PaperType(val displayName: String, val thicknessPerSheet: Float) {
    WHITE_50LB("50lb White Paper (0.002252\"/page)", 0.002252f),
    CREAM_55LB("55lb Cream Paper (0.0025\"/page)", 0.0025f),
    COLOR_70LB("70lb Color Paper (0.002719\"/page)", 0.002719f)
}

/**
 * Complete cover & dust jacket metadata and layout configuration.
 */
data class NovelCoverDesign(
    val title: String = "",
    val subtitle: String = "",
    val author: String = "",
    val tagline: String = "",
    val backCoverBlurb: String = "",
    val praiseQuote: String = "",
    val frontFlapTeaser: String = "",
    val backFlapAuthorBio: String = "",
    val isbn: String = "978-1-234567-89-0",
    val priceText: String = "$27.99 US / $34.99 CAN",
    val publisherImprint: String = "SESHAT BOOKS",
    val themePreset: CoverThemePreset = CoverThemePreset.OBSIDIAN_GOLD,
    val customArtworkPath: String? = null,
    val aiArtPrompt: String = "",
    val paperType: PaperType = PaperType.CREAM_55LB,
    val trimWidthInches: Float = 6.0f,
    val trimHeightInches: Float = 9.0f,
    val flapWidthInches: Float = 3.25f
) {
    fun calculateSpineWidthInches(pageCount: Int): Float {
        val calculated = pageCount * paperType.thicknessPerSheet
        return Math.max(0.2f, calculated)
    }

    fun calculateTotalWrapWidthInches(pageCount: Int): Float {
        // Standard formula: Left Flap + Back Cover + Spine + Front Cover + Right Flap + Bleed (0.25")
        return (flapWidthInches * 2) + (trimWidthInches * 2) + calculateSpineWidthInches(pageCount) + 0.25f
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("title", title)
        json.put("subtitle", subtitle)
        json.put("author", author)
        json.put("tagline", tagline)
        json.put("backCoverBlurb", backCoverBlurb)
        json.put("praiseQuote", praiseQuote)
        json.put("frontFlapTeaser", frontFlapTeaser)
        json.put("backFlapAuthorBio", backFlapAuthorBio)
        json.put("isbn", isbn)
        json.put("priceText", priceText)
        json.put("publisherImprint", publisherImprint)
        json.put("themePreset", themePreset.name)
        if (customArtworkPath != null) json.put("customArtworkPath", customArtworkPath)
        json.put("aiArtPrompt", aiArtPrompt)
        json.put("paperType", paperType.name)
        json.put("trimWidthInches", trimWidthInches.toDouble())
        json.put("trimHeightInches", trimHeightInches.toDouble())
        json.put("flapWidthInches", flapWidthInches.toDouble())
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): NovelCoverDesign {
            val themeStr = json.optString("themePreset", CoverThemePreset.OBSIDIAN_GOLD.name)
            val theme = try { CoverThemePreset.valueOf(themeStr) } catch (_: Exception) { CoverThemePreset.OBSIDIAN_GOLD }

            val paperStr = json.optString("paperType", PaperType.CREAM_55LB.name)
            val paper = try { PaperType.valueOf(paperStr) } catch (_: Exception) { PaperType.CREAM_55LB }

            return NovelCoverDesign(
                title = json.optString("title", ""),
                subtitle = json.optString("subtitle", ""),
                author = json.optString("author", ""),
                tagline = json.optString("tagline", ""),
                backCoverBlurb = json.optString("backCoverBlurb", ""),
                praiseQuote = json.optString("praiseQuote", ""),
                frontFlapTeaser = json.optString("frontFlapTeaser", ""),
                backFlapAuthorBio = json.optString("backFlapAuthorBio", ""),
                isbn = json.optString("isbn", "978-1-234567-89-0"),
                priceText = json.optString("priceText", "$27.99 US / $34.99 CAN"),
                publisherImprint = json.optString("publisherImprint", "SESHAT BOOKS"),
                themePreset = theme,
                customArtworkPath = if (json.has("customArtworkPath")) json.optString("customArtworkPath") else null,
                aiArtPrompt = json.optString("aiArtPrompt", ""),
                paperType = paper,
                trimWidthInches = json.optDouble("trimWidthInches", 6.0).toFloat(),
                trimHeightInches = json.optDouble("trimHeightInches", 9.0).toFloat(),
                flapWidthInches = json.optDouble("flapWidthInches", 3.25).toFloat()
            )
        }
    }
}

/**
 * Output formats supported by the Scrivener-style Novel Compiler.
 */
enum class CompilerFormat(val displayName: String, val extension: String) {
    EPUB("EPUB eBook (.epub)", "epub"),
    PDF_MANUSCRIPT("Standard Manuscript PDF (.pdf)", "pdf"),
    SHUNN_MANUSCRIPT("Standard Manuscript Format (.txt)", "txt"),
    MARKDOWN("Markdown Manuscript (.md)", "md"),
    HTML("Web Reader Manuscript (.html)", "html")
}

/**
 * Compilation options for building the final book / manuscript.
 */
data class CompilerOptions(
    val format: CompilerFormat = CompilerFormat.EPUB,
    val includeTitlePage: Boolean = true,
    val includeTableOfContents: Boolean = true,
    val includeCopyrightPage: Boolean = true,
    val includeDedication: Boolean = false,
    val dedicationText: String = "",
    val includeEpigraph: Boolean = false,
    val epigraphText: String = "",
    val epigraphSource: String = "",
    val includeAuthorBio: Boolean = true,
    val authorBioText: String = "",
    val includeAlsoByAuthor: Boolean = false,
    val alsoByAuthorTitles: String = "",
    val includeNewsletterCta: Boolean = false,
    val newsletterCtaText: String = "Sign up for new release alerts and exclusive bonus epilogues.",
    val newsletterLink: String = "",
    val includeAcknowledgments: Boolean = false,
    val acknowledgmentsText: String = "",
    val sceneBreakStyle: SceneBreakStyle = SceneBreakStyle.ASTERISKS,
    val sceneBreakSymbol: String = "* * *",
    val chapterHeaderStyle: String = "CHAPTER {number}",
    val selectedChapterIds: Set<String> = emptySet()
)
