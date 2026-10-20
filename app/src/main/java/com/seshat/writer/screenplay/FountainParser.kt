package com.seshat.writer.screenplay

import java.util.Locale

class FountainParser {

    companion object {
        private val SCENE_HEADING_REGEX = Regex(
            "^((INT|EXT|EST|INT\\.?/EXT|I/E|OVER BLACK)\\b[\\.\\-\\s].*|^OVER BLACK$|^\\.[a-zA-Z0-9].*)",
            RegexOption.IGNORE_CASE
        )
        private val TRANSITION_REGEX = Regex(
            "^(FADE OUT\\.?|FADE TO BLACK\\.?|CUT TO:|DISSOLVE TO:|SMASH CUT TO:|MATCH CUT TO:|JUMP CUT TO:|>.*)$",
            RegexOption.IGNORE_CASE
        )
        private const val LINES_PER_PAGE = 54.0
    }

    fun parse(rawText: String): List<ScreenplayElement> {
        val lines = rawText.lines()
        val elements = mutableListOf<ScreenplayElement>()
        var previousElement: ScreenplayElement? = null
        var inDialogueBlock = false

        var lineIndex = 0
        while (lineIndex < lines.size) {
            val rawLine = lines[lineIndex]
            val trimmed = rawLine.trim()

            if (trimmed.isEmpty()) {
                inDialogueBlock = false
                lineIndex++
                continue
            }

            // Check Section Heading (# Heading)
            if (trimmed.startsWith("#")) {
                val level = trimmed.takeWhile { it == '#' }.length
                val text = trimmed.drop(level).trim()
                val element = ScreenplayElement.SectionHeading(text, level, lineIndex)
                elements.add(element)
                previousElement = element
                inDialogueBlock = false
                lineIndex++
                continue
            }

            // Check Synopsis (= Synopsis)
            if (trimmed.startsWith("=") && !trimmed.startsWith("==")) {
                val element = ScreenplayElement.Synopsis(trimmed.drop(1).trim(), lineIndex)
                elements.add(element)
                previousElement = element
                inDialogueBlock = false
                lineIndex++
                continue
            }

            // Check Scene Heading
            if (isSceneHeading(trimmed)) {
                val cleanHeading = if (trimmed.startsWith(".")) trimmed.drop(1).trim() else trimmed
                val element = ScreenplayElement.SceneHeading(cleanHeading, null, lineIndex)
                elements.add(element)
                previousElement = element
                inDialogueBlock = false
                lineIndex++
                continue
            }

            // Check Transition
            if (isTransition(trimmed)) {
                val cleanTransition = if (trimmed.startsWith(">") && !trimmed.endsWith("<")) {
                    trimmed.drop(1).trim()
                } else {
                    trimmed
                }
                val element = ScreenplayElement.Transition(cleanTransition, lineIndex)
                elements.add(element)
                previousElement = element
                inDialogueBlock = false
                lineIndex++
                continue
            }

            // Check Character
            if (!inDialogueBlock && isCharacter(trimmed, lines, lineIndex)) {
                val name = parseCharacterName(trimmed)
                val ext = parseCharacterExtension(trimmed)
                val element = ScreenplayElement.Character(name, ext, lineIndex)
                elements.add(element)
                previousElement = element
                inDialogueBlock = true
                lineIndex++
                continue
            }

            // Check Parenthetical (following Character or Dialogue, starts with '(')
            if ((inDialogueBlock || previousElement is ScreenplayElement.Character || previousElement is ScreenplayElement.Dialogue) && trimmed.startsWith("(")) {
                val element = ScreenplayElement.Parenthetical(trimmed, lineIndex)
                elements.add(element)
                previousElement = element
                inDialogueBlock = true
                lineIndex++
                continue
            }

            // Check Dialogue (inside dialogue block following Character or Parenthetical)
            if (inDialogueBlock) {
                val element = ScreenplayElement.Dialogue(trimmed, lineIndex)
                elements.add(element)
                previousElement = element
                lineIndex++
                continue
            }

            // Otherwise, Action
            val element = ScreenplayElement.Action(trimmed, lineIndex)
            elements.add(element)
            previousElement = element
            lineIndex++
        }

        return elements
    }

    fun extractScenes(rawText: String): List<SceneItem> {
        val lines = rawText.lines()
        val scenes = mutableListOf<SceneItem>()
        var sceneCount = 1

        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (isSceneHeading(trimmed)) {
                val cleanHeading = if (trimmed.startsWith(".")) trimmed.drop(1).trim() else trimmed
                if (!cleanHeading.startsWith("FADE IN", ignoreCase = true)) {
                    scenes.add(SceneItem(sceneCount++, cleanHeading, index))
                }
            }
        }
        return scenes
    }

    fun extractLocations(rawText: String): List<String> {
        val scenes = extractScenes(rawText)
        val locations = linkedSetOf<String>()
        for (scene in scenes) {
            val loc = extractLocation(scene.heading)
            if (loc.isNotBlank()) {
                locations.add(loc)
            }
        }
        return locations.toList()
    }

    fun extractLocation(sceneHeading: String): String {
        var clean = sceneHeading.trim()
        val prefixes = listOf("INT./EXT.", "INT/EXT.", "I/E.", "INT.", "EXT.", "EST.", ".")
        for (prefix in prefixes) {
            if (clean.startsWith(prefix, ignoreCase = true)) {
                clean = clean.substring(prefix.length).trim()
                break
            }
        }
        val dashIdx = clean.indexOf('-')
        if (dashIdx > 0) {
            clean = clean.substring(0, dashIdx).trim()
        }
        return clean.uppercase(Locale.US)
    }


    fun extractCharacters(rawText: String): List<String> {
        val lines = rawText.lines()
        val characters = linkedSetOf<String>()

        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (isCharacter(trimmed, lines, index)) {
                characters.add(parseCharacterName(trimmed))
            }
        }
        return characters.toList()
    }

    fun estimatePageCount(rawText: String): Double {
        val lines = rawText.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return 1.0
        return Math.max(1.0, lines.size / LINES_PER_PAGE)
    }

    private fun isSceneHeading(line: String): Boolean {
        return SCENE_HEADING_REGEX.matches(line)
    }

    private fun isTransition(line: String): Boolean {
        if (line.endsWith("TO:") && line.uppercase(Locale.US) == line) return true
        return TRANSITION_REGEX.matches(line)
    }

    private fun isCharacter(line: String, lines: List<String>, lineIndex: Int): Boolean {
        if (line.startsWith("@")) return true
        if (isSceneHeading(line) || isTransition(line)) return false

        // Check if previous line is blank or at beginning of document
        val prevLineBlank = lineIndex == 0 || lines[lineIndex - 1].trim().isEmpty()
        if (!prevLineBlank) return false

        // Must be uppercase (allowing for extensions like (V.O.) and spaces)
        val nameWithoutExtension = line.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
        if (nameWithoutExtension.isEmpty() || nameWithoutExtension.length > 40) return false

        val isAllUppercase = nameWithoutExtension.all { it.isUpperCase() || it.isWhitespace() || it == '-' || it == '\'' }
        return isAllUppercase
    }

    private fun parseCharacterName(line: String): String {
        val clean = if (line.startsWith("@")) line.drop(1).trim() else line
        return clean.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
    }

    private fun parseCharacterExtension(line: String): String? {
        val match = Regex("\\(([^)]+)\\)").find(line)
        return match?.groupValues?.get(1)?.trim()
    }
}
