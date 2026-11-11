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
            screenplayType: ScreenplayType = ScreenplayType.FEATURE
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
                corkboard = CorkboardDocument(scriptTitle = title, actCount = acts.size, acts = acts)
            )
        }

        fun fromJson(json: JSONObject): BlendedAssignment {
            val formatStr = json.optString("format", ProjectFormat.SCREENPLAY.name)
            val format = ProjectFormat.entries.find { it.name == formatStr } ?: ProjectFormat.SCREENPLAY
            val typeStr = json.optString("screenplayType", ScreenplayType.FEATURE.name)
            val screenplayType = ScreenplayType.entries.find { it.name == typeStr } ?: ScreenplayType.FEATURE

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
                lastModified = json.optLong("lastModified", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Multi-medium Blended Universe project uniting Films, TV Series, Novels, Comics, and Stage Plays
 * under one unified Master Story Bible.
 */
data class BlendedUniverseProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "UNTITLED UNIVERSE",
    val universeBible: StoryBible = StoryBible(scriptTitle = "UNTITLED UNIVERSE"),
    val assignments: List<BlendedAssignment> = emptyList(),
    val activeAssignmentIndex: Int = 0,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun getActiveAssignment(): BlendedAssignment? {
        if (assignments.isEmpty()) return null
        return assignments[activeAssignmentIndex.coerceIn(0, assignments.lastIndex)]
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("activeAssignmentIndex", activeAssignmentIndex)
        json.put("lastModified", lastModified)
        json.put("universeBible", universeBible.toJson())

        val arr = JSONArray()
        for (a in assignments) {
            arr.put(a.toJson())
        }
        json.put("assignments", arr)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): BlendedUniverseProject {
            val title = json.optString("title", "UNTITLED UNIVERSE")
            val bibleObj = json.optJSONObject("universeBible")
            val bible = if (bibleObj != null) StoryBible.fromJson(bibleObj) else StoryBible(scriptTitle = title)

            val assignments = mutableListOf<BlendedAssignment>()
            val arr = json.optJSONArray("assignments")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    arr.optJSONObject(i)?.let { assignments.add(BlendedAssignment.fromJson(it)) }
                }
            }
            return BlendedUniverseProject(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = title,
                universeBible = bible,
                assignments = assignments,
                activeAssignmentIndex = json.optInt("activeAssignmentIndex", 0),
                lastModified = json.optLong("lastModified", System.currentTimeMillis())
            )
        }
    }
}
