package com.seshat.writer.novel

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

class NovelTest {

    @Test
    fun testNovelParagraphWordCount() {
        val p1 = NovelParagraph(text = "The quick brown fox jumps over the lazy dog.")
        assertEquals(9, p1.wordCount())

        val pEmpty = NovelParagraph(text = "   ")
        assertEquals(0, pEmpty.wordCount())

        val pHeader = NovelParagraph(
            type = NovelParagraphType.CHAPTER_HEADER,
            text = "CHAPTER ONE: THE BEGINNING"
        )
        assertEquals(4, pHeader.wordCount())
    }

    @Test
    fun testNovelChapterWordCount() {
        val ch = NovelChapter(
            chapterNumber = 1,
            title = "The Journey Begins",
            paragraphs = listOf(
                NovelParagraph(type = NovelParagraphType.CHAPTER_HEADER, text = "CHAPTER 1"),
                NovelParagraph(type = NovelParagraphType.NARRATIVE, text = "It was the best of times."),
                NovelParagraph(type = NovelParagraphType.DIALOGUE, text = "“Indeed it was,” he said.")
            )
        )
        // 2 + 6 + 5 = 13 words
        assertEquals(13, ch.wordCount())
    }

    @Test
    fun testNovelManuscriptCalculations() {
        val ch1 = NovelChapter(
            chapterNumber = 1,
            title = "Chapter 1",
            paragraphs = (1..50).map {
                NovelParagraph(text = "Word word word word word word word word word word.") // 10 words each
            }
        ) // 500 words

        val ch2 = NovelChapter(
            chapterNumber = 2,
            title = "Chapter 2",
            paragraphs = (1..50).map {
                NovelParagraph(text = "Another word another word another word another word here.") // 9 words each
            }
        ) // 450 words

        val manuscript = NovelManuscript(
            title = "Great Odyssey",
            author = "Seshat",
            chapters = listOf(ch1, ch2)
        )

        assertEquals(950, manuscript.totalWordCount())
        assertEquals(4, manuscript.estimatedPages()) // 950 / 250 = 3.8 -> 4 pages
        assertEquals(5, manuscript.estimatedReadingMinutes()) // 950 / 225 = 4.22 -> 5 mins
    }

    @Test
    fun testNovelManuscriptJsonRoundTrip() {
        val original = NovelManuscript(
            title = "Starbound Chronicles",
            author = "Alex Mercer",
            genre = "Sci-Fi",
            chapters = listOf(
                NovelChapter(
                    id = "ch-101",
                    chapterNumber = 1,
                    title = "The Nebula Incident",
                    synopsis = "An unexpected warp flare.",
                    status = ChapterStatus.FIRST_DRAFT,
                    paragraphs = listOf(
                        NovelParagraph(type = NovelParagraphType.CHAPTER_HEADER, text = "CHAPTER 1"),
                        NovelParagraph(type = NovelParagraphType.NARRATIVE, text = "Cold stars pierced the darkness."),
                        NovelParagraph(type = NovelParagraphType.DIALOGUE, text = "“Shields at maximum!”")
                    )
                )
            )
        )

        val json = original.toJson().toString()
        assertNotNull(json)
        assertTrue(json.contains("Starbound Chronicles"))
        assertTrue(json.contains("The Nebula Incident"))

        val loaded = NovelManuscript.fromJson(JSONObject(json))
        assertEquals(original.title, loaded.title)
        assertEquals(original.author, loaded.author)
        assertEquals(original.genre, loaded.genre)
        assertEquals(1, loaded.chapters.size)
        assertEquals("The Nebula Incident", loaded.chapters[0].title)
        assertEquals(ChapterStatus.FIRST_DRAFT, loaded.chapters[0].status)
        assertEquals(3, loaded.chapters[0].paragraphs.size)
    }

