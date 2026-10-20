package com.seshat.writer.analytics

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.DualDialogueSide
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.screenplay.ScreenplayViewModel
import java.util.Locale

data class CharacterSpeech(
    val sceneIndex: Int,
    val sceneNumber: String,
    val sceneHeading: String,
    val pageNumber: Int,
    val dialogueText: String,
    val wordCount: Int,
    val parenthetical: String? = null
)

data class CharacterBreakdown(
    val characterName: String,
    val totalSpeeches: Int,
    val totalWords: Int,
    val percentageOfDialogue: Float,
    val speakingSceneNumbers: List<String>,
    val speakingScenes: List<String>,
    val firstAppearanceScene: String?,
    val firstAppearancePage: Int?,
    val lastAppearanceScene: String?,
    val lastAppearancePage: Int?,
    val speeches: List<CharacterSpeech>
)

data class ScriptAnalytics(
    val totalCharacters: Int,
    val totalDialogueCues: Int,
    val totalDialogueWords: Int,
    val totalScriptWords: Int,
    val percentageSpoken: Float,
    val characterBreakdowns: List<CharacterBreakdown>
)

enum class SettingType(val displayName: String, val prefix: String) {
    INTERIOR("Interior", "INT."),
    EXTERIOR("Exterior", "EXT."),
    INT_EXT("Interior/Exterior", "INT./EXT."),
    OTHER("Other", "")
}

data class LocationSceneEntry(
    val sceneNumber: String,
    val sceneHeading: String,
    val locationName: String,
    val settingType: SettingType,
    val timeOfDay: String,
    val pageNumber: Int,
    val speakingCharacters: List<String>
)

data class LocationBreakdown(
    val locationName: String,
    val primarySetting: SettingType,
    val totalScenes: Int,
    val dayScenes: Int,
    val nightScenes: Int,
    val otherTimeScenes: Int,
    val sceneNumbers: List<String>,
    val scenes: List<LocationSceneEntry>,
    val uniqueCharacters: List<String>
)

data class LocationAnalytics(
    val totalUniqueLocations: Int,
    val totalScenes: Int,
    val intScenesCount: Int,
    val extScenesCount: Int,
    val intExtScenesCount: Int,
    val dayScenesCount: Int,
    val nightScenesCount: Int,
    val otherTimeScenesCount: Int,
    val locationBreakdowns: List<LocationBreakdown>
)

data class SceneMatrixItem(
    val sceneIndex: Int,
    val sceneNumber: String,
    val sceneHeading: String,
    val pageNumber: Int,
    val settingType: SettingType,
    val timeOfDay: String,
    val characters: List<String>,
    val isOmitted: Boolean = false
)

data class CastSceneMatrix(
    val allCharacters: List<String>,
    val scenes: List<SceneMatrixItem>,
    val appearancesByCharacter: Map<String, List<String>>,
    val totalScenesByCharacter: Map<String, Int>
)

object ScreenplayAnalyticsEngine {

    fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).count { it.any { c -> c.isLetterOrDigit() } }
    }

    fun analyzeScreenplay(
        blocks: List<ScreenplayBlock>,
        blockPageNumbers: Map<String, Int> = emptyMap()
    ): ScriptAnalytics {
        var currentSceneIndex = 0
        var currentSceneNumber = "1"
        var currentSceneHeading = "PROLOGUE"

        var lastCharacterName: String? = null
        var lastParenthetical: String? = null

        var leftCharacterName: String? = null
        var leftParenthetical: String? = null
        var rightCharacterName: String? = null
        var rightParenthetical: String? = null

        val characterSpeechesMap = mutableMapOf<String, MutableList<CharacterSpeech>>()

        for (block in blocks) {
            val pageNum = blockPageNumbers[block.id] ?: 1

            if (block.type == BlockType.SCENE_HEADING && ScreenplayViewModel.isCountableSceneHeading(block)) {
                currentSceneIndex++
                currentSceneNumber = block.sceneNumber ?: currentSceneIndex.toString()
                currentSceneHeading = if (block.isOmitted) "OMITTED" else block.text.ifBlank { "UNTITLED SCENE" }
                lastCharacterName = null
                lastParenthetical = null
                leftCharacterName = null
                leftParenthetical = null
                rightCharacterName = null
                rightParenthetical = null
                continue
            }

            if (block.isOmitted) continue

            if (block.dualDialogueId != null) {
                when (block.type) {
                    BlockType.CHARACTER -> {
                        val clean = ScreenplayViewModel.cleanCharacterName(block.text).uppercase(Locale.US).trim()
                        if (clean.isNotBlank()) {
                            if (block.dualSide == DualDialogueSide.LEFT) {
                                leftCharacterName = clean
                                leftParenthetical = null
                            } else {
                                rightCharacterName = clean
                                rightParenthetical = null
                            }
                        }
                    }
                    BlockType.PARENTHETICAL -> {
                        val parenText = block.text.trim('(', ')').trim()
                        if (block.dualSide == DualDialogueSide.LEFT) {
                            leftParenthetical = parenText
                        } else {
                            rightParenthetical = parenText
                        }
                    }
                    BlockType.DIALOGUE -> {
                        val speaker = if (block.dualSide == DualDialogueSide.LEFT) leftCharacterName else rightCharacterName
                        val paren = if (block.dualSide == DualDialogueSide.LEFT) leftParenthetical else rightParenthetical
                        if (!speaker.isNullOrBlank()) {
                            val wCount = countWords(block.text)
                            val speech = CharacterSpeech(
                                sceneIndex = currentSceneIndex,
                                sceneNumber = currentSceneNumber,
                                sceneHeading = currentSceneHeading,
                                pageNumber = pageNum,
                                dialogueText = block.text.trim(),
                                wordCount = wCount,
                                parenthetical = paren
                            )
                            characterSpeechesMap.getOrPut(speaker) { mutableListOf() }.add(speech)
                        }
                    }
                    else -> {}
                }
                continue
            }

            when (block.type) {
                BlockType.CHARACTER -> {
                    val clean = ScreenplayViewModel.cleanCharacterName(block.text).uppercase(Locale.US).trim()
                    lastCharacterName = if (clean.isNotBlank()) clean else null
                    lastParenthetical = null
                }
                BlockType.PARENTHETICAL -> {
                    lastParenthetical = block.text.trim('(', ')').trim()
                }
                BlockType.DIALOGUE -> {
                    if (!lastCharacterName.isNullOrBlank()) {
                        val wCount = countWords(block.text)
                        val speech = CharacterSpeech(
                            sceneIndex = currentSceneIndex,
                            sceneNumber = currentSceneNumber,
                            sceneHeading = currentSceneHeading,
                            pageNumber = pageNum,
                            dialogueText = block.text.trim(),
                            wordCount = wCount,
                            parenthetical = lastParenthetical
                        )
                        characterSpeechesMap.getOrPut(lastCharacterName) { mutableListOf() }.add(speech)
                    }
                }
                BlockType.ACTION, BlockType.TRANSITION -> {
                    lastCharacterName = null
                    lastParenthetical = null
                }
                else -> {}
            }
        }

        val totalDialogueWords = characterSpeechesMap.values.sumOf { speeches -> speeches.sumOf { it.wordCount } }
        val totalDialogueCues = characterSpeechesMap.values.sumOf { it.size }
        val totalScriptWords = blocks.filter { !it.isOmitted }.sumOf { countWords(it.text) }
        val percentageSpoken = if (totalScriptWords > 0) (totalDialogueWords.toFloat() / totalScriptWords.toFloat()) * 100f else 0f

        val breakdowns = characterSpeechesMap.map { (charName, speeches) ->
            val totalWords = speeches.sumOf { it.wordCount }
            val pct = if (totalDialogueWords > 0) (totalWords.toFloat() / totalDialogueWords.toFloat()) * 100f else 0f
            val distinctSceneNums = speeches.map { it.sceneNumber }.distinct()
            val distinctSceneHeadings = speeches.map { it.sceneHeading }.distinct()
            val first = speeches.firstOrNull()
            val last = speeches.lastOrNull()

            CharacterBreakdown(
                characterName = charName,
                totalSpeeches = speeches.size,
                totalWords = totalWords,
                percentageOfDialogue = pct,
                speakingSceneNumbers = distinctSceneNums,
                speakingScenes = distinctSceneHeadings,
                firstAppearanceScene = first?.sceneNumber,
                firstAppearancePage = first?.pageNumber,
                lastAppearanceScene = last?.sceneNumber,
                lastAppearancePage = last?.pageNumber,
                speeches = speeches
            )
        }.sortedByDescending { it.totalWords }

        return ScriptAnalytics(
            totalCharacters = breakdowns.size,
            totalDialogueCues = totalDialogueCues,
            totalDialogueWords = totalDialogueWords,
            totalScriptWords = totalScriptWords,
            percentageSpoken = percentageSpoken,
            characterBreakdowns = breakdowns
        )
    }

    fun formatTextReport(analytics: ScriptAnalytics, title: String): String {
        val sb = StringBuilder()
        val sep = "=".repeat(60)
        val subSep = "-".repeat(60)

        sb.append(sep).append("\n")
        sb.append("CHARACTER BREAKDOWN & DIALOGUE REPORT\n")
        sb.append("Script: ").append(title.ifBlank { "UNTITLED SCREENPLAY" }).append("\n")
        sb.append(sep).append("\n\n")

        sb.append("OVERVIEW SUMMARY:\n")
        sb.append("  Total Speaking Characters : ").append(analytics.totalCharacters).append("\n")
        sb.append("  Total Dialogue Speeches   : ").append(analytics.totalDialogueCues).append("\n")
        sb.append("  Total Spoken Words        : ").append(analytics.totalDialogueWords).append("\n")
        sb.append("  Total Script Words        : ").append(analytics.totalScriptWords).append("\n")
        sb.append("  Dialogue Volume           : ").append(String.format(Locale.US, "%.1f%%", analytics.percentageSpoken)).append(" of script\n\n")

        sb.append(subSep).append("\n")
        sb.append("RANKED CHARACTER LIST (BY DIALOGUE WORDS):\n")
        sb.append(subSep).append("\n")
        analytics.characterBreakdowns.forEachIndexed { idx, char ->
            sb.append(String.format(Locale.US, "%2d. %-22s : %4d words (%5.1f%%) | %3d cues | %2d scenes (Sc. %s)\n",
                idx + 1,
                char.characterName,
                char.totalWords,
                char.percentageOfDialogue,
                char.totalSpeeches,
                char.speakingSceneNumbers.size,
                char.speakingSceneNumbers.joinToString(", ")
            ))
        }

        sb.append("\n").append(sep).append("\n")
        sb.append("DETAILED CHARACTER DIALOGUE BREAKDOWN:\n")
        sb.append(sep).append("\n\n")

        for (char in analytics.characterBreakdowns) {
            sb.append(subSep).append("\n")
            sb.append("CHARACTER: ").append(char.characterName).append("\n")
            sb.append(String.format(Locale.US, "Speeches: %d | Words: %d (%.1f%%) | Scenes: %s | Pages: %s to %s\n",
                char.totalSpeeches,
                char.totalWords,
                char.percentageOfDialogue,
                char.speakingSceneNumbers.joinToString(", "),
                char.firstAppearancePage ?: 1,
                char.lastAppearancePage ?: 1
            ))
            sb.append(subSep).append("\n")

            for (speech in char.speeches) {
                val parenStr = if (!speech.parenthetical.isNullOrBlank()) " (${speech.parenthetical})" else ""
                sb.append(String.format(Locale.US, "[Scene %s | Page %d | %s] (%d words)\n",
                    speech.sceneNumber, speech.pageNumber, speech.sceneHeading, speech.wordCount))
                if (parenStr.isNotEmpty()) {
                    sb.append("  ").append(parenStr).append("\n")
                }
                sb.append("  \"").append(speech.dialogueText).append("\"\n\n")
            }
            sb.append("\n")
        }

        return sb.toString()
    }

    fun determineSettingType(prefix: String): SettingType {
        val p = prefix.uppercase(Locale.US)
        return when {
            p.contains("INT/EXT") || p.contains("INT./EXT.") || p.contains("I/E") -> SettingType.INT_EXT
            p.contains("INT") -> SettingType.INTERIOR
            p.contains("EXT") -> SettingType.EXTERIOR
            else -> SettingType.OTHER
        }
    }

    private fun isDayTime(timeOfDay: String): Boolean {
        val t = timeOfDay.uppercase(Locale.US)
        return t.contains("DAY") || t.contains("DAWN") || t.contains("MORNING") || t.contains("SUNRISE")
    }

    private fun isNightTime(timeOfDay: String): Boolean {
        val t = timeOfDay.uppercase(Locale.US)
        return t.contains("NIGHT") || t.contains("DUSK") || t.contains("EVENING") || t.contains("SUNSET")
    }

    fun analyzeLocations(
        blocks: List<ScreenplayBlock>,
        blockPageNumbers: Map<String, Int> = emptyMap()
    ): LocationAnalytics {
        var currentSceneIndex = 0
        var currentSceneNumber = "1"
        var currentHeading = "UNTITLED SCENE"
        var currentSetting = SettingType.OTHER
        var currentLocation = "UNTITLED LOCATION"
        var currentTime = "UNSPECIFIED"
        var currentPage = 1
        val currentSpeakingChars = mutableSetOf<String>()

        val sceneEntries = mutableListOf<LocationSceneEntry>()

        fun finalizeCurrentScene() {
            if (currentSceneIndex > 0) {
                sceneEntries.add(
                    LocationSceneEntry(
                        sceneNumber = currentSceneNumber,
                        sceneHeading = currentHeading,
                        locationName = currentLocation,
                        settingType = currentSetting,
                        timeOfDay = currentTime,
                        pageNumber = currentPage,
                        speakingCharacters = currentSpeakingChars.toList().sorted()
                    )
                )
            }
        }

        for (block in blocks) {
            val pNum = blockPageNumbers[block.id] ?: 1

            if (block.type == BlockType.SCENE_HEADING && ScreenplayViewModel.isCountableSceneHeading(block)) {
                finalizeCurrentScene()
                currentSceneIndex++
                currentSceneNumber = block.sceneNumber ?: currentSceneIndex.toString()
                currentHeading = if (block.isOmitted) "OMITTED" else block.text.ifBlank { "UNTITLED SCENE" }
                currentPage = pNum
                currentSpeakingChars.clear()

                val parts = ScreenplayViewModel.parseSceneHeading(block.text)
                currentSetting = determineSettingType(parts.prefix)
                currentLocation = parts.location.ifBlank { "UNTITLED LOCATION" }.uppercase(Locale.US).trim()
                currentTime = (parts.timeOfDay ?: "UNSPECIFIED").uppercase(Locale.US).trim()
                continue
            }

            if (!block.isOmitted && block.type == BlockType.CHARACTER) {
                val clean = ScreenplayViewModel.cleanCharacterName(block.text).uppercase(Locale.US).trim()
                if (clean.isNotBlank()) {
                    currentSpeakingChars.add(clean)
                }
            }
        }
        finalizeCurrentScene()

        val byLocation = sceneEntries.groupBy { it.locationName }
        val breakdowns = byLocation.map { (locName, entries) ->
            val primarySetting = entries.groupingBy { it.settingType }.eachCount().maxByOrNull { it.value }?.key ?: SettingType.OTHER
            val dayCount = entries.count { isDayTime(it.timeOfDay) }
            val nightCount = entries.count { isNightTime(it.timeOfDay) }
            val otherCount = entries.size - (dayCount + nightCount)
            val distinctSceneNums = entries.map { it.sceneNumber }.distinct()
            val allChars = entries.flatMap { it.speakingCharacters }.distinct().sorted()

            LocationBreakdown(
                locationName = locName,
                primarySetting = primarySetting,
                totalScenes = entries.size,
                dayScenes = dayCount,
                nightScenes = nightCount,
                otherTimeScenes = otherCount,
                sceneNumbers = distinctSceneNums,
                scenes = entries,
                uniqueCharacters = allChars
            )
        }.sortedByDescending { it.totalScenes }

        val totalInt = sceneEntries.count { it.settingType == SettingType.INTERIOR }
        val totalExt = sceneEntries.count { it.settingType == SettingType.EXTERIOR }
        val totalIntExt = sceneEntries.count { it.settingType == SettingType.INT_EXT }
        val totalDay = sceneEntries.count { isDayTime(it.timeOfDay) }
        val totalNight = sceneEntries.count { isNightTime(it.timeOfDay) }
        val totalOtherTime = sceneEntries.size - (totalDay + totalNight)

        return LocationAnalytics(
            totalUniqueLocations = breakdowns.size,
            totalScenes = sceneEntries.size,
            intScenesCount = totalInt,
            extScenesCount = totalExt,
            intExtScenesCount = totalIntExt,
            dayScenesCount = totalDay,
            nightScenesCount = totalNight,
            otherTimeScenesCount = totalOtherTime,
            locationBreakdowns = breakdowns
        )
    }

    fun buildCastSceneMatrix(
        blocks: List<ScreenplayBlock>,
        blockPageNumbers: Map<String, Int> = emptyMap()
    ): CastSceneMatrix {
        var currentSceneIndex = 0
        var currentSceneNumber = "1"
        var currentHeading = "UNTITLED SCENE"
        var currentSetting = SettingType.OTHER
        var currentTime = "UNSPECIFIED"
        var currentPage = 1
        var isCurrentOmitted = false
        val currentSpeakingChars = mutableSetOf<String>()

        val sceneMatrixItems = mutableListOf<SceneMatrixItem>()

        fun finalizeCurrentScene() {
            if (currentSceneIndex > 0) {
                sceneMatrixItems.add(
                    SceneMatrixItem(
                        sceneIndex = currentSceneIndex,
                        sceneNumber = currentSceneNumber,
                        sceneHeading = currentHeading,
                        pageNumber = currentPage,
                        settingType = currentSetting,
                        timeOfDay = currentTime,
                        characters = currentSpeakingChars.toList().sorted(),
                        isOmitted = isCurrentOmitted
                    )
                )
            }
        }

        for (block in blocks) {
            val pNum = blockPageNumbers[block.id] ?: 1

            if (block.type == BlockType.SCENE_HEADING && ScreenplayViewModel.isCountableSceneHeading(block)) {
                finalizeCurrentScene()
                currentSceneIndex++
                currentSceneNumber = block.sceneNumber ?: currentSceneIndex.toString()
                currentHeading = if (block.isOmitted) "OMITTED" else block.text.ifBlank { "UNTITLED SCENE" }
                currentPage = pNum
                isCurrentOmitted = block.isOmitted
                currentSpeakingChars.clear()

                val parts = ScreenplayViewModel.parseSceneHeading(block.text)
                currentSetting = determineSettingType(parts.prefix)
                currentTime = (parts.timeOfDay ?: "UNSPECIFIED").uppercase(Locale.US).trim()
                continue
            }

            if (!isCurrentOmitted && block.type == BlockType.CHARACTER) {
                val clean = ScreenplayViewModel.cleanCharacterName(block.text).uppercase(Locale.US).trim()
                if (clean.isNotBlank()) {
                    currentSpeakingChars.add(clean)
                }
            }
        }
        finalizeCurrentScene()

        val appearances = mutableMapOf<String, MutableList<String>>()
        for (item in sceneMatrixItems) {
            for (char in item.characters) {
                appearances.getOrPut(char) { mutableListOf() }.add(item.sceneNumber)
            }
        }

        val allCharacters = appearances.keys.sortedByDescending { appearances[it]?.size ?: 0 }
        val totals = appearances.mapValues { it.value.size }

        return CastSceneMatrix(
            allCharacters = allCharacters,
            scenes = sceneMatrixItems,
            appearancesByCharacter = appearances,
            totalScenesByCharacter = totals
        )
    }

    fun formatLocationReportText(analytics: LocationAnalytics, title: String): String {
        val sb = StringBuilder()
        val sep = "=".repeat(60)
        val subSep = "-".repeat(60)

        sb.append(sep).append("\n")
        sb.append("LOCATION & SET BREAKDOWN REPORT\n")
        sb.append("Script: ").append(title.ifBlank { "UNTITLED SCREENPLAY" }).append("\n")
        sb.append(sep).append("\n\n")

        sb.append("OVERVIEW SUMMARY:\n")
        sb.append("  Total Unique Locations : ").append(analytics.totalUniqueLocations).append("\n")
        sb.append("  Total Scenes           : ").append(analytics.totalScenes).append("\n")
        val intPct = if (analytics.totalScenes > 0) (analytics.intScenesCount * 100f / analytics.totalScenes) else 0f
        val extPct = if (analytics.totalScenes > 0) (analytics.extScenesCount * 100f / analytics.totalScenes) else 0f
        sb.append(String.format(Locale.US, "  Interior (INT) Scenes  : %d (%.1f%%)\n", analytics.intScenesCount, intPct))
        sb.append(String.format(Locale.US, "  Exterior (EXT) Scenes  : %d (%.1f%%)\n", analytics.extScenesCount, extPct))
        sb.append(String.format(Locale.US, "  Day Scenes             : %d | Night Scenes: %d | Other: %d\n\n",
            analytics.dayScenesCount, analytics.nightScenesCount, analytics.otherTimeScenesCount))

        sb.append(subSep).append("\n")
        sb.append("SETS BY SCENE VOLUME:\n")
        sb.append(subSep).append("\n")
        for ((idx, loc) in analytics.locationBreakdowns.withIndex()) {
            sb.append(String.format(Locale.US, "%2d. %-24s [%s] : %2d scenes (Sc. %s)\n",
                idx + 1,
                loc.locationName,
                loc.primarySetting.prefix,
                loc.totalScenes,
                loc.sceneNumbers.joinToString(", ")
            ))
            if (loc.uniqueCharacters.isNotEmpty()) {
                sb.append("    Cast: ").append(loc.uniqueCharacters.joinToString(", ")).append("\n")
            }
        }
        return sb.toString()
    }

    fun formatCastMatrixText(matrix: CastSceneMatrix, title: String): String {
        val sb = StringBuilder()
        val sep = "=".repeat(60)
        val subSep = "-".repeat(60)

        sb.append(sep).append("\n")
        sb.append("CAST-SCENE BREAKDOWN MATRIX\n")
        sb.append("Script: ").append(title.ifBlank { "UNTITLED SCREENPLAY" }).append("\n")
        sb.append(sep).append("\n\n")

        sb.append("SCENE-BY-SCENE CAST CALL SHEET:\n")
        sb.append(subSep).append("\n")
        for (scene in matrix.scenes) {
            val castStr = if (scene.isOmitted) "[OMITTED]" else if (scene.characters.isNotEmpty()) scene.characters.joinToString(", ") else "[NO DIALOGUE / SILENT]"
            sb.append(String.format(Locale.US, "Scene %-4s [Pg %2d] %-30s\n  Cast: %s\n\n",
                scene.sceneNumber, scene.pageNumber, scene.sceneHeading, castStr))
        }

        sb.append("\n").append(sep).append("\n")
        sb.append("CAST APPEARANCE SUMMARY:\n")
        sb.append(sep).append("\n")
        for (char in matrix.allCharacters) {
            val scenes = matrix.appearancesByCharacter[char] ?: emptyList()
            sb.append(String.format(Locale.US, "%-22s : %2d scenes (Sc. %s)\n",
                char, scenes.size, scenes.joinToString(", ")))
        }

        return sb.toString()
    }
}
