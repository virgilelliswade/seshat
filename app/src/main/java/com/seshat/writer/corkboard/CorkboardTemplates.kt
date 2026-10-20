package com.seshat.writer.corkboard

import androidx.compose.ui.graphics.Color
import com.seshat.writer.ui.theme.ElectrumGold
import com.seshat.writer.ui.theme.LightGold
import com.seshat.writer.ui.theme.ScribeStarNeonRed

/**
 * A single structured beat definition inside a Corkboard Template.
 */
data class CorkboardBeatTemplate(
    val beatNumber: Int,
    val title: String,
    val prompt: String,
    val actIndex: Int,
    val colorTag: String = "#F59E0B"
)

/**
 * A storytelling framework template that can be applied to the Corkboard.
 * Unlike script templates, this populates index cards and act columns on the corkboard
 * for the writer to fill in their beats, leaving the script editor pristine and clean.
 */
data class CorkboardTemplate(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val badgeIcon: String,
    val badgeColor: Color,
    val actCount: Int,
    val acts: List<CorkboardAct>,
    val beats: List<CorkboardBeatTemplate>
)

object CorkboardTemplateRegistry {

    // =========================================================================
    // 1. FOUR ARCHETYPES & YES-NO BEAT SHEET (ORPHAN, WANDERER, WARRIOR, MARTYR)
    // =========================================================================
    val FOUR_ARCHETYPES_YES_NO = CorkboardTemplate(
        id = "four_archetypes_yes_no",
        name = "Four Archetypes & Yes-No Beat Sheet",
        subtitle = "Orphan • Wanderer • Warrior • Martyr (23 Landmark Points & Yes/No Engine)",
        description = "The classic transformational screenwriting engine: Act 1 Orphan (12 plot points + Central Question), Act 2-A Wanderer (Yes-No 1–7), Midpoint Discovery, Act 2-B Warrior (Yes-No 8–14), and Act 3 Martyr (Big Yes-No to Final Victory).",
        badgeIcon = "⚔️",
        badgeColor = ScribeStarNeonRed,
        actCount = 4,
        acts = listOf(
            CorkboardAct(0, "Act I: Orphan", "Status Quo, Flaw & Decision to Save the Stakes Character"),
            CorkboardAct(1, "Act II-A: Wanderer", "Striving for the Answer • Yes-No Beats 1–7"),
            CorkboardAct(2, "Act II-B: Warrior", "Midpoint Discovery • Direct Action • Yes-No Beats 8–14"),
            CorkboardAct(3, "Act III: Martyr", "Willing to Lose All • Big Yes-No • Final Victory")
        ),
        beats = listOf(
            // --- ACT 1 (ORPHAN) ---
            CorkboardBeatTemplate(1, "PLOT POINT 1", "We meet either the HERO, the VICTIM/STAKES CHARACTER, or the ANTAGONIST.", 0, "#3B82F6"),
            CorkboardBeatTemplate(2, "PLOT POINT 2", "We see the hero's flaw in relation to the STAKES CHARACTER.", 0, "#8B5CF6"),
            CorkboardBeatTemplate(3, "PLOT POINT 3", "We meet the ANTAGONIST, or someone or something symbolic of the Antagonist.", 0, "#EF4444"),
            CorkboardBeatTemplate(4, "PLOT POINT 4", "The DEFLECTOR slows the hero down, pulling him off the path.", 0, "#64748B"),
            CorkboardBeatTemplate(5, "PLOT POINT 5: INCITING EVENT", "The INCITING EVENT happens, causing the hero to become emotionally involved.", 0, "#F59E0B"),
            CorkboardBeatTemplate(6, "PLOT POINT 6: GOAL CLARIFIED", "The hero's goal is clarified as it relates to the Stakes Character and/or Love Interest. The hero’s problem is made clear to the audience.", 0, "#3B82F6"),
            CorkboardBeatTemplate(7, "PLOT POINT 7: ALLY PROPELLER", "An ALLY aids the hero by propelling him out of the status quo.", 0, "#10B981"),
            CorkboardBeatTemplate(8, "PLOT POINT 8: HESITATION", "The hero seems ready to move forward toward a goal and/or Stakes Character, but can’t bring himself to do it.", 0, "#F59E0B"),
            CorkboardBeatTemplate(9, "PLOT POINT 9: DEFLECTOR CONFLICT", "Conflict with the Antagonist or a Deflector stops the hero or threatens the emotional stakes.", 0, "#EF4444"),
            CorkboardBeatTemplate(10, "PLOT POINT 10: STAKES DEPTH", "Either the depth of feeling between the hero and the Stakes Character or the severity of the threat to the Victims is made clear.", 0, "#EC4899"),
            CorkboardBeatTemplate(11, "PLOT POINT 11: STAKES THREATENED", "A Deflector or the Antagonist threatens to take the Stakes Character from the hero.", 0, "#EF4444"),
            CorkboardBeatTemplate(12, "PLOT POINT 12: DECISION TO SAVE", "The hero decides to save the Stakes Character.", 0, "#10B981"),
            CorkboardBeatTemplate(13, "CENTRAL QUESTION", "This is the pivotal question of your story. It MUST BE TANGIBLE and your protagonist MUST pursue it. It's the entire ACTION of your second act.", 0, "#F59E0B"),

            // --- ACT 2, PART 1 (WANDERER) ---
            CorkboardBeatTemplate(14, "YES 1", "Hero gets help from Allies and Aides (often a Mentor). Actively pursues what he thinks is needed to resolve the Central Question... BUT", 1, "#10B981"),
            CorkboardBeatTemplate(15, "NO 1", "Setback occurs. The Villain/opposition is established as not just bad, but really bad with very strong opposition.", 1, "#EF4444"),
            CorkboardBeatTemplate(16, "YES 2", "Hero continues pursuing the Central Question with new insight into what is really needed... BUT", 1, "#10B981"),
            CorkboardBeatTemplate(17, "NO 2", "Opposition pushes back harder; hero's old habits fall short.", 1, "#EF4444"),
            CorkboardBeatTemplate(18, "YES 3", "Hero learns the initial rules of the new world and makes initial progress... BUT", 1, "#10B981"),
            CorkboardBeatTemplate(19, "NO 3", "A complication prevents resolution; wandering is required.", 1, "#EF4444"),
            CorkboardBeatTemplate(20, "YES 4", "Hero continues to wander, learning with greater clarity what he needs to do while testing nascent abilities... BUT", 1, "#10B981"),
            CorkboardBeatTemplate(21, "NO 4", "Obstacles increase in intensity; trial fails.", 1, "#EF4444"),
            CorkboardBeatTemplate(22, "YES 5", "Hero finds a new approach or clue to the Central Question... BUT", 1, "#10B981"),
            CorkboardBeatTemplate(23, "NO 5", "The opposition becomes aware of the hero's existence (if they weren't already).", 1, "#EF4444"),
            CorkboardBeatTemplate(24, "YES 6", "The threat to the Stakes Character increases to life or death. The third act solution is shown (significance lost on hero). Opposite value of theme stated... BUT", 1, "#10B981"),
            CorkboardBeatTemplate(25, "NO 6", "Hero knows all the rules (or thinks he does); wandering further will not help him learn more.", 1, "#EF4444"),
            CorkboardBeatTemplate(26, "YES 7", "Villain's plan is explained (to/by hero or others). Hero enters the Villain's world or confronts the opposition... BUT", 1, "#10B981"),
            CorkboardBeatTemplate(27, "NO 7", "Hero is stopped cold; the wandering phase is exhausted.", 1, "#EF4444"),

            // --- ACT 2, PART 2 (WARRIOR) ---
            CorkboardBeatTemplate(28, "MIDPOINT (ONLY TO DISCOVER...)", "In the middle of your story, something happens that spins your protagonist in a whole new direction to solve the Central Question. Use the words ONLY TO DISCOVER... and answer what the hero discovers in the most exciting way! Helpers acquired or out of time—time to become a WARRIOR.", 2, "#F59E0B"),
            CorkboardBeatTemplate(29, "YES 8", "Hero shifts into action, making decisions in active/direct attempt to resolve the Central Question. Meets with initial success... BUT", 2, "#10B981"),
            CorkboardBeatTemplate(30, "NO 8", "Villain / opposing force tightens the screws on the hero's goal.", 2, "#EF4444"),
            CorkboardBeatTemplate(31, "YES 9", "Hero pushes forward; thematic question is raised and heightened (characters argue pro vs opposing arguments)... BUT", 2, "#10B981"),
            CorkboardBeatTemplate(32, "NO 9", "Hero bounces between viewpoints; opposing force strikes back.", 2, "#EF4444"),
            CorkboardBeatTemplate(33, "YES 10", "Hero prepares (possibly unknowingly) for their big change and launches decisive action... BUT", 2, "#10B981"),
            CorkboardBeatTemplate(34, "NO 10", "The counter-offensive hits; stakes spike to critical.", 2, "#EF4444"),
            CorkboardBeatTemplate(35, "YES 11", "Intense direct conflict takes place between hero and the opposing force/Villain... BUT", 2, "#10B981"),
            CorkboardBeatTemplate(36, "NO 11", "The hero realizes he still lacks the skills needed to succeed.", 2, "#EF4444"),
            CorkboardBeatTemplate(37, "YES 12", "The hero enters his darkest internal space (often a tight, enclosed physical place)... BUT", 2, "#10B981"),
            CorkboardBeatTemplate(38, "NO 12", "The hero undergoes the Ultimate Test leading to a death-moment.", 2, "#EF4444"),
            CorkboardBeatTemplate(39, "YES 13", "Hero emerges from near death, 'resurrected' as a new being with new understanding. Standard bearer for positive thematic argument... BUT", 2, "#10B981"),
            CorkboardBeatTemplate(40, "NO 13", "Whatever answers 'What's the worst that can happen?' happens. The Villain gains the ultimate upper hand.", 2, "#EF4444"),
            CorkboardBeatTemplate(41, "YES 14", "Hero refuses to abandon the Stakes Character; clarity of purpose locked... BUT", 2, "#10B981"),
            CorkboardBeatTemplate(42, "NO 14", "The hero is at their lowest moment (character interlude; dark night of the soul).", 2, "#EF4444"),

            // --- ACT 3 (MARTYR) ---
            CorkboardBeatTemplate(43, "BIG YES", "(1) Commitment to change (Private Conflict). (2) Willing to sacrifice self for Stakes Character (Personal Conflict). (3) Bravest thing possible (Professional Conflict)... BUT", 3, "#10B981"),
            CorkboardBeatTemplate(44, "NO", "The hero meets with initial failure, but instead of licking wounds, the hero presses on.", 3, "#EF4444"),
            CorkboardBeatTemplate(45, "BIG NO", "The Central Question is on the brink of being answered in the negative. The hero and allies make a huge, devastating sacrifice.", 3, "#EF4444"),
            CorkboardBeatTemplate(46, "FINAL YES", "The hero comes to terms with and resolves their Fatal Flaw, which in turn empowers them to resolve the Central Question once and for all!", 3, "#10B981"),
            CorkboardBeatTemplate(47, "THE END / FADE OUT", "The Central Question is answered; the new world order is established; FADE OUT.", 3, "#F59E0B")
        )
    )