    @Test
    fun testDialogueVerbSuggesterForPunctuation() {
        // Question mark verbs
        val qVerbs = DialogueVerbSuggester.getVerbsForPunctuation('?')
        assertTrue(qVerbs.contains("asked"))
        assertTrue(qVerbs.contains("inquired"))
        assertTrue(qVerbs.contains("demanded"))

        // Exclamation mark verbs
        val eVerbs = DialogueVerbSuggester.getVerbsForPunctuation('!')
        assertTrue(eVerbs.contains("exclaimed"))
        assertTrue(eVerbs.contains("shouted"))
        assertTrue(eVerbs.contains("cried"))

        // Period verbs
        val pVerbs = DialogueVerbSuggester.getVerbsForPunctuation('.')
        assertTrue(pVerbs.contains("said"))
        assertTrue(pVerbs.contains("replied"))
        assertTrue(pVerbs.contains("murmured"))

        // Comma verbs
        val cVerbs = DialogueVerbSuggester.getVerbsForPunctuation(',')
        assertTrue(cVerbs.contains("whispered"))
        assertTrue(cVerbs.contains("muttered"))
        assertTrue(cVerbs.contains("continued"))

        // Dash verbs
        val dVerbs = DialogueVerbSuggester.getVerbsForPunctuation('—')
        assertTrue(dVerbs.contains("interrupted"))
        assertTrue(dVerbs.contains("stammered"))
        assertTrue(dVerbs.contains("trailed off"))
    }

    @Test
    fun testDialogueVerbSuggesterDetection() {
        assertEquals('!', DialogueVerbSuggester.detectTerminalQuotePunctuation("“Run for your lives!”"))
        assertEquals('?', DialogueVerbSuggester.detectTerminalQuotePunctuation("“Where are you going?”"))
        assertEquals('.', DialogueVerbSuggester.detectTerminalQuotePunctuation("“I understand.”"))
        assertEquals(',', DialogueVerbSuggester.detectTerminalQuotePunctuation("“Take this,”"))
        assertEquals('—', DialogueVerbSuggester.detectTerminalQuotePunctuation("“Wait, don't—”"))
        assertEquals('—', DialogueVerbSuggester.detectTerminalQuotePunctuation("“Unless--”"))
    }

    @Test
    fun testShunnManuscriptCompiler() {
        val chapter = NovelChapter(
            chapterNumber = 1,
            title = "A Dark Awakening",
            paragraphs = listOf(
                NovelParagraph(type = NovelParagraphType.CHAPTER_HEADER, text = "CHAPTER 1"),
                NovelParagraph(type = NovelParagraphType.NARRATIVE, text = "The rain beat hard against the window panes."),
                NovelParagraph(type = NovelParagraphType.SCENE_BREAK, text = "* * *"),
                NovelParagraph(type = NovelParagraphType.DIALOGUE, text = "“Who goes there?” he challenged.")
            )
        )
        val manuscript = NovelManuscript(
            title = "The Obsidian Key",
            author = "Jane Doe",
            chapters = listOf(chapter)
        )

        val shunnText = ShunnCompiler.compileToShunnFormat(manuscript, CompilerOptions())
        assertTrue(shunnText.contains("Jane Doe"))
        assertTrue(shunnText.contains("THE OBSIDIAN KEY"))
        assertTrue(shunnText.contains("The rain beat hard against the window panes."))
        assertTrue(shunnText.contains("#")) // Shunn format scene break

        val markdownText = ShunnCompiler.compileToMarkdown(manuscript, CompilerOptions())
        assertTrue(markdownText.contains("# THE OBSIDIAN KEY"))
        assertTrue(markdownText.contains("CHAPTER 1"))
        assertTrue(markdownText.contains("* * *")) // Markdown scene break

        val htmlText = ShunnCompiler.compileToHtml(manuscript, CompilerOptions())
        assertTrue(htmlText.contains("<!DOCTYPE html>"))
        assertTrue(htmlText.contains("<h1 class=\"title\">The Obsidian Key</h1>"))
    }

