package com.seshat.writer.templates

import androidx.compose.ui.graphics.Color
import com.seshat.writer.bible.StoryBeat
import com.seshat.writer.corkboard.CorkboardAct
import com.seshat.writer.corkboard.CorkboardDocument
import com.seshat.writer.library.ProjectFormat
import com.seshat.writer.library.ScreenplayType
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.novel.NovelChapter
import com.seshat.writer.novel.NovelParagraph
import com.seshat.writer.novel.NovelParagraphType
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.ScribeStarNeonRed

/**
 * Universal project starter template definition for all Seshat mediums.
 */
data class ProjectTemplate(
    val id: String,
    val format: ProjectFormat,
    val screenplayType: ScreenplayType? = null,
    val name: String,
    val subtitle: String,
    val description: String,
    val badgeIcon: String,
    val badgeColor: Color,
    val isBlank: Boolean = false,
    val blocks: List<ScreenplayBlock> = emptyList(),
    val acts: List<CorkboardAct> = emptyList(),
    val storyBibleBeats: List<StoryBeat> = emptyList(),
    val novelChapters: List<NovelChapter> = emptyList()
)

object TemplateRegistry {

    // =========================================================================
    // FEATURE SCREENPLAY TEMPLATES
    // =========================================================================

    val BLANK_FEATURE = ProjectTemplate(
        id = "screenplay_blank",
        format = ProjectFormat.SCREENPLAY,
        screenplayType = ScreenplayType.FEATURE,
        name = "Blank Screenplay",
        subtitle = "Pristine blank page • Courier 12pt",
        description = "Start completely clean with a blank page and no demo content.",
        badgeIcon = "📄",
        badgeColor = ElectrumGold,
        isBlank = true,
        blocks = listOf(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")),
        acts = CorkboardDocument.actsForFeature()
    )

    val SAVE_THE_CAT_FEATURE = ProjectTemplate(
        id = "screenplay_save_the_cat",
        format = ProjectFormat.SCREENPLAY,
        screenplayType = ScreenplayType.FEATURE,
        name = "Save the Cat! Beat Sheet",
        subtitle = "Blake Snyder 15-Beat Commercial Structure",
        description = "The industry standard 15-beat blueprint with sequence headings and corkboard beats.",
        badgeIcon = "🐱",
        badgeColor = ScribeStarNeonRed,
        blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 01 - OPENING IMAGE - DAY"),
            ScreenplayBlock(type = BlockType.ACTION, text = "A snapshot of the protagonist's world before the adventure begins."),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 02 - THEME STATED - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 03 - SET-UP - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 04 - CATALYST - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 05 - DEBATE - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 06 - BREAK INTO TWO - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 07 - B STORY - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 08 - FUN AND GAMES - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 09 - MIDPOINT - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 10 - BAD GUYS CLOSE IN - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 11 - ALL IS LOST - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 12 - DARK NIGHT OF THE SOUL - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 13 - BREAK INTO THREE - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 14 - FINALE - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 15 - FINAL IMAGE - DAY")
        ),
        acts = CorkboardDocument.actsForFeature(),
        storyBibleBeats = listOf(
            StoryBeat(act = "Act I", title = "Opening Image", summary = "A visual statement of the starting state of the hero and world."),
            StoryBeat(act = "Act I", title = "Theme Stated", summary = "What the hero needs to learn (often spoken by an ally or mentor)."),
            StoryBeat(act = "Act I", title = "Set-Up", summary = "Establish the status quo, flaws, and the world of the hero."),
            StoryBeat(act = "Act I", title = "Catalyst", summary = "The inciting event that disrupts ordinary life permanently."),
            StoryBeat(act = "Act I", title = "Debate", summary = "The hero doubts, hesitates, or attempts to avoid the journey."),
            StoryBeat(act = "Act II-A", title = "Break into Two", summary = "The hero makes a deliberate choice to enter the upside-down world."),
            StoryBeat(act = "Act II-A", title = "B Story", summary = "The relationship or love story that carries the spiritual theme."),
            StoryBeat(act = "Act II-A", title = "Fun and Games", summary = "The promise of the premise—scenes that deliver the poster concept."),
            StoryBeat(act = "Act II-A", title = "Midpoint", summary = "False victory or false defeat; stakes escalate irreversibly."),
            StoryBeat(act = "Act II-B", title = "Bad Guys Close In", summary = "Internal doubt and external opposition mount relentlessly."),
            StoryBeat(act = "Act II-B", title = "All Is Lost", summary = "Whiff of death; lowest point where the old way of living dies."),
            StoryBeat(act = "Act II-B", title = "Dark Night of the Soul", summary = "The hero confronts rock bottom and synthesizes the theme."),
            StoryBeat(act = "Act III", title = "Break into Three", summary = "A new idea or revelation sparks the climactic solution."),
            StoryBeat(act = "Act III", title = "Finale", summary = "The climax where the hero applies the lesson and triumphs."),
            StoryBeat(act = "Act III", title = "Final Image", summary = "Opposite of the Opening Image; proves true transformation.")
        )
    )