    // =========================================================================
    // 2. SAVE THE CAT! BEAT SHEET (15 BEATS)
    // =========================================================================
    val SAVE_THE_CAT = CorkboardTemplate(
        id = "save_the_cat",
        name = "Save the Cat! Beat Sheet",
        subtitle = "Blake Snyder 15-Beat Commercial Blueprint",
        description = "The industry standard commercial narrative structure divided across 4 distinct storytelling acts.",
        badgeIcon = "🐱",
        badgeColor = ScribeStarNeonRed,
        actCount = 4,
        acts = listOf(
            CorkboardAct(0, "Act I: The Setup", "Opening Image to Break into Two"),
            CorkboardAct(1, "Act II-A: Fun & Games", "Promise of the Premise to Midpoint"),
            CorkboardAct(2, "Act II-B: The Crisis", "Bad Guys Close In to Dark Night of Soul"),
            CorkboardAct(3, "Act III: The Finale", "Break into Three to Final Image")
        ),
        beats = listOf(
            CorkboardBeatTemplate(
                beatNumber = 1,
                title = "1. OPENING IMAGE",
                prompt = "A visual statement of the starting state of the hero and world. Establish the status quo flaw.",
                actIndex = 0,
                colorTag = "#3B82F6"
            ),
            CorkboardBeatTemplate(
                beatNumber = 2,
                title = "2. THEME STATED",
                prompt = "What the hero needs to learn (often spoken by an ally, mentor, or bystander).",
                actIndex = 0,
                colorTag = "#8B5CF6"
            ),
            CorkboardBeatTemplate(
                beatNumber = 3,
                title = "3. SET-UP",
                prompt = "Establish the ordinary world, character flaws, relationships, and ticking problems.",
                actIndex = 0,
                colorTag = "#3B82F6"
            ),
            CorkboardBeatTemplate(
                beatNumber = 4,
                title = "4. CATALYST",
                prompt = "The inciting incident: a disruption that knocks the ordinary world permanently off balance.",
                actIndex = 0,
                colorTag = "#F59E0B"
            ),
            CorkboardBeatTemplate(
                beatNumber = 5,
                title = "5. DEBATE",
                prompt = "The protagonist hesitates, doubts, or resists entering the journey. Can they really do this?",
                actIndex = 0,
                colorTag = "#F59E0B"
            ),
            CorkboardBeatTemplate(
                beatNumber = 6,
                title = "6. BREAK INTO TWO",
                prompt = "The hero makes a deliberate choice to leave the old world and step into the special realm.",
                actIndex = 1,
                colorTag = "#10B981"
            ),
            CorkboardBeatTemplate(
                beatNumber = 7,
                title = "7. B STORY",
                prompt = "The secondary relationship or love story that carries the spiritual, thematic core.",
                actIndex = 1,
                colorTag = "#EC4899"
            ),
            CorkboardBeatTemplate(
                beatNumber = 8,
                title = "8. FUN AND GAMES",
                prompt = "The promise of the premise: classic scenes that deliver on the poster concept and genre tropes.",
                actIndex = 1,
                colorTag = "#10B981"
            ),
            CorkboardBeatTemplate(
                beatNumber = 9,
                title = "9. MIDPOINT",
                prompt = "False victory or false defeat; the stakes turn deadly serious, shifting the goal from want to need.",
                actIndex = 1,
                colorTag = "#F59E0B"
            ),
            CorkboardBeatTemplate(
                beatNumber = 10,
                title = "10. BAD GUYS CLOSE IN",
                prompt = "Internal doubts and external antagonists escalate pressure simultaneously on all fronts.",
                actIndex = 2,
                colorTag = "#EF4444"
            ),
            CorkboardBeatTemplate(
                beatNumber = 11,
                title = "11. ALL IS LOST",
                prompt = "The lowest point; the 'whiff of death' where the protagonist's old way of living dies completely.",
                actIndex = 2,
                colorTag = "#EF4444"
            ),
            CorkboardBeatTemplate(
                beatNumber = 12,
                title = "12. DARK NIGHT OF THE SOUL",
                prompt = "The hero sits in darkness, confronts their tragic flaw, and internalizes the theme stated.",
                actIndex = 2,
                colorTag = "#8B5CF6"
            ),
            CorkboardBeatTemplate(
                beatNumber = 13,
                title = "13. BREAK INTO THREE",
                prompt = "A sudden epiphany or synthesized realization sparks the climactic solution to the problem.",
                actIndex = 3,
                colorTag = "#10B981"
            ),
            CorkboardBeatTemplate(
                beatNumber = 14,
                title = "14. FINALE",
                prompt = "The climax: the hero executes the new plan, defeats the antagonistic force, and changes the world.",
                actIndex = 3,
                colorTag = "#F59E0B"
            ),
            CorkboardBeatTemplate(
                beatNumber = 15,
                title = "15. FINAL IMAGE",
                prompt = "The visual mirror of the Opening Image, proving the irreversible transformation of the protagonist.",
                actIndex = 3,
                colorTag = "#3B82F6"
            )
        )
    )

