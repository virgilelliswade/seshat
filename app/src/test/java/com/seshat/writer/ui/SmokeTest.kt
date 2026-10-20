package com.seshat.writer.ui

import androidx.compose.ui.graphics.Color
import com.seshat.writer.ui.theme.ScribeStarNeonRed
import com.seshat.writer.ui.theme.smokedGlassBrush
import org.junit.Test
import org.junit.Assert.assertNotNull

class SmokeTest {
    @Test
    fun testSmokedGlassBrush() {
        val brush = smokedGlassBrush(glassOpacity = 0.90f)
        assertNotNull(brush)
    }

    @Test
    fun testStrokeOnTextStyle() {
        // Test if TextStyle with Stroke can be constructed
        val style = androidx.compose.ui.text.TextStyle(
            drawStyle = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
        )
        assertNotNull(style)
    }
}