    @Test
    fun testEpubCompilerGeneratesValidZip() {
        val tempFile = File.createTempFile("test_novel_", ".epub")
        tempFile.deleteOnExit()

        val chapter1 = NovelChapter(
            chapterNumber = 1,
            title = "Departure",
            paragraphs = listOf(
                NovelParagraph(type = NovelParagraphType.CHAPTER_HEADER, text = "CHAPTER 1"),
                NovelParagraph(type = NovelParagraphType.NARRATIVE, text = "The ship cast off into the mist."),
                NovelParagraph(type = NovelParagraphType.DIALOGUE, text = "“Safe travels,” they whispered.")
            )
        )
        val manuscript = NovelManuscript(
            title = "Voyage of the Dawn",
            author = "Arthur Pendelton",
            genre = "Fantasy",
            chapters = listOf(chapter1)
        )

        val success = EpubCompiler.compileToEpub(
            manuscript = manuscript,
            options = CompilerOptions(includeTableOfContents = true, includeTitlePage = true),
            outputFile = tempFile
        )

        assertTrue("compileToEpub must return true", success)
        assertTrue("Output file must exist", tempFile.exists())
        assertTrue("Output file must be non-empty", tempFile.length() > 0)

        // Validate contents of the EPUB ZIP package
        val zip = ZipFile(tempFile)
        val entries = zip.entries().asSequence().map { it.name }.toList()

        assertTrue("mimetype must be present", entries.contains("mimetype"))
        assertTrue("META-INF/container.xml must be present", entries.contains("META-INF/container.xml"))
        assertTrue("OEBPS/content.opf must be present", entries.contains("OEBPS/content.opf"))
        assertTrue("OEBPS/toc.ncx must be present", entries.contains("OEBPS/toc.ncx"))
        assertTrue("OEBPS/nav.xhtml must be present", entries.contains("OEBPS/nav.xhtml"))
        assertTrue("OEBPS/style.css must be present", entries.contains("OEBPS/style.css"))
        assertTrue("OEBPS/chapter_1.xhtml must be present", entries.contains("OEBPS/chapter_1.xhtml"))

        // Verify mimetype is stored correctly
        val mimeEntry = zip.getEntry("mimetype")
        assertNotNull(mimeEntry)
        val mimeContent = zip.getInputStream(mimeEntry).bufferedReader().readText()
        assertEquals("application/epub+zip", mimeContent.trim())

        zip.close()
    }

    @Test
    fun testCorkboardSynchronization() {
        val chapter1 = NovelChapter(
            id = "chap_1",
            chapterNumber = 1,
            title = "The Call",
            synopsis = "Hero receives the strange missive.",
            actIndex = 0
        )
        val chapter2 = NovelChapter(
            id = "chap_2",
            chapterNumber = 2,
            title = "Crossing the Threshold",
            synopsis = "Hero leaves home.",
            actIndex = 1
        )
        val manuscript = NovelManuscript(
            title = "Hero's Journey",
            chapters = listOf(chapter1, chapter2)
        )

        val cards = NovelRepository.chaptersToSceneCards(manuscript)
        assertEquals(2, cards.size)
        assertEquals("chap_1", cards[0].sceneHeadingBlockId)
        assertEquals("THE CALL", cards[0].headingText)
        assertEquals("Hero receives the strange missive.", cards[0].summary)
        assertEquals(0, cards[0].actIndex)

        assertEquals("chap_2", cards[1].sceneHeadingBlockId)
        assertEquals("CROSSING THE THRESHOLD", cards[1].headingText)
        assertEquals(1, cards[1].actIndex)
    }

    @Test
    fun testBlankNovelChapterInitialization() {
        val blankParagraphs = NovelChapter.defaultParagraphs(1)
        assertEquals(1, blankParagraphs.size)
        assertEquals(NovelParagraphType.NARRATIVE, blankParagraphs[0].type)
        assertEquals("", blankParagraphs[0].text)

        val defaultChs = NovelManuscript.defaultChapters()
        assertEquals(1, defaultChs.size)
        assertEquals(1, defaultChs[0].chapterNumber)
        assertEquals("Chapter 1", defaultChs[0].title)
        assertEquals("", defaultChs[0].synopsis)
        assertEquals(1, defaultChs[0].paragraphs.size)
        assertEquals(NovelParagraphType.NARRATIVE, defaultChs[0].paragraphs[0].type)
        assertEquals("", defaultChs[0].paragraphs[0].text)
        assertEquals(0, defaultChs[0].wordCount())
    }

