package com.seshat.writer.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Semantic monochrome vector icon types for Seshat's Pro Studio UI.
 * Replaces multi-colored OS emojis with crisp 1.6dp single-weight architectural line icons.
 */
enum class StudioIconType {
    FOLDER,
    SAVE,
    EXPORT,
    MANUSCRIPT,
    CORKBOARD,
    BIBLE,
    SEARCH,
    AUDIO,
    MIC,
    DOCTOR,
    SETTINGS,
    HOME,
    MENU,
    ADD,
    CLOSE,
    CHECK,
    SPARK,
    REVISION,
    REPORT,
    COMPARE,
    LOCK,
    UNLOCK,
    PAGE,
    CHAPTER,
    COVER,
    COMPILE,
    SCRIPT,
    SPARKLE,
    LIST,
    NONE
}

val Pair<StudioIconType, String>.iconType: StudioIconType
    get() = first

val Pair<StudioIconType, String>.cleanLabel: String
    get() = second

/**
 * Detects a leading emoji or studio keyword in a label and returns the matching
 * [StudioIconType] along with the cleaned, emoji-free label text.
 */
fun parseStudioButtonLabel(rawLabel: String): Pair<StudioIconType, String> {
    val trimmed = rawLabel.trim()
    val iconType = when {
        trimmed.startsWith("💾") -> StudioIconType.SAVE
        trimmed.startsWith("📤") || trimmed.startsWith("📥") -> StudioIconType.EXPORT
        trimmed.startsWith("📁") || trimmed.startsWith("📂") -> StudioIconType.FOLDER
        trimmed.startsWith("🏠") -> StudioIconType.HOME
        trimmed.startsWith("📌") || trimmed.startsWith("📋") || trimmed.startsWith("🪟") || trimmed.startsWith("⛶") -> StudioIconType.CORKBOARD
        trimmed.startsWith("📖") || trimmed.startsWith("📚") || trimmed.startsWith("🌍") -> StudioIconType.BIBLE
        trimmed.startsWith("🎬") || trimmed.startsWith("✍️") || trimmed.startsWith("📄") || trimmed.startsWith("📜") -> StudioIconType.MANUSCRIPT
        trimmed.startsWith("🔍") -> StudioIconType.SEARCH
        trimmed.startsWith("🎭") || trimmed.startsWith("🔊") -> StudioIconType.AUDIO
        trimmed.startsWith("🎙") || trimmed.startsWith("🔴") -> StudioIconType.MIC
        trimmed.startsWith("🧠") -> StudioIconType.DOCTOR
        trimmed.startsWith("✨") || trimmed.startsWith("⚡") -> StudioIconType.SPARK
        trimmed.startsWith("⚙") -> StudioIconType.SETTINGS
        trimmed.startsWith("☰") -> StudioIconType.MENU
        trimmed.startsWith("🎨") -> StudioIconType.COVER
        trimmed.startsWith("📊") -> StudioIconType.REPORT
        trimmed.startsWith("🔀") -> StudioIconType.COMPARE
        trimmed.startsWith("🔒") -> StudioIconType.LOCK
        trimmed.startsWith("🔓") -> StudioIconType.UNLOCK
        trimmed.startsWith("🏛") || trimmed.startsWith("⏳") -> StudioIconType.BIBLE
        trimmed.startsWith("👥") || trimmed.startsWith("🗺") -> StudioIconType.BIBLE
        trimmed.startsWith("📺") || trimmed.startsWith("💥") -> StudioIconType.MANUSCRIPT
        else -> StudioIconType.NONE
    }

    // Strip leading emojis, variation selectors, and extra whitespace
    val cleaned = trimmed
        .replace(
            Regex("^[\\p{So}\\p{Cn}\\p{Cs}\\uFE0F\\u200D\\u2600-\\u27BF\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+\\s*"),
            ""
        )
        .trim()

    return iconType to (if (cleaned.isNotEmpty()) cleaned else trimmed)
}

/**
 * Draws a crisp 1.6dp-stroke monochrome vector line icon matching Final Draft / Scrivener chrome.
 */
