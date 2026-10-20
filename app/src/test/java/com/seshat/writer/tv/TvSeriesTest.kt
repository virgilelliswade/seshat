package com.seshat.writer.tv

import com.seshat.writer.bible.StoryBible
import com.seshat.writer.bundle.SeshatBundleManager
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.RevisionColor
import com.seshat.writer.model.ScreenplayBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TvSeriesTest {

    @Test
    fun testTvEpisodeJsonRoundtrip() {
        val blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. WRITERS ROOM - DAY"),
            ScreenplayBlock(type = BlockType.ACTION, text = "Whiteboards line the mahogany walls.")
        )
        val corkboard = CorkboardDocument(
            scriptTitle = "Ep 101: Pilot",
            actCount = 5,
            acts = CorkboardDocument.actsForTvHourlong()
        )
        val episode = TvEpisode(
            episodeNumber = "101",
            title = "Pilot",
            writerCredit = "Jane Doe",
            synopsis = "Series premiere introducing the world.",
            blocks = blocks,
            corkboard = corkboard,
            revisionColor = RevisionColor.BLUE,
            draftTag = "Blue Draft",
            sceneCount = 1,
            pageCount = 1
        )

        val json = episode.toJson()
        val restored = TvEpisode.fromJson(json)

        assertEquals("101", restored.episodeNumber)
        assertEquals("Pilot", restored.title)
        assertEquals("Jane Doe", restored.writerCredit)
        assertEquals(2, restored.blocks.size)
        assertEquals("INT. WRITERS ROOM - DAY", restored.blocks[0].text)
        assertEquals(RevisionColor.BLUE, restored.revisionColor)
        assertEquals(5, restored.corkboard.actCount)
    }

    @Test
    fun testTvSeriesProjectDefaultCreation() {
        val dramaSeries = TvSeriesProject.createDefault(
            title = "CHRONICLES OF NIBIRU",
            showrunner = "Jane Doe",
            format = ScreenplayType.TV_HOUR_DRAMA,
            pilotTitle = "The Arrival"
        )

        assertEquals("CHRONICLES OF NIBIRU", dramaSeries.title)
        assertEquals("Jane Doe", dramaSeries.showrunner)
        assertEquals(ScreenplayType.TV_HOUR_DRAMA, dramaSeries.format)
        assertEquals(1, dramaSeries.episodes.size)

        val pilot = dramaSeries.episodes[0]
        assertEquals("101", pilot.episodeNumber)
        assertEquals("The Arrival", pilot.title)
        // 1-Hour drama default acts: 5 acts (or 5 acts + teaser)
        assertTrue(pilot.corkboard.actCount >= 5)

        // Unified Lore Bible should be present and title should match
        assertEquals("CHRONICLES OF NIBIRU", dramaSeries.seriesBible.scriptTitle)
    }

    @Test
    fun testTvSeriesComedyActs() {
        val comedySeries = TvSeriesProject.createDefault(
            title = "OFFICE HOURS",
            showrunner = "John Smith",
            format = ScreenplayType.TV_HALF_HOUR_COMEDY,
            pilotTitle = "Orientation"
        )

        assertEquals(ScreenplayType.TV_HALF_HOUR_COMEDY, comedySeries.format)
        val pilot = comedySeries.episodes[0]
        // 30-min comedy acts
        assertTrue(pilot.corkboard.actCount in 3..4)
    }

    @Test
    fun testTvSeriesProjectJsonRoundtrip() {
        val series = TvSeriesProject.createDefault(
            title = "SUCCESSION CLONE",
            showrunner = "Logan",
            format = ScreenplayType.TV_HOUR_DRAMA
        )
        val ep2 = TvEpisode(
            episodeNumber = "102",
            title = "The Boardroom Battle",
            writerCredit = "Kendall",
            blocks = listOf(
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. WAYSTAR BOARDROOM - DAY")
            )
        )
        val updatedSeries = series.copy(
            episodes = series.episodes + ep2,
            activeEpisodeIndex = 1
        )

        val json = updatedSeries.toJson()
        val restored = TvSeriesProject.fromJson(json)

        assertEquals("SUCCESSION CLONE", restored.title)
        assertEquals("Logan", restored.showrunner)
        assertEquals(2, restored.episodes.size)
        assertEquals(1, restored.activeEpisodeIndex)
        assertEquals("102", restored.episodes[1].episodeNumber)
        assertEquals("The Boardroom Battle", restored.episodes[1].title)
    }

    @Test
    fun testSeshatBundleManagerExportAndImport() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "seshat_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        try {
            val series = TvSeriesProject.createDefault(
                title = "BUNDLE TEST SHOW",
                showrunner = "Alex",
                format = ScreenplayType.TV_HOUR_DRAMA,
                pilotTitle = "Genesis"
            )
            val bundleFile = File(tempDir, "test_show.seshat")

            // Test export
            val exported = SeshatBundleManager.exportTvSeriesBundle(
                project = series,
                outputFile = bundleFile
            )

            assertNotNull(exported)
            assertTrue(bundleFile.exists())
            assertTrue(bundleFile.length() > 0)

            // Test import
            val imported = SeshatBundleManager.importTvSeriesBundle(bundleFile)
            assertNotNull(imported)
            assertEquals("BUNDLE TEST SHOW", imported!!.title)
            assertEquals("Alex", imported.showrunner)
            assertEquals(1, imported.episodes.size)
            assertEquals("Genesis", imported.episodes[0].title)
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