    // =========================================================================
    // 2. THE HERO'S JOURNEY (12 STAGES)
    // =========================================================================
    val HEROS_JOURNEY = CorkboardTemplate(
        id = "heros_journey",
        name = "The Hero's Journey (Monomyth)",
        subtitle = "12-Stage Mythic Campbell & Vogler Structure",
        description = "The timeless archetypal story arc from Ordinary World through Trials and Supreme Ordeal to Elixir.",
        badgeIcon = "⚔️",
        badgeColor = ElectrumGold,
        actCount = 3,
        acts = listOf(
            CorkboardAct(0, "Part I: Departure", "Separation from the Ordinary World"),
            CorkboardAct(1, "Part II: Initiation", "Trials, Inmost Cave & Supreme Ordeal"),
            CorkboardAct(2, "Part III: Return", "The Road Back & Resurrection with Elixir")
        ),
        beats = listOf(
            CorkboardBeatTemplate(1, "1. THE ORDINARY WORLD", "Establish the hero's daily existence, limitations, and internal longing.", 0, "#3B82F6"),
            CorkboardBeatTemplate(2, "2. THE CALL TO ADVENTURE", "A disruption or messenger delivers a challenge that changes everything.", 0, "#F59E0B"),
            CorkboardBeatTemplate(3, "3. REFUSAL OF THE CALL", "The hero hesitates or refuses due to fear, responsibility, or insecurity.", 0, "#64748B"),
            CorkboardBeatTemplate(4, "4. MEETING THE MENTOR", "A seasoned guide provides wisdom, talisman, training, or encouragement.", 0, "#8B5CF6"),
            CorkboardBeatTemplate(5, "5. CROSSING THE FIRST THRESHOLD", "The hero commits to the adventure and steps into the unfamiliar realm.", 0, "#10B981"),
            CorkboardBeatTemplate(6, "6. TESTS, ALLIES & ENEMIES", "Exploring the special world; making trusted allies and discovering rivals.", 1, "#3B82F6"),
            CorkboardBeatTemplate(7, "7. APPROACH TO INMOST CAVE", "Preparations for the central challenge in the heart of darkness.", 1, "#F59E0B"),
            CorkboardBeatTemplate(8, "8. THE SUPREME ORDEAL", "A critical life-or-death confrontation confronting the hero's greatest fear.", 1, "#EF4444"),
            CorkboardBeatTemplate(9, "9. REWARD (SEIZING THE SWORD)", "Surviving the ordeal and claiming the prize, elixir, or insight.", 1, "#F59E0B"),
            CorkboardBeatTemplate(10, "10. THE ROAD BACK", "Urgency strikes; consequences of seizing the prize spark pursuit.", 2, "#EC4899"),
            CorkboardBeatTemplate(11, "11. THE RESURRECTION", "The final climactic test: purified through a second, transcendent rebirth.", 2, "#EF4444"),
            CorkboardBeatTemplate(12, "12. RETURN WITH THE ELIXIR", "Returning home transformed, using the prize to heal and elevate their world.", 2, "#10B981")
        )
    )

