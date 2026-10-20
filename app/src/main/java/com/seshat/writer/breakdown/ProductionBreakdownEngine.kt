package com.seshat.writer.breakdown

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import java.util.Locale

object ProductionBreakdownEngine {

    private val PROP_KEYWORDS = listOf(
        "gun", "pistol", "rifle", "revolver", "shotgun", "knife", "blade", "sword",
        "phone", "smartphone", "cellphone", "cell", "laptop", "computer", "suitcase",
        "briefcase", "duffel bag", "bag", "backpack", "wallet", "money", "cash",
        "cigarette", "lighter", "glass", "cup", "bottle", "drink", "coffee", "beer",
        "whiskey", "wine", "keys", "flashlight", "badge", "document", "file", "envelope",
        "letter", "passport", "book", "photo", "camera", "watch", "rope", "box",
        "blowtorch", "dynamite", "bomb"
    )

    private val WARDROBE_KEYWORDS = listOf(
        "suit", "tuxedo", "dress", "gown", "uniform", "jacket", "coat", "trenchcoat",
        "helmet", "gloves", "boots", "sunglasses", "glasses", "disguise", "mask",
        "apron", "vest", "hat", "tie", "armor"
    )

    private val VEHICLE_KEYWORDS = listOf(
        "car", "vehicle", "truck", "van", "sedan", "suv", "motorcycle", "bike",
        "police cruiser", "squad car", "cruiser", "ambulance", "taxi", "cab", "bus",
        "helicopter", "chopper", "airplane", "jet", "boat", "yacht", "ship", "submarine"
    )

    private val STUNT_KEYWORDS = listOf(
        "fight", "punch", "punches", "kick", "kicks", "brawl", "tackle", "fall",
        "falls", "jump", "jumps", "leap", "leaps", "crash", "collision", "chase",
        "dive", "dives", "shoot", "shoots", "shootout", "struggle", "explosion",
        "blast", "duel", "grapple"
    )

    private val SFX_KEYWORDS = listOf(
        "blood", "bleeding", "wound", "fire", "flames", "burn", "burns", "smoke",
        "explosion", "explode", "sparks", "shatter", "rain", "storm", "lightning",
        "green screen", "wire", "cgi", "monster", "creature", "laser", "blast", "debris"
    )

    private val SOUND_KEYWORDS = listOf(
        "siren", "sirens", "gunshot", "gunshots", "tire screech", "alarm", "bells",
        "explosion", "thunder", "glass", "doorbell", "phone ring", "heartbeat", "whisper", "static"
    )

