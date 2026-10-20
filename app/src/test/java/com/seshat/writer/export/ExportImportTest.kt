package com.seshat.writer.export

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.DualDialogueSide
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.screenplay.ScreenplayViewModel
import com.seshat.writer.titlepage.ScreenplayDocumentStore
import com.seshat.writer.titlepage.TitlePageData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportImportTest {

    private lateinit var sampleBlocks: List<ScreenplayBlock>
    private lateinit var sampleTitlePage: TitlePageData

    @Before
    fun setUp() {
        sampleTitlePage = TitlePageData(
            title = "THE DEEP HORIZON",
            writtenByLabel = "written by",
            author = "Jane Doe & John Smith",
            sourceMaterial = "Based on true events",
            contactInfo = "Agent: CAA\n555-0199",
            draftInfo = "First Draft\nOctober 2026"
        )

        sampleBlocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "FADE IN:"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. SUBMARINE CONTROL ROOM - NIGHT"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Red emergency lights pulse rhythmically. Water drips onto the metal console."),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "CAPTAIN REYES"),
            ScreenplayBlock(type = BlockType.PARENTHETICAL, text = "(quietly, into the comms)"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Hold your positions. Nobody makes a sound."),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "CREWMAN (O.S.)"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Sir, depth charges are in the water!"),
            ScreenplayBlock(type = BlockType.TRANSITION, text = "CUT TO:"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. OCEAN SURFACE - CONTINUOUS"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Explosions shatter the dark ocean surface, throwing geysers into the sky."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "OVER BLACK")
        )
    }

    @Test
    fun fountainExportAndImportRoundtrip() {
        val fountainString = FountainSerializer.exportToFountain(sampleBlocks, sampleTitlePage)
        assertNotNull(fountainString)
        assertTrue(fountainString.contains("Title: THE DEEP HORIZON"))
        assertTrue(fountainString.contains("Author: Jane Doe & John Smith"))
        assertTrue(fountainString.contains("INT. SUBMARINE CONTROL ROOM - NIGHT"))

        val importedDoc = FountainSerializer.importFromFountain(fountainString)
        assertEquals("THE DEEP HORIZON", importedDoc.titlePage.title)
        assertEquals("Jane Doe & John Smith", importedDoc.titlePage.author)
        assertEquals("written by", importedDoc.titlePage.writtenByLabel)
        assertEquals("Based on true events", importedDoc.titlePage.sourceMaterial)

        // Compare screenplay blocks
        assertEquals(sampleBlocks.size, importedDoc.blocks.size)
        for (i in sampleBlocks.indices) {
            val original = sampleBlocks[i]
            val imported = importedDoc.blocks[i]
            assertEquals("Block $i type mismatch", original.type, imported.type)
            assertEquals("Block $i text mismatch", original.text, imported.text)
        }
    }

    @Test
    fun finalDraftExportAndImportRoundtrip() {
        val fdxString = FinalDraftSerializer.exportToFdx(sampleBlocks, sampleTitlePage)
        assertNotNull(fdxString)
        assertTrue(fdxString.contains("<FinalDraft"))
        assertTrue(fdxString.contains("<TitlePage>"))
        assertTrue(fdxString.contains("<Content>"))
        assertTrue(fdxString.contains("CAPTAIN REYES"))

        val importedDoc = FinalDraftSerializer.importFromFdx(fdxString)
        assertEquals("THE DEEP HORIZON", importedDoc.titlePage.title)
        assertEquals("Jane Doe & John Smith", importedDoc.titlePage.author)
        assertEquals("written by", importedDoc.titlePage.writtenByLabel)
        assertEquals("Based on true events", importedDoc.titlePage.sourceMaterial)

        // Compare screenplay blocks
        assertEquals(sampleBlocks.size, importedDoc.blocks.size)
        for (i in sampleBlocks.indices) {
            val original = sampleBlocks[i]
            val imported = importedDoc.blocks[i]
            assertEquals("Block $i type mismatch", original.type, imported.type)
            assertEquals("Block $i text mismatch", original.text, imported.text)
        }
    }

    @Test
    fun screenplayViewModelLoadAndNewDocument() {
        val viewModel = ScreenplayViewModel()

        // Load document
        viewModel.loadDocument(sampleBlocks, sampleTitlePage)
        val stateAfterLoad = viewModel.uiState.value
        assertEquals("THE DEEP HORIZON", stateAfterLoad.title)
        assertEquals("Jane Doe & John Smith", stateAfterLoad.author)
        assertEquals(sampleBlocks.size, stateAfterLoad.blocks.size)
        assertEquals("FADE IN:", stateAfterLoad.blocks[0].text)

        // Document Store title page should match
        val storeData = ScreenplayDocumentStore.titlePage.value
        assertEquals("THE DEEP HORIZON", storeData.title)

        // New Document
        viewModel.newDocument()
        val stateAfterNew = viewModel.uiState.value
        assertEquals("UNTITLED SCREENPLAY", stateAfterNew.title)
        assertEquals("", stateAfterNew.author)
        assertEquals(1, stateAfterNew.blocks.size)
        assertEquals(BlockType.SCENE_HEADING, stateAfterNew.blocks[0].type)
        assertEquals("", stateAfterNew.blocks[0].text)
    }

    @Test
    fun dualDialogueFountainRoundtrip() {
        val dualId = "test-group"
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. OFFICE - DAY"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "BRICK", dualDialogueId = dualId, dualSide = DualDialogueSide.LEFT),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "We go now!", dualDialogueId = dualId, dualSide = DualDialogueSide.LEFT),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "STEEL", dualDialogueId = dualId, dualSide = DualDialogueSide.RIGHT),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Not on my watch!", dualDialogueId = dualId, dualSide = DualDialogueSide.RIGHT)
        )
        val titlePage = TitlePageData(title = "DUAL TEST")

        val exported = FountainSerializer.exportToFountain(blocks, titlePage)
        assertTrue(exported.contains("BRICK\n"))
        assertTrue(exported.contains("STEEL ^\n"))

        val imported = FountainSerializer.importFromFountain(exported)
        val importedBlocks = imported.blocks
        val brickChar = importedBlocks.first { it.text == "BRICK" }
        val brickDiag = importedBlocks.first { it.text == "We go now!" }
        val steelChar = importedBlocks.first { it.text == "STEEL" }
        val steelDiag = importedBlocks.first { it.text == "Not on my watch!" }

        assertNotNull(brickChar.dualDialogueId)
        assertEquals(brickChar.dualDialogueId, steelChar.dualDialogueId)
        assertEquals(DualDialogueSide.LEFT, brickChar.dualSide)
        assertEquals(DualDialogueSide.LEFT, brickDiag.dualSide)
        assertEquals(DualDialogueSide.RIGHT, steelChar.dualSide)
        assertEquals(DualDialogueSide.RIGHT, steelDiag.dualSide)
    }

    @Test
    fun fountainSceneNumbersRoundtrip() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. BANK - DAY", sceneNumber = "14"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Inside the vault."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. STREET - CONTINUOUS", sceneNumber = "15A")
        )
        val titlePage = TitlePageData(title = "SHOOTING SCRIPT")

        val fountainString = FountainSerializer.exportToFountain(blocks, titlePage)
        assertTrue(fountainString.contains("INT. BANK - DAY #14#"))
        assertTrue(fountainString.contains("EXT. STREET - CONTINUOUS #15A#"))

        val imported = FountainSerializer.importFromFountain(fountainString)
        val h1 = imported.blocks.first { it.text.contains("INT. BANK - DAY") }
        val h2 = imported.blocks.first { it.text.contains("EXT. STREET - CONTINUOUS") }

        assertEquals("14", h1.sceneNumber)
        assertEquals("INT. BANK - DAY", h1.text)
        assertEquals("15A", h2.sceneNumber)
        assertEquals("EXT. STREET - CONTINUOUS", h2.text)
    }

    @Test
    fun finalDraftSceneNumbersRoundtrip() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. VAULT - NIGHT", sceneNumber = "42"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Alarm bells ring."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. ROOF - CONTINUOUS", sceneNumber = "43B")
        )
        val titlePage = TitlePageData(title = "SHOOTING SCRIPT FDX")

        val fdxString = FinalDraftSerializer.exportToFdx(blocks, titlePage)
        assertTrue(fdxString.contains("Number=\"42\""))
        assertTrue(fdxString.contains("Number=\"43B\""))

        val imported = FinalDraftSerializer.importFromFdx(fdxString)
        val h1 = imported.blocks.first { it.text.contains("INT. VAULT - NIGHT") }
        val h2 = imported.blocks.first { it.text.contains("EXT. ROOF - CONTINUOUS") }

        assertEquals("42", h1.sceneNumber)
        assertEquals("INT. VAULT - NIGHT", h1.text)
        assertEquals("43B", h2.sceneNumber)
        assertEquals("EXT. ROOF - CONTINUOUS", h2.text)
    }

    @Test
    fun writersNotesFountainRoundtrip() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. STUDY - NIGHT", note = "Check if study should be library"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Marcus reads the dusty journal.", note = "Page 42 discovery"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "MARCUS"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "It's all here. Every word.", note = "Deliver with quiet intensity")
        )
        val titlePage = TitlePageData(title = "NOTE TEST FOUNTAIN")

        val fountain = FountainSerializer.exportToFountain(blocks, titlePage)
        assertTrue(fountain.contains("[[Check if study should be library]]"))
        assertTrue(fountain.contains("[[Page 42 discovery]]"))
        assertTrue(fountain.contains("[[Deliver with quiet intensity]]"))

        val imported = FountainSerializer.importFromFountain(fountain)
        assertEquals(blocks.size, imported.blocks.size)
        assertEquals("Check if study should be library", imported.blocks[0].note)
        assertEquals("Page 42 discovery", imported.blocks[1].note)
        assertEquals("Deliver with quiet intensity", imported.blocks[3].note)
    }

    @Test
    fun writersNotesFinalDraftRoundtrip() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. ALLEYWAY - NIGHT", note = "Consider neon lighting"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Shadows move across the wet asphalt.", note = "Film noir tone"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "DETECTIVE REED"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "We're not alone.", note = "Whispered")
        )
        val titlePage = TitlePageData(title = "NOTE TEST FDX")

        val fdx = FinalDraftSerializer.exportToFdx(blocks, titlePage)
        assertTrue(fdx.contains("<ScriptAnnotation>"))
        assertTrue(fdx.contains("Consider neon lighting"))
        assertTrue(fdx.contains("Film noir tone"))
        assertTrue(fdx.contains("Whispered"))

        val imported = FinalDraftSerializer.importFromFdx(fdx)
        assertEquals(blocks.size, imported.blocks.size)
        assertEquals("EXT. ALLEYWAY - NIGHT", imported.blocks[0].text)
        assertEquals("Consider neon lighting", imported.blocks[0].note)
        assertEquals("Shadows move across the wet asphalt.", imported.blocks[1].text)
        assertEquals("Film noir tone", imported.blocks[1].note)
        assertEquals("We're not alone.", imported.blocks[3].text)
        assertEquals("Whispered", imported.blocks[3].note)
    }

    @Test
    fun pdfExportOptionsConfiguration() {
        val options = PdfExporter.PdfExportOptions(
            includeTitlePage = true,
            showPageNumbers = true,
            showSceneNumbers = true,
            watermarkText = "CONFIDENTIAL - PRODUCER COPY",
            headerText = "PILOT - 2ND DRAFT",
            includeNotes = true
        )

        assertEquals("CONFIDENTIAL - PRODUCER COPY", options.watermarkText)
        assertEquals("PILOT - 2ND DRAFT", options.headerText)
        assertTrue(options.includeNotes)
        assertTrue(options.includeTitlePage)
        assertTrue(options.showSceneNumbers)
    }
}
