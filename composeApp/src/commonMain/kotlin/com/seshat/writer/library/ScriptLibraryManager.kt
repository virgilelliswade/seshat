package com.seshat.writer.library

import android.content.Context
import com.seshat.writer.export.FinalDraftSerializer
import com.seshat.writer.export.FountainSerializer
import com.seshat.writer.export.ScriptFileManager
import com.seshat.writer.importer.ScriptFormat
import com.seshat.writer.importer.ScriptImporter
import com.seshat.writer.model.BlockType
import com.seshat.writer.model.RevisionColor
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.titlepage.TitlePageData
import java.io.File
import java.util.Locale

object ScriptLibraryManager {

    fun getScriptsDirectory(context: Context? = null): File {
        return ScriptFileManager.getScriptsDirectory(context)
    }

    fun listProjects(context: Context? = null, activeTitle: String = ""): List<ScriptProject> {
        val dir = getScriptsDirectory(context)
        return listProjectsInDir(dir, activeTitle)
    }


    fun listProjectsInDir(directory: File, activeTitle: String = ""): List<ScriptProject> {
        val files = directory.listFiles { f ->
            f.isFile && (f.extension.equals("fountain", ignoreCase = true) || f.extension.equals("fdx", ignoreCase = true))
        } ?: return emptyList()

        return files.map { file ->
            val importResult = ScriptImporter.importFromFile(file)
            val doc = importResult.document
            val title = doc?.titlePage?.title?.ifBlank { file.nameWithoutExtension } ?: file.nameWithoutExtension
            val author = doc?.titlePage?.author ?: ""
            val sceneCount = doc?.blocks?.count { it.type == BlockType.SCENE_HEADING } ?: 1
            val estimatedPageCount = (doc?.blocks?.size ?: 10) / 25 + 1
            val formatStr = if (importResult.detectedFormat == ScriptFormat.FINAL_DRAFT) "FDX" else "FOUNTAIN"
            val isActive = activeTitle.isNotBlank() && title.equals(activeTitle, ignoreCase = true)

            ScriptProject(
                id = file.name,
                title = title,
                author = author,
                file = file,
                format = formatStr,
                lastModified = file.lastModified(),
                fileSizeBytes = file.length(),
                sceneCount = sceneCount,
                pageCount = estimatedPageCount,
                revisionColor = RevisionColor.WHITE,
                draftTag = doc?.titlePage?.draftInfo?.ifBlank { "White Draft" } ?: "White Draft",
                isActive = isActive
            )
        }.sortedByDescending { it.lastModified }
    }

    fun createNewProject(
        context: Context? = null,
        title: String,
        author: String = "",
        template: ScriptTemplate = ScriptTemplate.BLANK
    ): File {
        return createNewProjectInDir(getScriptsDirectory(context), title, author, template)
    }

    fun createNewProjectInDir(
        directory: File,
        title: String,
        author: String = "",
        template: ScriptTemplate = ScriptTemplate.BLANK
    ): File {
        if (!directory.exists()) directory.mkdirs()
        val safeTitle = sanitizeFileName(title.ifBlank { "Untitled_Screenplay" })
        val file = File(directory, "$safeTitle.fountain")

        val titlePage = TitlePageData(
            title = title.ifBlank { "UNTITLED SCREENPLAY" },
            author = author,
            writtenByLabel = "written by",
            draftInfo = "First Draft"
        )

        val blocks = buildTemplateBlocks(template)
        val content = FountainSerializer.exportToFountain(blocks, titlePage)
        file.writeText(content)
        return file
    }

    fun duplicateProject(
        context: Context? = null,
        sourceFile: File,
        newTitle: String
    ): File {
        return duplicateProjectInDir(getScriptsDirectory(context), sourceFile, newTitle)
    }


