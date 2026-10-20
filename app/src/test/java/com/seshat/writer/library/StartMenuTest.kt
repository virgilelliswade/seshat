package com.seshat.writer.library

import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.model.BlockType
import com.seshat.writer.screenplay.ScreenplayUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StartMenuTest {

    @Test
    fun testStartMenuEnumsAndDisplayNames() {
        assertEquals("Screenplay", ProjectFormat.SCREENPLAY.displayName)
        assertEquals("Novel", ProjectFormat.NOVEL.displayName)

        assertEquals("Feature Film", ScreenplayType.FEATURE.displayName)
        assertEquals("TV (Hourlong / Drama)", ScreenplayType.TV_HOUR_DRAMA.displayName)
        assertEquals("TV (Half-Hour / Comedy)", ScreenplayType.TV_HALF_HOUR_COMEDY.displayName)

        assertEquals("Lore Doc & Story Bible", InitialWorkspaceView.LORE_DOC.displayName)
        assertEquals("Corkboard Outline", InitialWorkspaceView.OUTLINE.displayName)
        assertEquals("Drafting Canvas", InitialWorkspaceView.EDITOR.displayName)
    }

    @Test
    fun testActStructureForFeatureScreenplay() {
        val acts = CorkboardDocument.actsForFeature()
        assertEquals(4, acts.size)
        assertEquals("Act I", acts[0].title)
        assertEquals("Act II-A", acts[1].title)
        assertEquals("Act II-B", acts[2].title)
        assertEquals("Act III", acts[3].title)
    }

    @Test
    fun testActStructureForTvHourlongDrama() {
        val acts = CorkboardDocument.actsForTvHourlong()
        assertEquals(6, acts.size)
        assertEquals("Teaser", acts[0].title)
        assertEquals("Act I", acts[1].title)
        assertEquals("Act II", acts[2].title)
        assertEquals("Act III", acts[3].title)
        assertEquals("Act IV", acts[4].title)
        assertEquals("Act V", acts[5].title)
    }

    @Test
    fun testActStructureForTvHalfHourComedy() {
        val acts = CorkboardDocument.actsForTvHalfHour()
        assertEquals(4, acts.size)
        assertEquals("Cold Open", acts[0].title)
        assertEquals("Act I", acts[1].title)
        assertEquals("Act II", acts[2].title)
        assertEquals("Tag / Epilogue", acts[3].title)
    }

    @Test
    fun testActStructureForNovel() {
        val acts = CorkboardDocument.actsForNovel()
        assertEquals(3, acts.size)
        assertEquals("Part I", acts[0].title)
        assertEquals("Part II", acts[1].title)
        assertEquals("Part III", acts[2].title)
    }

    @Test
    fun testInitialUiStateOpensStartMenu() {
        val state = ScreenplayUiState()
        // Fresh launch defaults to Start Menu open
        assertTrue(state.isStartMenuOpen)
        assertEquals(ProjectFormat.SCREENPLAY, state.projectFormat)
        assertEquals(ScreenplayType.FEATURE, state.screenplayType)
    }

    @Test
    fun testNovelTemplateCreation() {
        val novelTemplate = ScriptTemplate.NOVEL
        assertEquals("NOVEL", novelTemplate.name)
    }
}
