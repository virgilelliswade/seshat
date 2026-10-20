package com.seshat.writer.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiAssistantTest {

    @Test
    fun testAiConfigDefaultsAndConfiguredState() {
        val defaultConfig = AiConfig()
        assertFalse("AI should be disabled by default", defaultConfig.isEnabled)
        assertFalse("Unconfigured when disabled", defaultConfig.isConfigured)
        assertEquals(AiProvider.GOOGLE_GEMINI, defaultConfig.provider)

        val enabledNoKey = defaultConfig.copy(isEnabled = true, apiKey = "")
        assertFalse("Key is required for Gemini", enabledNoKey.isConfigured)

        val enabledWithKey = defaultConfig.copy(isEnabled = true, apiKey = "AIzaSyTestKey123")
        assertTrue("Configured when enabled with key", enabledWithKey.isConfigured)

        assertEquals("gemini-3.8-flash", defaultConfig.modelName)
        assertTrue("Voice dictation is enabled by default in AiConfig", defaultConfig.enableVoiceDictation)

        val localOllamaNoKey = defaultConfig.copy(isEnabled = true, provider = AiProvider.CUSTOM_LOCAL, apiKey = "")
        assertTrue("Local Ollama does not require key to be configured", localOllamaNoKey.isConfigured)
    }

    @Test
    fun testStudioCoverageReportFormatting() {
        val report = StudioCoverageReport(
            scriptTitle = "THE GOLDEN SCRIBE",
            author = "Jane Doe",
            logline = "An ancient archivist discovers a conspiracy that threatens the kingdom.",
            premiseScore = "9.2 / 10 - Exceptional High Concept",
            synopsis = "Act I: Scribe finds the scroll. Act II: Escapes royal guards. Act III: Unmasks the traitor.",
            dialogueReview = "Sharp, rhythmic dialogue with layered subtext.",
            pacingReview = "Relentless second act momentum.",
            commercialProspects = "High international prestige appeal.",
            recommendation = CoverageVerdict.RECOMMEND,
            characterArcs = listOf(
                CharacterArc("SARAH", "From timid archivist to defiant truth-teller."),
                CharacterArc("MARCUS", "From obedient guard to principled ally.")
            )
        )

        val text = report.formatPlainText()
        assertTrue(text.contains("SESHAT STUDIO COVERAGE REPORT"))
        assertTrue(text.contains("THE GOLDEN SCRIBE"))
        assertTrue(text.contains("RECOMMEND"))
        assertTrue(text.contains("SARAH"))
        assertTrue(text.contains("MARCUS"))
        assertTrue(text.contains("Relentless second act momentum."))
    }

    @Test
    fun testCoverageVerdicts() {
        assertEquals("RECOMMEND", CoverageVerdict.RECOMMEND.displayName)
        assertEquals("CONSIDER", CoverageVerdict.CONSIDER.displayName)
        assertEquals("PASS", CoverageVerdict.PASS.displayName)
    }

    @Test
    fun testAiChatMessageCreation() {
        val msg = AiChatMessage(role = "user", content = "Punch up this action line")
        assertEquals("user", msg.role)
        assertEquals("Punch up this action line", msg.content)
        assertTrue(msg.timestamp > 0)
    }
}