    @Test
    fun testChapterHeaderTriggerDetection() {
        assertTrue(NovelChapter.isChapterHeaderTrigger("Chapter 1"))
        assertTrue(NovelChapter.isChapterHeaderTrigger("CHAPTER 2"))
        assertTrue(NovelChapter.isChapterHeaderTrigger("Chapter One: The Awakening"))
        assertTrue(NovelChapter.isChapterHeaderTrigger("Prologue"))
        assertTrue(NovelChapter.isChapterHeaderTrigger("EPILOGUE"))
        assertTrue(NovelChapter.isChapterHeaderTrigger("Part 1"))
        assertTrue(NovelChapter.isChapterHeaderTrigger("Act I"))

        assertFalse(NovelChapter.isChapterHeaderTrigger("The morning was cold and grey."))
        assertFalse(NovelChapter.isChapterHeaderTrigger("He walked into the chapter room."))
        assertFalse(NovelChapter.isChapterHeaderTrigger("“Chapter meetings are on Mondays,” she said."))
    }

    @Test
    fun testCompilerFormatIncludesPdfManuscript() {
        val formats = CompilerFormat.values()
        assertTrue(formats.contains(CompilerFormat.EPUB))
        assertTrue(formats.contains(CompilerFormat.PDF_MANUSCRIPT))
        assertEquals("pdf", CompilerFormat.PDF_MANUSCRIPT.extension)
        assertEquals("Standard Manuscript PDF (.pdf)", CompilerFormat.PDF_MANUSCRIPT.displayName)
    }

    @Test
    fun testNovelCoverDesignSpineCalculation() {
        val design = NovelCoverDesign(
            paperType = PaperType.CREAM_55LB,
            trimWidthInches = 6.0f,
            flapWidthInches = 3.25f
        )
        // 300 pages * 0.0025 = 0.75"
        val spine = design.calculateSpineWidthInches(300)
        assertEquals(0.75f, spine, 0.001f)

        // Total wrap = (3.25 * 2) + (6.0 * 2) + 0.75 + 0.25 = 6.5 + 12 + 0.75 + 0.25 = 19.5"
        val totalWrap = design.calculateTotalWrapWidthInches(300)
        assertEquals(19.5f, totalWrap, 0.001f)
    }

    @Test
    fun testNovelCoverDesignJsonRoundTrip() {
        val original = NovelCoverDesign(
            title = "THE OBSIDIAN ARCHIVE",
            subtitle = "Book One of Starlight",
            author = "Alex Mercer",
            tagline = "The stars are dying.",
            backCoverBlurb = "An epic quest across a forgotten galaxy.",
            praiseQuote = "“Spectacular.” — Review",
            frontFlapTeaser = "A hidden relic awakens.",
            backFlapAuthorBio = "Alex Mercer writes speculative fiction.",
            isbn = "978-0-123456-78-9",
            themePreset = CoverThemePreset.NEON_SCARLET,
            paperType = PaperType.WHITE_50LB
        )

        val json = original.toJson()
        val loaded = NovelCoverDesign.fromJson(json)

        assertEquals(original.title, loaded.title)
        assertEquals(original.subtitle, loaded.subtitle)
        assertEquals(original.author, loaded.author)
        assertEquals(original.tagline, loaded.tagline)
        assertEquals(original.backCoverBlurb, loaded.backCoverBlurb)
        assertEquals(original.praiseQuote, loaded.praiseQuote)
        assertEquals(original.frontFlapTeaser, loaded.frontFlapTeaser)
        assertEquals(original.backFlapAuthorBio, loaded.backFlapAuthorBio)
        assertEquals(original.isbn, loaded.isbn)
        assertEquals(CoverThemePreset.NEON_SCARLET, loaded.themePreset)
        assertEquals(PaperType.WHITE_50LB, loaded.paperType)
    }

    @Test
    fun testNovelCoverDesignCustomArtworkJsonRoundTrip() {
        val original = NovelCoverDesign(
            title = "THE SHADOWED VAULT",
            customArtworkPath = "/storage/emulated/10/Documents/Writing App/covers/imported_cover_12345.jpg"
        )
        val json = original.toJson()
        val loaded = NovelCoverDesign.fromJson(json)

        assertEquals("/storage/emulated/10/Documents/Writing App/covers/imported_cover_12345.jpg", loaded.customArtworkPath)

        val cleared = loaded.copy(customArtworkPath = null)
        val clearedJson = cleared.toJson()
        val reloaded = NovelCoverDesign.fromJson(clearedJson)
        assertEquals(null, reloaded.customArtworkPath)
    }
}
