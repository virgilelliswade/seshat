package com.seshat.writer.corkboard

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Represents a ground-level Scene Card on the Corkboard.
 * A scene is defined as everything occurring under a single SCENE_HEADING block.
 */
data class SceneCard(
    val sceneHeadingBlockId: String,
    val sceneNumber: Int,
    val headingText: String,
    val summary: String = "",
    val actIndex: Int = 0,
    val colorTag: String = "#F59E0B", // Default ElectrumGold accent
    val charactersInScene: List<String> = emptyList(),
    val blockCount: Int = 1,
    val estimatedMinutes: Float = 1.0f,
    val isAiSummarizing: Boolean = false,
    val prompt: String = "" // Optional beat guidance
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("sceneHeadingBlockId", sceneHeadingBlockId)
        json.put("sceneNumber", sceneNumber)
        json.put("headingText", headingText)
        json.put("summary", summary)
        json.put("actIndex", actIndex)
        json.put("colorTag", colorTag)
        json.put("prompt", prompt)
        val charArr = JSONArray()
        for (c in charactersInScene) charArr.put(c)
        json.put("charactersInScene", charArr)
        json.put("blockCount", blockCount)
        json.put("estimatedMinutes", estimatedMinutes.toDouble())
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): SceneCard {
            val charList = mutableListOf<String>()
            val charArr = json.optJSONArray("charactersInScene")
            if (charArr != null) {
                for (i in 0 until charArr.length()) {
                    charList.add(charArr.optString(i))
                }
            }
            return SceneCard(
                sceneHeadingBlockId = json.optString("sceneHeadingBlockId", UUID.randomUUID().toString()),
                sceneNumber = json.optInt("sceneNumber", 1),
                headingText = json.optString("headingText", "UNTITLED SCENE"),
                summary = json.optString("summary", ""),
                actIndex = json.optInt("actIndex", 0),
                colorTag = json.optString("colorTag", "#F59E0B"),
                charactersInScene = charList,
                blockCount = json.optInt("blockCount", 1),
                estimatedMinutes = json.optDouble("estimatedMinutes", 1.0).toFloat(),
                isAiSummarizing = false,
                prompt = json.optString("prompt", "")
            )
        }
    }
}

/**
 * Represents a discrete beat or scene card on the Corkboard.
 * Supports templated beats (e.g. Save the Cat, Hero's Journey) with guidance prompts
 * that the writer can fill in without cluttering the screenplay text editor.
 */
data class CorkboardCard(
    val id: String = UUID.randomUUID().toString(),
    val cardNumber: Int = 1,
    val title: String = "UNTITLED BEAT",
    val summary: String = "",
    val actIndex: Int = 0,
    val colorTag: String = "#F59E0B",
    val prompt: String = "",
    val charactersInScene: List<String> = emptyList(),
    val isAiSummarizing: Boolean = false,
    val sceneHeadingBlockId: String? = null
) {
    fun toSceneCard(): SceneCard = SceneCard(
        sceneHeadingBlockId = sceneHeadingBlockId ?: id,
        sceneNumber = cardNumber,
        headingText = title,
        summary = summary,
        actIndex = actIndex,
        colorTag = colorTag,
        charactersInScene = charactersInScene,
        blockCount = 1,
        estimatedMinutes = 1.0f,
        isAiSummarizing = isAiSummarizing,
        prompt = prompt
    )

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("cardNumber", cardNumber)
        json.put("title", title)
        json.put("summary", summary)
        json.put("actIndex", actIndex)
        json.put("colorTag", colorTag)
        json.put("prompt", prompt)
        val charArr = JSONArray()
        for (c in charactersInScene) charArr.put(c)
        json.put("charactersInScene", charArr)
        if (sceneHeadingBlockId != null) json.put("sceneHeadingBlockId", sceneHeadingBlockId)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): CorkboardCard {
            val charList = mutableListOf<String>()
            val charArr = json.optJSONArray("charactersInScene")
            if (charArr != null) {
                for (i in 0 until charArr.length()) {
                    charList.add(charArr.optString(i))
                }
            }
            return CorkboardCard(
                id = json.optString("id", UUID.randomUUID().toString()),
                cardNumber = json.optInt("cardNumber", 1),
                title = json.optString("title", "UNTITLED BEAT"),
                summary = json.optString("summary", ""),
                actIndex = json.optInt("actIndex", 0),
                colorTag = json.optString("colorTag", "#F59E0B"),
                prompt = json.optString("prompt", ""),
                charactersInScene = charList,
                sceneHeadingBlockId = if (json.has("sceneHeadingBlockId")) json.optString("sceneHeadingBlockId") else null
            )
        }
    }
}

