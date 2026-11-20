package com.seshat.writer.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Represents the physical folding posture and display geometry of foldable devices
 * such as the Google Pixel 11 Pro Fold, Samsung Galaxy Z Fold 8, and dual-pane tablets.
 */
enum class FoldablePosture {
    /** Standard candybar phone, folded outer cover screen, or desktop window. */
    NORMAL,

    /** Fully unfolded inner display (e.g., Pixel Pro Fold ~8" near-square 2076x2152 OLED). */
    BOOK_UNFOLDED,

    /** Half-folded vertically (book-reading / dual-pane posture across vertical hinge). */
    BOOK_HALF_OPENED,

    /** Half-folded horizontally (~90°-120° Tabletop / Flex Mode: top canvas, bottom Writer's Deck). */
    TABLETOP_FLEX
}

@Immutable
data class FoldableWindowState(
    val posture: FoldablePosture = FoldablePosture.NORMAL,
    val isFoldableDevice: Boolean = false,
    val isVerticalHinge: Boolean = false,
    val isHorizontalHinge: Boolean = false,
    val hingeSizeDp: Int = 0,
    val hingeSplitRatio: Float = 0.5f,
    val screenWidthDp: Int = 0,
    val screenHeightDp: Int = 0,
    val isCoverScreen: Boolean = false,
    val isSquareInnerScreen: Boolean = false
) {
    /** True when the app should split panes around a vertical fold crease. */
    val shouldAlignToVerticalHinge: Boolean
        get() = isVerticalHinge && (posture == FoldablePosture.BOOK_HALF_OPENED || posture == FoldablePosture.BOOK_UNFOLDED)

    /** True when the app is in horizontal Tabletop / Flex Mode. */
    val isTabletopFlex: Boolean
        get() = posture == FoldablePosture.TABLETOP_FLEX && isHorizontalHinge

    /** Safe gutter padding in dp to keep text and interactive controls off the hinge crease. */
    val safeHingeGutterDp: Int
        get() = if (isVerticalHinge || isHorizontalHinge) hingeSizeDp.coerceAtLeast(14) else 0
}

val LocalFoldableWindowState = staticCompositionLocalOf { FoldableWindowState() }

/**
 * Computes a resolution-safe page card width (in dp) that respects the actual available
 * pane width on narrow cover screens (~410dp), unfolded near-square foldables (~840dp),
 * dual-pane half-screens (~410-560dp), and wide desktop monitors without overflowing or clipping.
 */
fun computeResponsivePageWidthDp(
    availableWidthDp: Float,
    zoom: Float,
    maxDesktopWidthDp: Float = 780f
): Float {
    val safeAvailable = (availableWidthDp - 16f).coerceAtLeast(260f)
    if (availableWidthDp < 620f) {
        return safeAvailable
    }
    val desired = maxDesktopWidthDp * zoom
    return desired.coerceAtMost(safeAvailable)
}

/**
 * Computes horizontal page margins (start, end, dragGutter) scaled smoothly for
 * narrow cover screens, foldable half-panes, unfolded near-square screens, and desktop.
 */
data class ResponsivePageMargins(
    val startDp: Float,
    val endDp: Float,
    val topDp: Float,
    val bottomDp: Float,
    val dragGutterDp: Float,
    val useCompactIndents: Boolean
)

fun computeResponsivePageMargins(
    availableWidthDp: Float,
    zoom: Float,
    isCompactScreen: Boolean
): ResponsivePageMargins {
    return when {
        isCompactScreen || availableWidthDp < 520f -> ResponsivePageMargins(
            startDp = (16f * zoom).coerceIn(12f, 28f),
            endDp = (12f * zoom).coerceIn(10f, 22f),
            topDp = (20f * zoom).coerceIn(14f, 32f),
            bottomDp = (28f * zoom).coerceIn(20f, 44f),
            dragGutterDp = 22f,
            useCompactIndents = true
        )
        availableWidthDp < 740f -> ResponsivePageMargins(
            startDp = (44f * zoom).coerceIn(28f, 64f),
            endDp = (28f * zoom).coerceIn(18f, 44f),
            topDp = (36f * zoom).coerceIn(24f, 48f),
            bottomDp = (44f * zoom).coerceIn(28f, 56f),
            dragGutterDp = 42f,
            useCompactIndents = availableWidthDp < 580f
        )
        else -> ResponsivePageMargins(
            startDp = 110f * zoom,
            endDp = 70f * zoom,
            topDp = 54f * zoom,
            bottomDp = 64f * zoom,
            dragGutterDp = 100f * zoom,
            useCompactIndents = false
        )
    }
}