    fun duplicateProjectInDir(
        directory: File,
        sourceFile: File,
        newTitle: String
    ): File {
        if (!directory.exists()) directory.mkdirs()
        val ext = sourceFile.extension.ifBlank { "fountain" }
        val safeTitle = sanitizeFileName(newTitle.ifBlank { "${sourceFile.nameWithoutExtension}_Copy" })
        val targetFile = File(directory, "$safeTitle.$ext")

        if (sourceFile.exists()) {
            val content = sourceFile.readText()
            // If it's Fountain, update Title header
            val updatedContent = if (ext.equals("fountain", ignoreCase = true)) {
                if (content.contains("Title:")) {
                    content.replaceFirst(Regex("Title:[^\\n]*"), "Title: $newTitle")
                } else {
                    "Title: $newTitle\n\n$content"
                }
            } else content

            targetFile.writeText(updatedContent)
        }
        return targetFile
    }

    fun renameProject(
        context: Context,
        file: File,
        newTitle: String
    ): File {
        return renameProjectInDir(getScriptsDirectory(context), file, newTitle)
    }

    fun renameProjectInDir(
        directory: File,
        file: File,
        newTitle: String
    ): File {
        val ext = file.extension.ifBlank { "fountain" }
        val safeTitle = sanitizeFileName(newTitle.ifBlank { "Renamed_Script" })
        val newFile = File(directory, "$safeTitle.$ext")

        if (file.exists()) {
            val content = file.readText()
            val updatedContent = if (ext.equals("fountain", ignoreCase = true)) {
                if (content.contains("Title:")) {
                    content.replaceFirst(Regex("Title:[^\\n]*"), "Title: $newTitle")
                } else {
                    "Title: $newTitle\n\n$content"
                }
            } else content
            newFile.writeText(updatedContent)
            if (file.absolutePath != newFile.absolutePath) {
                file.delete()
            }
        }
        return newFile
    }

    fun deleteProject(file: File): Boolean {
        return if (file.exists()) file.delete() else false
    }

