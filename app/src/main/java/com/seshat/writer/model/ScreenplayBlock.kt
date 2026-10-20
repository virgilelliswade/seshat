package com.seshat.writer.model

import com.seshat.writer.library.ProjectFormat
import java.util.UUID

enum class BlockType(val displayName: String) {
    // Standard Screenplay
    SCENE_HEADING("Scene Heading"),
    ACTION("Action"),
    CHARACTER("Character"),
    PARENTHETICAL("Parenthetical"),
    DIALOGUE("Dialogue"),
    TRANSITION("Transition"),

    // Theatrical Stage Play
    STAGE_ACT_SCENE("Act / Scene"),
    STAGE_DIRECTION("Stage Direction"),
    STAGE_CURTAIN("Curtain / Transition"),

    // Comic Book Script
    COMIC_PAGE("Page Heading"),
    COMIC_PANEL("Panel Heading"),
    COMIC_ART_NOTE("Art Direction"),
    COMIC_BALLOON("Speech Balloon"),
    COMIC_CAPTION("Caption"),
    COMIC_SFX("Sound Effect");

    companion object {
        fun forFormat(format: ProjectFormat): List<BlockType> = when (format) {
            ProjectFormat.SCREENPLAY, ProjectFormat.TV_SERIES -> listOf(
                SCENE_HEADING,
                ACTION,
                CHARACTER,
                PARENTHETICAL,
                DIALOGUE,
                TRANSITION
            )
            ProjectFormat.STAGE_PLAY -> listOf(
                STAGE_ACT_SCENE,
                STAGE_DIRECTION,
                CHARACTER,
                PARENTHETICAL,
                DIALOGUE,
                STAGE_CURTAIN
            )
            ProjectFormat.COMIC_BOOK -> listOf(
                COMIC_PAGE,
                COMIC_PANEL,
                COMIC_ART_NOTE,
                CHARACTER,
                COMIC_BALLOON,
                COMIC_CAPTION,
                COMIC_SFX
            )
            ProjectFormat.NOVEL -> listOf(
                ACTION,
                DIALOGUE
            )
        }
    }
}

enum class DualDialogueSide {
    LEFT,
    RIGHT
}

data class ScreenplayBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType,
    val text: String = "",
    val dualDialogueId: String? = null,
    val dualSide: DualDialogueSide? = null,
    val sceneNumber: String? = null,
    val note: String? = null,
    val isRevised: Boolean = false,
    val revisionColor: String? = null,
    val isOmitted: Boolean = false
)
