package com.seshat.writer.model

import androidx.compose.ui.graphics.Color

/**
 * Standard Hollywood Production Revision Colors according to AMPAS, WGA, and Final Draft.
 */
enum class RevisionColor(
    val displayName: String,
    val draftTag: String,
    val primaryColorHex: Long,
    val bgTintHex: Long,
    val printColorInt: Int
) {
    WHITE(
        displayName = "White (First Draft)",
        draftTag = "WHITE DRAFT",
        primaryColorHex = 0xFFCCCCCC,
        bgTintHex = 0xFFFFFFFF,
        printColorInt = 0xFFFFFFFF.toInt()
    ),
    BLUE(
        displayName = "Blue (1st Revision)",
        draftTag = "BLUE REV.",
        primaryColorHex = 0xFF42A5F5,
        bgTintHex = 0xFFEBF4FA,
        printColorInt = 0xFFD8EEFB.toInt()
    ),
    PINK(
        displayName = "Pink (2nd Revision)",
        draftTag = "PINK REV.",
        primaryColorHex = 0xFFEC407A,
        bgTintHex = 0xFFFCECF3,
        printColorInt = 0xFFFCDDEB.toInt()
    ),
    YELLOW(
        displayName = "Yellow (3rd Revision)",
        draftTag = "YELLOW REV.",
        primaryColorHex = 0xFFFFCA28,
        bgTintHex = 0xFFFEFDE8,
        printColorInt = 0xFFFFFDD6.toInt()
    ),
    GREEN(
        displayName = "Green (4th Revision)",
        draftTag = "GREEN REV.",
        primaryColorHex = 0xFF66BB6A,
        bgTintHex = 0xFFEBF7ED,
        printColorInt = 0xFFDAF2DC.toInt()
    ),
    GOLDENROD(
        displayName = "Goldenrod (5th Revision)",
        draftTag = "GOLDENROD REV.",
        primaryColorHex = 0xFFFFA726,
        bgTintHex = 0xFFFFF7EB,
        printColorInt = 0xFFFFEED4.toInt()
    ),
    BUFF(
        displayName = "Buff (6th Revision)",
        draftTag = "BUFF REV.",
        primaryColorHex = 0xFFBCAAA4,
        bgTintHex = 0xFFF7F5F3,
        printColorInt = 0xFFEFEBE7.toInt()
    ),
    SALMON(
        displayName = "Salmon (7th Revision)",
        draftTag = "SALMON REV.",
        primaryColorHex = 0xFFFF7043,
        bgTintHex = 0xFFFFF2EE,
        printColorInt = 0xFFFFE3DB.toInt()
    ),
    CHERRY(
        displayName = "Cherry (8th Revision)",
        draftTag = "CHERRY REV.",
        primaryColorHex = 0xFFEF5350,
        bgTintHex = 0xFFFDEEEF,
        printColorInt = 0xFFFFE0E2.toInt()
    ),
    SECOND_BLUE(
        displayName = "2nd Blue (9th Revision)",
        draftTag = "2ND BLUE REV.",
        primaryColorHex = 0xFF29B6F6,
        bgTintHex = 0xFFE1F5FE,
        printColorInt = 0xFFC7EBFD.toInt()
    );

    val composeColor: Color
        get() = Color(primaryColorHex)

    val composeBgTint: Color
        get() = Color(bgTintHex)

    companion object {
        fun fromName(name: String?): RevisionColor {
            if (name == null) return WHITE
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: WHITE
        }

        fun fromTag(tag: String?): RevisionColor {
            if (tag == null) return WHITE
            return entries.find { it.draftTag.equals(tag, ignoreCase = true) || it.name.equals(tag, ignoreCase = true) } ?: WHITE
        }
    }
}
