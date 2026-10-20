package com.seshat.writer.diff

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock
import com.seshat.writer.titlepage.TitlePageData
import java.util.Locale

object DraftDiffEngine {

    fun compareDrafts(
        blocksA: List<ScreenplayBlock>,
        titlePageA: TitlePageData,
        blocksB: List<ScreenplayBlock>,
        titlePageB: TitlePageData
    ): DraftDiffResult {
        val scenesA = partitionIntoScenes(blocksA)
        val scenesB = partitionIntoScenes(blocksB)

        val matchedBIndices = mutableSetOf<Int>()
        val sceneDiffs = mutableListOf<SceneDiff>()

        var scenesAddedCount = 0
        var scenesRemovedCount = 0
        var scenesModifiedCount = 0
        var scenesUnchangedCount = 0
        var totalBlocksAdded = 0
        var totalBlocksRemoved = 0
        var totalBlocksModified = 0

        // 1. Process scenes from A, matching with B
        for (scA in scenesA) {
            val headingA = scA.firstOrNull { it.type == BlockType.SCENE_HEADING }?.text?.trim()?.uppercase(Locale.US) ?: ""
            val numA = scA.firstOrNull { it.type == BlockType.SCENE_HEADING }?.sceneNumber?.trim()

            // Find best matching scene in B that hasn't been matched yet
            var matchIdxB = -1
            for (j in scenesB.indices) {
                if (j in matchedBIndices) continue
                val scB = scenesB[j]
                val headingB = scB.firstOrNull { it.type == BlockType.SCENE_HEADING }?.text?.trim()?.uppercase(Locale.US) ?: ""
                val numB = scB.firstOrNull { it.type == BlockType.SCENE_HEADING }?.sceneNumber?.trim()

                if (numA != null && numB != null && numA == numB) {
                    matchIdxB = j
                    break
                }
                if (headingA.isNotEmpty() && headingA == headingB) {
                    matchIdxB = j
                    break
                }
            }

            if (matchIdxB != -1) {
                matchedBIndices.add(matchIdxB)
                val scB = scenesB[matchIdxB]
                val (sceneDiff, stats) = compareSceneBlocks(scA, scB)
                sceneDiffs.add(sceneDiff)

                when (sceneDiff.type) {
                    DiffType.UNCHANGED -> scenesUnchangedCount++
                    DiffType.MODIFIED -> {
                        scenesModifiedCount++
                        totalBlocksAdded += stats.addedBlocks
                        totalBlocksRemoved += stats.removedBlocks
                        totalBlocksModified += stats.modifiedBlocks
                    }
                    DiffType.ADDED -> scenesAddedCount++
                    DiffType.REMOVED -> scenesRemovedCount++
                }
            } else {
                // Scene A was removed in Draft B
                scenesRemovedCount++
                val headA = scA.firstOrNull { it.type == BlockType.SCENE_HEADING }
                val blockDiffs = scA.map {
                    totalBlocksRemoved++
                    BlockDiff(
                        type = DiffType.REMOVED,
                        blockA = it,
                        blockB = null,
                        oldText = it.text,
                        newText = null,
                        blockType = it.type
                    )
                }
                sceneDiffs.add(
                    SceneDiff(
                        type = DiffType.REMOVED,
                        sceneNumberA = headA?.sceneNumber,
                        headingA = headA?.text,
                        blockDiffs = blockDiffs,
                        stats = SceneDiffStats(removedBlocks = scA.size)
                    )
                )
            }
        }

        // 2. Any remaining scenes in B are ADDED
        for (j in scenesB.indices) {
            if (j !in matchedBIndices) {
                scenesAddedCount++
                val scB = scenesB[j]
                val headB = scB.firstOrNull { it.type == BlockType.SCENE_HEADING }
                val blockDiffs = scB.map {
                    totalBlocksAdded++
                    BlockDiff(
                        type = DiffType.ADDED,
                        blockA = null,
                        blockB = it,
                        oldText = null,
                        newText = it.text,
                        blockType = it.type
                    )
                }
                sceneDiffs.add(
                    SceneDiff(
                        type = DiffType.ADDED,
                        sceneNumberB = headB?.sceneNumber,
                        headingB = headB?.text,
                        blockDiffs = blockDiffs,
                        stats = SceneDiffStats(addedBlocks = scB.size)
                    )
                )
            }
        }

        val wordsA = countWords(blocksA)
        val wordsB = countWords(blocksB)
        val hasDiffs = scenesAddedCount > 0 || scenesRemovedCount > 0 || scenesModifiedCount > 0 || totalBlocksAdded > 0 || totalBlocksRemoved > 0 || totalBlocksModified > 0

        val summary = DraftDiffSummary(
            totalScenesA = scenesA.size,
            totalScenesB = scenesB.size,
            scenesAdded = scenesAddedCount,
            scenesRemoved = scenesRemovedCount,
            scenesModified = scenesModifiedCount,
            scenesUnchanged = scenesUnchangedCount,
            blocksAdded = totalBlocksAdded,
            blocksRemoved = totalBlocksRemoved,
            blocksModified = totalBlocksModified,
            wordsDelta = wordsB - wordsA,
            hasDifferences = hasDiffs
        )

        return DraftDiffResult(
            titleA = titlePageA.title.ifBlank { "Baseline Draft" },
            titleB = titlePageB.title.ifBlank { "Comparison Draft" },
            authorA = titlePageA.author,
            authorB = titlePageB.author,
            draftTagA = titlePageA.draftInfo.ifBlank { "Draft A" },
            draftTagB = titlePageB.draftInfo.ifBlank { "Draft B" },
            sceneDiffs = sceneDiffs,
            summary = summary
        )
    }