    // =========================================================================
    // 3. STANDARD 3-ACT STRUCTURE (8 SEQUENCES)
    // =========================================================================
    val STANDARD_3ACT = CorkboardTemplate(
        id = "standard_3act",
        name = "Standard 3-Act Structure (8 Sequences)",
        subtitle = "Syd Field Framework • Turning Points & Midpoint",
        description = "Clean three-act paradigm highlighting key turning points, dramatic reversals, and climactic payoff.",
        badgeIcon = "🎬",
        badgeColor = Color(0xFF38BDF8),
        actCount = 3,
        acts = listOf(
            CorkboardAct(0, "Act I: The Setup", "Status Quo, Inciting Event & Plot Point 1"),
            CorkboardAct(1, "Act II: The Confrontation", "Rising Obstacles, Midpoint & Crisis Point"),
            CorkboardAct(2, "Act III: The Resolution", "Climactic Battle & Final Denouement")
        ),
        beats = listOf(
            CorkboardBeatTemplate(1, "1. STATUS QUO & SETUP", "Introduce protagonist, core relationships, and dramatic premise.", 0, "#3B82F6"),
            CorkboardBeatTemplate(2, "2. INCITING INCIDENT", "The event that disrupts life and poses the central dramatic question.", 0, "#F59E0B"),
            CorkboardBeatTemplate(3, "3. PLOT POINT 1 (TURNING POINT)", "Irreversible event that locks protagonist into the journey into Act II.", 0, "#10B981"),
            CorkboardBeatTemplate(4, "4. RISING CONFRONTATION", "First attempts to resolve the crisis meet increasing resistance.", 1, "#3B82F6"),
            CorkboardBeatTemplate(5, "5. THE MIDPOINT PIVOT", "Major revelation or reversal that shifts the balance of power.", 1, "#F59E0B"),
            CorkboardBeatTemplate(6, "6. PLOT POINT 2 (THE CRISIS)", "The lowest point: plan fails, stakes peak, dark night before the dawn.", 1, "#EF4444"),
            CorkboardBeatTemplate(7, "7. THE CLIMAX", "The decisive showdown where the protagonist confronts the ultimate test.", 2, "#F59E0B"),
            CorkboardBeatTemplate(8, "8. RESOLUTION & DENOUEMENT", "The new normal is established; emotional closure and character payoff.", 2, "#10B981")
        )
    )

