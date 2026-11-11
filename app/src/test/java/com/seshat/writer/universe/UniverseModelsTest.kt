package com.seshat.writer.universe

import com.seshat.writer.bible.LoreDocument
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UniverseModelsTest {

    @Test
    fun defaultAssignmentBuildsTailoredDefaultsForAllFiveMediums() {
        val film = BlendedAssignment.defaultAssignment("Feature 1", ProjectFormat.SCREENPLAY, ScreenplayType.FEATURE)
        val tv = BlendedAssignment.defaultAssignment("Pilot 101", ProjectFormat.TV_SERIES, ScreenplayType.FEATURE)
        val novel = BlendedAssignment.defaultAssignment("Book One", ProjectFormat.NOVEL)
        val comic = BlendedAssignment.defaultAssignment("Issue #1", ProjectFormat.COMIC_BOOK)
        val stage = BlendedAssignment.defaultAssignment("Stage One", ProjectFormat.STAGE_PLAY)

        assertEquals("🎬 FILM", film.mediumBadge())
        assertEquals(ScreenplayType.FEATURE, film.screenplayType)
        assertEquals(BlockType.SCENE_HEADING, film.blocks.first().type)

        assertEquals("📺 TV", tv.mediumBadge())
        assertEquals(ScreenplayType.TV_HOUR_DRAMA, tv.screenplayType)

        assertEquals("📖 NOVEL", novel.mediumBadge())
        assertTrue(novel.novelManuscript.chapters.isNotEmpty())

        assertEquals("💥 COMIC", comic.mediumBadge())
        assertEquals(BlockType.COMIC_PAGE, comic.blocks.first().type)

        assertEquals("🎭 STAGE", stage.mediumBadge())
        assertEquals(BlockType.STAGE_ACT_SCENE, stage.blocks.first().type)
    }

    @Test
    fun blendedUniverseProjectJsonRoundTripPreservesAllMediumsAndActiveIndex() {
        val assignments = listOf(
            BlendedAssignment.defaultAssignment("Origins Film", ProjectFormat.SCREENPLAY, ScreenplayType.FEATURE)
                .copy(
                    blocks = listOf(
                        ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. CITADEL - NIGHT", sceneNumber = "1")
                    )
                ),
            BlendedAssignment.defaultAssignment("Spin-Off Series", ProjectFormat.TV_SERIES, ScreenplayType.TV_HALF_HOUR_COMEDY),
            BlendedAssignment.defaultAssignment("Companion Novel", ProjectFormat.NOVEL),
            BlendedAssignment.defaultAssignment("Prequel Comic", ProjectFormat.COMIC_BOOK),
            BlendedAssignment.defaultAssignment("Chamber Play", ProjectFormat.STAGE_PLAY)
        )

        val project = BlendedUniverseProject(
            title = "CHRONICLE UNIVERSE",
            universeBible = StoryBible(
                scriptTitle = "CHRONICLE UNIVERSE",
                author = "SESHAT WRITER",
                lore = LoreDocument(cosmology = "Shared continuity")
            ),
            assignments = assignments,
            activeAssignmentIndex = 2
        )

        val restored = BlendedUniverseProject.fromJson(project.toJson())

        assertEquals("CHRONICLE UNIVERSE", restored.title)
        assertEquals("SESHAT WRITER", restored.universeBible.author)
        assertEquals("Shared continuity", restored.universeBible.lore.cosmology)
        assertEquals(5, restored.assignments.size)
        assertEquals(2, restored.activeAssignmentIndex)
        assertEquals(ProjectFormat.NOVEL, restored.getActiveAssignment()?.format)
        assertEquals("Companion Novel", restored.getActiveAssignment()?.title)
        assertEquals("EXT. CITADEL - NIGHT", restored.assignments[0].blocks.first().text)
        assertEquals(ScreenplayType.TV_HALF_HOUR_COMEDY, restored.assignments[1].screenplayType)
        assertEquals(ProjectFormat.COMIC_BOOK, restored.assignments[3].format)
        assertEquals(ProjectFormat.STAGE_PLAY, restored.assignments[4].format)
    }

    @Test
    fun getActiveAssignmentHandlesEmptyAndOutOfBoundsIndices() {
        val empty = BlendedUniverseProject(assignments = emptyList(), activeAssignmentIndex = 3)
        assertNull(empty.getActiveAssignment())

        val single = BlendedUniverseProject(
            assignments = listOf(BlendedAssignment.defaultAssignment("Only", ProjectFormat.NOVEL)),
            activeAssignmentIndex = 99
        )
        val active = single.getActiveAssignment()
        assertNotNull(active)
        assertEquals("Only", active?.title)
    }
}