    private fun compareSceneBlocks(
        blocksA: List<ScreenplayBlock>,
        blocksB: List<ScreenplayBlock>
    ): Pair<SceneDiff, SceneDiffStats> {
        val headA = blocksA.firstOrNull { it.type == BlockType.SCENE_HEADING }
        val headB = blocksB.firstOrNull { it.type == BlockType.SCENE_HEADING }

        val bDiffs = mutableListOf<BlockDiff>()
        var added = 0
        var removed = 0
        var modified = 0
        var unchanged = 0

        // Compare non-heading blocks
        val bodyA = blocksA.filter { it.type != BlockType.SCENE_HEADING }
        val bodyB = blocksB.filter { it.type != BlockType.SCENE_HEADING }

        fun findSpeaker(list: List<ScreenplayBlock>, idx: Int): String? {
            for (i in idx.coerceAtMost(list.size - 1) downTo 0) {
                if (list[i].type == BlockType.CHARACTER) {
                    return list[i].text.trim()
                }
            }
            return null
        }

        var idxA = 0
        var idxB = 0

        while (idxA < bodyA.size && idxB < bodyB.size) {
            val a = bodyA[idxA]
            val b = bodyB[idxB]
            val speaker = findSpeaker(bodyB, idxB) ?: findSpeaker(bodyA, idxA)

            if (a.type == b.type && a.text.trim() == b.text.trim()) {
                unchanged++
                bDiffs.add(BlockDiff(DiffType.UNCHANGED, a, b, a.text, b.text, a.type, speaker))
                idxA++
                idxB++
            } else if (a.type == b.type) {
                // Same type but modified text
                modified++
                bDiffs.add(BlockDiff(DiffType.MODIFIED, a, b, a.text, b.text, a.type, speaker))
                idxA++
                idxB++
            } else {
                // Lookahead to see if 'b' was inserted or 'a' was deleted
                val futureMatchB = bodyB.drop(idxB + 1).indexOfFirst { it.type == a.type && it.text.trim() == a.text.trim() }
                val futureMatchA = bodyA.drop(idxA + 1).indexOfFirst { it.type == b.type && it.text.trim() == b.text.trim() }

                if (futureMatchB != -1 && (futureMatchA == -1 || futureMatchB <= futureMatchA)) {
                    // b is an added block
                    added++
                    bDiffs.add(BlockDiff(DiffType.ADDED, null, b, null, b.text, b.type, findSpeaker(bodyB, idxB)))
                    idxB++
                } else if (futureMatchA != -1) {
                    // a was removed
                    removed++
                    bDiffs.add(BlockDiff(DiffType.REMOVED, a, null, a.text, null, a.type, findSpeaker(bodyA, idxA)))
                    idxA++
                } else {
                    // Fallback to replacement / modification
                    modified++
                    bDiffs.add(BlockDiff(DiffType.MODIFIED, a, b, a.text, b.text, b.type, speaker))
                    idxA++
                    idxB++
                }
            }
        }

        // Remaining in A are removed
        while (idxA < bodyA.size) {
            val a = bodyA[idxA]
            removed++
            bDiffs.add(BlockDiff(DiffType.REMOVED, a, null, a.text, null, a.type, findSpeaker(bodyA, idxA)))
            idxA++
        }

        // Remaining in B are added
        while (idxB < bodyB.size) {
            val b = bodyB[idxB]
            added++
            bDiffs.add(BlockDiff(DiffType.ADDED, null, b, null, b.text, b.type, findSpeaker(bodyB, idxB)))
            idxB++
        }

        val sceneType = if (added == 0 && removed == 0 && modified == 0) {
            DiffType.UNCHANGED
        } else {
            DiffType.MODIFIED
        }

        val stats = SceneDiffStats(
            addedBlocks = added,
            removedBlocks = removed,
            modifiedBlocks = modified,
            unchangedBlocks = unchanged
        )

        return Pair(
            SceneDiff(
                type = sceneType,
                sceneNumberA = headA?.sceneNumber,
                sceneNumberB = headB?.sceneNumber,
                headingA = headA?.text,
                headingB = headB?.text,
                blockDiffs = bDiffs,
                stats = stats
            ),
            stats
        )
    }

