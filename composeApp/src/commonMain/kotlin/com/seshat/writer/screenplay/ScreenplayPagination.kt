package com.seshat.writer.screenplay

import com.seshat.writer.model.BlockType
import com.seshat.writer.model.ScreenplayBlock

data class PaginationResult(
    val totalPages: Int,
    val totalLines: Int,
    val estimatedMinutes: Int,
    val pageBreakBeforeBlockIds: Set<String>,
    val blockPageNumbers: Map<String, Int>
)

object ScreenplayPagination {

    const val LINES_PER_PAGE = 55
    const val SCENE_HEADING_MAX_CHARS = 60
    const val ACTION_MAX_CHARS = 60
    const val CHARACTER_MAX_CHARS = 35
    const val PARENTHETICAL_MAX_CHARS = 30
    const val DIALOGUE_MAX_CHARS = 35
    const val TRANSITION_MAX_CHARS = 25

    fun countWrappedLines(text: String, maxChars: Int): Int {
        if (text.length <= maxChars) return 1
        val words = text.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) return 1

        var lines = 1
        var currentLineLength = 0
        for (word in words) {
            if (currentLineLength == 0) {
                currentLineLength = word.length
            } else if (currentLineLength + 1 + word.length <= maxChars) {
                currentLineLength += 1 + word.length
            } else {
                lines++
                currentLineLength = word.length
            }
        }
        return lines
    }

    fun calculateBlockLines(block: ScreenplayBlock): Int {
        if (block.isOmitted) return 1
        val maxChars = when (block.type) {
            BlockType.SCENE_HEADING, BlockType.STAGE_ACT_SCENE, BlockType.COMIC_PAGE, BlockType.COMIC_PANEL -> SCENE_HEADING_MAX_CHARS
            BlockType.ACTION, BlockType.STAGE_DIRECTION, BlockType.COMIC_ART_NOTE, BlockType.COMIC_CAPTION -> ACTION_MAX_CHARS
            BlockType.CHARACTER, BlockType.COMIC_SFX -> CHARACTER_MAX_CHARS
            BlockType.PARENTHETICAL -> PARENTHETICAL_MAX_CHARS
            BlockType.DIALOGUE, BlockType.COMIC_BALLOON -> DIALOGUE_MAX_CHARS
            BlockType.TRANSITION, BlockType.STAGE_CURTAIN -> TRANSITION_MAX_CHARS
        }
        if (block.text.isEmpty()) return 1

        val paragraphs = block.text.split('\n')
        var total = 0
        for (para in paragraphs) {
            val trimmed = para.trim()
            if (trimmed.isEmpty()) {
                total += 1
            } else {
                total += countWrappedLines(trimmed, maxChars)
            }
        }
        return total.coerceAtLeast(1)
    }

    fun getBlankLinesBefore(
        currentBlock: ScreenplayBlock,
        previousBlock: ScreenplayBlock?,
        isTopPage: Boolean
    ): Int {
        if (isTopPage) return 0
        if (previousBlock == null) return 0

        return when (currentBlock.type) {
            BlockType.SCENE_HEADING, BlockType.STAGE_ACT_SCENE, BlockType.COMIC_PAGE -> 1
            BlockType.ACTION, BlockType.STAGE_DIRECTION, BlockType.COMIC_PANEL, BlockType.COMIC_ART_NOTE, BlockType.COMIC_CAPTION, BlockType.COMIC_SFX -> 1
            BlockType.CHARACTER -> 1
            BlockType.PARENTHETICAL -> {
                if (previousBlock.type == BlockType.CHARACTER || previousBlock.type == BlockType.DIALOGUE) 0 else 1
            }
            BlockType.DIALOGUE, BlockType.COMIC_BALLOON -> {
                if (previousBlock.type == BlockType.CHARACTER || previousBlock.type == BlockType.PARENTHETICAL) 0 else 1
            }
            BlockType.TRANSITION, BlockType.STAGE_CURTAIN -> 1
        }
    }

    fun calculatePagination(blocks: List<ScreenplayBlock>): PaginationResult {
        if (blocks.isEmpty()) {
            return PaginationResult(
                totalPages = 1,
                totalLines = 0,
                estimatedMinutes = 1,
                pageBreakBeforeBlockIds = emptySet(),
                blockPageNumbers = emptyMap()
            )
        }

        var currentPage = 1
        var linesOnCurrentPage = 0
        var totalScriptLines = 0
        val pageBreakIds = mutableSetOf<String>()
        val pageMap = mutableMapOf<String, Int>()
        var prevBlock: ScreenplayBlock? = null

        var i = 0
        while (i < blocks.size) {
            val block = blocks[i]
            if (block.isOmitted && block.type != BlockType.SCENE_HEADING) {
                i++
                continue
            }
            if (block.dualDialogueId != null) {
                // Group consecutive blocks sharing the same dualDialogueId
                val dualId = block.dualDialogueId
                val dualGroup = mutableListOf<ScreenplayBlock>()
                while (i < blocks.size && blocks[i].dualDialogueId == dualId) {
                    dualGroup.add(blocks[i])
                    i++
                }

                val leftBlocks = dualGroup.filter { it.dualSide == com.seshat.writer.model.DualDialogueSide.LEFT }
                val rightBlocks = dualGroup.filter { it.dualSide == com.seshat.writer.model.DualDialogueSide.RIGHT }

                val isTopPage = (linesOnCurrentPage == 0)
                val spacingBefore = if (isTopPage) 0 else 1

                val leftLines = calculateClusterLines(leftBlocks)
                val rightLines = calculateClusterLines(rightBlocks)
                val groupHeight = maxOf(leftLines, rightLines)
                val totalNeeded = spacingBefore + groupHeight

                if (!isTopPage && (linesOnCurrentPage + totalNeeded > LINES_PER_PAGE)) {
                    currentPage++
                    pageBreakIds.add(dualGroup.first().id)
                    linesOnCurrentPage = groupHeight
                    totalScriptLines += groupHeight
                } else {
                    linesOnCurrentPage += totalNeeded
                    totalScriptLines += totalNeeded
                }

                dualGroup.forEach { pageMap[it.id] = currentPage }
                prevBlock = dualGroup.last()
                continue
            }

            val isTopPage = (linesOnCurrentPage == 0)
            val spacingBefore = getBlankLinesBefore(block, prevBlock, isTopPage)
            val blockLines = calculateBlockLines(block)
            val totalNeeded = spacingBefore + blockLines

            // Orphan checks:
            // 1. Scene heading orphan check: needs heading + at least 2 lines of action on same page
            val isSceneHeadingOrphan = block.type == BlockType.SCENE_HEADING &&
                !block.isOmitted &&
                !isTopPage &&
                (linesOnCurrentPage + spacingBefore + blockLines + 2 > LINES_PER_PAGE)

            // 2. Character orphan check: needs character + at least 1 line of dialogue on same page
            val isCharacterOrphan = block.type == BlockType.CHARACTER &&
                !isTopPage &&
                (linesOnCurrentPage + spacingBefore + blockLines + 2 > LINES_PER_PAGE)

            // 3. General overflow check:
            val isGeneralOverflow = !isTopPage && (linesOnCurrentPage + totalNeeded > LINES_PER_PAGE)

            if (isSceneHeadingOrphan || isCharacterOrphan || isGeneralOverflow) {
                // Break to next page!
                currentPage++
                pageBreakIds.add(block.id)
                pageMap[block.id] = currentPage
                linesOnCurrentPage = blockLines
                totalScriptLines += blockLines
            } else {
                linesOnCurrentPage += totalNeeded
                totalScriptLines += totalNeeded
                pageMap[block.id] = currentPage
            }

            prevBlock = block
            i++
        }

        return PaginationResult(
            totalPages = Math.max(1, currentPage),
            totalLines = totalScriptLines,
            estimatedMinutes = Math.max(1, currentPage),
            pageBreakBeforeBlockIds = pageBreakIds,
            blockPageNumbers = pageMap
        )
    }

    private fun calculateClusterLines(cluster: List<ScreenplayBlock>): Int {
        if (cluster.isEmpty()) return 1
        var lines = 0
        for (idx in cluster.indices) {
            val b = cluster[idx]
            lines += calculateBlockLines(b)
            if (idx < cluster.size - 1) {
                val next = cluster[idx + 1]
                val continuous = (b.type == BlockType.CHARACTER && (next.type == BlockType.PARENTHETICAL || next.type == BlockType.DIALOGUE)) ||
                    (b.type == BlockType.PARENTHETICAL && next.type == BlockType.DIALOGUE)
                if (!continuous) {
                    lines += 1
                }
            }
        }
        return lines.coerceAtLeast(1)
    }
}
