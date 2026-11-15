package com.seshat.writer.universe

import com.seshat.writer.bible.StoryBible
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.novel.NovelChapter
import com.seshat.writer.novel.NovelManuscript
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * A single writing assignment / installment inside a Blended Universe project.
 * Supports ANY creative medium: Film (Feature), Television, Novel, Comic Book, or Stage Play.
 */
data class BlendedAssignment(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Assignment 1",
    val format: ProjectFormat = ProjectFormat.SCREENPLAY,
    val screenplayType: ScreenplayType = ScreenplayType.FEATURE,
    val blocks: List<ScreenplayBlock> = defaultBlocksFor(format),
    val novelManuscript: NovelManuscript = NovelManuscript(title = title),
    val corkboard: CorkboardDocument = CorkboardDocument(scriptTitle = title),
    val branchRole: BranchRole = BranchRole.MAIN_SPINE,
    val spinoffParentId: String? = null,
    val focusCharacterId: String? = null,
    val eraTag: String = "",
    val starringCharacterNames: List<String> = emptyList(),
    val projectNotes: String = "",
    val lastModified: Long = System.currentTimeMillis()
) {
    fun mediumBadge(): String = when (format) {
        ProjectFormat.SCREENPLAY -> "🎬 FILM"
        ProjectFormat.TV_SERIES -> "📺 TV"
        ProjectFormat.NOVEL -> "📖 NOVEL"
        ProjectFormat.COMIC_BOOK -> "💥 COMIC"
        ProjectFormat.STAGE_PLAY -> "🎭 STAGE"
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("format", format.name)
        json.put("screenplayType", screenplayType.name)
        json.put("branchRole", branchRole.name)
        spinoffParentId?.let { json.put("spinoffParentId", it) }
        focusCharacterId?.let { json.put("focusCharacterId", it) }
        if (eraTag.isNotBlank()) json.put("eraTag", eraTag)
        if (projectNotes.isNotBlank()) json.put("projectNotes", projectNotes)
        val charArr = JSONArray()
        for (name in starringCharacterNames) {
            charArr.put(name)
        }
        json.put("starringCharacterNames", charArr)
        json.put("lastModified", lastModified)

        val blocksArr = JSONArray()
        for (b in blocks) {
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("type", b.type.name)
            bObj.put("text", b.text)
            b.sceneNumber?.let { bObj.put("sceneNumber", it) }
            b.note?.let { bObj.put("note", it) }
            bObj.put("isRevised", b.isRevised)
            b.revisionColor?.let { bObj.put("revisionColor", it) }
            bObj.put("isOmitted", b.isOmitted)
            blocksArr.put(bObj)
        }
        json.put("blocks", blocksArr)
        json.put("novelManuscript", novelManuscript.toJson())
        json.put("corkboard", corkboard.toJson())
        return json
    }

    companion object {
        fun defaultBlocksFor(format: ProjectFormat): List<ScreenplayBlock> = when (format) {
            ProjectFormat.STAGE_PLAY -> listOf(
                ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 1"),
                ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "")
            )
            ProjectFormat.COMIC_BOOK -> listOf(
                ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 1"),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "")
            )
            else -> listOf(
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")
            )
        }

        fun defaultAssignment(
            title: String,
            format: ProjectFormat,
            screenplayType: ScreenplayType = ScreenplayType.FEATURE,
            branchRole: BranchRole = BranchRole.MAIN_SPINE,
            spinoffParentId: String? = null,
            focusCharacterId: String? = null,
            eraTag: String = "",
            starringCharacterNames: List<String> = emptyList(),
            projectNotes: String = ""
        ): BlendedAssignment {
            val resolvedType = if (format == ProjectFormat.TV_SERIES && screenplayType == ScreenplayType.FEATURE) {
                ScreenplayType.TV_HOUR_DRAMA
            } else screenplayType

            val ms = NovelManuscript(
                title = title,
                chapters = if (format == ProjectFormat.NOVEL) NovelManuscript.defaultChapters() else listOf(
                    NovelChapter(chapterNumber = 1, title = "Chapter 1", actIndex = 0)
                )
            )
            val acts = when (format) {
                ProjectFormat.NOVEL -> CorkboardDocument.actsForNovel()
                ProjectFormat.STAGE_PLAY -> CorkboardDocument.actsForStagePlay()
                ProjectFormat.COMIC_BOOK -> CorkboardDocument.actsForComicBook()
                ProjectFormat.TV_SERIES -> CorkboardDocument.actsForTvHourlong()
                ProjectFormat.SCREENPLAY -> CorkboardDocument.actsForFeature()
            }
            return BlendedAssignment(
                title = title,
                format = format,
                screenplayType = resolvedType,
                blocks = defaultBlocksFor(format),
                novelManuscript = ms,
                corkboard = CorkboardDocument(scriptTitle = title, actCount = acts.size, acts = acts),
                branchRole = branchRole,
                spinoffParentId = spinoffParentId,
                focusCharacterId = focusCharacterId,
                eraTag = eraTag,
                starringCharacterNames = starringCharacterNames,
                projectNotes = projectNotes
            )
        }

        fun fromJson(json: JSONObject): BlendedAssignment {
            val formatStr = json.optString("format", ProjectFormat.SCREENPLAY.name)
            val format = ProjectFormat.entries.find { it.name == formatStr } ?: ProjectFormat.SCREENPLAY
            val typeStr = json.optString("screenplayType", ScreenplayType.FEATURE.name)
            val screenplayType = ScreenplayType.entries.find { it.name == typeStr } ?: ScreenplayType.FEATURE

            val branchRole = BranchRole.fromName(json.optString("branchRole", BranchRole.MAIN_SPINE.name))
            val spinoffParentId = if (json.has("spinoffParentId")) json.getString("spinoffParentId") else null
            val focusCharacterId = if (json.has("focusCharacterId")) json.getString("focusCharacterId") else null
            val eraTag = json.optString("eraTag", "")
            val projectNotes = json.optString("projectNotes", "")
            val starringNames = mutableListOf<String>()
            val charArr = json.optJSONArray("starringCharacterNames")
            if (charArr != null) {
                for (i in 0 until charArr.length()) {
                    val s = charArr.optString(i)
                    if (s.isNotBlank()) starringNames.add(s)
                }
            }

            val blocksList = mutableListOf<ScreenplayBlock>()
            val blocksArr = json.optJSONArray("blocks")
            if (blocksArr != null) {
                for (i in 0 until blocksArr.length()) {
                    val bObj = blocksArr.optJSONObject(i) ?: continue
                    val typeName = bObj.optString("type", "ACTION")
                    val bType = try { BlockType.valueOf(typeName) } catch (_: Exception) { BlockType.ACTION }
                    blocksList.add(
                        ScreenplayBlock(
                            id = bObj.optString("id", UUID.randomUUID().toString()),
                            type = bType,
                            text = bObj.optString("text", ""),
                            sceneNumber = if (bObj.has("sceneNumber")) bObj.getString("sceneNumber") else null,
                            note = if (bObj.has("note")) bObj.getString("note") else null,
                            isRevised = bObj.optBoolean("isRevised", false),
                            revisionColor = if (bObj.has("revisionColor")) bObj.getString("revisionColor") else null,
                            isOmitted = bObj.optBoolean("isOmitted", false)
                        )
                    )
                }
            }
            if (blocksList.isEmpty()) {
                blocksList.addAll(defaultBlocksFor(format))
            }

            val title = json.optString("title", "Assignment")
            val novelObj = json.optJSONObject("novelManuscript")
            val novelMs = if (novelObj != null) NovelManuscript.fromJson(novelObj) else NovelManuscript(title = title)

            val cbObj = json.optJSONObject("corkboard")
            val cbDoc = if (cbObj != null) CorkboardDocument.fromJson(cbObj) else CorkboardDocument(scriptTitle = title)

            return BlendedAssignment(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = title,
                format = format,
                screenplayType = screenplayType,
                blocks = blocksList,
                novelManuscript = novelMs,
                corkboard = cbDoc,
                branchRole = branchRole,
                spinoffParentId = spinoffParentId,
                focusCharacterId = focusCharacterId,
                eraTag = eraTag,
                starringCharacterNames = starringNames,
                projectNotes = projectNotes,
                lastModified = json.optLong("lastModified", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Multi-medium Blended Universe project uniting Films, TV Series, Novels, Comics, and Stage Plays
 * under one unified Master Story Bible and Living World Tree.
 */
data class BlendedUniverseProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "UNTITLED UNIVERSE",
    val universeKind: String = "BLENDED",
    val universeBible: StoryBible = StoryBible(scriptTitle = "UNTITLED UNIVERSE"),
    val assignments: List<BlendedAssignment> = emptyList(),
    val activeAssignmentIndex: Int = 0,
    val theme: UniverseTheme = UniverseTheme.ARBOREAL,
    val eras: List<UniverseTimelineEra> = defaultUniverseEras(UniverseTheme.ARBOREAL),
    val lastModified: Long = System.currentTimeMillis()
) {
    fun universeKindLabel(): String = when (universeKind.uppercase()) {
        "CINEMATIC" -> "CINEMATIC UNIVERSE"
        "NOVEL" -> "NOVEL UNIVERSE"
        "TELEVISION" -> "TELEVISION UNIVERSE"
        else -> "BLENDED UNIVERSE"
    }

    fun getActiveAssignment(): BlendedAssignment? {
        if (assignments.isEmpty()) return null
        return assignments[activeAssignmentIndex.coerceIn(0, assignments.lastIndex)]
    }

    fun withTheme(newTheme: UniverseTheme): BlendedUniverseProject {
        return copy(theme = newTheme, lastModified = System.currentTimeMillis())
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("universeKind", universeKind)
        json.put("activeAssignmentIndex", activeAssignmentIndex)
        json.put("theme", theme.id)
        json.put("lastModified", lastModified)
        json.put("universeBible", universeBible.toJson())

        val arr = JSONArray()
        for (a in assignments) {
            arr.put(a.toJson())
        }
        json.put("assignments", arr)

        val erasArr = JSONArray()
        for (e in eras) {
            erasArr.put(e.toJson())
        }
        json.put("eras", erasArr)
        return json
    }

    companion object {
        fun genericEras(prefix: String = "Era", count: Int = 4): List<UniverseTimelineEra> = (1..count).map { i ->
            UniverseTimelineEra(
                name = "$prefix $i",
                timeSpan = "$prefix $i",
                description = "Timeline segment $i of your story world."
            )
        }

        fun genericPhases(count: Int = 4): List<UniverseTimelineEra> = (1..count).map { i ->
            UniverseTimelineEra(
                name = "Phase $i",
                timeSpan = "Phase $i",
                description = "Narrative saga / phase $i."
            )
        }

        fun classicalEpochs(): List<UniverseTimelineEra> = listOf(
            UniverseTimelineEra(name = "Epoch I", timeSpan = "Epoch I", description = "Primordial origin, world creation, and ancient elder lore."),
            UniverseTimelineEra(name = "Epoch II", timeSpan = "Epoch II", description = "The Golden Age, rise of kingdoms and civilization."),
            UniverseTimelineEra(name = "Epoch III", timeSpan = "Epoch III", description = "The Great Sundering, cataclysmic turning point."),
            UniverseTimelineEra(name = "Epoch IV", timeSpan = "Epoch IV", description = "Modern era where the core canon narratives unfold.")
        )

        fun narrativeSagas(): List<UniverseTimelineEra> = listOf(
            UniverseTimelineEra(name = "Origin Era", timeSpan = "Origin", description = "World foundation, origin myths, and first civilization."),
            UniverseTimelineEra(name = "Classical Age", timeSpan = "Classical", description = "The height of civilization and classical history."),
            UniverseTimelineEra(name = "The Cataclysm", timeSpan = "Crisis", description = "The major turning point, conflict, or collapse."),
            UniverseTimelineEra(name = "Modern Canon", timeSpan = "Modern", description = "The contemporary timeline of the core projects.")
        )

        fun defaultUniverseEras(theme: UniverseTheme = UniverseTheme.STANDARD): List<UniverseTimelineEra> = genericEras("Era", 4)

        fun fromJson(json: JSONObject): BlendedUniverseProject {
            val title = json.optString("title", "UNTITLED UNIVERSE")
            val universeKind = json.optString("universeKind", "BLENDED")
            val bibleObj = json.optJSONObject("universeBible")
            val bible = if (bibleObj != null) StoryBible.fromJson(bibleObj) else StoryBible(scriptTitle = title)
            val theme = UniverseTheme.fromId(json.optString("theme", "ARBOREAL"))

            val assignments = mutableListOf<BlendedAssignment>()
            val arr = json.optJSONArray("assignments")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    arr.optJSONObject(i)?.let { assignments.add(BlendedAssignment.fromJson(it)) }
                }
            }

            val eras = mutableListOf<UniverseTimelineEra>()
            val erasArr = json.optJSONArray("eras")
            if (erasArr != null && erasArr.length() > 0) {
                for (i in 0 until erasArr.length()) {
                    erasArr.optJSONObject(i)?.let { eras.add(UniverseTimelineEra.fromJson(it)) }
                }
            } else {
                eras.addAll(defaultUniverseEras(theme))
            }

            return BlendedUniverseProject(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = title,
                universeKind = universeKind,
                universeBible = bible,
                assignments = assignments,
                activeAssignmentIndex = json.optInt("activeAssignmentIndex", 0),
                theme = theme,
                eras = eras,
                lastModified = json.optLong("lastModified", System.currentTimeMillis())
            )
        }
    }
}
