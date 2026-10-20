package com.seshat.writer.titlepage

import com.seshat.writer.screenplay.ScreenplayViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TitlePageTest {

    private lateinit var titlePageViewModel: TitlePageViewModel
    private lateinit var screenplayViewModel: ScreenplayViewModel

    @Before
    fun setUp() {
        ScreenplayDocumentStore.setTitlePage(
            TitlePageData(
                title = "UNTITLED SCREENPLAY",
                writtenByLabel = "written by",
                author = "",
                sourceMaterial = "",
                contactInfo = "",
                draftInfo = ""
            )
        )
        titlePageViewModel = TitlePageViewModel()
        screenplayViewModel = ScreenplayViewModel()
    }

    @Test
    fun defaultTitlePageInitialValuesAreCorrect() {
        val data = titlePageViewModel.titlePageState.value
        assertEquals("UNTITLED SCREENPLAY", data.title)
        assertEquals("written by", data.writtenByLabel)
        assertEquals("", data.author)
        assertEquals("", data.sourceMaterial)
        assertEquals("", data.contactInfo)
        assertEquals("", data.draftInfo)
        assertFalse(data.isComplete)
    }

    @Test
    fun updatingTitlePageMetadataSyncsAcrossDocumentStore() {
        titlePageViewModel.updateTitle("THE LAST SUNRISE")
        titlePageViewModel.updateAuthor("Jane Doe")
        titlePageViewModel.updateWrittenByLabel("by")
        titlePageViewModel.updateSourceMaterial("Based on a short story by John Doe")
        titlePageViewModel.updateContactInfo("Agent: CAA\nPhone: 555-0199")
        titlePageViewModel.updateDraftInfo("First Draft\nOctober 2026")

        val data = ScreenplayDocumentStore.titlePage.value
        assertEquals("THE LAST SUNRISE", data.title)
        assertEquals("THE LAST SUNRISE", data.formattedTitle())
        assertEquals("Jane Doe", data.author)
        assertEquals("by", data.writtenByLabel)
        assertEquals("Based on a short story by John Doe", data.sourceMaterial)
        assertEquals("Agent: CAA\nPhone: 555-0199", data.contactInfo)
        assertEquals("First Draft\nOctober 2026", data.draftInfo)
        assertTrue(data.isComplete)
    }

    @Test
    fun insertCurrentDateAppendsDateToDraftInfo() {
        titlePageViewModel.updateDraftInfo("FIRST DRAFT")
        titlePageViewModel.insertCurrentDateToDraft()

        val draftInfo = titlePageViewModel.titlePageState.value.draftInfo
        assertTrue(draftInfo.startsWith("FIRST DRAFT\n"))
        // Check that month and year are present
        assertTrue(draftInfo.contains("2026"))
    }

    @Test
    fun setDraftPresetAddsPresetToDraftInfo() {
        titlePageViewModel.updateDraftInfo("")
        titlePageViewModel.setDraftPreset("SHOOTING SCRIPT")
        assertEquals("SHOOTING SCRIPT", titlePageViewModel.titlePageState.value.draftInfo)

        titlePageViewModel.setDraftPreset("REVISED DRAFT")
        val draftInfo = titlePageViewModel.titlePageState.value.draftInfo
        assertTrue(draftInfo.contains("REVISED DRAFT"))
        assertTrue(draftInfo.contains("SHOOTING SCRIPT"))
    }

    @Test
    fun screenplayViewModelAndTitlePageViewModelStaySynchronized() {
        // Updating title in ScreenplayViewModel
        screenplayViewModel.updateTitle("CYBERPUNK DETECTIVE")
        assertEquals("CYBERPUNK DETECTIVE", ScreenplayDocumentStore.titlePage.value.title)
        assertEquals("CYBERPUNK DETECTIVE", titlePageViewModel.titlePageState.value.title)

        // Updating author in TitlePageViewModel
        titlePageViewModel.updateAuthor("Alex Vance")
        assertEquals("Alex Vance", ScreenplayDocumentStore.titlePage.value.author)

        // Syncing ScreenplayViewModel reflects TitlePage changes
        screenplayViewModel.syncTitlePage()
        assertEquals("Alex Vance", screenplayViewModel.uiState.value.author)
        assertEquals("CYBERPUNK DETECTIVE", screenplayViewModel.uiState.value.title)
    }
}