    val HEROS_JOURNEY_FEATURE = ProjectTemplate(
        id = "screenplay_heros_journey",
        format = ProjectFormat.SCREENPLAY,
        screenplayType = ScreenplayType.FEATURE,
        name = "The Hero's Journey (Monomyth)",
        subtitle = "12-Stage Mythic Campbell & Vogler Structure",
        description = "The timeless storytelling archetypal arc from Ordinary World to Return with Elixir.",
        badgeIcon = "⚔️",
        badgeColor = Color(0xFFF59E0B),
        blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 01 - THE ORDINARY WORLD - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 02 - THE CALL TO ADVENTURE - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 03 - REFUSAL OF THE CALL - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 04 - MEETING THE MENTOR - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 05 - CROSSING THE FIRST THRESHOLD - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 06 - TESTS, ALLIES & ENEMIES - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 07 - APPROACH TO THE INMOST CAVE - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 08 - THE ORDEAL - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 09 - REWARD (SEIZING THE SWORD) - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 10 - THE ROAD BACK - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 11 - THE RESURRECTION - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 12 - RETURN WITH THE ELIXIR - DAY")
        ),
        acts = CorkboardDocument.actsForFeature()
    )

    val STANDARD_3ACT_FEATURE = ProjectTemplate(
        id = "screenplay_standard_3act",
        format = ProjectFormat.SCREENPLAY,
        screenplayType = ScreenplayType.FEATURE,
        name = "Standard 3-Act Structure",
        subtitle = "Syd Field Framework • Plot Points & Midpoint",
        description = "Clean three-act paradigm highlighting key turning points and dramatic reversals.",
        badgeIcon = "🎬",
        badgeColor = Color(0xFF38BDF8),
        blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 01 - STATUS QUO & SETUP - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 02 - INCITING INCIDENT - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 03 - PLOT POINT 1 (TURNING POINT) - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 04 - RISING CONFRONTATION - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 05 - THE MIDPOINT PIVOT - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 06 - PLOT POINT 2 (CRISIS) - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 07 - CLIMAX - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 08 - RESOLUTION & EPILOGUE - DAY")
        ),
        acts = CorkboardDocument.actsForFeature()
    )

    val SHORT_FILM_FEATURE = ProjectTemplate(
        id = "screenplay_short_film",
        format = ProjectFormat.SCREENPLAY,
        screenplayType = ScreenplayType.FEATURE,
        name = "Short Film (Festival Format)",
        subtitle = "3-Beat High-Impact Compact Structure (10-15 min)",
        description = "Optimized for festival submissions: concise hook, sudden complication, and unforgettable twist.",
        badgeIcon = "⏱️",
        badgeColor = Color(0xFF10B981),
        blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 01 - THE HOOK & DILEMMA - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 02 - THE COMPLICATION & PIVOT - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. 03 - THE TWIST & RESONANCE - DAY")
        ),
        acts = listOf(
            CorkboardAct(0, "The Hook", "Establish anomaly and desire"),
            CorkboardAct(1, "The Pivot", "Ticking clock and point of no return"),
            CorkboardAct(2, "The Twist", "Climax and moral consequence")
        )
    )

    // =========================================================================
    // TELEVISION SERIES TEMPLATES
    // =========================================================================

    val BLANK_TV = ProjectTemplate(
        id = "tv_blank",
        format = ProjectFormat.TV_SERIES,
        name = "Blank Television Episode",
        subtitle = "Pristine blank pilot • Episode Deck binder",
        description = "Start completely clean with a blank pilot page and episodic project deck.",
        badgeIcon = "📺",
        badgeColor = Color(0xFF3B82F6),
        isBlank = true,
        blocks = listOf(ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")),
        acts = CorkboardDocument.actsForTvHourlong()
    )

    val TV_1HOUR_DRAMA = ProjectTemplate(
        id = "tv_1hour_drama",
        format = ProjectFormat.TV_SERIES,
        screenplayType = ScreenplayType.TV_HOUR_DRAMA,
        name = "1-Hour Drama (Teaser + 5 Acts)",
        subtitle = "Prestige & Network Hourlong Dramatic Arc",
        description = "Structured for procedural or serialized 60-minute television with commercial breaks.",
        badgeIcon = "🎭",
        badgeColor = Color(0xFF3B82F6),
        blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. TEASER - THE COLD HOOK - NIGHT"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. ACT I - STATUS QUO DISRUPTED - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. ACT II - COMPLICATIONS & SUBPLOTS - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. ACT III - MIDPOINT CRISIS - NIGHT"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. ACT IV - ALL HOPE LOST - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. ACT V - CLIMAX & CLIFFHANGER - NIGHT")
        ),
        acts = CorkboardDocument.actsForTvHourlong()
    )

    val TV_HALF_HOUR_SITCOM = ProjectTemplate(
        id = "tv_half_hour_sitcom",
        format = ProjectFormat.TV_SERIES,
        screenplayType = ScreenplayType.TV_HALF_HOUR_COMEDY,
        name = "30-Min Sitcom (Cold Open + 2 Acts + Tag)",
        subtitle = "Comedy Show Pacing • Rapid Punchlines & Farce",
        description = "Standard half-hour comedy structure with A/B story intertwining and closing tag.",
        badgeIcon = "🛋️",
        badgeColor = Color(0xFFEC4899),
        blocks = listOf(
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. COLD OPEN - INCITING GAG - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. ACT I - COMEDIC PREMISE & GOAL - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. ACT II - ESCALATING FARCE & CRISIS - DAY"),
            ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. TAG - CLOSING BUTTON - NIGHT")
        ),
        acts = CorkboardDocument.actsForTvHalfHour()
    )

    // =========================================================================
    // NOVEL TEMPLATES
    // =========================================================================

    val BLANK_NOVEL = ProjectTemplate(
        id = "novel_blank",
        format = ProjectFormat.NOVEL,
        name = "Blank Novel",
        subtitle = "Pristine blank manuscript • Chapter 1",
        description = "Start completely clean with a blank chapter prose page and binder.",
        badgeIcon = "📖",
        badgeColor = Color(0xFF8B5CF6),
        isBlank = true,
        novelChapters = listOf(
            NovelChapter(
                chapterNumber = 1,
                title = "Chapter 1",
                synopsis = "",
                actIndex = 0,
                paragraphs = listOf(NovelParagraph(type = NovelParagraphType.NARRATIVE, text = ""))
            )
        ),
        acts = CorkboardDocument.actsForNovel()
    )

    val NOVEL_3ACT_12CHAPTER = ProjectTemplate(
        id = "novel_3act_12ch",
        format = ProjectFormat.NOVEL,
        name = "Classic 3-Act Structure (12 Chapters)",
        subtitle = "3 Parts • Scrivener-Style Chapter Binder",
        description = "Part I: Departure (Ch 1-3), Part II: Initiation (Ch 4-9), Part III: Return (Ch 10-12).",
        badgeIcon = "📚",
        badgeColor = Color(0xFF8B5CF6),
        novelChapters = (1..12).map { chNum ->
            val actIdx = when (chNum) {
                in 1..3 -> 0
                in 4..9 -> 1
                else -> 2
            }
            val title = when (chNum) {
                1 -> "Chapter 1: The Ordinary World"
                2 -> "Chapter 2: The Inciting Disturbance"
                3 -> "Chapter 3: Crossing the Threshold"
                4 -> "Chapter 4: The New Arena & Allies"
                5 -> "Chapter 5: Rising Complications"
                6 -> "Chapter 6: The Midpoint Shift"
                7 -> "Chapter 7: Bad Guys Close In"
                8 -> "Chapter 8: The Shatering Crisis"
                9 -> "Chapter 9: Dark Night of the Soul"
                10 -> "Chapter 10: The Reawakening"
                11 -> "Chapter 11: The Climax"
                12 -> "Chapter 12: The New Equilibrium"
                else -> "Chapter $chNum"
            }
            NovelChapter(
                chapterNumber = chNum,
                title = title,
                actIndex = actIdx,
                paragraphs = listOf(NovelParagraph(type = NovelParagraphType.NARRATIVE, text = ""))
            )
        },
        acts = CorkboardDocument.actsForNovel()
    )

    val NOVEL_HEROS_JOURNEY = ProjectTemplate(
        id = "novel_heros_journey",
        format = ProjectFormat.NOVEL,
        name = "Hero's Journey Monomyth (12 Stages)",
        subtitle = "12 Mythic Chapters from Ordinary World to Elixir",
        description = "Deep archetypal character arc mapped across 12 structured chapters.",
        badgeIcon = "🗡️",
        badgeColor = Color(0xFFF59E0B),
        novelChapters = listOf(
            "Chapter 1: The Ordinary World",
            "Chapter 2: The Call to Adventure",
            "Chapter 3: Refusal of the Call",
            "Chapter 4: Meeting the Mentor",
            "Chapter 5: Crossing the First Threshold",
            "Chapter 6: Tests, Allies & Enemies",
            "Chapter 7: Approach to the Inmost Cave",
            "Chapter 8: The Supreme Ordeal",
            "Chapter 9: Seizing the Reward",
            "Chapter 10: The Road Back",
            "Chapter 11: Resurrection",
            "Chapter 12: Return with the Elixir"
        ).mapIndexed { idx, title ->
            NovelChapter(
                chapterNumber = idx + 1,
                title = title,
                actIndex = if (idx < 4) 0 else if (idx < 9) 1 else 2,
                paragraphs = listOf(NovelParagraph(type = NovelParagraphType.NARRATIVE, text = ""))
            )
        },
        acts = CorkboardDocument.actsForNovel()
    )

    val NOVEL_MYSTERY_THRILLER = ProjectTemplate(
        id = "novel_mystery_thriller",
        format = ProjectFormat.NOVEL,
        name = "Mystery & Detective Thriller",
        subtitle = "8-Chapter Suspense & Deduction Blueprint",
        description = "The Crime, The Evidence, The Red Herrings, and The Unmasking.",
        badgeIcon = "🔍",
        badgeColor = ScribeStarNeonRed,
        novelChapters = listOf(
            "Chapter 1: The Body / The Crime",
            "Chapter 2: The Sleuth Arrives",
            "Chapter 3: The First Suspect & Clue",
            "Chapter 4: The Red Herring",
            "Chapter 5: The Second Attack / Stakes Double",
            "Chapter 6: Cornered in the Dark",
            "Chapter 7: The Final Confrontation",
            "Chapter 8: The Deduction & Denouement"
        ).mapIndexed { idx, title ->
            NovelChapter(
                chapterNumber = idx + 1,
                title = title,
                actIndex = if (idx < 3) 0 else if (idx < 6) 1 else 2,
                paragraphs = listOf(NovelParagraph(type = NovelParagraphType.NARRATIVE, text = ""))
            )
        },
        acts = CorkboardDocument.actsForNovel()
    )

    // =========================================================================
    // THEATRICAL STAGE PLAY TEMPLATES
    // =========================================================================

    val BLANK_STAGE_PLAY = ProjectTemplate(
        id = "stage_play_blank",
        format = ProjectFormat.STAGE_PLAY,
        name = "Blank Stage Play",
        subtitle = "Pristine proscenium script • ACT I, SCENE 1",
        description = "Start completely clean with a blank stage direction and dialogue page.",
        badgeIcon = "🎭",
        badgeColor = ElectrumGold,
        isBlank = true,
        blocks = listOf(
            ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 1"),
            ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "")
        ),
        acts = CorkboardDocument.actsForStagePlay()
    )

    val STAGE_PLAY_TWO_ACT = ProjectTemplate(
        id = "stage_play_two_act",
        format = ProjectFormat.STAGE_PLAY,
        name = "Standard Two-Act Play",
        subtitle = "Act I & II with Intermission & Final Curtain",
        description = "Classic theatrical staging with opening grievance, midpoint pivot, and climax.",
        badgeIcon = "🎟️",
        badgeColor = ElectrumGold,
        blocks = listOf(
            ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 1 - THE GRIEVANCE"),
            ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "[Setting: The curtain rises. Center stage, the room holds quiet anticipation.]"),
            ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 2 - POINT OF NO RETURN"),
            ScreenplayBlock(type = BlockType.STAGE_CURTAIN, text = "END OF ACT I (INTERMISSION)"),
            ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT II\nSCENE 1 - THE RECKONING"),
            ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT II\nSCENE 2 - THE RESOLUTION"),
            ScreenplayBlock(type = BlockType.STAGE_CURTAIN, text = "CURTAIN (END OF PLAY)")
        ),
        acts = CorkboardDocument.actsForStagePlay()
    )

    val STAGE_PLAY_ONE_ACT = ProjectTemplate(
        id = "stage_play_one_act",
        format = ProjectFormat.STAGE_PLAY,
        name = "Modern One-Act Play",
        subtitle = "Continuous Unbroken Dramatic Tension (35-50 min)",
        description = "Intense chamber drama or black box staging with no intermission.",
        badgeIcon = "⏳",
        badgeColor = Color(0xFF10B981),
        blocks = listOf(
            ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 1 - THE ENCOUNTER"),
            ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 2 - THE INTERROGATION"),
            ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 3 - THE PARTING"),
            ScreenplayBlock(type = BlockType.STAGE_CURTAIN, text = "BLACKOUT")
        ),
        acts = listOf(
            CorkboardAct(0, "Scene 1", "The Encounter & False Facade"),
            CorkboardAct(1, "Scene 2", "The Interrogation & Stripping Away"),
            CorkboardAct(2, "Scene 3", "The Parting & Permanent Wound")
        )
    )

    // =========================================================================
    // COMIC BOOK SCRIPT TEMPLATES
    // =========================================================================

    val BLANK_COMIC_BOOK = ProjectTemplate(
        id = "comic_blank",
        format = ProjectFormat.COMIC_BOOK,
        name = "Blank Comic Book",
        subtitle = "Pristine comic script • PAGE 1, PANEL 1",
        description = "Start completely clean with a blank comic page, panel and art note.",
        badgeIcon = "💥",
        badgeColor = ScribeStarNeonRed,
        isBlank = true,
        blocks = listOf(
            ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 1"),
            ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1"),
            ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "")
        ),
        acts = CorkboardDocument.actsForComicBook()
    )

    val COMIC_22_PAGE_ISSUE = ProjectTemplate(
        id = "comic_22_page_issue",
        format = ProjectFormat.COMIC_BOOK,
        name = "Standard 22-Page Single Issue",
        subtitle = "Industry Single Issue • Opening Splash to Cliffhanger",
        description = "Page 1 Splash, rising multi-panel sequences, double spread, and cliffhanger.",
        badgeIcon = "🦸",
        badgeColor = ScribeStarNeonRed,
        blocks = listOf(
            ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 1"),
            ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1 (FULL PAGE SPLASH)"),
            ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "Massive visual hook establishing protagonist in dramatic conflict."),
            ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 2"),
            ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1"),
            ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGES 10-11 (DOUBLE PAGE SPREAD)"),
            ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1 (WIDE HORIZONTAL SPREAD)"),
            ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 22"),
            ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1 (CLIFFHANGER SPLASH)")
        ),
        acts = CorkboardDocument.actsForComicBook()
    )

    val COMIC_4_KOMA_WEBCOMIC = ProjectTemplate(
        id = "comic_4_koma",
        format = ProjectFormat.COMIC_BOOK,
        name = "4-Koma / Vertical Webtoon Strip",
        subtitle = "Kī-Shō-Ten-Ketsu Four-Panel Comic",
        description = "Setup (Kī), Development (Shō), Twist (Ten), and Conclusion (Ketsu).",
        badgeIcon = "📱",
        badgeColor = Color(0xFF38BDF8),
        blocks = listOf(
            ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 1"),
            ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1 (KĪ / THE SETUP)"),
            ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 2 (SHŌ / DEVELOPMENT)"),
            ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 3 (TEN / THE TWIST)"),
            ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 4 (KETSU / CONCLUSION)")
        ),
        acts = listOf(
            CorkboardAct(0, "Panel 1", "The Setup"),
            CorkboardAct(1, "Panel 2", "Development"),
            CorkboardAct(2, "Panel 3", "The Twist"),
            CorkboardAct(3, "Panel 4", "The Punchline")
        )
    )

    /**
     * Resolves all available templates for a format, placing the Blank option first.
     */
    fun getTemplatesFor(format: ProjectFormat, screenplayType: ScreenplayType? = null): List<ProjectTemplate> {
        return when (format) {
            ProjectFormat.SCREENPLAY -> listOf(
                BLANK_FEATURE,
                SAVE_THE_CAT_FEATURE,
                HEROS_JOURNEY_FEATURE,
                STANDARD_3ACT_FEATURE,
                SHORT_FILM_FEATURE
            )
            ProjectFormat.TV_SERIES -> when (screenplayType) {
                ScreenplayType.TV_HALF_HOUR_COMEDY -> listOf(
                    BLANK_TV.copy(
                        screenplayType = ScreenplayType.TV_HALF_HOUR_COMEDY,
                        name = "Blank 30-Min Comedy",
                        subtitle = "Pristine blank comedy script"
                    ),
                    TV_HALF_HOUR_SITCOM
                )
                ScreenplayType.TV_HOUR_DRAMA -> listOf(
                    BLANK_TV.copy(
                        screenplayType = ScreenplayType.TV_HOUR_DRAMA,
                        name = "Blank 1-Hour Drama",
                        subtitle = "Pristine blank hourlong script"
                    ),
                    TV_1HOUR_DRAMA
                )
                else -> listOf(
                    BLANK_TV,
                    TV_1HOUR_DRAMA,
                    TV_HALF_HOUR_SITCOM
                )
            }
            ProjectFormat.NOVEL -> listOf(
                BLANK_NOVEL,
                NOVEL_3ACT_12CHAPTER,
                NOVEL_HEROS_JOURNEY,
                NOVEL_MYSTERY_THRILLER
            )
            ProjectFormat.STAGE_PLAY -> listOf(
                BLANK_STAGE_PLAY,
                STAGE_PLAY_TWO_ACT,
                STAGE_PLAY_ONE_ACT
            )
            ProjectFormat.COMIC_BOOK -> listOf(
                BLANK_COMIC_BOOK,
                COMIC_22_PAGE_ISSUE,
                COMIC_4_KOMA_WEBCOMIC
            )
        }
    }

    /**
     * Gets the designated clean blank template for any medium.
     */
    fun getBlankTemplate(format: ProjectFormat, screenplayType: ScreenplayType? = null): ProjectTemplate {
        return when (format) {
            ProjectFormat.SCREENPLAY -> BLANK_FEATURE
            ProjectFormat.TV_SERIES -> BLANK_TV.copy(screenplayType = screenplayType)
            ProjectFormat.NOVEL -> BLANK_NOVEL
            ProjectFormat.STAGE_PLAY -> BLANK_STAGE_PLAY
            ProjectFormat.COMIC_BOOK -> BLANK_COMIC_BOOK
        }
    }
}