    // =========================================================================
    // 4. DAN HARMON'S STORY CIRCLE (8 BEATS)
    // =========================================================================
    val STORY_CIRCLE = CorkboardTemplate(
        id = "dan_harmon_circle",
        name = "Dan Harmon's Story Circle",
        subtitle = "8-Beat Universal Transformation Engine",
        description = "The modern narrative wheel from comfort zone through descent and price to return transformed.",
        badgeIcon = "⭕",
        badgeColor = Color(0xFF10B981),
        actCount = 4,
        acts = listOf(
            CorkboardAct(0, "Act I: You & Need", "The Comfort Zone and the Driving Lack"),
            CorkboardAct(1, "Act II-A: Go & Search", "Entering the Unfamiliar and Adapting"),
            CorkboardAct(2, "Act II-B: Find & Take", "Getting the Goal and Paying the Price"),
            CorkboardAct(3, "Act III: Return & Change", "Going Back Transformed Forever")
        ),
        beats = listOf(
            CorkboardBeatTemplate(1, "1. YOU (COMFORT ZONE)", "A character is in a zone of comfort, familiarity, and habit.", 0, "#3B82F6"),
            CorkboardBeatTemplate(2, "2. NEED (DESIRE LINE)", "But they want or need something; an itch that demands scratching.", 0, "#F59E0B"),
            CorkboardBeatTemplate(3, "3. GO (UNFAMILIAR)", "They cross over into an unfamiliar, strange situation or environment.", 1, "#10B981"),
            CorkboardBeatTemplate(4, "4. SEARCH (ADAPTATION)", "They adapt to the road of trials, learning the new rules through failure.", 1, "#3B82F6"),
            CorkboardBeatTemplate(5, "5. FIND (THE PRIZE)", "They discover what they were looking for; the goal is within reach.", 2, "#F59E0B"),
            CorkboardBeatTemplate(6, "6. TAKE (HEAVY PRICE)", "They take it, but pay a heavy, painful, unexpected price.", 2, "#EF4444"),
            CorkboardBeatTemplate(7, "7. RETURN (GOING BACK)", "They return to their familiar world with their hard-won prize.", 3, "#EC4899"),
            CorkboardBeatTemplate(8, "8. CHANGE (TRANSFORMED)", "Having transformed, they are capable of change and master of two worlds.", 3, "#10B981")
        )
    )

