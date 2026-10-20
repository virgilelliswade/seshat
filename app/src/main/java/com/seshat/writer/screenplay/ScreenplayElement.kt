package com.seshat.writer.screenplay

sealed interface ScreenplayElement {
    data class SceneHeading(
        val text: String,
        val sceneNumber: Int? = null,
        val lineNumber: Int = 0
    ) : ScreenplayElement

    data class Action(
        val text: String,
        val lineNumber: Int = 0
    ) : ScreenplayElement

    data class Character(
        val name: String,
        val extension: String? = null,
        val lineNumber: Int = 0
    ) : ScreenplayElement

    data class Parenthetical(
        val text: String,
        val lineNumber: Int = 0
    ) : ScreenplayElement

    data class Dialogue(
        val text: String,
        val lineNumber: Int = 0
    ) : ScreenplayElement

    data class Transition(
        val text: String,
        val lineNumber: Int = 0
    ) : ScreenplayElement

    data class SectionHeading(
        val text: String,
        val level: Int,
        val lineNumber: Int = 0
    ) : ScreenplayElement

    data class Synopsis(
        val text: String,
        val lineNumber: Int = 0
    ) : ScreenplayElement
}

data class SceneItem(
    val index: Int,
    val heading: String,
    val lineNumber: Int,
    val sceneNumber: String = index.toString(),
    val blockId: String = "",
    val isOmitted: Boolean = false
)
