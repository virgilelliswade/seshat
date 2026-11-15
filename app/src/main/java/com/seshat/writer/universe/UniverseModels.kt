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
            eraTag: String = ""
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
                eraTag = eraTag
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
        fun defaultUniverseEras(theme: UniverseTheme = UniverseTheme.ARBOREAL): List<UniverseTimelineEra> = when (theme) {
            UniverseTheme.ARBOREAL -> listOf(
                UniverseTimelineEra(name = "Age of Roots", timeSpan = "Epoch I", description = "Primordial origin, world-seed planting, ancient elder beings."),
                UniverseTimelineEra(name = "Age of Growth", timeSpan = "Epoch II", description = "The great canopy rises, kingdoms and pantheons flourish."),
                UniverseTimelineEra(name = "The Great Blight", timeSpan = "Epoch III", description = "Cosmic catastrophe, shattering of branches, dark schism."),
                UniverseTimelineEra(name = "Modern Flowering", timeSpan = "Epoch IV", description = "Current flowering era where main canon stories unfold.")
            )
            UniverseTheme.STELLAR -> listOf(
                UniverseTimelineEra(name = "Singularity Genesis", timeSpan = "Epoch I", description = "Big bang origin, formation of universal constants and prime matter."),
                UniverseTimelineEra(name = "Galactic Zenith", timeSpan = "Epoch II", description = "Stellar empires expand across primary orbit sectors."),
                UniverseTimelineEra(name = "Supernova Fracture", timeSpan = "Epoch III", description = "Dark matter anomaly and cosmic diaspora."),
                UniverseTimelineEra(name = "The Stellarium", timeSpan = "Epoch IV", description = "Modern stellar age of flagship fleets and planetary sagas.")
            )
            UniverseTheme.DEOXYRIBONUCLEAR -> listOf(
                UniverseTimelineEra(name = "Primordial Transcription", timeSpan = "Epoch I", description = "First base pairs laid down; ancient god metals encoded."),
                UniverseTimelineEra(name = "The Five Dynasties", timeSpan = "Epoch II", description = "Ascendance of the Great Houses, bloodline synthesis."),
                UniverseTimelineEra(name = "Genome Fragmentation", timeSpan = "Epoch III", description = "The great biological war, mutation of the core codons."),
                UniverseTimelineEra(name = "The Living Codex", timeSpan = "Epoch IV", description = "Contemporary era where canon strands express new destiny.")
            )
            UniverseTheme.TEMPORAL -> listOf(
                UniverseTimelineEra(name = "Phase I: Genesis", timeSpan = "Epoch I", description = "Establishment of the Sacred Timeline and prime realities."),
                UniverseTimelineEra(name = "Phase II: Divergence", timeSpan = "Epoch II", description = "Nexus events emerge; alternate branches multiply."),
                UniverseTimelineEra(name = "Phase III: Multiverse War", timeSpan = "Epoch III", description = "Collapsing timelines, catastrophic incursions, Loom stress."),
                UniverseTimelineEra(name = "Phase IV: Sacred Order", timeSpan = "Epoch IV", description = "Present continuum protected by anchor beings.")
            )
            UniverseTheme.MYTHOLOGICAL -> listOf(
                UniverseTimelineEra(name = "The Golden Age", timeSpan = "Epoch I", description = "The age of primordial deities, cosmic balance, and origin myths."),
                UniverseTimelineEra(name = "The Silver Age", timeSpan = "Epoch II", description = "Rise of the great heroic pantheons, mythical realms, and dynasties."),
                UniverseTimelineEra(name = "The Sundering War", timeSpan = "Epoch III", description = "The great war of titans and gods, shattering of divine order."),
                UniverseTimelineEra(name = "The Heroic Age", timeSpan = "Epoch IV", description = "Present age of mortal champions, epic quests, and legendary sagas.")
            )
        }

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