    // =========================================================================
    // 5. TELEVISION 1-HOUR DRAMA (TEASER + 5 ACTS)
    // =========================================================================
    val TV_1HOUR_DRAMA = CorkboardTemplate(
        id = "tv_1hour_drama",
        name = "Television 1-Hour Drama",
        subtitle = "Teaser + 5 Acts • Procedural & Serialized Arc",
        description = "Engineered for network and streaming 60-minute drama with act-out cliffhangers and commercial beats.",
        badgeIcon = "📺",
        badgeColor = Color(0xFF3B82F6),
        actCount = 6,
        acts = listOf(
            CorkboardAct(0, "Teaser", "The Hook & Inciting Anomaly"),
            CorkboardAct(1, "Act I", "The Case / Status Quo Disrupted"),
            CorkboardAct(2, "Act II", "Complications & Rising Stakes"),
            CorkboardAct(3, "Act III", "Midpoint Shift & Ticking Clock"),
            CorkboardAct(4, "Act IV", "The Crisis / All Hope Lost"),
            CorkboardAct(5, "Act V", "The Climax, Resolution & Fallout")
        ),
        beats = listOf(
            CorkboardBeatTemplate(1, "TEASER - THE COLD HOOK", "High-octane opening scene that introduces the central mystery or threat.", 0, "#EF4444"),
            CorkboardBeatTemplate(2, "ACT I - STATUS QUO DISRUPTED", "Our protagonists arrive on scene; the central dramatic goal is locked.", 1, "#3B82F6"),
            CorkboardBeatTemplate(3, "ACT II - COMPLICATIONS & B-STORY", "Initial theories crumble; personal subplots collide with the main case.", 2, "#8B5CF6"),
            CorkboardBeatTemplate(4, "ACT III - MIDPOINT CRISIS", "The antagonist strikes back; stakes double and ticking clock accelerates.", 3, "#F59E0B"),
            CorkboardBeatTemplate(5, "ACT IV - ALL HOPE LOST", "The investigation stalls; protagonists pushed to their absolute limits.", 4, "#EF4444"),
            CorkboardBeatTemplate(6, "ACT V - CLIMAX & CLIFFHANGER", "The showdown, case resolution, and serialized seasonal cliffhanger.", 5, "#10B981")
        )
    )

