package com.seshat.writer.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ScriptLibraryManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var scriptsDir: File

    @Before
    fun setUp() {
        scriptsDir = tempFolder.newFolder("scripts")
    }

    @Test
    fun createProjectFromTemplateCreatesValidFile() {
        val file = ScriptLibraryManager.createNewProjectInDir(
            directory = scriptsDir,
            title = "THE GODFATHER",
            author = "Mario Puzo & Francis Ford Coppola",
            template = ScriptTemplate.FEATURE_FILM
        )

        assertNotNull(file)
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
        val content = file.readText()
        assertTrue(content.contains("Title: THE GODFATHER"))
        assertTrue(content.contains("Author: Mario Puzo & Francis Ford Coppola"))
        val imported = com.seshat.writer.importer.ScriptImporter.importFromFile(file)
        assertTrue(imported.isSuccess)
        assertNotNull(imported.document)
        assertEquals(1, imported.document!!.blocks.size)
        assertEquals(com.seshat.writer.model.BlockType.SCENE_HEADING, imported.document!!.blocks[0].type)
        assertEquals("", imported.document!!.blocks[0].text)
    }

    @Test
    fun listProjectsReturnsSortedList() {
        ScriptLibraryManager.createNewProjectInDir(
            directory = scriptsDir,
            title = "SCRIPT A",
            author = "Writer A",
            template = ScriptTemplate.BLANK
        )
        Thread.sleep(20)
        ScriptLibraryManager.createNewProjectInDir(
            directory = scriptsDir,
            title = "SCRIPT B",
            author = "Writer B",
            template = ScriptTemplate.ONE_HOUR_TV
        )

        val list = ScriptLibraryManager.listProjectsInDir(scriptsDir)
        assertEquals(2, list.size)
        // Most recent first
        assertEquals("SCRIPT B", list[0].title)
        assertEquals("SCRIPT A", list[1].title)
    }

    @Test
    fun duplicateProjectCreatesExactCopy() {
        val sourceFile = ScriptLibraryManager.createNewProjectInDir(
            directory = scriptsDir,
            title = "CHINATOWN",
            author = "Robert Towne",
            template = ScriptTemplate.FEATURE_FILM
        )

        val duplicateFile = ScriptLibraryManager.duplicateProjectInDir(
            directory = scriptsDir,
            sourceFile = sourceFile,
            newTitle = "CHINATOWN_DRAFT_2"
        )

        assertNotNull(duplicateFile)
        assertTrue(duplicateFile.exists())
        assertTrue(duplicateFile.name.contains("CHINATOWN_DRAFT_2"))
        assertTrue(duplicateFile.readText().contains("Title: CHINATOWN_DRAFT_2"))
        val imported = com.seshat.writer.importer.ScriptImporter.importFromFile(duplicateFile)
        assertTrue(imported.isSuccess)
        assertEquals(1, imported.document!!.blocks.size)
        assertEquals("", imported.document!!.blocks[0].text)
    }

    @Test
    fun renameProjectRenamesFileAndTitleMetadata() {
        val originalFile = ScriptLibraryManager.createNewProjectInDir(
            directory = scriptsDir,
            title = "WORKING_TITLE",
            author = "Jane Doe",
            template = ScriptTemplate.BLANK
        )

        val renamedFile = ScriptLibraryManager.renameProjectInDir(
            directory = scriptsDir,
            file = originalFile,
            newTitle = "FINAL_TITLE"
        )

        assertNotNull(renamedFile)
        assertTrue(renamedFile.exists())
        assertFalse(originalFile.exists())
        assertTrue(renamedFile.readText().contains("Title: FINAL_TITLE"))
    }

    @Test
    fun deleteProjectRemovesFile() {
        val file = ScriptLibraryManager.createNewProjectInDir(
            directory = scriptsDir,
            title = "DISCARDED_SCRIPT",
            author = "Anonymous",
            template = ScriptTemplate.BLANK
        )
        assertTrue(file.exists())

        val deleted = ScriptLibraryManager.deleteProject(file)
        assertTrue(deleted)
        assertFalse(file.exists())
    }
}