    fun generateBreakdown(
        blocks: List<ScreenplayBlock>,
        blockPageNumbers: Map<String, Int>
    ): ScriptBreakdownSummary {
        val scenes = partitionIntoScenes(blocks)
        val breakdownList = mutableListOf<SceneBreakdown>()
        var totalEighthsCount = 0

        for (i in scenes.indices) {
            val sceneBlocks = scenes[i]
            val headingBlock = sceneBlocks.firstOrNull { it.type == BlockType.SCENE_HEADING }
            val rawHeading = headingBlock?.text?.trim() ?: "SCENE ${i + 1}"
            val sceneNum = headingBlock?.sceneNumber ?: (i + 1).toString()

            val intExt = parseDirection(rawHeading)
            val location = parseLocation(rawHeading)
            val timeOfDay = parseTimeOfDay(rawHeading)

            val startPage = headingBlock?.let { blockPageNumbers[it.id] } ?: 1

            // 1/8th page calculation
            val estimatedLines = sceneBlocks.sumOf { b ->
                val lineLen = when (b.type) {
                    BlockType.ACTION -> 58
                    BlockType.DIALOGUE -> 35
                    BlockType.CHARACTER -> 25
                    else -> 40
                }
                (b.text.length / lineLen).coerceAtLeast(1) + 1
            }
            val eighths = (estimatedLines / 6).coerceAtLeast(1)
            totalEighthsCount += eighths
            val pageCountEighths = formatEighths(eighths)

            // Cast in scene
            val castMembers = sceneBlocks
                .filter { it.type == BlockType.CHARACTER }
                .map { cleanCharacterName(it.text) }
                .filter { it.isNotBlank() }
                .distinct()

            // Keyword analysis from Action and Dialogue
            val actionText = sceneBlocks
                .filter { it.type == BlockType.ACTION || it.type == BlockType.DIALOGUE }
                .joinToString(" ") { it.text.lowercase(Locale.US) }

            val detectedProps = detectKeywords(actionText, PROP_KEYWORDS)
            val detectedWardrobe = detectKeywords(actionText, WARDROBE_KEYWORDS)
            val detectedVehicles = detectKeywords(actionText, VEHICLE_KEYWORDS)
            val detectedStunts = detectKeywords(actionText, STUNT_KEYWORDS)
            val detectedSfx = detectKeywords(actionText, SFX_KEYWORDS)
            val detectedSound = detectKeywords(actionText, SOUND_KEYWORDS)

            breakdownList.add(
                SceneBreakdown(
                    sceneIndex = i + 1,
                    sceneNumber = sceneNum,
                    heading = rawHeading,
                    intExt = intExt,
                    location = location,
                    timeOfDay = timeOfDay,
                    pageCountEighths = pageCountEighths,
                    pageEighthsFraction = eighths,
                    startPage = startPage,
                    castMembers = castMembers,
                    props = detectedProps,
                    wardrobe = detectedWardrobe,
                    vehicles = detectedVehicles,
                    stunts = detectedStunts,
                    specialEffects = detectedSfx,
                    soundNotes = detectedSound,
                    scriptDay = "DAY 1"
                )
            )
        }

        val allProps = breakdownList.flatMap { it.props }.distinct().sorted()
        val allVehicles = breakdownList.flatMap { it.vehicles }.distinct().sorted()
        val allStunts = breakdownList.flatMap { it.stunts }.distinct().sorted()
        val allSfx = breakdownList.flatMap { it.specialEffects }.distinct().sorted()
        val allWardrobe = breakdownList.flatMap { it.wardrobe }.distinct().sorted()
        val uniqueCast = breakdownList.flatMap { it.castMembers }.distinct().sorted()

        return ScriptBreakdownSummary(
            totalScenes = breakdownList.size,
            totalEighths = totalEighthsCount,
            totalPagesFormatted = formatEighths(totalEighthsCount),
            uniqueCastCount = uniqueCast.size,
            uniquePropsCount = allProps.size,
            uniqueVehiclesCount = allVehicles.size,
            uniqueStuntsCount = allStunts.size,
            uniqueSfxCount = allSfx.size,
            uniqueWardrobeCount = allWardrobe.size,
            allProps = allProps,
            allVehicles = allVehicles,
            allStunts = allStunts,
            allSfx = allSfx,
            allWardrobe = allWardrobe,
            scenes = breakdownList
        )
    }

    private fun detectKeywords(text: String, keywords: List<String>): List<String> {
        val matches = mutableListOf<String>()
        for (kw in keywords) {
            val regex = Regex("\\b${Regex.escape(kw)}\\b", RegexOption.IGNORE_CASE)
            if (regex.containsMatchIn(text)) {
                matches.add(kw.uppercase(Locale.US))
            }
        }
        return matches.distinct()
    }

    fun formatEighths(eighths: Int): String {
        val fullPages = eighths / 8
        val remainder = eighths % 8
        return when {
            fullPages == 0 -> "$remainder/8 pgs"
            remainder == 0 -> "$fullPages pgs"
            else -> "$fullPages $remainder/8 pgs"
        }
    }

    private fun parseDirection(heading: String): String {
        val upper = heading.trim().uppercase(Locale.US)
        return when {
            upper.startsWith("INT./EXT.") || upper.startsWith("INT/EXT.") || upper.startsWith("I/E.") -> "INT./EXT."
            upper.startsWith("INT.") -> "INT."
            upper.startsWith("EXT.") -> "EXT."
            else -> "INT."
        }
    }

    private fun parseLocation(heading: String): String {
        val upper = heading.trim().uppercase(Locale.US)
        val clean = upper.removePrefix("INT.").removePrefix("EXT.").removePrefix("INT./EXT.").removePrefix("INT/EXT.").removePrefix("I/E.").trim()
        val dashIndex = clean.lastIndexOf(" - ")
        return if (dashIndex != -1) {
            clean.substring(0, dashIndex).trim()
        } else {
            clean
        }
    }

    private fun parseTimeOfDay(heading: String): String {
        val upper = heading.trim().uppercase(Locale.US)
        val dashIndex = upper.lastIndexOf(" - ")
        return if (dashIndex != -1) {
            upper.substring(dashIndex + 3).trim()
        } else {
            when {
                upper.contains("NIGHT") -> "NIGHT"
                upper.contains("DAY") -> "DAY"
                upper.contains("DUSK") -> "DUSK"
                upper.contains("DAWN") -> "DAWN"
                upper.contains("CONTINUOUS") -> "CONTINUOUS"
                else -> "DAY"
            }
        }
    }

    private fun cleanCharacterName(raw: String): String {
        var clean = raw.trim().uppercase(Locale.US)
        val parenIdx = clean.indexOf("(")
        if (parenIdx != -1) {
            clean = clean.substring(0, parenIdx).trim()
        }
        return clean.trim('^', '@', ' ')
    }