    private fun partitionIntoScenes(blocks: List<ScreenplayBlock>): List<List<ScreenplayBlock>> {
        val scenes = mutableListOf<List<ScreenplayBlock>>()
        var currentScene = mutableListOf<ScreenplayBlock>()

        for (block in blocks) {
            if (block.type == BlockType.SCENE_HEADING && isCountableSceneHeading(block.text)) {
                if (currentScene.isNotEmpty()) {
                    scenes.add(currentScene)
                    currentScene = mutableListOf()
                }
            }
            currentScene.add(block)
        }
        if (currentScene.isNotEmpty()) {
            scenes.add(currentScene)
        }
        return scenes
    }

    private fun isCountableSceneHeading(text: String): Boolean {
        val trimmed = text.trim().uppercase(Locale.US)
        if (trimmed.isEmpty()) return false
        val clean = if (trimmed.startsWith(".")) trimmed.substring(1).trim() else trimmed
        return clean.startsWith("INT.") || clean.startsWith("EXT.") ||
                clean.startsWith("INT/EXT.") || clean.startsWith("INT./EXT.") ||
                clean.startsWith("I/E.") || clean.startsWith("I/E ") ||
                clean.startsWith("EST.") || clean.startsWith("ESTABLISHING")
    }

    private fun countWords(blocks: List<ScreenplayBlock>): Int {
        return blocks.sumOf { b ->
            b.text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        }
    }

