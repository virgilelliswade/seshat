package com.seshat.writer.library

import com.seshat.writer.model.RevisionColor
import java.io.File

enum class ScriptTemplate {
    BLANK,
    FEATURE_FILM,
    ONE_HOUR_TV,
    HALF_HOUR_SITCOM,
    NOVEL,
    STAGE_PLAY,
    COMIC_BOOK
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