    // =========================================================================
    // 6. TELEVISION 30-MIN SITCOM (COLD OPEN + 2 ACTS + TAG)
    // =========================================================================
    val TV_SITCOM = CorkboardTemplate(
        id = "tv_sitcom",
        name = "Television 30-Min Sitcom",
        subtitle = "Cold Open + 2 Acts + Tag • Rapid Comedy Arc",
        description = "Multi-cam or single-cam half-hour comedic structure with A/B story intertwining and closing tag.",
        badgeIcon = "🛋️",
        badgeColor = Color(0xFFEC4899),
        actCount = 4,
        acts = listOf(
            CorkboardAct(0, "Cold Open", "Inciting Comedy Beat"),
            CorkboardAct(1, "Act I", "The Premise & Comedic Goal"),
            CorkboardAct(2, "Act II", "Escalating Chaos & Misunderstandings"),
            CorkboardAct(3, "Tag / Epilogue", "Resolution & Closing Punchline")
        ),
        beats = listOf(
            CorkboardBeatTemplate(1, "COLD OPEN - INCITING GAG", "Quick character-driven comedic sketch setting the tone of the episode.", 0, "#EC4899"),
            CorkboardBeatTemplate(2, "ACT I - COMEDIC PREMISE & GOAL", "Protagonist takes on a ridiculous goal or misguided lie; B-story starts.", 1, "#3B82F6"),
            CorkboardBeatTemplate(3, "ACT II - ESCALATING FARCE & CRISIS", "Lies spiral out of control; public spectacle or chaotic climax.", 2, "#EF4444"),
            CorkboardBeatTemplate(4, "TAG - CLOSING BUTTON", "Warm resolution, status quo restored, and final comedic kicker.", 3, "#10B981")
        )
    )

    // =========================================================================
    // 7. CLASSIC NOVEL 3-ACT (12 CHAPTERS)
    // =========================================================================
    val NOVEL_12CHAPTER = CorkboardTemplate(
        id = "novel_12chapter",
        name = "Classic Novel 3-Act (12 Chapters)",
        subtitle = "3 Parts • Scrivener-Style Saga Chapters",
        description = "Part I: Departure (Ch 1-3), Part II: Initiation (Ch 4-9), Part III: Return (Ch 10-12).",
        badgeIcon = "📚",
        badgeColor = Color(0xFF8B5CF6),
        actCount = 3,
        acts = listOf(
            CorkboardAct(0, "Part I: Departure", "Chapters 1–3: The Old World & Threshold"),
            CorkboardAct(1, "Part II: Initiation", "Chapters 4–9: The Arena, Midpoint & Dark Night"),
            CorkboardAct(2, "Part III: Return", "Chapters 10–12: Reawakening, Climax & New Order")
        ),
        beats = listOf(
            CorkboardBeatTemplate(1, "CH 1: THE ORDINARY WORLD", "Introduce the world, internal conflict, and the status quo.", 0, "#3B82F6"),
            CorkboardBeatTemplate(2, "CH 2: THE INCITING DISTURBANCE", "The event that shatters peace and forces a reckoning.", 0, "#F59E0B"),
            CorkboardBeatTemplate(3, "CH 3: CROSSING THE THRESHOLD", "The protagonist commits to the journey and leaves safety behind.", 0, "#10B981"),
            CorkboardBeatTemplate(4, "CH 4: THE NEW ARENA & ALLIES", "Entering the new landscape; forming friendships and rivalries.", 1, "#3B82F6"),
            CorkboardBeatTemplate(5, "CH 5: RISING COMPLICATIONS", "Obstacles mount; initial strategies fail to yield results.", 1, "#3B82F6"),
            CorkboardBeatTemplate(6, "CH 6: THE MIDPOINT SHIFT", "A pivotal truth is revealed; the mission shifts from passive to active.", 1, "#F59E0B"),
            CorkboardBeatTemplate(7, "CH 7: BAD GUYS CLOSE IN", "Opposition consolidates; paranoia, betrayal, or pressure increases.", 1, "#EF4444"),
            CorkboardBeatTemplate(8, "CH 8: THE SHATTERING CRISIS", "The disaster strikes; the master plan crumbles completely.", 1, "#EF4444"),
            CorkboardBeatTemplate(9, "CH 9: DARK NIGHT OF THE SOUL", "Isolation and grief; protagonist must face their fundamental flaw.", 1, "#8B5CF6"),
            CorkboardBeatTemplate(10, "CH 10: THE REAWAKENING", "Synthesizing lessons learned; forging the final resolve and strategy.", 2, "#10B981"),
            CorkboardBeatTemplate(11, "CH 11: THE CLIMAX", "The ultimate confrontation; testing the character's full transformation.", 2, "#F59E0B"),
            CorkboardBeatTemplate(12, "CH 12: THE NEW EQUILIBRIUM", "The aftermath: resolution, rewards, and the enduring new world.", 2, "#10B981")
        )
    )

