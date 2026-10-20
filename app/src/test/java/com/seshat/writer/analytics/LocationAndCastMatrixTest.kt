package com.seshat.writer.analytics

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationAndCastMatrixTest {

    @Test
    fun testLocationBreakdownIntExtAndDayNight() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. DETECTIVE OFFICE - DAY"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Dust motes in the light."),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "SARAH"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "We have a problem."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. HARBOR - NIGHT"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Waves crash against the pier."),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "JOHN"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Look over there."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. DETECTIVE OFFICE - NIGHT"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "SARAH"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Did you find it?"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "JOHN"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Not yet.")
        )
        val pageMap = blocks.associate { it.id to 1 }

        val analytics = ScreenplayAnalyticsEngine.analyzeLocations(blocks, pageMap)

        assertEquals(2, analytics.totalUniqueLocations)
        assertEquals(3, analytics.totalScenes)
        assertEquals(2, analytics.intScenesCount)
        assertEquals(1, analytics.extScenesCount)
        assertEquals(1, analytics.dayScenesCount)
        assertEquals(2, analytics.nightScenesCount)

        // DETECTIVE OFFICE should be rank 1 with 2 scenes
        val office = analytics.locationBreakdowns.find { it.locationName == "DETECTIVE OFFICE" }
        assertNotNull(office)
        assertEquals(2, office!!.totalScenes)
        assertEquals(SettingType.INTERIOR, office.primarySetting)
        assertEquals(1, office.dayScenes)
        assertEquals(1, office.nightScenes)
        assertEquals(listOf("JOHN", "SARAH"), office.uniqueCharacters)

        // HARBOR should be rank 2 with 1 scene
        val harbor = analytics.locationBreakdowns.find { it.locationName == "HARBOR" }
        assertNotNull(harbor)
        assertEquals(1, harbor!!.totalScenes)
        assertEquals(SettingType.EXTERIOR, harbor.primarySetting)
        assertEquals(1, harbor.nightScenes)
        assertEquals(listOf("JOHN"), harbor.uniqueCharacters)

        val reportText = ScreenplayAnalyticsEngine.formatLocationReportText(analytics, "MY NOIR")
        assertTrue(reportText.contains("LOCATION & SET BREAKDOWN REPORT"))
        assertTrue(reportText.contains("DETECTIVE OFFICE"))
        assertTrue(reportText.contains("HARBOR"))
    }

    @Test
    fun testCastSceneMatrixGeneration() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. BANK - DAY", sceneNumber = "1"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "ROBBER"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Hands up!"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "TELLER"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Don't shoot!"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. GETAWAY CAR - CONTINUOUS", sceneNumber = "2"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "DRIVER"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Get in!"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "ROBBER"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Drive!")
        )
        val pageMap = blocks.associate { it.id to 1 }

        val matrix = ScreenplayAnalyticsEngine.buildCastSceneMatrix(blocks, pageMap)

        assertEquals(3, matrix.allCharacters.size)
        // ROBBER appears in 2 scenes -> rank 1
        assertEquals("ROBBER", matrix.allCharacters[0])
        assertEquals(2, matrix.totalScenesByCharacter["ROBBER"])
        assertEquals(listOf("1", "2"), matrix.appearancesByCharacter["ROBBER"])

        // TELLER appears in scene 1
        assertEquals(1, matrix.totalScenesByCharacter["TELLER"])
        assertEquals(listOf("1"), matrix.appearancesByCharacter["TELLER"])

        // DRIVER appears in scene 2
        assertEquals(1, matrix.totalScenesByCharacter["DRIVER"])
        assertEquals(listOf("2"), matrix.appearancesByCharacter["DRIVER"])

        val matrixText = ScreenplayAnalyticsEngine.formatCastMatrixText(matrix, "THE HEIST")
        assertTrue(matrixText.contains("CAST-SCENE BREAKDOWN MATRIX"))
        assertTrue(matrixText.contains("ROBBER"))
        assertTrue(matrixText.contains("TELLER"))
        assertTrue(matrixText.contains("DRIVER"))
    }

    @Test
    fun testOmittedSceneInCastMatrix() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. APARTMENT - DAY", sceneNumber = "1"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "ALICE"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Good morning."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. PARK - DAY", sceneNumber = "2", isOmitted = true),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "BOB", isOmitted = true),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "Running late.", isOmitted = true),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. CAFE - DAY", sceneNumber = "3"),
            ScreenplayBlock(type = BlockType.CHARACTER, text = "ALICE"),
            ScreenplayBlock(type = BlockType.DIALOGUE, text = "One coffee please.")
        )
        val pageMap = blocks.associate { it.id to 1 }

        val matrix = ScreenplayAnalyticsEngine.buildCastSceneMatrix(blocks, pageMap)

        assertEquals(3, matrix.scenes.size)
        assertFalse(matrix.scenes[0].isOmitted)
        assertTrue(matrix.scenes[1].isOmitted)
        assertEquals("OMITTED", matrix.scenes[1].sceneHeading)
        assertTrue(matrix.scenes[1].characters.isEmpty()) // omitted characters are not on call sheet
        assertFalse(matrix.scenes[2].isOmitted)

        // BOB was only in the omitted scene, so BOB is not in allCharacters
        assertFalse(matrix.allCharacters.contains("BOB"))
        assertTrue(matrix.allCharacters.contains("ALICE"))
    }
}
