package com.seshat.writer.screenplay

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenplayLayoutStandardsTest {

    @Test
    fun verifyFinalDraftPageGeometry() {
        assertEquals(8.5f, ScreenplayLayoutStandards.PAGE_WIDTH_INCHES, 0.01f)
        assertEquals(11.0f, ScreenplayLayoutStandards.PAGE_HEIGHT_INCHES, 0.01f)
        assertEquals(1.5f, ScreenplayLayoutStandards.PAGE_LEFT_MARGIN_INCHES, 0.01f)
        assertEquals(1.0f, ScreenplayLayoutStandards.PAGE_RIGHT_MARGIN_INCHES, 0.01f)
        assertEquals(1.0f, ScreenplayLayoutStandards.PAGE_TOP_MARGIN_INCHES, 0.01f)
        assertEquals(1.0f, ScreenplayLayoutStandards.PAGE_BOTTOM_MARGIN_INCHES, 0.01f)
    }

    @Test
    fun verifyFinalDraftElementOffsets() {
        assertEquals(1.5f, ScreenplayLayoutStandards.SCENE_HEADING_LEFT_INCHES, 0.01f)
        assertEquals(1.5f, ScreenplayLayoutStandards.ACTION_LEFT_INCHES, 0.01f)
        assertEquals(3.7f, ScreenplayLayoutStandards.CHARACTER_LEFT_INCHES, 0.01f)
        assertEquals(3.1f, ScreenplayLayoutStandards.PARENTHETICAL_LEFT_INCHES, 0.01f)
        assertEquals(2.5f, ScreenplayLayoutStandards.DIALOGUE_LEFT_INCHES, 0.01f)
        assertEquals(5.5f, ScreenplayLayoutStandards.TRANSITION_LEFT_INCHES, 0.01f)
    }

    @Test
    fun verifyElementWidthBudgets() {
        // Action line maximum writable width = 8.5" - 1.5" - 1.0" = 6.0" (60 chars in 10-pitch Courier)
        assertEquals(6.0f, ScreenplayLayoutStandards.ACTION_WIDTH_INCHES, 0.01f)
        // Dialogue width = 3.5" (35 chars)
        assertEquals(3.5f, ScreenplayLayoutStandards.DIALOGUE_WIDTH_INCHES, 0.01f)
        // Parenthetical width = 2.5" (25 chars)
        assertEquals(2.5f, ScreenplayLayoutStandards.PARENTHETICAL_WIDTH_INCHES, 0.01f)
    }

    @Test
    fun verifyDualDialogueLayoutStandards() {
        assertEquals(1.5f, ScreenplayLayoutStandards.DUAL_DIALOGUE_LEFT_COLUMN_LEFT_INCHES, 0.01f)
        assertEquals(2.75f, ScreenplayLayoutStandards.DUAL_DIALOGUE_COLUMN_WIDTH_INCHES, 0.01f)
        assertEquals(0.5f, ScreenplayLayoutStandards.DUAL_DIALOGUE_COLUMN_GUTTER_INCHES, 0.01f)
        assertEquals(4.75f, ScreenplayLayoutStandards.DUAL_DIALOGUE_RIGHT_COLUMN_LEFT_INCHES, 0.01f)
        // Total span: 1.5 + 2.75 + 0.5 + 2.75 = 7.5" (leaves 1.0" right margin on 8.5" page)
        val totalSpan = ScreenplayLayoutStandards.DUAL_DIALOGUE_RIGHT_COLUMN_LEFT_INCHES +
                ScreenplayLayoutStandards.DUAL_DIALOGUE_COLUMN_WIDTH_INCHES
        assertEquals(7.5f, totalSpan, 0.01f)
    }
}