    // =========================================================================
    // 8. MYSTERY & DETECTIVE THRILLER (8 BEATS)
    // =========================================================================
    val MYSTERY_THRILLER = CorkboardTemplate(
        id = "mystery_thriller",
        name = "Mystery & Detective Thriller",
        subtitle = "8-Beat Suspense & Deduction Blueprint",
        description = "Classic whodunit and crime investigation framework from crime scene to climactic deduction.",
        badgeIcon = "🔍",
        badgeColor = ScribeStarNeonRed,
        actCount = 4,
        acts = listOf(
            CorkboardAct(0, "Act I: The Crime", "The Incident & Detective's Arrival"),
            CorkboardAct(1, "Act II-A: Investigation", "Suspects, Clues & Red Herrings"),
            CorkboardAct(2, "Act II-B: Danger Escalates", "Second Strike & Threat to Detective"),
            CorkboardAct(3, "Act III: The Unmasking", "Showdown & Denouement Deduction")
        ),
        beats = listOf(
            CorkboardBeatTemplate(1, "1. THE BODY / THE CRIME", "The crime is discovered; crime scene details, victim background.", 0, "#EF4444"),
            CorkboardBeatTemplate(2, "2. THE DETECTIVE ARRIVES", "Protagonist takes the case; initial assessment and eccentric methods.", 0, "#3B82F6"),
            CorkboardBeatTemplate(3, "3. THE FIRST CLUES & MOTIVES", "Interviewing suspects; gathering physical evidence and alibis.", 1, "#F59E0B"),
            CorkboardBeatTemplate(4, "4. THE RED HERRING", "A deceptive lead misdirects the investigation; someone is framed.", 1, "#8B5CF6"),
            CorkboardBeatTemplate(5, "5. SECOND ATTACK / STAKES DOUBLE", "The killer strikes again; ticking clock; the detective is targeted.", 2, "#EF4444"),
            CorkboardBeatTemplate(6, "6. CORNERED IN THE DARK", "Crucial evidence is destroyed or detective trapped; all seems lost.", 2, "#EF4444"),
            CorkboardBeatTemplate(7, "7. THE FINAL CONFRONTATION", "Cornering the true culprit in a life-or-death confrontation.", 3, "#F59E0B"),
            CorkboardBeatTemplate(8, "8. THE DEDUCTION & DENOUEMENT", "The classic unmasking: explaining the method, motive, and justice served.", 3, "#10B981")
        )
    )

    // =========================================================================
    // 9. SHORT FILM (3 BEATS)
    // =========================================================================
    val SHORT_FILM = CorkboardTemplate(
        id = "short_film",
        name = "Short Film (Festival Format)",
        subtitle = "3-Beat High Impact Compact Arc",
        description = "Optimized for festival submissions: concise hook, sudden complication, and unforgettable twist.",
        badgeIcon = "⏱️",
        badgeColor = Color(0xFF10B981),
        actCount = 3,
        acts = listOf(
            CorkboardAct(0, "The Hook", "Establish anomaly and desire"),
            CorkboardAct(1, "The Pivot", "Ticking clock and point of no return"),
            CorkboardAct(2, "The Twist", "Climax and moral consequence")
        ),
        beats = listOf(
            CorkboardBeatTemplate(1, "1. THE HOOK & DILEMMA", "Introduce compelling visual dilemma in under 60 seconds.", 0, "#3B82F6"),
            CorkboardBeatTemplate(2, "2. THE COMPLICATION & PIVOT", "A sudden unexpected obstacle turns the character's choice dangerous.", 1, "#F59E0B"),
            CorkboardBeatTemplate(3, "3. THE TWIST & MORAL CONSEQUENCE", "An ironic twist or emotional resolution that recontextualizes the whole story.", 2, "#10B981")
        )
    )

    val ALL_TEMPLATES = listOf(
        FOUR_ARCHETYPES_YES_NO,
        SAVE_THE_CAT,
        HEROS_JOURNEY,
        STANDARD_3ACT,
        STORY_CIRCLE,
        TV_1HOUR_DRAMA,
        TV_SITCOM,
        NOVEL_12CHAPTER,
        MYSTERY_THRILLER,
        SHORT_FILM
    )

    fun getById(id: String): CorkboardTemplate? = ALL_TEMPLATES.find { it.id == id }
}