    fun generateChangeReportText(result: DraftDiffResult): String {
        val sb = StringBuilder()
        sb.append("================================================================================\n")
        sb.append("               DRAFT REVISION COMPARISON REPORT\n")
        sb.append("================================================================================\n")
        sb.append("SCRIPT: ${result.titleA}\n")
        if (result.authorA.isNotBlank()) sb.append("AUTHOR: ${result.authorA}\n")
        sb.append("BASELINE DRAFT (A): ${result.draftTagA} (${result.summary.totalScenesA} scenes)\n")
        sb.append("REVISED DRAFT (B):  ${result.draftTagB} (${result.summary.totalScenesB} scenes)\n")
        sb.append("--------------------------------------------------------------------------------\n")
        sb.append("EXECUTIVE SUMMARY:\n")
        sb.append("  • Scenes Added:     +${result.summary.scenesAdded}\n")
        sb.append("  • Scenes Removed:   -${result.summary.scenesRemoved}\n")
        sb.append("  • Scenes Modified:  ~${result.summary.scenesModified}\n")
        sb.append("  • Scenes Unchanged:  ${result.summary.scenesUnchanged}\n")
        sb.append("  • Block Changes:    +${result.summary.blocksAdded} added, -${result.summary.blocksRemoved} removed, ~${result.summary.blocksModified} modified\n")
        sb.append("  • Word Count Delta: ${if (result.summary.wordsDelta >= 0) "+" else ""}${result.summary.wordsDelta} words\n")
        sb.append("================================================================================\n\n")

        // 1. Added Scenes
        val addedScenes = result.sceneDiffs.filter { it.type == DiffType.ADDED }
        if (addedScenes.isNotEmpty()) {
            sb.append("### ADDED SCENES (${addedScenes.size}):\n")
            for (sc in addedScenes) {
                val num = sc.sceneNumberB?.let { "#$it " } ?: ""
                sb.append("  [+] $num${sc.headingB ?: "UNTITLED SCENE"} (+${sc.stats.addedBlocks} blocks)\n")
            }
            sb.append("\n")
        }

        // 2. Removed Scenes
        val removedScenes = result.sceneDiffs.filter { it.type == DiffType.REMOVED }
        if (removedScenes.isNotEmpty()) {
            sb.append("### REMOVED SCENES (${removedScenes.size}):\n")
            for (sc in removedScenes) {
                val num = sc.sceneNumberA?.let { "#$it " } ?: ""
                sb.append("  [-] $num${sc.headingA ?: "UNTITLED SCENE"} (-${sc.stats.removedBlocks} blocks)\n")
            }
            sb.append("\n")
        }

        // 3. Modified Scenes
        val modifiedScenes = result.sceneDiffs.filter { it.type == DiffType.MODIFIED }
        if (modifiedScenes.isNotEmpty()) {
            sb.append("### MODIFIED SCENES (${modifiedScenes.size}):\n")
            for (sc in modifiedScenes) {
                val num = sc.sceneNumberB ?: sc.sceneNumberA ?: ""
                val numPrefix = if (num.isNotBlank()) "#$num " else ""
                val heading = sc.headingB ?: sc.headingA ?: "SCENE"
                sb.append("  [~] $numPrefix$heading\n")
                sb.append("      Changes: +${sc.stats.addedBlocks} added, -${sc.stats.removedBlocks} removed, ~${sc.stats.modifiedBlocks} modified\n")

                // Detail dialogue/action changes
                for (b in sc.blockDiffs) {
                    val spk = if (b.speaker != null) " (${b.speaker})" else ""
                    when (b.type) {
                        DiffType.ADDED -> sb.append("        + [${b.blockType.name}$spk]: ${b.newText}\n")
                        DiffType.REMOVED -> sb.append("        - [${b.blockType.name}$spk]: ${b.oldText}\n")
                        DiffType.MODIFIED -> {
                            sb.append("        ~ [${b.blockType.name}$spk]:\n")
                            sb.append("            OLD: ${b.oldText}\n")
                            sb.append("            NEW: ${b.newText}\n")
                        }
                        DiffType.UNCHANGED -> {}
                    }
                }
                sb.append("\n")
            }
        }

        sb.append("================================================================================\n")
        sb.append("Generated by Seshat Screenwriting Engine\n")
        return sb.toString()
    }
}