@Composable
fun StudioMonoIcon(
    type: StudioIconType,
    tint: Color,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    if (type == StudioIconType.NONE) return

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(
            width = 1.55.dp.toPx(),
            cap = StrokeCap.Square,
            join = StrokeJoin.Miter
        )

        when (type) {
            StudioIconType.FOLDER -> {
                val path = Path().apply {
                    moveTo(w * 0.10f, h * 0.24f)
                    lineTo(w * 0.40f, h * 0.24f)
                    lineTo(w * 0.50f, h * 0.36f)
                    lineTo(w * 0.90f, h * 0.36f)
                    lineTo(w * 0.90f, h * 0.82f)
                    lineTo(w * 0.10f, h * 0.82f)
                    close()
                }
                drawPath(path, color = tint, style = stroke)
            }
            StudioIconType.SAVE -> {
                val outer = Path().apply {
                    moveTo(w * 0.14f, h * 0.14f)
                    lineTo(w * 0.72f, h * 0.14f)
                    lineTo(w * 0.86f, h * 0.28f)
                    lineTo(w * 0.86f, h * 0.86f)
                    lineTo(w * 0.14f, h * 0.86f)
                    close()
                }
                drawPath(outer, color = tint, style = stroke)
                drawRect(
                    color = tint,
                    topLeft = Offset(w * 0.30f, h * 0.14f),
                    size = Size(w * 0.34f, h * 0.24f),
                    style = stroke
                )
                drawRect(
                    color = tint,
                    topLeft = Offset(w * 0.26f, h * 0.54f),
                    size = Size(w * 0.48f, h * 0.32f),
                    style = stroke
                )
            }
            StudioIconType.EXPORT, StudioIconType.COMPILE -> {
                // Up-arrow out of tray
                drawLine(
                    color = tint,
                    start = Offset(w * 0.50f, h * 0.66f),
                    end = Offset(w * 0.50f, h * 0.16f),
                    strokeWidth = stroke.width
                )
                val head = Path().apply {
                    moveTo(w * 0.28f, h * 0.36f)
                    lineTo(w * 0.50f, h * 0.15f)
                    lineTo(w * 0.72f, h * 0.36f)
                }
                drawPath(head, color = tint, style = stroke)
                val tray = Path().apply {
                    moveTo(w * 0.14f, h * 0.60f)
                    lineTo(w * 0.14f, h * 0.86f)
                    lineTo(w * 0.86f, h * 0.86f)
                    lineTo(w * 0.86f, h * 0.60f)
                }
                drawPath(tray, color = tint, style = stroke)
            }
            StudioIconType.MANUSCRIPT, StudioIconType.PAGE, StudioIconType.CHAPTER, StudioIconType.SCRIPT -> {
                val page = Path().apply {
                    moveTo(w * 0.20f, h * 0.12f)
                    lineTo(w * 0.64f, h * 0.12f)
                    lineTo(w * 0.82f, h * 0.30f)
                    lineTo(w * 0.82f, h * 0.88f)
                    lineTo(w * 0.20f, h * 0.88f)
                    close()
                }
                drawPath(page, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.33f, h * 0.46f), Offset(w * 0.68f, h * 0.46f), stroke.width)
                drawLine(tint, Offset(w * 0.33f, h * 0.64f), Offset(w * 0.68f, h * 0.64f), stroke.width)
            }
            StudioIconType.CORKBOARD -> {
                // 2x2 architectural grid
                val s = w * 0.32f
                drawRect(tint, Offset(w * 0.14f, h * 0.14f), Size(s, s), style = stroke)
                drawRect(tint, Offset(w * 0.54f, h * 0.14f), Size(s, s), style = stroke)
                drawRect(tint, Offset(w * 0.14f, h * 0.54f), Size(s, s), style = stroke)
                drawRect(tint, Offset(w * 0.54f, h * 0.54f), Size(s, s), style = stroke)
            }
            StudioIconType.BIBLE -> {
                // Open book / codex
                drawRect(
                    color = tint,
                    topLeft = Offset(w * 0.14f, h * 0.16f),
                    size = Size(w * 0.72f, h * 0.68f),
                    style = stroke
                )
                drawLine(tint, Offset(w * 0.50f, h * 0.16f), Offset(w * 0.50f, h * 0.84f), stroke.width)
            }
            StudioIconType.SEARCH -> {
                drawCircle(
                    color = tint,
                    radius = w * 0.27f,
                    center = Offset(w * 0.43f, h * 0.43f),
                    style = stroke
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.63f, h * 0.63f),
                    end = Offset(w * 0.86f, h * 0.86f),
                    strokeWidth = stroke.width
                )
            }
            StudioIconType.AUDIO -> {
                val speaker = Path().apply {
                    moveTo(w * 0.14f, h * 0.38f)
                    lineTo(w * 0.34f, h * 0.38f)
                    lineTo(w * 0.56f, h * 0.18f)
                    lineTo(w * 0.56f, h * 0.82f)
                    lineTo(w * 0.34f, h * 0.62f)
                    lineTo(w * 0.14f, h * 0.62f)
                    close()
                }
                drawPath(speaker, color = tint, style = stroke)
                drawLine(tint, Offset(w * 0.72f, h * 0.34f), Offset(w * 0.72f, h * 0.66f), stroke.width)
                drawLine(tint, Offset(w * 0.86f, h * 0.24f), Offset(w * 0.86f, h * 0.76f), stroke.width)
            }
            StudioIconType.MIC -> {
                drawRect(
                    color = tint,
                    topLeft = Offset(w * 0.36f, h * 0.14f),
                    size = Size(w * 0.28f, h * 0.44f),
                    style = stroke
                )
                drawLine(tint, Offset(w * 0.24f, h * 0.48f), Offset(w * 0.24f, h * 0.68f), stroke.width)
                drawLine(tint, Offset(w * 0.76f, h * 0.48f), Offset(w * 0.76f, h * 0.68f), stroke.width)
                drawLine(tint, Offset(w * 0.24f, h * 0.68f), Offset(w * 0.76f, h * 0.68f), stroke.width)
                drawLine(tint, Offset(w * 0.50f, h * 0.68f), Offset(w * 0.50f, h * 0.88f), stroke.width)
            }
            StudioIconType.DOCTOR, StudioIconType.SPARK, StudioIconType.SPARKLE -> {
                // Precision 4-point star / pulse cross
                val path = Path().apply {
                    moveTo(w * 0.50f, h * 0.12f)
                    lineTo(w * 0.61f, h * 0.39f)
                    lineTo(w * 0.88f, h * 0.50f)
                    lineTo(w * 0.61f, h * 0.61f)
                    lineTo(w * 0.50f, h * 0.88f)
                    lineTo(w * 0.39f, h * 0.61f)
                    lineTo(w * 0.12f, h * 0.50f)
                    lineTo(w * 0.39f, h * 0.39f)
                    close()
                }
                drawPath(path, color = tint, style = stroke)
            }
            StudioIconType.SETTINGS -> {
                // Architectural sliders / tuning bars
                drawLine(tint, Offset(w * 0.14f, h * 0.28f), Offset(w * 0.86f, h * 0.28f), stroke.width)
                drawLine(tint, Offset(w * 0.14f, h * 0.50f), Offset(w * 0.86f, h * 0.50f), stroke.width)
                drawLine(tint, Offset(w * 0.14f, h * 0.72f), Offset(w * 0.86f, h * 0.72f), stroke.width)
                drawRect(tint, Offset(w * 0.28f, h * 0.19f), Size(w * 0.16f, h * 0.18f), style = stroke)
                drawRect(tint, Offset(w * 0.58f, h * 0.41f), Size(w * 0.16f, h * 0.18f), style = stroke)
                drawRect(tint, Offset(w * 0.36f, h * 0.63f), Size(w * 0.16f, h * 0.18f), style = stroke)
            }
            StudioIconType.HOME -> {
                val roof = Path().apply {
                    moveTo(w * 0.12f, h * 0.48f)
                    lineTo(w * 0.50f, h * 0.16f)
                    lineTo(w * 0.88f, h * 0.48f)
                }
                drawPath(roof, color = tint, style = stroke)
                drawRect(tint, Offset(w * 0.22f, h * 0.46f), Size(w * 0.56f, h * 0.40f), style = stroke)
            }
            StudioIconType.MENU, StudioIconType.LIST -> {
                drawLine(tint, Offset(w * 0.16f, h * 0.28f), Offset(w * 0.84f, h * 0.28f), stroke.width)
                drawLine(tint, Offset(w * 0.16f, h * 0.50f), Offset(w * 0.84f, h * 0.50f), stroke.width)
                drawLine(tint, Offset(w * 0.16f, h * 0.72f), Offset(w * 0.84f, h * 0.72f), stroke.width)
            }
            StudioIconType.ADD -> {
                drawLine(tint, Offset(w * 0.50f, h * 0.18f), Offset(w * 0.50f, h * 0.82f), stroke.width)
                drawLine(tint, Offset(w * 0.18f, h * 0.50f), Offset(w * 0.82f, h * 0.50f), stroke.width)
            }
            StudioIconType.CLOSE -> {
                drawLine(tint, Offset(w * 0.22f, h * 0.22f), Offset(w * 0.78f, h * 0.78f), stroke.width)
                drawLine(tint, Offset(w * 0.78f, h * 0.22f), Offset(w * 0.22f, h * 0.78f), stroke.width)
            }
            StudioIconType.CHECK -> {
                val p = Path().apply {
                    moveTo(w * 0.18f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.76f)
                    lineTo(w * 0.84f, h * 0.24f)
                }
                drawPath(p, color = tint, style = stroke)
            }
            StudioIconType.REVISION, StudioIconType.COVER -> {
                drawRect(tint, Offset(w * 0.18f, h * 0.14f), Size(w * 0.64f, h * 0.72f), style = stroke)
                drawLine(tint, Offset(w * 0.18f, h * 0.34f), Offset(w * 0.82f, h * 0.34f), stroke.width)
            }
            StudioIconType.REPORT -> {
                drawLine(tint, Offset(w * 0.22f, h * 0.82f), Offset(w * 0.22f, h * 0.48f), stroke.width)
                drawLine(tint, Offset(w * 0.50f, h * 0.82f), Offset(w * 0.50f, h * 0.24f), stroke.width)
                drawLine(tint, Offset(w * 0.78f, h * 0.82f), Offset(w * 0.78f, h * 0.36f), stroke.width)
            }
            StudioIconType.COMPARE -> {
                drawRect(tint, Offset(w * 0.14f, h * 0.18f), Size(w * 0.32f, h * 0.64f), style = stroke)
                drawRect(tint, Offset(w * 0.54f, h * 0.18f), Size(w * 0.32f, h * 0.64f), style = stroke)
            }
            StudioIconType.LOCK, StudioIconType.UNLOCK -> {
                drawRect(tint, Offset(w * 0.22f, h * 0.46f), Size(w * 0.56f, h * 0.40f), style = stroke)
                drawRect(tint, Offset(w * 0.34f, h * 0.20f), Size(w * 0.32f, h * 0.26f), style = stroke)
            }
            StudioIconType.NONE -> Unit
        }
    }
}
