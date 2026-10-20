package com.seshat.writer.importer

import com.seshat.writer.model.BlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptImporterTest {

    @Test
    fun parseFountainContentReturnsValidScreenplayDocument() {
        val fountainContent = """
            Title: CITIZEN KANE
            Author: Orson Welles & Herman J. Mankiewicz
            Credit: written by
            
            INT. XANADU - BEDROOM - NIGHT #1#
            
            A snow globe falls from a limp hand and shatters across the floor.
            
            KANE
            (whispering)
            Rosebud.
        """.trimIndent()

        val result = ScriptImporter.parseScriptContent(fountainContent, "citizen_kane.fountain")
        assertTrue(result.isSuccess)
        val doc = result.document!!
        assertEquals("CITIZEN KANE", doc.titlePage.title)
        assertEquals("Orson Welles & Herman J. Mankiewicz", doc.titlePage.author)
        assertEquals(ScriptFormat.FOUNTAIN, result.detectedFormat)

        val blocks = doc.blocks
        assertTrue(blocks.any { it.type == BlockType.SCENE_HEADING && it.text.contains("XANADU") })
        assertTrue(blocks.any { it.type == BlockType.CHARACTER && it.text == "KANE" })
        assertTrue(blocks.any { it.type == BlockType.DIALOGUE && it.text == "Rosebud." })
    }

    @Test
    fun parseFinalDraftXmlReturnsValidScreenplayDocument() {
        val fdxContent = """
            <?xml version="1.0" encoding="UTF-8" standalone="no" ?>
            <FinalDraft DocumentType="Script" Template="No" Version="5">
              <Content>
                <Paragraph Type="Scene Heading" Number="1">
                  <Text>INT. POLICE HEADQUARTERS - DAY</Text>
                </Paragraph>
                <Paragraph Type="Action">
                  <Text>Detective Somerset examines a file folder on his desk.</Text>
                </Paragraph>
                <Paragraph Type="Character">
                  <Text>SOMERSET</Text>
                </Paragraph>
                <Paragraph Type="Dialogue">
                  <Text>This is only the beginning.</Text>
                </Paragraph>
              </Content>
              <TitlePage>
                <Content>
                  <Paragraph Alignment="Center">
                    <Text Style="Bold">SEVEN</Text>
                  </Paragraph>
                  <Paragraph Alignment="Center">
                    <Text>written by</Text>
                  </Paragraph>
                  <Paragraph Alignment="Center">
                    <Text Style="Bold">Andrew Kevin Walker</Text>
                  </Paragraph>
                </Content>
              </TitlePage>
            </FinalDraft>
        """.trimIndent()

        val result = ScriptImporter.parseScriptContent(fdxContent, "seven.fdx")
        assertTrue(result.isSuccess)
        val doc = result.document!!
        assertEquals("SEVEN", doc.titlePage.title)
        assertEquals("Andrew Kevin Walker", doc.titlePage.author)
        assertEquals(ScriptFormat.FINAL_DRAFT, result.detectedFormat)

        val blocks = doc.blocks
        assertTrue(blocks.any { it.type == BlockType.SCENE_HEADING && it.sceneNumber == "1" })
        assertTrue(blocks.any { it.type == BlockType.CHARACTER && it.text == "SOMERSET" })
    }

    @Test
    fun detectFormatAutoSensesFromContentEvenWithoutExtension() {
        val fdxWithoutExtension = """
            <?xml version="1.0" encoding="UTF-8"?>
            <FinalDraft DocumentType="Script">
              <Content>
                <Paragraph Type="Action"><Text>Testing.</Text></Paragraph>
              </Content>
            </FinalDraft>
        """.trimIndent()

        assertEquals(ScriptFormat.FINAL_DRAFT, ScriptImporter.detectFormat(fdxWithoutExtension, "unknown_file"))

        val fountainWithoutExtension = """
            INT. COFFEE SHOP - DAY
            
            JOHN drinks a cup of coffee.
        """.trimIndent()

        assertEquals(ScriptFormat.FOUNTAIN, ScriptImporter.detectFormat(fountainWithoutExtension, "unknown_file"))
    }

    @Test
    fun parseEmptyContentReturnsFailure() {
        val result = ScriptImporter.parseScriptContent("", "empty.txt")
        assertTrue(!result.isSuccess)
        assertNotNull(result.errorMessage)
    }
}