    fun buildTemplateBlocks(template: ScriptTemplate): List<ScreenplayBlock> {
        // If blank template selected or starting clean: return completely empty single block
        if (template.isBlank) {
            return when (template.targetFormat) {
                ProjectFormat.STAGE_PLAY -> listOf(
                    ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "")
                )
                ProjectFormat.COMIC_BOOK -> listOf(
                    ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "")
                )
                ProjectFormat.NOVEL -> listOf(
                    ScreenplayBlock(type = BlockType.ACTION, text = "")
                )
                else -> listOf(
                    ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")
                )
            }
        }

        return when (template) {
            ScriptTemplate.FEATURE_FILM -> listOf(
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. MAIN CHARACTER'S APARTMENT - DAY"),
                ScreenplayBlock(type = BlockType.ACTION, text = "ACT I - THE SETUP\nEstablish the protagonist's ordinary world, fatal flaw, and unfulfilled longing before the disruption arrives."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. CITY STREETS - NIGHT"),
                ScreenplayBlock(type = BlockType.ACTION, text = "THE INCITING INCIDENT\nA catalyst shatters the status quo. The protagonist must make a definitive choice to cross the threshold into Act II."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. CROSSROADS - DAY"),
                ScreenplayBlock(type = BlockType.ACTION, text = "ACT II - THE MIDPOINT & CRISIS\nEscalating conflict, rising stakes, and the false defeat that strips away old defenses, leading to the All Hope Is Lost beat."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. FINAL SHOWDOWN - NIGHT"),
                ScreenplayBlock(type = BlockType.ACTION, text = "ACT III - THE CLIMAX & RESOLUTION\nThe decisive confrontation where internal character transformation enables external resolution.")
            )
            ScriptTemplate.ONE_HOUR_TV -> listOf(
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "TEASER"),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. THE CRIME SCENE / DISRUPTION - NIGHT"),
                ScreenplayBlock(type = BlockType.ACTION, text = "A high-stakes opening mystery that commands the audience's attention in the first three minutes."),
                ScreenplayBlock(type = BlockType.TRANSITION, text = "SMASH CUT TO:"),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "ACT ONE"),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. HEADQUARTERS / BULLPEN - DAY"),
                ScreenplayBlock(type = BlockType.ACTION, text = "The ensemble takes on the case; personal dynamics and thematic friction emerge."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "ACT TWO"),
                ScreenplayBlock(type = BlockType.ACTION, text = "Complications multiply; the first major lead collapses and personal stakes ignite."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "ACT THREE"),
                ScreenplayBlock(type = BlockType.ACTION, text = "The ticking clock; an imminent threat puts someone our lead cares about in peril."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "ACT FOUR"),
                ScreenplayBlock(type = BlockType.ACTION, text = "The revelation; a shocking twist solves the immediate crisis while leaving a major season arc cliffhanger.")
            )
            ScriptTemplate.HALF_HOUR_SITCOM -> listOf(
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "COLD OPEN"),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. APARTMENT LIVING ROOM - MORNING"),
                ScreenplayBlock(type = BlockType.ACTION, text = "Fast-paced banter establishes the comedic premise before the main title sequence."),
                ScreenplayBlock(type = BlockType.CHARACTER, text = "PROTAGONIST"),
                ScreenplayBlock(type = BlockType.DIALOGUE, text = "Tell me you didn't just agree to let them stay here."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "ACT ONE"),
                ScreenplayBlock(type = BlockType.ACTION, text = "A-Plot: Protagonist conceals an escalating mistake. B-Plot: Supporting cast embarks on an absurd side scheme."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "ACT TWO"),
                ScreenplayBlock(type = BlockType.ACTION, text = "The plots collide in high farce. True motives are confessed, leading to a comedic or heartwarming truce."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "TAG"),
                ScreenplayBlock(type = BlockType.ACTION, text = "Post-credit punchline button wrapping up the comedic runner.")
            )
            ScriptTemplate.SHORT_FILM -> listOf(
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. WINDSWEPT HIGHWAY - DUSK"),
                ScreenplayBlock(type = BlockType.ACTION, text = "THE SETUP: A striking cinematic visual sets mood, character status, and an unspoken dilemma."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "INT. DESERT MOTEL - NIGHT"),
                ScreenplayBlock(type = BlockType.ACTION, text = "THE TURN: A revelation turns the scene upside down; assumptions crumble."),
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "EXT. DESERT MOTEL - DAWN"),
                ScreenplayBlock(type = BlockType.ACTION, text = "THE PAYOFF: A resonant closing visual and poetic emotional conclusion.")
            )
            ScriptTemplate.STAGE_PLAY_TWO_ACT -> listOf(
                ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "DRAMATIS PERSONAE"),
                ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "ELENA (40s) - A master restorer of classical paintings.\nJULIAN (50s) - An art provenance investigator with a personal score to settle.\n\nTIME: Autumn, rainy night.\nPLACE: A high-ceilinged conservation studio in a historic district."),
                ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT I\nSCENE 1"),
                ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "[At rise: Rain lashes the tall studio windows. The beam of an inspection lamp illuminates a centuries-old canvas.]"),
                ScreenplayBlock(type = BlockType.CHARACTER, text = "ELENA"),
                ScreenplayBlock(type = BlockType.DIALOGUE, text = "Every layer of varnish tells a lie. You scrape deep enough, you find who actually held the brush."),
                ScreenplayBlock(type = BlockType.STAGE_CURTAIN, text = "[INTERMISSION]"),
                ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "ACT II\nSCENE 1"),
                ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "[Later that night. The storm has passed. Morning fog rolls across the floor.]"),
                ScreenplayBlock(type = BlockType.STAGE_CURTAIN, text = "[CURTAIN]")
            )
            ScriptTemplate.STAGE_PLAY_ONE_ACT -> listOf(
                ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "SCENE 1"),
                ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "[Setting: The radio broadcast booth of an overnight station during an emergency broadcast alert.]"),
                ScreenplayBlock(type = BlockType.CHARACTER, text = "HOST"),
                ScreenplayBlock(type = BlockType.DIALOGUE, text = "If anyone is still listening across the valley, keep your dials locked right here."),
                ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "[The red ON AIR beacon flickers and hums.]"),
                ScreenplayBlock(type = BlockType.STAGE_CURTAIN, text = "[BLACKOUT]")
            )
            ScriptTemplate.STAGE_PLAY_TEN_MINUTE -> listOf(
                ScreenplayBlock(type = BlockType.STAGE_ACT_SCENE, text = "SCENE 1"),
                ScreenplayBlock(type = BlockType.STAGE_DIRECTION, text = "[Setting: Two rivals trapped inside an elevator stalled between the 40th and 41st floors.]"),
                ScreenplayBlock(type = BlockType.CHARACTER, text = "DEVON"),
                ScreenplayBlock(type = BlockType.DIALOGUE, text = "One of us has the winning bid in their briefcase. The question is who hits the lobby first."),
                ScreenplayBlock(type = BlockType.CHARACTER, text = "CHLOE"),
                ScreenplayBlock(type = BlockType.DIALOGUE, text = "The cable just snapped a notch, Devon. We might both be going down together."),
                ScreenplayBlock(type = BlockType.STAGE_CURTAIN, text = "[RAPID FADE OUT]")
            )
            ScriptTemplate.COMIC_SUPERHERO_22PAGE -> listOf(
                ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 1"),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1 - FULL PAGE SPLASH"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "Cinematic bird's-eye perspective over Obsidian City at dusk. Rain glistens on the stone gargoyles. Our masked vigilante perches over the abyss, trench coat snapping in the squall."),
                ScreenplayBlock(type = BlockType.COMIC_CAPTION, text = "CAPTION: They say this city swallows secrets whole. Tonight, I'm digging them back up."),
                ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 2"),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "Medium shot inside the armored laboratory below. A heavy reinforced vault door blows outward in a brilliant burst of cyan sparks."),
                ScreenplayBlock(type = BlockType.COMIC_SFX, text = "SFX: KKKRRRAAA-BOOOOM!"),
                ScreenplayBlock(type = BlockType.CHARACTER, text = "CYBER ROOK"),
                ScreenplayBlock(type = BlockType.COMIC_BALLOON, text = "Did someone order a security review?")
            )
            ScriptTemplate.COMIC_MANGA_CHAPTER -> listOf(
                ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGE 1 (OPENING COLOR BLEED)"),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "Atmospheric establishing frame. Mist curls around the ancient torii gate at the summit of Mount Hiei. A single scarred wooden sword rests driven into the ground."),
                ScreenplayBlock(type = BlockType.COMIC_CAPTION, text = "NARRATION: The blade that was broken ten years ago was never meant to be reforged."),
                ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "PAGES 2-3 (DOUBLE-PAGE SPREAD)"),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1 - DYNAMIC ACTION SPREAD"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "High speed action! Dynamic diagonal perspective with explosive radial speedlines as two martial artists clash mid-air!"),
                ScreenplayBlock(type = BlockType.COMIC_SFX, text = "SFX: DZZZZZSHHH! (IMPACT)")
            )
            ScriptTemplate.COMIC_FOUR_PANEL_STRIP -> listOf(
                ScreenplayBlock(type = BlockType.COMIC_PAGE, text = "STRIP 1 (4-PANEL YONKOMA)"),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 1 - KI (SETUP)"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "An energetic cat stares intently at an empty cardboard shoe box placed on the kitchen rug."),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 2 - SHŌ (DEVELOPMENT)"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "Cat wiggles rear hips in extreme predatory concentration, eyes dilated completely black."),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 3 - TEN (TWIST)"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "A tiny newborn kitten casually walks right into the box and curls up asleep."),
                ScreenplayBlock(type = BlockType.COMIC_PANEL, text = "PANEL 4 - KETSU (PUNCHLINE)"),
                ScreenplayBlock(type = BlockType.COMIC_ART_NOTE, text = "Big cat sits up with nonchalant dignity, furiously licking its paw as if that was the plan all along."),
                ScreenplayBlock(type = BlockType.CHARACTER, text = "CAT"),
                ScreenplayBlock(type = BlockType.COMIC_BALLOON, text = "I was just checking the structural integrity.")
            )
            else -> listOf(
                ScreenplayBlock(type = BlockType.SCENE_HEADING, text = "")
            )
        }
    }

    private fun sanitizeFileName(name: String): String {
        val cleaned = name.replace(Regex("[^a-zA-Z0-9._-]"), "_").trim('_')
        return cleaned.ifBlank { "Screenplay" }
    }
}
