package com.seshat.writer.tv

import com.seshat.writer.bible.StoryBible
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.RevisionColor
import com.seshat.writer.model.ScreenplayBlock
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * An individual episode in a television series project.
 * Contains its own screenplay blocks, distinct act-level corkboard outline,
 * revision color tracking, and metadata.
 */
data class TvEpisode(
    val id: String = UUID.randomUUID().toString(),
    val episodeNumber: String = "101",
    val title: String = "Pilot",
    val writerCredit: String = "",
    val synopsis: String = "",
    val blocks: List<ScreenplayBlock> = listOf(
        ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")
    ),
    val corkboard: CorkboardDocument = CorkboardDocument(),
    val revisionColor: RevisionColor = RevisionColor.WHITE,
    val draftTag: String = "White Draft",
    val sceneCount: Int = 1,
    val pageCount: Int = 1,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("episodeNumber", episodeNumber)
        json.put("title", title)
        json.put("writerCredit", writerCredit)
        json.put("synopsis", synopsis)
        json.put("revisionColor", revisionColor.name)
        json.put("draftTag", draftTag)
        json.put("sceneCount", sceneCount)
        json.put("pageCount", pageCount)
        json.put("lastModified", lastModified)

        // Blocks
        val blocksArr = JSONArray()
        for (b in blocks) {
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("type", b.type.name)
            bObj.put("text", b.text)
            b.dualDialogueId?.let { bObj.put("dualDialogueId", it) }
            b.dualSide?.let { bObj.put("dualSide", it.name) }
            b.sceneNumber?.let { bObj.put("sceneNumber", it) }
            b.note?.let { bObj.put("note", it) }
            bObj.put("isRevised", b.isRevised)
            b.revisionColor?.let { bObj.put("revisionColor", it) }
            bObj.put("isOmitted", b.isOmitted)
            blocksArr.put(bObj)
        }
        json.put("blocks", blocksArr)

        // Corkboard
        json.put("corkboard", corkboard.toJson())

        return json
    }

    companion object {
        fun fromJson(json: JSONObject): TvEpisode {
            val blocksList = mutableListOf<ScreenplayBlock>()
            val blocksArr = json.optJSONArray("blocks")
            if (blocksArr != null) {
                for (i in 0 until blocksArr.length()) {
                    val bObj = blocksArr.optJSONObject(i) ?: continue
                    val typeName = bObj.optString("type", "SCENE_HEADING")
                    val blockType = try {
                        BlockType.valueOf(typeName)
                    } catch (_: Exception) {
                        BlockType.ACTION
                    }
                    val dualSideName = bObj.optString("dualSide", "")
                    val dualSide = if (dualSideName.isNotBlank()) {
                        try { com.seshat.writer.model.DualDialogueSide.valueOf(dualSideName) } catch (_: Exception) { null }
                    } else null

                    blocksList.add(
                        ScreenplayBlock(
                            id = bObj.optString("id", UUID.randomUUID().toString()),
                            type = blockType,
                            text = bObj.optString("text", ""),
                            dualDialogueId = if (bObj.has("dualDialogueId")) bObj.getString("dualDialogueId") else null,
                            dualSide = dualSide,
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
                blocksList.add(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = ""))
            }

            val corkboardObj = json.optJSONObject("corkboard")
            val corkboardDoc = if (corkboardObj != null) {
                CorkboardDocument.fromJson(corkboardObj)
            } else {
                CorkboardDocument(scriptTitle = json.optString("title", "Pilot"))
            }

            val revColorName = json.optString("revisionColor", "WHITE")
            val revColor = try { RevisionColor.valueOf(revColorName) } catch (_: Exception) { RevisionColor.WHITE }

            return TvEpisode(
                id = json.optString("id", UUID.randomUUID().toString()),
                episodeNumber = json.optString("episodeNumber", "101"),
                title = json.optString("title", "Pilot"),
                writerCredit = json.optString("writerCredit", ""),
                synopsis = json.optString("synopsis", ""),
                blocks = blocksList,
                corkboard = corkboardDoc,
                revisionColor = revColor,
                draftTag = json.optString("draftTag", "White Draft"),
                sceneCount = json.optInt("sceneCount", 1),
                pageCount = json.optInt("pageCount", 1),
                lastModified = json.optLong("lastModified", System.currentTimeMillis())
            )
        }
    }
}

/**
 * A complete Television Series Project.
 * Houses multiple episodic scripts, each with their own outline corkboards,
 * while maintaining a unified, persistently accessible Series Lore Bible.
 */
data class TvSeriesProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "UNTITLED TV SERIES",
    val showrunner: String = "",
    val networkOrLogline: String = "",
    val format: ScreenplayType = ScreenplayType.TV_HOUR_DRAMA,
    val seriesBible: StoryBible = StoryBible(scriptTitle = "UNTITLED TV SERIES"),
    val episodes: List<TvEpisode> = emptyList(),
    val activeEpisodeIndex: Int = 0,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun getActiveEpisode(): TvEpisode? {
        if (episodes.isEmpty()) return null
        val safeIndex = activeEpisodeIndex.coerceIn(0, episodes.size - 1)
        return episodes[safeIndex]
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("showrunner", showrunner)
        json.put("networkOrLogline", networkOrLogline)
        json.put("format", format.name)
        json.put("activeEpisodeIndex", activeEpisodeIndex)
        json.put("lastModified", lastModified)

        // Series Bible
        json.put("seriesBible", seriesBible.toJson())

        // Episodes
        val epArr = JSONArray()
        for (ep in episodes) {
            epArr.put(ep.toJson())
        }
        json.put("episodes", epArr)

        return json
    }

    companion object {
        fun fromJson(json: JSONObject): TvSeriesProject {
            val formatName = json.optString("format", "TV_HOUR_DRAMA")
            val format = try {
                ScreenplayType.valueOf(formatName)
            } catch (_: Exception) {
                ScreenplayType.TV_HOUR_DRAMA
            }

            val bibleObj = json.optJSONObject("seriesBible")
            val seriesBible = if (bibleObj != null) {
                StoryBible.fromJson(bibleObj)
            } else {
                StoryBible(scriptTitle = json.optString("title", "UNTITLED TV SERIES"))
            }

            val epList = mutableListOf<TvEpisode>()
            val epArr = json.optJSONArray("episodes")
            if (epArr != null) {
                for (i in 0 until epArr.length()) {
                    val epObj = epArr.optJSONObject(i) ?: continue
                    epList.add(TvEpisode.fromJson(epObj))
                }
            }

            return TvSeriesProject(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "UNTITLED TV SERIES"),
                showrunner = json.optString("showrunner", ""),
                networkOrLogline = json.optString("networkOrLogline", ""),
                format = format,
                seriesBible = seriesBible,
                episodes = epList,
                activeEpisodeIndex = json.optInt("activeEpisodeIndex", 0),
                lastModified = json.optLong("lastModified", System.currentTimeMillis())
            )
        }

        fun createDefault(
            title: String,
            showrunner: String = "",
            format: ScreenplayType = ScreenplayType.TV_HOUR_DRAMA,
            pilotTitle: String = "Pilot"
        ): TvSeriesProject {
            val acts = if (format == ScreenplayType.TV_HALF_HOUR_COMEDY) {
                CorkboardDocument.actsForTvHalfHour()
            } else {
                CorkboardDocument.actsForTvHourlong()
            }

            val pilotCorkboard = CorkboardDocument(
                scriptTitle = "$title - Ep 101: $pilotTitle",
                actCount = acts.size,
                acts = acts
            )

            val initialBlock = ScreenplayBlock(
                type = BlockType.SCENE_HEADING,
                text = ""
            )

            val pilot = TvEpisode(
                episodeNumber = "101",
                title = pilotTitle,
                writerCredit = showrunner,
                synopsis = "",
                blocks = listOf(initialBlock),
                corkboard = pilotCorkboard,
                revisionColor = RevisionColor.WHITE,
                draftTag = "White Draft",
                sceneCount = 1,
                pageCount = 1
            )

            val defaultBible = StoryBible(
                scriptTitle = title,
                author = showrunner
            ).copy(
                tvConfig = com.seshat.writer.bible.TvSeriesBibleConfig(
                    isTvSeries = true,
                    seriesTitle = title,
                    format = if (format == ScreenplayType.TV_HALF_HOUR_COMEDY) "30-Min Comedy" else "1-Hour Drama",
                    seasonalArcSummary = "Season 1 Arc: The central conflict escalates across the episodic roster.",
                    episodes = listOf(
                        com.seshat.writer.bible.TvEpisodeOutline(
                            episodeNumber = 1,
                            title = pilotTitle,
                            aStory = "Series premiere introducing the world and characters.",
                            status = "In Draft"
                        )
                    )
                )
            )

            return TvSeriesProject(
                title = title,
                showrunner = showrunner,
                format = format,
                seriesBible = defaultBible,
                episodes = listOf(pilot),
                activeEpisodeIndex = 0
            )
        }
    }
}
