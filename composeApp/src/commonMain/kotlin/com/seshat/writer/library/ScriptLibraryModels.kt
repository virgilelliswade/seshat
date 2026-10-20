package com.seshat.writer.library

import com.seshat.writer.model.RevisionColor
import java.io.File

enum class ScriptTemplate(
    val displayName: String,
    val description: String,
    val targetFormat: ProjectFormat,
    val isBlank: Boolean = false
) {
    // Screenplay & TV
    BLANK(
        displayName = "Blank Screenplay",
        description = "Clean empty page with industry standard Courier 12pt formatting",
        targetFormat = ProjectFormat.SCREENPLAY,
        isBlank = true
    ),
    FEATURE_FILM(
        displayName = "Feature Film (3-Act)",
        description = "Hollywood 3-Act structure (Inciting incident, Midpoint reversal, Climax)",
        targetFormat = ProjectFormat.SCREENPLAY
    ),
    ONE_HOUR_TV(
        displayName = "1-Hour TV Drama",
        description = "Episodic television structure (Teaser + 4 Acts + Cliffhanger tag)",
        targetFormat = ProjectFormat.SCREENPLAY
    ),
    HALF_HOUR_SITCOM(
        displayName = "30-Min Sitcom",
        description = "Broadcast comedy structure (Cold Open + 2 Acts + Button Tag)",
        targetFormat = ProjectFormat.SCREENPLAY
    ),
    SHORT_FILM(
        displayName = "Short Film",
        description = "Tight 10-15 minute narrative festival format (Setup, Turn, Payoff)",
        targetFormat = ProjectFormat.SCREENPLAY
    ),

    // Stage Play
    STAGE_PLAY(
        displayName = "Blank Stage Play",
        description = "Clean empty stage play script ready for your first scene",
        targetFormat = ProjectFormat.STAGE_PLAY,
        isBlank = true
    ),
    STAGE_PLAY_TWO_ACT(
        displayName = "Full-Length Two-Act",
        description = "Standard theatrical structure (Dramatis Personae, Act I, Intermission, Act II)",
        targetFormat = ProjectFormat.STAGE_PLAY
    ),
    STAGE_PLAY_ONE_ACT(
        displayName = "One-Act Chamber Play",
        description = "Single continuous high-stakes dramatic arc with no intermission",
        targetFormat = ProjectFormat.STAGE_PLAY
    ),
    STAGE_PLAY_TEN_MINUTE(
        displayName = "10-Minute Festival Play",
        description = "Rapid-fire opening conflict, immediate turning point, and dramatic resolution",
        targetFormat = ProjectFormat.STAGE_PLAY
    ),

    // Comic Book
    COMIC_BOOK(
        displayName = "Blank Comic Book",
        description = "Clean empty comic script ready for Page 1, Panel 1",
        targetFormat = ProjectFormat.COMIC_BOOK,
        isBlank = true
    ),
    COMIC_SUPERHERO_22PAGE(
        displayName = "22-Page Standard Issue",
        description = "Classic comic pacing (Full-page splash, multi-panel layouts, SFX, dialogue balloons)",
        targetFormat = ProjectFormat.COMIC_BOOK
    ),
    COMIC_MANGA_CHAPTER(
        displayName = "Manga Chapter",
        description = "Graphic manga pacing (Opening color bleed, action double-spread, internal monologue)",
        targetFormat = ProjectFormat.COMIC_BOOK
    ),
    COMIC_FOUR_PANEL_STRIP(
        displayName = "4-Panel Strip (Yonkoma)",
        description = "Classic strip rhythm: Ki (Setup), Shō (Development), Ten (Twist), Ketsu (Punchline)",
        targetFormat = ProjectFormat.COMIC_BOOK
    ),

    // Novel
    NOVEL(
        displayName = "Blank Novel",
        description = "Clean empty novel manuscript and fresh first chapter",
        targetFormat = ProjectFormat.NOVEL,
        isBlank = true
    ),
    NOVEL_HEROS_JOURNEY(
        displayName = "Hero's Journey (12 Stages)",
        description = "Joseph Campbell / Vogler mythic storytelling structure across 3 Parts and 12 Chapters",
        targetFormat = ProjectFormat.NOVEL
    ),
    NOVEL_THRILLER_MYSTERY(
        displayName = "Mystery / Thriller",
        description = "High-stakes narrative pacing (The Crime Hook, False Lead, Escalation, Climax)",
        targetFormat = ProjectFormat.NOVEL
    ),
    NOVEL_DUAL_POV_ROMANCE(
        displayName = "Dual-POV Romance",
        description = "Alternating perspective chapters, forced proximity, and emotional reconciliation",
        targetFormat = ProjectFormat.NOVEL
    );

    companion object {
        fun templatesForFormat(format: ProjectFormat): List<ScriptTemplate> {
            return entries.filter {
                it.targetFormat == format || (format == ProjectFormat.TV_SERIES && it.targetFormat == ProjectFormat.SCREENPLAY)
            }
        }

        fun defaultBlankForFormat(format: ProjectFormat): ScriptTemplate {
            return when (format) {
                ProjectFormat.SCREENPLAY, ProjectFormat.TV_SERIES -> BLANK
                ProjectFormat.STAGE_PLAY -> STAGE_PLAY
                ProjectFormat.COMIC_BOOK -> COMIC_BOOK
                ProjectFormat.NOVEL -> NOVEL
            }
        }
    }
}

enum class ProjectFormat(val displayName: String) {
    SCREENPLAY("Screenplay"),
    NOVEL("Novel"),
    TV_SERIES("TV Series"),
    STAGE_PLAY("Stage Play"),
    COMIC_BOOK("Comic Book")
}

enum class ScreenplayType(val displayName: String) {
    FEATURE("Feature Film"),
    TV_HOUR_DRAMA("TV (Hourlong / Drama)"),
    TV_HALF_HOUR_COMEDY("TV (Half-Hour / Comedy)")
}

enum class InitialWorkspaceView(val displayName: String) {
    LORE_DOC("Lore Doc & Story Bible"),
    OUTLINE("Corkboard Outline"),
    EDITOR("Drafting Canvas")
}

data class ScriptProject(
    val id: String,
    val title: String,
    val author: String,
    val file: File,
    val format: String, // "FOUNTAIN" or "FDX"
    val lastModified: Long,
    val fileSizeBytes: Long,
    val sceneCount: Int,
    val pageCount: Int,
    val revisionColor: RevisionColor = RevisionColor.WHITE,
    val draftTag: String = "White Draft",
    val isActive: Boolean = false
)