    private fun partitionIntoScenes(blocks: List<ScreenplayBlock>): List<List<ScreenplayBlock>> {
        val scenes = mutableListOf<List<ScreenplayBlock>>()
        var current = mutableListOf<ScreenplayBlock>()

        for (b in blocks) {
            if (b.type == BlockType.SCENE_HEADING && isCountableSceneHeading(b.text)) {
                if (current.isNotEmpty()) {
                    scenes.add(current)
                    current = mutableListOf()
                }
            }
            current.add(b)
        }
        if (current.isNotEmpty()) {
            scenes.add(current)
        }
        return scenes
    }

    private fun isCountableSceneHeading(text: String): Boolean {
        val trimmed = text.trim().uppercase(Locale.US)
        if (trimmed.isEmpty()) return false
        val clean = if (trimmed.startsWith(".")) trimmed.substring(1).trim() else trimmed
        return clean.startsWith("INT.") || clean.startsWith("EXT.") ||
                clean.startsWith("INT/EXT.") || clean.startsWith("INT./EXT.") ||
                clean.startsWith("I/E.") || clean.startsWith("I/E ") ||
                clean.startsWith("EST.") || clean.startsWith("ESTABLISHING")
    }

    fun generateBreakdownTextReport(summary: ScriptBreakdownSummary, title: String): String {
        val sb = StringBuilder()
        sb.append("================================================================================\n")
        sb.append("             ASSISTANT DIRECTOR PRODUCTION BREAKDOWN\n")
        sb.append("================================================================================\n")
        sb.append("SCREENPLAY: ${title.uppercase(Locale.US)}\n")
        sb.append("TOTAL SCENES: ${summary.totalScenes}  |  TOTAL LENGTH: ${summary.totalPagesFormatted} (${summary.totalEighths}/8ths)\n")
        sb.append("TOTAL CAST: ${summary.uniqueCastCount}  |  PROPS: ${summary.uniquePropsCount}  |  VEHICLES: ${summary.uniqueVehiclesCount}  |  STUNTS: ${summary.uniqueStuntsCount}  |  SFX: ${summary.uniqueSfxCount}\n")
        sb.append("--------------------------------------------------------------------------------\n\n")

        for (scene in summary.scenes) {
            sb.append("SCENE ${scene.sceneNumber}: ${scene.heading}\n")
            sb.append("  • INT/EXT: ${scene.intExt}  |  LOCATION: ${scene.location}  |  TIME: ${scene.timeOfDay}\n")
            sb.append("  • LENGTH: ${scene.pageCountEighths}  |  PAGE: ${scene.startPage}  |  SCRIPT DAY: ${scene.scriptDay}\n")

            if (scene.castMembers.isNotEmpty()) {
                sb.append("  • CAST: ${scene.castMembers.joinToString(", ")}\n")
            }
            if (scene.props.isNotEmpty()) {
                sb.append("  • PROPS: ${scene.props.joinToString(", ")}\n")
            }
            if (scene.wardrobe.isNotEmpty()) {
                sb.append("  • WARDROBE: ${scene.wardrobe.joinToString(", ")}\n")
            }
            if (scene.vehicles.isNotEmpty()) {
                sb.append("  • VEHICLES: ${scene.vehicles.joinToString(", ")}\n")
            }
            if (scene.stunts.isNotEmpty()) {
                sb.append("  • STUNTS: ${scene.stunts.joinToString(", ")}\n")
            }
            if (scene.specialEffects.isNotEmpty()) {
                sb.append("  • SFX/VFX: ${scene.specialEffects.joinToString(", ")}\n")
            }
            if (scene.soundNotes.isNotEmpty()) {
                sb.append("  • SOUND: ${scene.soundNotes.joinToString(", ")}\n")
            }
            sb.append("\n")
        }

        sb.append("================================================================================\n")
        sb.append("MASTER PRODUCTION ELEMENT INVENTORY:\n")
        if (summary.allProps.isNotEmpty()) sb.append("  PROPS: ${summary.allProps.joinToString(", ")}\n")
        if (summary.allVehicles.isNotEmpty()) sb.append("  VEHICLES: ${summary.allVehicles.joinToString(", ")}\n")
        if (summary.allStunts.isNotEmpty()) sb.append("  STUNTS: ${summary.allStunts.joinToString(", ")}\n")
        if (summary.allSfx.isNotEmpty()) sb.append("  SFX: ${summary.allSfx.joinToString(", ")}\n")
        if (summary.allWardrobe.isNotEmpty()) sb.append("  WARDROBE: ${summary.allWardrobe.joinToString(", ")}\n")
        sb.append("================================================================================\n")
        return sb.toString()
    }
}