/**
 * Represents a structural Act column on the Corkboard.
 * Unlike Final Draft, acts do not require artificial elements in the screenplay text.
 */
data class CorkboardAct(
    val actIndex: Int,
    val title: String,
    val subtitle: String = ""
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("actIndex", actIndex)
        json.put("title", title)
        json.put("subtitle", subtitle)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): CorkboardAct {
            return CorkboardAct(
                actIndex = json.optInt("actIndex", 0),
                title = json.optString("title", "Act 1"),
                subtitle = json.optString("subtitle", "")
            )
        }
    }
}

/**
 * Persistent corkboard state for a screenplay.
 * Tracks custom act counts, custom act titles, and scene card metadata
 * (summaries, color tags, act assignments) indexed by scene heading block ID.
 */
data class CorkboardDocument(
    val scriptTitle: String = "UNTITLED SCREENPLAY",
    val actCount: Int = 4,
    val acts: List<CorkboardAct> = defaultActs(4),
    val cardMetadata: Map<String, CardMetadata> = emptyMap(),
    val cards: List<CorkboardCard> = emptyList(),
    val activeTemplateId: String? = null
) {
    data class CardMetadata(
        val summary: String = "",
        val actIndex: Int = 0,
        val colorTag: String = "#F59E0B"
    ) {
        fun toJson(): JSONObject {
            val json = JSONObject()
            json.put("summary", summary)
            json.put("actIndex", actIndex)
            json.put("colorTag", colorTag)
            return json
        }

        companion object {
            fun fromJson(json: JSONObject): CardMetadata {
                return CardMetadata(
                    summary = json.optString("summary", ""),
                    actIndex = json.optInt("actIndex", 0),
                    colorTag = json.optString("colorTag", "#F59E0B")
                )
            }
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("scriptTitle", scriptTitle)
        json.put("actCount", actCount)

        val actsArr = JSONArray()
        for (act in acts) actsArr.put(act.toJson())
        json.put("acts", actsArr)

        val metaObj = JSONObject()
        for ((blockId, meta) in cardMetadata) {
            metaObj.put(blockId, meta.toJson())
        }
        json.put("cardMetadata", metaObj)

        val cardsArr = JSONArray()
        for (c in cards) cardsArr.put(c.toJson())
        json.put("cards", cardsArr)

        if (activeTemplateId != null) {
            json.put("activeTemplateId", activeTemplateId)
        }
        return json
    }

    companion object {
        fun defaultActs(count: Int): List<CorkboardAct> {
            val clamped = count.coerceIn(1, 9)
            return when (clamped) {
                1 -> listOf(
                    CorkboardAct(0, "Full Story", "Single Act Narrative")
                )
                2 -> listOf(
                    CorkboardAct(0, "Part I", "Setup & Escalation"),
                    CorkboardAct(1, "Part II", "Climax & Resolution")
                )
                3 -> listOf(
                    CorkboardAct(0, "Act I", "The Setup"),
                    CorkboardAct(1, "Act II", "The Confrontation"),
                    CorkboardAct(2, "Act III", "The Resolution")
                )
                4 -> listOf(
                    CorkboardAct(0, "Act I", "The Setup"),
                    CorkboardAct(1, "Act II-A", "Rising Confrontation"),
                    CorkboardAct(2, "Act II-B", "All Hope Lost / Crisis"),
                    CorkboardAct(3, "Act III", "Climax & Resolution")
                )
                5 -> listOf(
                    CorkboardAct(0, "Act I", "Exposition & Inciting Incident"),
                    CorkboardAct(1, "Act II", "Rising Action & Complications"),
                    CorkboardAct(2, "Act III", "The Climax / Midpoint"),
                    CorkboardAct(3, "Act IV", "Falling Action & Dark Night"),
                    CorkboardAct(4, "Act V", "Catastrophe / Resolution")
                )
                else -> (0 until clamped).map { idx ->
                    CorkboardAct(idx, "Act ${idx + 1}")
                }
            }
        }

        fun actsForFeature(): List<CorkboardAct> = defaultActs(4)

        fun actsForTvHourlong(): List<CorkboardAct> = listOf(
            CorkboardAct(0, "Teaser", "The Hook & Inciting Anomaly"),
            CorkboardAct(1, "Act I", "The Case / Status Quo Disrupted"),
            CorkboardAct(2, "Act II", "Complications & Rising Stakes"),
            CorkboardAct(3, "Act III", "Midpoint Shift & Ticking Clock"),
            CorkboardAct(4, "Act IV", "The Crisis / All Hope Lost"),
            CorkboardAct(5, "Act V", "The Climax, Resolution & Fallout")
        )

        fun actsForTvHalfHour(): List<CorkboardAct> = listOf(
            CorkboardAct(0, "Cold Open", "Inciting Comedy Beat"),
            CorkboardAct(1, "Act I", "The Premise & Comedic Goal"),
            CorkboardAct(2, "Act II", "Escalating Chaos & Misunderstandings"),
            CorkboardAct(3, "Tag / Epilogue", "Resolution & Closing Punchline")
        )

        fun actsForNovel(): List<CorkboardAct> = listOf(
            CorkboardAct(0, "Part I", "Departure & The Known World"),
            CorkboardAct(1, "Part II", "Initiation, Trials & Midpoint"),
            CorkboardAct(2, "Part III", "Return, Climax & Resolution")
        )

        fun actsForStagePlay(): List<CorkboardAct> = listOf(
            CorkboardAct(0, "Act I", "Exposition, Inciting Event & First Dramatic Pivot"),
            CorkboardAct(1, "Act II", "Rising Conflict, Confrontation & Climax / Curtain")
        )

        fun actsForComicBook(): List<CorkboardAct> = listOf(
            CorkboardAct(0, "Pages 1-5", "The Hook & Opening Splash"),
            CorkboardAct(1, "Pages 6-11", "Escalation & Panel Complications"),
            CorkboardAct(2, "Pages 12-17", "The Mid-Issue Turn & Cliffhanger"),
            CorkboardAct(3, "Pages 18-22", "The Climax & Final Page Reveal")
        )

        fun fromJson(json: JSONObject): CorkboardDocument {
            val count = json.optInt("actCount", 4)
            val actsList = mutableListOf<CorkboardAct>()
            val actsArr = json.optJSONArray("acts")
            if (actsArr != null && actsArr.length() > 0) {
                for (i in 0 until actsArr.length()) {
                    val obj = actsArr.optJSONObject(i)
                    if (obj != null) actsList.add(CorkboardAct.fromJson(obj))
                }
            } else {
                actsList.addAll(defaultActs(count))
            }

            val metaMap = mutableMapOf<String, CardMetadata>()
            val metaObj = json.optJSONObject("cardMetadata")
            if (metaObj != null) {
                val keys = metaObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val obj = metaObj.optJSONObject(k)
                    if (obj != null) {
                        metaMap[k] = CardMetadata.fromJson(obj)
                    }
                }
            }

            val cardsList = mutableListOf<CorkboardCard>()
            val cardsArr = json.optJSONArray("cards")
            if (cardsArr != null) {
                for (i in 0 until cardsArr.length()) {
                    val obj = cardsArr.optJSONObject(i)
                    if (obj != null) cardsList.add(CorkboardCard.fromJson(obj))
                }
            }
            val tmplId = if (json.has("activeTemplateId")) json.optString("activeTemplateId") else null

            return CorkboardDocument(
                scriptTitle = json.optString("scriptTitle", "UNTITLED SCREENPLAY"),
                actCount = count,
                acts = if (actsList.isNotEmpty()) actsList else defaultActs(count),
                cardMetadata = metaMap,
                cards = cardsList,
                activeTemplateId = tmplId
            )
        }
    }
}
